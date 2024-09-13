package space.block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import space.block.entity.AtmosphereGeneratorBlockEntity;
import space.util.AirUtil;
import space.util.BlockSearch;
import space.util.StarflightEffects;

public class HabitableAirBlock extends AirBlock
{
	public static final BooleanProperty UNSTABLE = Properties.UNSTABLE;
	public static final double DENSITY = 1.0; // The density of habitable air in kilograms per cubic meter.
	
	public HabitableAirBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(UNSTABLE, false));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(UNSTABLE);
	}

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
    {
        if(world.isClient || !state.get(UNSTABLE).booleanValue())
        	return;
        
        BiPredicate<World, BlockPos> include = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return !AirUtil.airBlocking(w, p) || blockState.getBlock() == StarflightBlocks.HABITABLE_AIR;
		};
		
		BiPredicate<World, BlockPos> edgeCase = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return blockState.getBlock() == StarflightBlocks.ATMOSPHERE_GENERATOR;
		};
		
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
		BlockSearch.search(world, pos, checkList, foundList, include, edgeCase, BlockSearch.MAX_VOLUME, true);
		
		for(BlockPos blockPos : foundList)
		{
			BlockEntity blockEntity = world.getBlockEntity(blockPos);

			if(blockEntity != null && blockEntity instanceof AtmosphereGeneratorBlockEntity)
			{
				if(((AtmosphereGeneratorBlockEntity) blockEntity).getPowerState() == 0)
					AirUtil.remove(world, pos, BlockSearch.MAX_VOLUME);
				else
					world.setBlockState(pos, state.with(UNSTABLE, false), Block.NOTIFY_LISTENERS);

				return;
			}
		}
		
		AirUtil.remove(world, pos, BlockSearch.MAX_VOLUME);
    }
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
		if(world.isClient)
			return;
		
		BlockState currentNeighborState = world.getBlockState(fromPos);
		
		if(block == StarflightBlocks.HABITABLE_AIR || block == StarflightBlocks.LEAK)
			return;
		
		if(currentNeighborState.getBlock() == block && !(block instanceof SealedDoorBlock || block instanceof SealedTrapdoorBlock))
			return;

		//long time = System.currentTimeMillis();
		ArrayList<BlockPos> volumeList = new ArrayList<BlockPos>();
		ArrayList<BlockPos> updateList = new ArrayList<BlockPos>();
		boolean foundVolume = AirUtil.findVolume(world, pos, volumeList, updateList, BlockSearch.MAX_VOLUME);

		if(foundVolume)
		{
			//System.out.println("Found Volume: " + (System.currentTimeMillis() - time));
			//time = System.currentTimeMillis();

			if(volumeList.size() == 0)
				return;
			else if(volumeList.size() > 0 && volumeList.size() < 3)
			{
				AirUtil.fillVolume(world, volumeList, updateList);
				return;
			}

			Set<BlockPos> set = new HashSet<BlockPos>();
			ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
			BlockSearch.sourceSearch(world, pos, set, foundList);

			//System.out.println("Found Source: " + (System.currentTimeMillis() - time));
			//time = System.currentTimeMillis();

			for(BlockPos blockPos : foundList)
			{
				BlockEntity blockEntity = world.getBlockEntity(blockPos);

				if(blockEntity != null && blockEntity instanceof AtmosphereGeneratorBlockEntity)
				{
					if(((AtmosphereGeneratorBlockEntity) blockEntity).getPowerState() == 0)
						setUnstable(world, pos, state);
					else
						world.setBlockState(pos, state.with(UNSTABLE, false), Block.NOTIFY_LISTENERS);

					if(AirUtil.requestSupply(world, blockPos, volumeList.size() * DENSITY, StarflightBlocks.ATMOSPHERE_GENERATOR))
						AirUtil.fillVolume(world, volumeList, updateList);
					else
						AirUtil.remove(world, pos, BlockSearch.MAX_VOLUME);

					return;
				}
			}

			setUnstable(world, pos, state);
		}
		else
		{
			BiPredicate<World, BlockPos> include = (w, p) -> {
				BlockState blockState = w.getBlockState(p);
				return blockState.getBlock() != Blocks.AIR && (!AirUtil.airBlocking(w, p) || blockState.getBlock() == StarflightBlocks.HABITABLE_AIR);
			};

			BiPredicate<World, BlockPos> edgeCase = (w, p) -> {
				return false;
			};

			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
			BlockSearch.search(world, pos, checkList, foundList, include, edgeCase, BlockSearch.MAX_VOLUME, true);
			AirUtil.removeOrLeak(world, pos, fromPos, checkList.size() / 5, BlockSearch.MAX_VOLUME);
			StarflightEffects.sendOutgas(world, pos, fromPos, true);
		}
    }
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(world.isClient)
			return;
		
		if(newState.getBlock() == Blocks.AIR || newState.getBlock() == StarflightBlocks.LEAK || !AirUtil.airBlockingState(world, pos, newState))
			return;
		
		Set<BlockPos> set = new HashSet<BlockPos>();
		
		for(Direction direction : Direction.values())
		{
			BlockPos offset = pos.offset(direction);
			checkSource(world, offset, set);
		}
	}
	
	public static void checkSource(World world, BlockPos pos, Set<BlockPos> set)
	{
		BlockState state = world.getBlockState(pos);
		
		if(!(state.getBlock() instanceof HabitableAirBlock) || set.contains(pos))
			return;
		
		//long time = System.currentTimeMillis();
		ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
		BlockSearch.sourceSearch(world, pos, set, foundList);
		boolean source = false;
		//System.out.println("Found Source: " + (System.currentTimeMillis() - time));

		for(BlockPos blockPos : foundList)
		{
			BlockEntity blockEntity = world.getBlockEntity(blockPos);

			if(blockEntity != null && blockEntity instanceof AtmosphereGeneratorBlockEntity)
			{
				if(((AtmosphereGeneratorBlockEntity) blockEntity).getPowerState() == 1)
				{
					source = true;
					break;
				}
			}
		}

		if(!source)
			setUnstable(world, pos, state);
	}
	
	public static void setUnstable(World world, BlockPos pos, BlockState state)
	{
		if(!state.get(UNSTABLE))
		{
			world.setBlockState(pos, state.with(UNSTABLE, true), Block.NOTIFY_LISTENERS);
			
			if(!world.getBlockTickScheduler().isQueued(pos, state.getBlock()))
				world.scheduleBlockTick(pos, state.getBlock(), 400 + world.getRandom().nextInt(200));
			
			//System.out.println("Unstable");
		}
	}
}