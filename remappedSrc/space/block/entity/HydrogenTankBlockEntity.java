package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import space.block.StarflightBlocks;

public class HydrogenTankBlockEntity extends FluidTankControllerBlockEntity
{
	public HydrogenTankBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.HYDROGEN_TANK_BLOCK_ENTITY, pos, state);
	}
	
	public String getFluidName()
	{
		return "hydrogen";
	}
}