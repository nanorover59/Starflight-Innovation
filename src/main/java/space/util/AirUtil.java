package space.util;

import java.util.ArrayList;
import java.util.function.BiPredicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.AirwayBlock;
import space.block.AtmosphereGeneratorBlock;
import space.block.FluidPipeBlock;
import space.block.OxygenSensorBlock;
import space.block.SealedDoorBlock;
import space.block.SealedTrapdoorBlock;
import space.block.StarflightBlocks;
import space.block.ValveBlock;
import space.block.entity.LeakBlockEntity;
import space.block.entity.ValveBlockEntity;
import space.planet.PlanetDimensionData;

public class AirUtil
{
	/**
	 * Get the air resistance multiplier for the atmospheric conditions at the given location.
	 */
	public static double getAirResistanceMultiplier(World world, PlanetDimensionData data, BlockPos pos)
	{
		if(data == null)
			return 1.0;
		
		if(world.getBlockState(pos).getBlock() == StarflightBlocks.HABITABLE_AIR)
			return 0.9;
		
		return data.isOrbit() ? 0.0 : Math.min(data.getPressure(), 1.0);
	}
	
	/**
	 * Return true if the given entity can breathe in its current location.
	 */
	public static boolean canEntityBreathe(LivingEntity entity, PlanetDimensionData data)
	{
		return canEntityBreathe(entity, entity.getBlockPos(), data);
	}
	
	/**
	 * Return true if the given entity can breathe at the given location.
	 */
	public static boolean canEntityBreathe(LivingEntity entity, BlockPos pos, PlanetDimensionData data)
	{
		World world = entity.getWorld();
		
		if(data == null)
			return true;
		
		if(data.getPressure() > 0.5 && data.hasOxygen() && !data.isOrbit())
			return true;
		
		for(Direction direction : Direction.values())
		{
			if(world.getBlockState(pos.offset(direction)).getBlock() == StarflightBlocks.HABITABLE_AIR)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Find a closed volume that can be filled with habitable air. Return true if one is found.
	 */
	public static boolean findVolume(World world, BlockPos position, ArrayList<BlockPos> checkList, ArrayList<BlockPos> updateList, int limit)
	{
		BiPredicate<World, BlockPos> include = (w, p) -> {
			return !airBlocking(w, p);
		};
		
		BiPredicate<World, BlockPos> passThrough = (w, p) -> {
			return w.getBlockState(p).getBlock() != Blocks.AIR && !airBlocking(w, p);
		};
		
		BiPredicate<World, BlockPos> edgeCase = (w, p) -> {
			return w.getBlockState(p).getBlock() == StarflightBlocks.ATMOSPHERE_GENERATOR || w.getBlockState(p).getBlock() == StarflightBlocks.OXYGEN_SENSOR;
		};
		
		return BlockSearch.passThroughSearch(world, position, checkList, updateList, include, edgeCase, passThrough, limit, true);
	}
	
	/**
	 * Fill the provided list of block positions with habitable air.
	 */
	public static void fillVolume(World world, ArrayList<BlockPos> posList, ArrayList<BlockPos> updateList)
	{
		for(BlockPos pos : posList)
		{
			BlockState blockState = world.getBlockState(pos);
			
			if(blockState.getBlock() == Blocks.AIR)
				world.setBlockState(pos, StarflightBlocks.HABITABLE_AIR.getDefaultState(), Block.FORCE_STATE);		
		}
		
		for(BlockPos pos : updateList)
		{
			BlockState blockState = world.getBlockState(pos);
			
			if(blockState.getBlock() == StarflightBlocks.ATMOSPHERE_GENERATOR)
				world.setBlockState(pos, (BlockState) blockState.with(AtmosphereGeneratorBlock.LIT, true));
			else if(blockState.getBlock() == StarflightBlocks.OXYGEN_SENSOR)
				world.setBlockState(pos, (BlockState) blockState.with(OxygenSensorBlock.LIT, true));
		}
	}
	
	/**
	 * Remove habitable air blocks.
	 */
	public static void remove(World world, BlockPos position, int limit)
	{
		BiPredicate<World, BlockPos> include = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return blockState.getBlock() != Blocks.AIR && (!AirUtil.airBlocking(w, p) || blockState.isIn(StarflightBlocks.INSTANT_REMOVE_TAG) || blockState.getBlock() == StarflightBlocks.HABITABLE_AIR || blockState.getBlock() == StarflightBlocks.LEAK);
		};
		
		BiPredicate<World, BlockPos> edgeCase = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return blockState.getBlock() == StarflightBlocks.ATMOSPHERE_GENERATOR || blockState.getBlock() == StarflightBlocks.OXYGEN_SENSOR || (blockState.getBlock() == StarflightBlocks.AIRWAY && !blockState.get(AirwayBlock.CLOSED));
		};
		
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
		BlockSearch.search(world, position, checkList, foundList, include, edgeCase, limit, true);
		
		for(BlockPos pos : checkList)
		{
			BlockState blockState = world.getBlockState(pos);
			
			if(blockState.getBlock() == StarflightBlocks.HABITABLE_AIR || blockState.getBlock() == StarflightBlocks.LEAK)
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
			else if(blockState.isIn(StarflightBlocks.INSTANT_REMOVE_TAG))
			{
				if(blockState.isIn(BlockTags.SAPLINGS) && world.getRandom().nextBoolean())
				{
					world.setBlockState(pos, Blocks.DEAD_BUSH.getDefaultState(), Block.NOTIFY_LISTENERS);
					continue;
				}
				
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
				Block.dropStacks(blockState, world, pos, world.getBlockEntity(pos));
			}
		}
		
		for(BlockPos pos : foundList)
		{
			BlockState blockState = world.getBlockState(pos);
			
			if(blockState.getBlock() == StarflightBlocks.ATMOSPHERE_GENERATOR)
				world.setBlockState(pos, (BlockState) blockState.with(AtmosphereGeneratorBlock.LIT, false));
			else if(blockState.getBlock() == StarflightBlocks.OXYGEN_SENSOR)
				world.setBlockState(pos, (BlockState) blockState.with(OxygenSensorBlock.LIT, false));
			else if(blockState.getBlock() == StarflightBlocks.AIRWAY)
				world.setBlockState(pos, (BlockState) blockState.with(AirwayBlock.CLOSED, true));
		}
	}
	
