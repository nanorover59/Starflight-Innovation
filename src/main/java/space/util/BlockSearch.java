package space.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.SimpleFacingBlock;
import space.block.StarflightBlocks;
import space.block.entity.AtmosphereGeneratorBlockEntity;
import space.block.entity.EnergyBlockEntity;

public class BlockSearch
{
	public static final int MAX_VOLUME = 2097152;
	public static final int MAX_DISTANCE = 256;
	private static final Direction[] DIRECTIONS = Direction.values();
	
	/**
	 * Search the world starting at the given coordinates for coordinates where the BiPredicate returns true.
	 */
	public static void search(World world, BlockPos pos, ArrayList<BlockPos> positionList, BiPredicate<World, BlockPos> include, int limit, boolean distanceLimit)
	{
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		Set<BlockPos> set = new HashSet<BlockPos>();
		stack.push(pos);
		
		while(stack.size() > 0 && set.size() < limit)
		{
			BlockPos blockPos = stack.pop();
			
			if(set.contains(blockPos))
				continue;
			
			if(distanceLimit && tooFar(pos, blockPos))
				return;
			
			if(include.test(world, blockPos))
			{
				set.add(blockPos);
				
				for(Direction direction : DIRECTIONS)
				{
					BlockPos offset = blockPos.offset(direction);
					stack.push(offset);
				}
			}
		}
		
		if(set.size() < limit)
			positionList.addAll(set);
	}
	
	/**
	 * Search the world starting at the given coordinates for coordinates where either BiPredicate returns true, but do not continue searching past edge case coordinates.
	 */
	public static void search(World world, BlockPos pos, ArrayList<BlockPos> positionList, BiPredicate<World, BlockPos> include, BiPredicate<World, BlockPos> edgeCase, int limit, boolean distanceLimit)
	{
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		Set<BlockPos> set = new HashSet<BlockPos>();
		stack.push(pos);
		
		while(stack.size() > 0 && set.size() < limit)
		{
			BlockPos blockPos = stack.pop();
			
			if(set.contains(blockPos))
				continue;
			
			if(distanceLimit && tooFar(pos, blockPos))
				return;
			
			if(include.test(world, blockPos))
			{
				set.add(blockPos);
				
				for(Direction direction : DIRECTIONS)
				{
					BlockPos offset = blockPos.offset(direction);
					stack.push(offset);
				}
			}
			else if(edgeCase.test(world, blockPos))
				set.add(blockPos);
		}
		
		if(set.size() < limit)
			positionList.addAll(set);
	}
	
	/**
	 * Search the world starting at the given coordinates for coordinates where either BiPredicate returns true, but do not continue searching past edge case coordinates.
	 * Also add any edge case blocks to a second position list.
	 */
	public static void search(World world, BlockPos pos, ArrayList<BlockPos> positionList, ArrayList<BlockPos> foundList, BiPredicate<World, BlockPos> include, BiPredicate<World, BlockPos> edgeCase, int limit, boolean distanceLimit)
	{
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		Set<BlockPos> set = new HashSet<BlockPos>();
		Set<BlockPos> foundSet = new HashSet<BlockPos>();
		stack.push(pos);
		
		while(stack.size() > 0 && set.size() < limit)
		{
			BlockPos blockPos = stack.pop();
			
			if(set.contains(blockPos) || foundSet.contains(blockPos))
				continue;
			
			if(distanceLimit && tooFar(pos, blockPos))
				return;
			
			if(edgeCase.test(world, blockPos))
				foundSet.add(blockPos);
			else if(include.test(world, blockPos))
			{
				set.add(blockPos);
				
				for(Direction direction : DIRECTIONS)
				{
					BlockPos offset = blockPos.offset(direction);
					stack.push(offset);
				}
			}
		}
		
		if(set.size() < limit)
		{
			positionList.addAll(set);
			foundList.addAll(foundSet);
		}
	}
	
