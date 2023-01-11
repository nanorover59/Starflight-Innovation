package space.block;

import java.util.ArrayList;
import java.util.function.BiPredicate;

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
import space.util.BlockSearch;
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
	public boolean canMobSpawnInside()
	{
		return false;
	}
	
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		if(neighborState.getBlock() == Blocks.AIR)
		{
			BiPredicate<WorldAccess, BlockPos> include = (w, p) -> {
				BlockState b = w.getBlockState(p);
				return b.getBlock() == StarflightBlocks.FLUID_TANK_INSIDE || b.getBlock() instanceof FluidTankControllerBlock || b.getBlock() instanceof FluidUtilityBlock;
			};
			
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();	
			BlockSearch.search(world, pos, checkList, include, BlockSearch.MAX_VOLUME, true);
			
			for(BlockPos fluidTankBlockPos : checkList)
			{
				if(world.getBlockState(fluidTankBlockPos).getBlock() == StarflightBlocks.FLUID_TANK_INSIDE)
					world.setBlockState(fluidTankBlockPos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
				else if(world.getBlockState(fluidTankBlockPos).getBlock() instanceof FluidTankControllerBlock)
				{
					BlockEntity blockEntity = world.getBlockEntity(fluidTankBlockPos);
					
					if(blockEntity != null)
					{
						if(blockEntity instanceof FluidTankControllerBlockEntity)
						{
							FluidTankControllerBlockEntity fluidTankController = (FluidTankControllerBlockEntity) blockEntity;
							
							if(fluidTankController.getStoredFluid() > StarflightBlocks.HYDROGEN_PIPE_CAPACITY)
								StarflightEffects.sendOutgas(world, pos, neighborPos, true);
							
							if(fluidTankController.getStoredFluid() > StarflightBlocks.HYDROGEN_TANK_CAPACITY)
								blockEntity.getWorld().createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0f, DestructionType.DESTROY);
							
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
				}
			}
		}
		
		return state;
	}
}