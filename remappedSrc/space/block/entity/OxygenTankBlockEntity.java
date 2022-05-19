package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import space.block.StarflightBlocks;

public class OxygenTankBlockEntity extends FluidTankControllerBlockEntity
{
	public OxygenTankBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.OXYGEN_TANK_BLOCK_ENTITY, pos, state);
	}
	
	public String getFluidName()
	{
		return "oxygen";
	}
}