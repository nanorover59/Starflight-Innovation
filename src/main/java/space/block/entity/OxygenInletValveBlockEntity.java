package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import space.block.StarflightBlocks;

public class OxygenInletValveBlockEntity extends FluidTankInterfaceBlockEntity
{
	public OxygenInletValveBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.OXYGEN_INLET_VALVE_BLOCK_ENTITY, pos, state);
	}
	
	public String getFluidName()
	{
		return "oxygen";
	}
}