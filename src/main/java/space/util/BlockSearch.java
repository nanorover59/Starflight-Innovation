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
import space.block.AtmosphereGeneratorBlock;
import space.block.ElectrolyzerBlock;
import space.block.EnergyBlock;
import space.block.ExtractorBlock;
import space.block.FluidPipeBlock;
import space.block.FluidUtilityBlock;
import space.block.PumpBlock;
import space.block.SimpleFacingBlock;
import space.block.StarflightBlocks;
import space.block.entity.AtmosphereGeneratorBlockEntity;
import space.block.entity.ElectrolyzerBlockEntity;
import space.block.entity.EnergyBlockEntity;
import space.block.entity.ExtractorBlockEntity;
import space.block.entity.FluidStorageBlockEntity;
import space.block.entity.PumpBlockEntity;

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
		Set<BlockPos> set = new HashSet<>();
		Set<BlockPos> actionSet = new HashSet<BlockPos>();
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		stack.push(pos);
		BlockState originBlockState = world.getBlockState(pos);
		
		if(originBlockState.getBlock() instanceof EnergyBlock)
		{
			for(Direction direction : DIRECTIONS)
			{
				if(((EnergyBlock) originBlockState.getBlock()).isOutput(world, pos, originBlockState, direction))
				{
					actionSet.add(pos);
					break;
				}
			}
		}
		
		while(stack.size() > 0 && set.size() < MAX_VOLUME)
		{
			BlockPos blockPos = stack.pop();
			set.add(blockPos);
			
			for(Direction direction : DIRECTIONS)
			{
				BlockPos offset = blockPos.offset(direction);
				BlockState offsetBlockState = world.getBlockState(offset);
				
				if(!set.contains(offset) && offsetBlockState.getBlock() instanceof EnergyBlock)
				{
					if(((EnergyBlock) offsetBlockState.getBlock()).isPassThrough(world, offset, offsetBlockState, direction.getOpposite()))
						stack.push(offset);
					
					if(((EnergyBlock) offsetBlockState.getBlock()).isOutput(world, offset, offsetBlockState, direction.getOpposite()))
						actionSet.add(offset);
				}
			}
		}
		
		for(BlockPos blockPos : actionSet)
		{
			BlockState blockState = world.getBlockState(blockPos);
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			
			if(blockEntity != null && blockEntity instanceof EnergyBlockEntity)
			{
				EnergyBlockEntity energyBlockEntity = (EnergyBlockEntity) blockEntity;
				Set<BlockPos> outputSet = new HashSet<>();
				energyBlockEntity.getOutputs().clear();
				
				for(Direction direction : DIRECTIONS)
				{
					if(((EnergyBlock) blockState.getBlock()).isOutput(world, blockPos, blockState, direction))
						outputSet.addAll(energyConsumerSearch(world, blockPos.offset(direction), direction));
				}
				
				energyBlockEntity.getOutputs().addAll(outputSet);
			}
		}
	}
	
	private static Set<BlockPos> energyConsumerSearch(World world, BlockPos pos, Direction initialDirection)
	{
		BlockState startingBlockState = world.getBlockState(pos);
		
		if(startingBlockState.getBlock() instanceof EnergyBlock)
		{
			EnergyBlock energyBlock = (EnergyBlock) startingBlockState.getBlock();
			
			if(energyBlock.isInput(world, pos, startingBlockState, initialDirection.getOpposite()))
				return Set.of(pos);
		}
		
		Set<BlockPos> set = new HashSet<>();
		Set<BlockPos> energyConsumers = new HashSet<BlockPos>();
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		stack.push(pos);
		
		while(stack.size() > 0 && set.size() < MAX_VOLUME)
		{
			BlockPos blockPos = stack.pop();
			BlockState blockState = world.getBlockState(blockPos);
			set.add(blockPos);

			if(blockState.getBlock() instanceof EnergyBlock)
			{
				for(Direction direction : DIRECTIONS)
				{
					BlockPos offset = blockPos.offset(direction);
					BlockState offsetBlockState = world.getBlockState(offset);
					
					if(!set.contains(offset) && offsetBlockState.getBlock() instanceof EnergyBlock)
					{
						EnergyBlock offsetEnergyBlock = (EnergyBlock) offsetBlockState.getBlock();
	
						if(offsetEnergyBlock.isPassThrough(world, offset, offsetBlockState, direction.getOpposite()))
							stack.push(offset);
						
						if(offsetEnergyBlock.isInput(world, offset, offsetBlockState, direction.getOpposite()))
							energyConsumers.add(offset);
					}
				}
			}
		}
		
		return energyConsumers;
	}
	
	public static void fluidConnectionSearch(World world, BlockPos pos, FluidResourceType fluidType)
	{
		Set<BlockPos> set = new HashSet<>();
		Set<BlockPos> actionSet = new HashSet<BlockPos>();
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		stack.push(pos);
		BlockState originBlockState = world.getBlockState(pos);
		
		if(originBlockState.getBlock() instanceof PumpBlock
		|| originBlockState.getBlock() instanceof ExtractorBlock
		|| originBlockState.getBlock() instanceof ElectrolyzerBlock
		|| originBlockState.getBlock() instanceof AtmosphereGeneratorBlock)
			actionSet.add(pos);
		
		while(stack.size() > 0 && set.size() < MAX_VOLUME)
		{
			BlockPos blockPos = stack.pop();
			set.add(blockPos);
			
			for(Direction direction : DIRECTIONS)
			{
				BlockPos offset = blockPos.offset(direction);
				BlockState offsetBlockState = world.getBlockState(offset);
				
				if(!set.contains(offset) && offsetBlockState.getBlock() instanceof FluidUtilityBlock
				&& ((FluidUtilityBlock) offsetBlockState.getBlock()).canPipeConnectToSide(world, offset, offsetBlockState, direction.getOpposite(), fluidType))
				{
					if(offsetBlockState.getBlock() instanceof FluidPipeBlock)
						stack.push(offset);
					else if(offsetBlockState.getBlock() instanceof PumpBlock
					|| offsetBlockState.getBlock() instanceof ExtractorBlock
					|| offsetBlockState.getBlock() instanceof ElectrolyzerBlock
					|| offsetBlockState.getBlock() instanceof AtmosphereGeneratorBlock)
						actionSet.add(offset);
				}
			}
		}
		
		for(BlockPos blockPos : actionSet)
		{
			BlockState blockState = world.getBlockState(blockPos);
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			
			if(blockEntity == null)
				continue;
			
			if(blockState.getBlock() instanceof PumpBlock)
			{
				PumpBlockEntity pumpBlockEntity = (PumpBlockEntity) blockEntity;
				Direction pumpFacing = blockState.get(PumpBlock.FACING);
				pumpBlockEntity.fluidSources.clear();
				pumpBlockEntity.fluidSinks.clear();
				pumpBlockEntity.fluidSources.addAll(fluidStorageSearch(world, blockPos.offset(pumpFacing.getOpposite()), pumpFacing.getOpposite(), fluidType));
				pumpBlockEntity.fluidSinks.addAll(fluidStorageSearch(world, blockPos.offset(pumpFacing), pumpFacing, fluidType));
			}
			else if(blockState.getBlock() instanceof ExtractorBlock)
			{
				ExtractorBlockEntity extractorBlockEntity = (ExtractorBlockEntity) blockEntity;
				Set<BlockPos> outputSet = new HashSet<>();
				extractorBlockEntity.waterOutputs.clear();
				
				for(Direction direction : DIRECTIONS)
				{
					if(direction == blockState.get(ExtractorBlock.FACING))
						continue;
					
					outputSet.addAll(fluidStorageSearch(world, blockPos.offset(direction), direction, FluidResourceType.WATER));
				}
				
				extractorBlockEntity.waterOutputs.addAll(outputSet);
			}
			else if(blockState.getBlock() instanceof ElectrolyzerBlock)
			{
				ElectrolyzerBlockEntity electrolyzerBlockEntity = (ElectrolyzerBlockEntity) blockEntity;
				Set<BlockPos> oxygenOutputSet = new HashSet<>();
				Set<BlockPos> hydrogenOutputSet = new HashSet<>();
				electrolyzerBlockEntity.oxygenOutputs.clear();
				electrolyzerBlockEntity.hydrogenOutputs.clear();
				
				for(Direction direction : DIRECTIONS)
				{
					if(direction == blockState.get(ElectrolyzerBlock.FACING))
						continue;
					
					oxygenOutputSet.addAll(fluidStorageSearch(world, blockPos.offset(direction), direction, FluidResourceType.OXYGEN));
					hydrogenOutputSet.addAll(fluidStorageSearch(world, blockPos.offset(direction), direction, FluidResourceType.HYDROGEN));
				}
				
				electrolyzerBlockEntity.oxygenOutputs.addAll(oxygenOutputSet);
				electrolyzerBlockEntity.hydrogenOutputs.addAll(hydrogenOutputSet);
			}
			else if(blockState.getBlock() instanceof AtmosphereGeneratorBlock)
			{
				AtmosphereGeneratorBlockEntity atmosphereGeneratorBlockEntity = (AtmosphereGeneratorBlockEntity) blockEntity;
				Set<BlockPos> oxygenSourceSet = new HashSet<>();
				atmosphereGeneratorBlockEntity.oxygenSources.clear();
				
				for(Direction direction : DIRECTIONS)
				{
					if(direction == blockState.get(AtmosphereGeneratorBlock.FACING))
						continue;
					
					oxygenSourceSet.addAll(fluidStorageSearch(world, blockPos.offset(direction), direction, FluidResourceType.OXYGEN));
				}
				
				atmosphereGeneratorBlockEntity.oxygenSources.addAll(oxygenSourceSet);
			}
		}
	}
	
	private static Set<BlockPos> fluidStorageSearch(World world, BlockPos pos, Direction initialDirection, FluidResourceType fluidType)
	{
		BlockState startingBlockState = world.getBlockState(pos);
		
		if(startingBlockState.getBlock() instanceof FluidUtilityBlock)
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);

			if(blockEntity != null && blockEntity instanceof FluidStorageBlockEntity)
			{
				if(((FluidUtilityBlock) startingBlockState.getBlock()).canPipeConnectToSide(world, pos, world.getBlockState(pos), initialDirection.getOpposite(), fluidType))
					return Set.of(pos);
			}
		}
		
		Set<BlockPos> set = new HashSet<>();
		Set<BlockPos> fluidStorage = new HashSet<BlockPos>();
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		stack.push(pos);
		
		while(stack.size() > 0 && set.size() < MAX_VOLUME)
		{
			BlockPos blockPos = stack.pop();
			BlockState blockState = world.getBlockState(blockPos);
			set.add(blockPos);
			
			if(blockState.getBlock() instanceof FluidUtilityBlock)
			{
				if(blockState.getBlock() instanceof FluidPipeBlock)
				{
					for(Direction direction : DIRECTIONS)
					{
						BlockPos offset = blockPos.offset(direction);
						BlockState offsetBlockState = world.getBlockState(offset);
						
						if(!set.contains(offset) && offsetBlockState.getBlock() instanceof FluidUtilityBlock
						&& ((FluidUtilityBlock) offsetBlockState.getBlock()).canPipeConnectToSide(world, offset, offsetBlockState, direction.getOpposite(), fluidType))
							stack.push(offset);
					}
				}
				else
				{
					BlockEntity offsetBlockEntity = world.getBlockEntity(blockPos);
					
					if(offsetBlockEntity != null && offsetBlockEntity instanceof FluidStorageBlockEntity)
						fluidStorage.add(blockPos);
				}
			}
		}
		
		return fluidStorage;
	}
	
	private static boolean tooFar(BlockPos pos1, BlockPos pos2)
	{
		int xd = Math.abs(pos1.getX() - pos2.getX());
		int yd = Math.abs(pos1.getY() - pos2.getY());
		int zd = Math.abs(pos1.getZ() - pos2.getZ());
		return xd > MAX_DISTANCE || yd > MAX_DISTANCE || zd > MAX_DISTANCE;
	}
}