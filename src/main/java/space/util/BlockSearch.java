package space.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.function.BiPredicate;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.StarflightBlocks;

public class BlockSearch
{
	public static final int MAX_VOLUME = 16384;
	public static final int MAX_DISTANCE = 256;
	private static final Direction[] DIRECTIONS = Direction.values();
	
	/**
	 * Search the world starting at the given coordinates for coordinates where the BiPredicate returns true.
	 */
	public static void search(World world, BlockPos pos, ArrayList<BlockPos> positionList, BiPredicate<World, BlockPos> include, int limit, boolean distanceLimit)
	{
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		
		if(include.test(world, pos))
			stack.push(pos);
		
		while(stack.size() > 0 && positionList.size() < limit)
		{
			BlockPos blockPos = stack.pop();
			positionList.add(blockPos);
			
			if(distanceLimit && tooFar(pos, blockPos))
			{
				positionList.clear();
				return;
			}
				
			for(Direction direction : DIRECTIONS)
			{
				BlockPos offset = blockPos.offset(direction);
				
				if(!stack.contains(offset) && !positionList.contains(offset) && include.test(world, offset))
					stack.push(offset);
			}
		}
	}
	
	/**
	 * Search the world starting at the given coordinates for coordinates where the BiPredicate returns true.
	 */
	public static void search(WorldAccess world, BlockPos pos, ArrayList<BlockPos> positionList, BiPredicate<WorldAccess, BlockPos> include, int limit, boolean distanceLimit)
	{
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		
		if(include.test(world, pos))
			stack.push(pos);
		
		while(stack.size() > 0 && positionList.size() < limit)
		{
			BlockPos blockPos = stack.pop();
			positionList.add(blockPos);
			
			if(distanceLimit && tooFar(pos, blockPos))
			{
				positionList.clear();
				return;
			}
			
			for(Direction direction : DIRECTIONS)
			{
				BlockPos offset = blockPos.offset(direction);
				
				if(!stack.contains(offset) && !positionList.contains(offset) && include.test(world, offset))
					stack.push(offset);
			}
		}
	}
	
	/**
	 * Search the world starting at the given coordinates for coordinates where either BiPredicate returns true, but do not continue searching past edge case coordinates.
	 */
	public static void search(World world, BlockPos pos, ArrayList<BlockPos> positionList, BiPredicate<World, BlockPos> include, BiPredicate<World, BlockPos> edgeCase, int limit, boolean distanceLimit)
	{
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		
		if(include.test(world, pos))
			stack.push(pos);
		
		while(stack.size() > 0 && positionList.size() < limit)
		{
			BlockPos blockPos = stack.pop();
			positionList.add(blockPos);
			
			if(distanceLimit && tooFar(pos, blockPos))
			{
				positionList.clear();
				return;
			}
				
			for(Direction direction : DIRECTIONS)
			{
				BlockPos offset = blockPos.offset(direction);
				
				if(!stack.contains(offset) && !positionList.contains(offset))
				{
					if(include.test(world, offset))
						stack.push(offset);
					else if(edgeCase.test(world, offset))
						positionList.add(offset);
				}
			}
		}
	}
	
	public static void movingCraftSearch(World world, BlockPos blockPos, ArrayList<BlockPos> positionList, int limit)
	{
		BiPredicate<World, BlockPos> include = (w, p) -> {
			BlockState b = w.getBlockState(p);
			return b.getBlock() != Blocks.AIR && !b.isIn(StarflightBlocks.EXCLUDED_BLOCK_TAG) && !b.isIn(StarflightBlocks.EDGE_CASE_TAG);
		};
		
		BiPredicate<World, BlockPos> edgeCase = (w, p) -> {
			BlockState b = w.getBlockState(p);
			return b.isIn(StarflightBlocks.EDGE_CASE_TAG);
		};
		
		BlockSearch.search(world, blockPos, positionList, include, edgeCase, limit, true);
	}
	
	private static boolean tooFar(BlockPos pos1, BlockPos pos2)
	{
		int xd = Math.abs(pos1.getX() - pos2.getX());
		int yd = Math.abs(pos1.getY() - pos2.getY());
		int zd = Math.abs(pos1.getZ() - pos2.getZ());
		return xd > MAX_DISTANCE || yd > MAX_DISTANCE || zd > MAX_DISTANCE;
	}
}