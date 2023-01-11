package space.block;

import java.util.ArrayList;
import java.util.function.BiPredicate;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.util.AirUtil;
import space.util.BlockSearch;
import space.util.StarflightEffects;

public class HabitableAirBlock extends AirBlock
{
	public static final double DENSITY = 2.0; // The density of habitable air in kilograms per cubic meter.
	
	public HabitableAirBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
		if(world.isClient)
			return;
		
		if(!AirUtil.airBlocking(world, fromPos))
		{
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
			AirUtil.findVolume(world, fromPos, foundList, BlockSearch.MAX_VOLUME);
			
			BiPredicate<World, BlockPos> include = (w, p) -> {
				BlockState blockState = w.getBlockState(p);
				return !AirUtil.airBlocking(w, p) || blockState.getBlock() == StarflightBlocks.HABITABLE_AIR;
			};
			
			BiPredicate<World, BlockPos> edgeCase = (w, p) -> {
				BlockState blockState = w.getBlockState(p);
				return blockState.getBlock() == StarflightBlocks.ATMOSPHERE_GENERATOR;
			};
			
			BlockSearch.search(world, pos, checkList, include, edgeCase, BlockSearch.MAX_VOLUME, true);
			
			for(BlockPos blockPos : checkList)
			{
				if(world.getBlockState(blockPos).getBlock() == StarflightBlocks.ATMOSPHERE_GENERATOR && AirUtil.requestSupply(world, blockPos, foundList.size() * DENSITY, StarflightBlocks.ATMOSPHERE_GENERATOR))
				{
					AirUtil.fillVolume(world, foundList);
					return;
				}
			}
			
			AirUtil.remove(world, pos, checkList, BlockSearch.MAX_VOLUME);
			StarflightEffects.sendOutgas(world, pos, fromPos, true);
		}
    }
}