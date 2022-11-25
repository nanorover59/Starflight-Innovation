package space.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public interface EnergyBlock
{
	double getPowerOutput(World world, BlockPos pos, BlockState state);
	
	double getPowerDraw(World world, BlockPos pos, BlockState state);
	
	boolean isSideInput(WorldAccess world, BlockPos pos, BlockState state, Direction direction);
	
	boolean isSideOutput(WorldAccess world, BlockPos pos, BlockState state, Direction direction);
	
	default boolean canSideConnect(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return isSideInput(world, pos, state, direction) || isSideOutput(world, pos, state, direction);
	}
	
	void addNode(World world, BlockPos pos);
}