package space.block;

import java.util.ArrayList;
import java.util.function.BiPredicate;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import space.block.entity.AtmosphereGeneratorBlockEntity;
import space.util.AirUtil;
import space.util.BlockSearch;
import space.util.StarflightEffects;

public class HabitableAirBlock extends AirBlock
{
	public static final BooleanProperty UNSTABLE = Properties.UNSTABLE;
	
	public static final double DENSITY = 2.0; // The density of habitable air in kilograms per cubic meter.
	
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
					AirUtil.remove(world, pos, checkList, BlockSearch.MAX_VOLUME);
				else
					world.setBlockState(pos, state.with(UNSTABLE, false), 0);

				return;
			}
		}
		
		AirUtil.remove(world, pos, checkList, BlockSearch.MAX_VOLUME);
    }
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
		if(world.isClient)
			return;
		
		if(block != StarflightBlocks.HABITABLE_AIR && !AirUtil.airBlocking(world, fromPos))
		{
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			ArrayList<BlockPos> volumeList = new ArrayList<BlockPos>();
			AirUtil.findVolume(world, fromPos, volumeList, BlockSearch.MAX_VOLUME);
			
			System.out.println("Volume: " + volumeList.size());
			
			if(!volumeList.isEmpty())
			{
				BiPredicate<World, BlockPos> include = (w, p) -> {
					BlockState blockState = w.getBlockState(p);
					return !AirUtil.airBlocking(w, p) || blockState.getBlock() == StarflightBlocks.HABITABLE_AIR;
				};
				
				BiPredicate<World, BlockPos> edgeCase = (w, p) -> {
					BlockState blockState = w.getBlockState(p);
					return blockState.getBlock() == StarflightBlocks.ATMOSPHERE_GENERATOR;
				};
				
				ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
				BlockSearch.search(world, pos, checkList, foundList, include, edgeCase, BlockSearch.MAX_VOLUME, true);
				
				System.out.println("Found:" + foundList.size());
				
				for(BlockPos blockPos : foundList)
				{
					BlockEntity blockEntity = world.getBlockEntity(blockPos);

					if(blockEntity != null && blockEntity instanceof AtmosphereGeneratorBlockEntity)
					{
						if(((AtmosphereGeneratorBlockEntity) blockEntity).getPowerState() == 0)
							setUnstable(world, pos, state);
						else
							world.setBlockState(pos, state.with(UNSTABLE, false), 0);

						if(AirUtil.requestSupply(world, blockPos, volumeList.size() * DENSITY, StarflightBlocks.ATMOSPHERE_GENERATOR))
						{
							AirUtil.fillVolume(world, volumeList);
							return;
						}
					}
				}
				
				setUnstable(world, pos, state);
			}
			else
			{
				AirUtil.remove(world, pos, checkList, BlockSearch.MAX_VOLUME);
				StarflightEffects.sendOutgas(world, pos, fromPos, true);
			}
		}
    }
	
	private static void setUnstable(World world, BlockPos pos, BlockState state)
	{
		if(!state.get(UNSTABLE))
		{
			world.setBlockState(pos, state.with(UNSTABLE, true), 0);
			
			if(!world.getBlockTickScheduler().isQueued(pos, state.getBlock()))
				world.createAndScheduleBlockTick(pos, state.getBlock(), 40); //200 + world.getRandom().nextInt(200));
		}
	}
}