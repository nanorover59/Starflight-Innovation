package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
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
}