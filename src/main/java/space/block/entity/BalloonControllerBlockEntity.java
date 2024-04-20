package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.FluidPipeBlock;
import space.block.StarflightBlocks;
import space.block.ValveBlock;
import space.util.FluidResourceType;

public class BalloonControllerBlockEntity extends FluidTankControllerBlockEntity
{
	public BalloonControllerBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.BALLOON_CONTROLLER_BLOCK_ENTITY, FluidResourceType.HYDROGEN, pos, state);
	}
	
	public int getMode()
	{
		return world.getBlockState(getPos()).get(ValveBlock.MODE);
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, BalloonControllerBlockEntity blockEntity)
	{
		if(world.isClient || blockEntity.getMode() != 1 || blockEntity.getStoredFluid() <= 0)
			return;
		
		for(Direction direction : Direction.values())
		{
			BlockPos offsetPos = pos.offset(direction);
			BlockState adjacentState = world.getBlockState(offsetPos);
			
			if(adjacentState.getBlock() instanceof FluidPipeBlock && ((FluidPipeBlock) adjacentState.getBlock()).getFluidType().getID() == FluidResourceType.HYDROGEN.getID())
			{
				FluidPipeBlockEntity adjacentBlockEntity = ((FluidPipeBlockEntity) world.getBlockEntity(offsetPos));
				double adjecentCapacity = adjacentBlockEntity.getStorageCapacity();
				double adjecentFluid = adjacentBlockEntity.getStoredFluid();
				
				if(adjecentFluid < adjecentCapacity)
				{
					double deltaFluid = Math.min(blockEntity.getStoredFluid(), adjecentCapacity - adjecentFluid);
					blockEntity.changeStoredFluid(-deltaFluid);
					adjacentBlockEntity.changeStoredFluid(deltaFluid);
					blockEntity.markDirty();
					adjacentBlockEntity.markDirty();
				}
			}
		}
    }
}