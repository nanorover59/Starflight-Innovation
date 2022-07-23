package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import space.block.StarflightBlocks;

public class EncasedOxygenPipeALBlockEntity extends FluidContainerBlockEntity
{
	public EncasedOxygenPipeALBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.OXYGEN_PIPE_AL_BLOCK_ENTITY, pos, state);
	}
	
	public String getFluidName()
	{
		return "oxygen";
	}

	public double getStorageCapacity()
	{
		return StarflightBlocks.OXYGEN_PIPE_CAPACITY;
	}
}