	/**
	 * Remove habitable air blocks or start a leak if there is a sufficient number of them.
	 */
	public static void removeOrLeak(World world, BlockPos position, BlockPos leakPosition, int leakTime, int limit)
	{
		BiPredicate<World, BlockPos> include = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return blockState.getBlock() != Blocks.AIR && (!AirUtil.airBlocking(w, p) || blockState.isIn(StarflightBlocks.INSTANT_REMOVE_TAG) || blockState.getBlock() == StarflightBlocks.HABITABLE_AIR);
		};
		
		BiPredicate<World, BlockPos> edgeCase = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return blockState.getBlock() == StarflightBlocks.ATMOSPHERE_GENERATOR || blockState.getBlock() == StarflightBlocks.OXYGEN_SENSOR || (blockState.getBlock() == StarflightBlocks.AIRWAY && !blockState.get(AirwayBlock.CLOSED));
		};
		
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
		BlockSearch.search(world, position, checkList, foundList, include, edgeCase, limit, true);
		
		if(leakTime > 1200)
		{
			createLeak(world, leakPosition, leakTime);
			return;
		}
		
		for(BlockPos pos : checkList)
		{
			BlockState blockState = world.getBlockState(pos);
			FluidState fluidState = world.getFluidState(pos);
			
			if(blockState.getBlock() == StarflightBlocks.HABITABLE_AIR)
				world.setBlockState(pos, fluidState.getBlockState(), Block.NOTIFY_LISTENERS);
			else if(blockState.isIn(StarflightBlocks.INSTANT_REMOVE_TAG))
			{
				if(blockState.isIn(BlockTags.SAPLINGS) && world.getRandom().nextBoolean())
				{
					world.setBlockState(pos, Blocks.DEAD_BUSH.getDefaultState(), Block.NOTIFY_LISTENERS);
					continue;
				}
				
				world.setBlockState(pos, fluidState.getBlockState(), Block.NOTIFY_LISTENERS);
				Block.dropStacks(blockState, world, pos, world.getBlockEntity(pos));
			}
		}
		
		for(BlockPos pos : foundList)
		{
			BlockState blockState = world.getBlockState(pos);
			
			if(blockState.getBlock() == StarflightBlocks.ATMOSPHERE_GENERATOR)
				world.setBlockState(pos, (BlockState) blockState.with(AtmosphereGeneratorBlock.LIT, false));
			else if(blockState.getBlock() == StarflightBlocks.OXYGEN_SENSOR)
				world.setBlockState(pos, (BlockState) blockState.with(OxygenSensorBlock.LIT, false));
			else if(blockState.getBlock() == StarflightBlocks.AIRWAY)
				world.setBlockState(pos, (BlockState) blockState.with(AirwayBlock.CLOSED, true));
		}
	}
	
	/**
	 * Returns true if the block at the given location is a valid wall for a closed volume.
	 */
	public static boolean airBlocking(World world, BlockPos position)
	{
		BlockState blockState = world.getBlockState(position);
		return airBlockingState(world, position, blockState);
	}
	
	public static boolean airBlockingState(World world, BlockPos position, BlockState blockState)
	{
		Block block = blockState.getBlock();
		
		if(block == Blocks.AIR)
			return false;
		else if(block == StarflightBlocks.HABITABLE_AIR || block == StarflightBlocks.LEAK)
			return true;
		else if(block == Blocks.GLASS_PANE || block instanceof StainedGlassPaneBlock)
			return true;
		else if(block instanceof SealedDoorBlock && !blockState.get(SealedDoorBlock.OPEN))
		{
			Direction upOrDownDirection = blockState.get(SealedDoorBlock.HALF) == DoubleBlockHalf.UPPER ? Direction.DOWN : Direction.UP;
			Direction direction1 = blockState.get(SealedDoorBlock.FACING).rotateYClockwise();
			Direction direction2 = blockState.get(SealedDoorBlock.FACING).rotateYCounterclockwise();
			BlockPos upOrDownPos = blockState.get(SealedDoorBlock.HALF) == DoubleBlockHalf.UPPER ? position.up() : position.down();
			BlockPos side1 = position.offset(direction1.getOpposite());
			BlockPos side2 = position.offset(direction2.getOpposite());
			return world.getBlockState(upOrDownPos).isSideSolidFullSquare(world, upOrDownPos, upOrDownDirection) && world.getBlockState(side1).isSideSolidFullSquare(world, side1, direction1) && world.getBlockState(side2).isSideSolidFullSquare(world, side2, direction2);
		}
		else if(block instanceof SealedTrapdoorBlock && !blockState.get(SealedTrapdoorBlock.OPEN))
		{
			for(int i = 0; i < 4; i++)
			{
				Direction direction = Direction.fromHorizontal(i);
				BlockPos pos = position.offset(direction);
				
				if(!world.getBlockState(pos).isSideSolidFullSquare(world, pos, direction.getOpposite()))
					return false;	
			}	
			
			return true;
		}
		else if(block instanceof AirwayBlock && blockState.get(AirwayBlock.CLOSED))
		{
			int solidCount = 0;
			
			for(Direction direction : Direction.values())
			{
				if(direction == blockState.get(AirwayBlock.FACING) || direction == blockState.get(AirwayBlock.FACING).getOpposite())
					continue;
				
				if(world.getBlockState(position.offset(direction)).isSideSolidFullSquare(world, position.offset(direction), direction.getOpposite()))
					solidCount++;
			}
			
			return solidCount == 4;
		}
		else
			return blockState.isFullCube(world, position);
	}
	
	public static void createLeak(World world, BlockPos pos, int leakTime)
	{
        world.breakBlock(pos, true);
		world.setBlockState(pos, StarflightBlocks.LEAK.getDefaultState(), Block.NOTIFY_ALL);
		BlockEntity blockEntity = world.getBlockEntity(pos);

		if(blockEntity instanceof LeakBlockEntity)
			((LeakBlockEntity) blockEntity).setLeakTime(leakTime);
	}
}