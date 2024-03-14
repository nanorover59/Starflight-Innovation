package space.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface EnergyBlock
{
	/**
	 * Is this side of the block an energy output?
	 */
	default boolean isOutput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}
	
	/**
	 * Is this side of the block an energy input?
	 */
	default boolean isInput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}
	
	/**
	 * Is this side of the block used for energy pass through?
	 * Used for energy cables.
	 */
	default boolean isPassThrough(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}
	
	/**
	 * Can this side of the block connect to energy cable blocks?
	 * Always true for energy cable blocks.
	 */
	default boolean canConnectToCables(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return isOutput(world, pos, state, direction) || isInput(world, pos, state, direction) || isPassThrough(world, pos, state, direction);
	}
	
	/**
	 * Expected energy output per second.
	 */
	default double getOutput()
	{
		return 0.0;
	}
	
	/**
	 * Expected energy input per second.
	 */
	default double getInput()
	{
		return 0.0;
	}
	
	/**
	 * Maximum energy that can be stored.
	 */
	default double getEnergyCapacity()
	{
		return 0.0;
	}
}