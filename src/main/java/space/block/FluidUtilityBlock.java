package space.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import space.util.FluidResourceType;

public interface FluidUtilityBlock
{
	FluidResourceType getFluidType();
	
	boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction);
}