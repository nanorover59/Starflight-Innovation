package space.block;

import java.util.ArrayList;
import java.util.function.BiPredicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.entity.BalloonControllerBlockEntity;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.ValveBlockEntity;
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
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
		BlockState neighborState = world.getBlockState(sourcePos);
		
		if(neighborState.getBlock() == Blocks.AIR)
		{
			BiPredicate<World, BlockPos> include = (w, p) -> {
				BlockState b = w.getBlockState(p);
				return b.getBlock() == StarflightBlocks.FLUID_TANK_INSIDE || b.getBlock() instanceof FluidTankControllerBlock || b.getBlock() instanceof BalloonControllerBlock || b.getBlock() instanceof ValveBlock;
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
							
							if(fluidTankController.getStoredFluid() > fluidTankController.getFluidType().getStorageDensity() * 0.1)
								StarflightEffects.sendOutgas(world, pos, sourcePos, true);
							
							if(fluidTankController.getStoredFluid() > fluidTankController.getFluidType().getStorageDensity())
								blockEntity.getWorld().createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0f, World.ExplosionSourceType.BLOCK);
							
							fluidTankController.setStorageCapacity(0);
							fluidTankController.setStoredFluid(0);
							fluidTankController.setCenterOfMass(new BlockPos(0, 0, 0));
						}
						else if(blockEntity instanceof ValveBlockEntity)
						{
							ValveBlockEntity fluidTankInterface = (ValveBlockEntity) blockEntity;
							fluidTankInterface.setControllerPosition(new BlockPos(0, 0, 0));
						}
					}
				}
				else if(world.getBlockState(fluidTankBlockPos).getBlock() instanceof BalloonControllerBlock)
				{
					BlockEntity blockEntity = world.getBlockEntity(fluidTankBlockPos);

					if(blockEntity != null && blockEntity instanceof BalloonControllerBlockEntity)
					{
						BalloonControllerBlockEntity balloonController = (BalloonControllerBlockEntity) blockEntity;

						if(balloonController.getStoredFluid() > 1.0)
							StarflightEffects.sendOutgas(world, pos, sourcePos, true);

						if(balloonController.getStoredFluid() > 2.0)
							blockEntity.getWorld().createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0f, World.ExplosionSourceType.BLOCK);

						balloonController.setStorageCapacity(0);
						balloonController.setStoredFluid(0);
						balloonController.setCenterOfMass(new BlockPos(0, 0, 0));
					}
				}
			}
		}
	}
}