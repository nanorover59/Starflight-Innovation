package space.block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.util.AirUtil;
import space.util.BlockSearch;

public class SealedTrapdoorBlock extends TrapdoorBlock
{
	public SealedTrapdoorBlock(Settings settings)
	{
		super(settings, BlockSetType.IRON);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(world.isClient || !state.isOf(newState.getBlock()))
			return;
		
		BiPredicate<World, BlockPos> include = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return blockState.getBlock() instanceof SealedTrapdoorBlock || (blockState.getBlock() != Blocks.AIR && !AirUtil.airBlocking(w, p));
		};

		BiPredicate<World, BlockPos> edgeCase = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return blockState.getBlock() == StarflightBlocks.HABITABLE_AIR;
		};

		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
		Set<BlockPos> set = new HashSet<BlockPos>();
		BlockSearch.search(world, pos, checkList, foundList, include, edgeCase, BlockSearch.MAX_VOLUME, true);
		
		for(BlockPos blockPos : foundList)
		{
			if(newState.get(OPEN))
				world.updateNeighbor(blockPos, StarflightBlocks.AIRLOCK_TRAPDOOR, pos);
			else
				HabitableAirBlock.checkSource(world, blockPos, set);
		}
	}
}