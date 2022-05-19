package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import space.block.StarflightBlocks;

public class EncasedHydrogenPipeALBlockEntity extends FluidContainerBlockEntity
{
	public EncasedHydrogenPipeALBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.HYDROGEN_PIPE_AL_BLOCK_ENTITY, pos, state);
	}
	
	public String getFluidName()
	{
		return "hydrogen";
	}

	public double getStorageCapacity()
	{
		return StarflightBlocks.HYDROGEN_PIPE_CAPACITY;
	}
}