	/**
	 * Search the world starting at the given coordinates for coordinates where the include BiPredicate returns true and the passThrough BiPredicate returns false.
	 * Also add any edge case blocks to a second position list.
	 */
	public static boolean passThroughSearch(World world, BlockPos pos, ArrayList<BlockPos> positionList, ArrayList<BlockPos> edgeList, BiPredicate<World, BlockPos> include, BiPredicate<World, BlockPos> edgeCase, BiPredicate<World, BlockPos> passThrough, int limit, boolean distanceLimit)
	{
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		Set<BlockPos> set = new HashSet<BlockPos>();
		Set<BlockPos> edgeSet = new HashSet<BlockPos>();
		Set<BlockPos> passThroughSet = new HashSet<BlockPos>();
		stack.push(pos);
		
		while(stack.size() > 0 && set.size() < limit)
		{
			BlockPos blockPos = stack.pop();
			
			if(include.test(world, blockPos))
				set.add(blockPos);
			
			if(passThrough.test(world, blockPos))
				passThroughSet.add(blockPos);
			
			if(distanceLimit && tooFar(pos, blockPos))
				return false;
			
			for(Direction direction : DIRECTIONS)
			{
				BlockPos offset = blockPos.offset(direction);
				
				if(!set.contains(offset) && include.test(world, offset))
					stack.push(offset);
				else if(edgeCase.test(world, offset))
					edgeSet.add(offset);
			}
		}
		
		if(set.size() <= limit)
		{
			set.removeAll(passThroughSet);
			positionList.addAll(set);
			edgeList.addAll(edgeSet);
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Search for powered atmosphere generator blocks in a volume of habitable air.
	 */
	public static void sourceSearch(World world, BlockPos pos, Set<BlockPos> set, ArrayList<BlockPos> foundList)
	{
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		Set<BlockPos> foundSet = new HashSet<BlockPos>();
		stack.push(pos);
		
		while(stack.size() > 0 && set.size() < MAX_VOLUME)
		{
			BlockPos blockPos = stack.pop();
			
			if(set.contains(blockPos) || foundSet.contains(blockPos))
				continue;
			
			if(tooFar(pos, blockPos))
				return;
			
			BlockState blockState = world.getBlockState(blockPos);
			
			if(blockState.getBlock() != Blocks.AIR && (!AirUtil.airBlocking(world, blockPos) || blockState.getBlock() == StarflightBlocks.HABITABLE_AIR))
			{
				set.add(blockPos);
				
				for(Direction direction : DIRECTIONS)
				{
					BlockPos offset = blockPos.offset(direction);
					stack.push(offset);
				}
			}
			else if(blockState.getBlock() == StarflightBlocks.ATMOSPHERE_GENERATOR)
				foundSet.add(blockPos);
		}
		
		if(set.size() < MAX_VOLUME)
		{
			for(BlockPos blockPos : foundSet)
			{
				BlockEntity blockEntity = world.getBlockEntity(blockPos);

				if(blockEntity != null && blockEntity instanceof AtmosphereGeneratorBlockEntity)
					foundList.add(blockPos);
			}
		}
	}
	
	public static void movingCraftSearch(World world, BlockPos pos, ArrayList<BlockPos> positionList, Set<BlockPos> set, @Nullable Direction movementDirection, int limit, int distance)
	{
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		stack.push(pos);

		while(stack.size() > 0 && set.size() < limit)
		{
			BlockPos blockPos = stack.pop();
			BlockState blockState = world.getBlockState(blockPos);

			if(!pos.isWithinDistance(blockPos, distance))
			{
				positionList.clear();
				return;
			}

			if(!set.contains(blockPos) && !blockState.isAir() && !blockState.isLiquid())
			{
				set.add(blockPos);
				positionList.add(blockPos);
				
				for(Direction checkDirection : DIRECTIONS)
				{
					BlockPos offset = blockPos.offset(checkDirection);
					BlockState offsetState = world.getBlockState(offset);

					if(movementDirection != null && movementDirection.equals(checkDirection))
						stack.push(offset);
					else if(!blockState.isIn(StarflightBlocks.EDGE_CASE_TAG))
					{
						if(!((blockState.getBlock() == StarflightBlocks.BUFFER && blockState.get(SimpleFacingBlock.FACING) == checkDirection)
						|| (offsetState.getBlock() == StarflightBlocks.BUFFER && offsetState.get(SimpleFacingBlock.FACING) == checkDirection.getOpposite())))
							stack.push(offset);
					}
				}
			}
		}

		if(positionList.size() >= limit)
			positionList.clear();
	}
	
	public static void energyConnectionSearch(World world, BlockPos pos)
	{
		BlockState blockState = world.getBlockState(pos);
		
		if(!(blockState.getBlock() instanceof EnergyBlock))
		{
			for(Direction direction : DIRECTIONS)
			{
				BlockPos offset = pos.offset(direction);
				
				if(world.getBlockState(offset).getBlock() instanceof EnergyBlock)
					energyConnectionSearch(world, offset);
			}
			
			return;
		}
		
		// Repeat for pass through, output, and input sides of the initial block.
		for(int i = 0; i < 3; i++)
		{
			Set<BlockPos> set = new HashSet<>();
			Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
			Set<BlockPos> energyProducers = new HashSet<BlockPos>();
			Set<BlockPos> energyConsumers = new HashSet<BlockPos>();
			stack.push(pos);
			
			// Search for energy producers to update.
			while(stack.size() > 0 && set.size() < MAX_VOLUME)
			{
				BlockPos blockPos = stack.pop();
				blockState = world.getBlockState(blockPos);
				set.add(blockPos);
				
				if(blockState.getBlock() instanceof EnergyBlock)
				{
					EnergyBlock energyBlock = (EnergyBlock) blockState.getBlock();
					
					for(Direction direction : DIRECTIONS)
					{
						BlockPos offset = blockPos.offset(direction);
						
						if(!set.contains(offset))
						{
							boolean continueSearch = false;
							
							if(blockPos.equals(pos))
							{
								if(i == 0)
									continueSearch = energyBlock.isPassThrough(world, blockPos, blockState, direction);
								else if(i == 1)
								{
									continueSearch = energyBlock.isOutput(world, blockPos, blockState, direction);
									
									if(continueSearch)
										energyProducers.add(blockPos);
										
								}
								else if(i == 2)
								{
									continueSearch = energyBlock.isInput(world, blockPos, blockState, direction);

									if(continueSearch)
										energyConsumers.add(blockPos);
								}
									
							}
							else
								continueSearch = energyBlock.isPassThrough(world, blockPos, blockState, direction);
							
							if(continueSearch)
							{
								BlockState offsetBlockState = world.getBlockState(offset);
								
								if(offsetBlockState.getBlock() instanceof EnergyBlock)
								{
									EnergyBlock offsetEnergyBlock = (EnergyBlock) offsetBlockState.getBlock();
									
									if(offsetEnergyBlock.isPassThrough(world, offset, offsetBlockState, direction.getOpposite()))
										stack.push(offset);
									
									if(offsetEnergyBlock.isOutput(world, offset, offsetBlockState, direction.getOpposite()))
										energyProducers.add(offset);
									
									if(offsetEnergyBlock.isInput(world, offset, offsetBlockState, direction.getOpposite()))
										energyConsumers.add(offset);
								}
							}
						}
					}
				}
			}
			
			for(BlockPos producerPos : energyProducers)
			{
				BlockEntity blockEntity = world.getBlockEntity(producerPos);
				
				if(blockEntity instanceof EnergyBlockEntity)
				{
					EnergyBlockEntity energyBlockEntity = (EnergyBlockEntity) blockEntity;
					energyBlockEntity.clearOutputs();
					//System.out.println("Updated: " + energyBlockEntity);
					
					for(BlockPos consumerBlockPos : energyConsumers)
					{
						energyBlockEntity.addOutput(consumerBlockPos);
						//System.out.println("        " + consumerBlockPos.toShortString());
					}
				}
			}
		}
	}
	
	private static boolean tooFar(BlockPos pos1, BlockPos pos2)
	{
		int xd = Math.abs(pos1.getX() - pos2.getX());
		int yd = Math.abs(pos1.getY() - pos2.getY());
		int zd = Math.abs(pos1.getZ() - pos2.getZ());
		return xd > MAX_DISTANCE || yd > MAX_DISTANCE || zd > MAX_DISTANCE;
	}
}