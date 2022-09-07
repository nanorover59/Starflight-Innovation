package space.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion.DestructionType;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.FluidTankInterfaceBlockEntity;
import space.util.StarflightEffects;

public class FluidTankInsideBlock extends Block
{
	public FluidTankInsideBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public BlockRenderType getRenderType(BlockState state)
	{
        return BlockRenderType.INVISIBLE;
    }
	
	@Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
        return VoxelShapes.empty();
    }
	
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		int limit = 2048;
		
		for(Direction checkDirection : Direction.values())
			checkAir(world, pos.offset(checkDirection), checkList, limit);
		
		if(checkList.size() >= limit)
		{
			checkList.clear();
			checkFluidTankBlocks(world, pos, pos, neighborPos, checkList, 4096);
			
			for(BlockPos fluidTankBlockPos : checkList)
			{
				if(world.getBlockState(fluidTankBlockPos).getBlock() == StarflightBlocks.FLUID_TANK_INSIDE)
					world.setBlockState(fluidTankBlockPos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
			}
		}
		
		return state;
	}
	
	private void checkAir(WorldAccess world, BlockPos position, ArrayList<BlockPos> checkList, int limit)
	{
		if(world.getBlockState(position).getBlock() != Blocks.AIR || checkList.contains(position))
			return;
		
		checkList.add(position);
		
		if(checkList.size() >= limit)
			return;
		
		for(Direction direction : Direction.values())
			checkAir(world, position.offset(direction), checkList, limit);
	}
	
	private void checkFluidTankBlocks(WorldAccess world, BlockPos position, BlockPos pos1, BlockPos pos2, ArrayList<BlockPos> checkList, int limit)
	{
		if(checkList.contains(position))
			return;
		else if(world.getBlockState(position).isIn(StarflightBlocks.FLUID_TANK_BLOCK_TAG))
		{
			BlockEntity blockEntity = world.getBlockEntity(position);
			
			if(blockEntity != null)
			{
				if(blockEntity instanceof FluidTankControllerBlockEntity)
				{
					FluidTankControllerBlockEntity fluidTankController = (FluidTankControllerBlockEntity) blockEntity;
					
					if(fluidTankController.getStoredFluid() > StarflightBlocks.HYDROGEN_PIPE_CAPACITY)
						StarflightEffects.sendOutgas(world, pos1, pos2, true);
					
					if(fluidTankController.getStoredFluid() > StarflightBlocks.HYDROGEN_TANK_CAPACITY)
						blockEntity.getWorld().createExplosion(null, pos1.getX() + 0.5, pos1.getY() + 0.5, pos1.getZ() + 0.5, 2.0f, DestructionType.DESTROY);
					
					fluidTankController.setActive(false);
					fluidTankController.setStorageCapacity(0);
					fluidTankController.setStoredFluid(0);
					fluidTankController.setCenterOfMass(new BlockPos(0, 0, 0));
				}
				else if(blockEntity instanceof FluidTankInterfaceBlockEntity)
				{
					FluidTankInterfaceBlockEntity fluidTankInterface = (FluidTankInterfaceBlockEntity) blockEntity;
					fluidTankInterface.setActive(false);
					fluidTankInterface.setControllerPosition(new BlockPos(0, 0, 0));
				}
			}
			
			return;
		}
		else if(world.getBlockState(position).getBlock() != StarflightBlocks.FLUID_TANK_INSIDE)
			return;
		
		checkList.add(position);
		
		if(checkList.size() >= limit)
			return;
		
		for(Direction direction : Direction.values())
			checkFluidTankBlocks(world, position.offset(direction), pos1, pos2, checkList, limit);
	}
}
