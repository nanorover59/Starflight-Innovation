package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import space.block.StarflightBlocks;

public class HydrogenInletValveBlockEntity extends FluidTankInterfaceBlockEntity
{
	public HydrogenInletValveBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.HYDROGEN_INLET_VALVE_BLOCK_ENTITY, pos, state);
	}
	
	public String getFluidName()
	{
		return "hydrogen";
	}
}