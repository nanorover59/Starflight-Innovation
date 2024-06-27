package space.block;

import java.util.ArrayList;
import java.util.function.BiPredicate;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TransparentBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import space.util.BlockSearch;

public class RedstoneDopedBlock extends TransparentBlock
{
	public static final MapCodec<RedstoneDopedBlock> CODEC = RedstoneDopedBlock.createCodec(RedstoneDopedBlock::new);
	public static final BooleanProperty LIT = Properties.LIT;
	private final int pressTicks;

	public RedstoneDopedBlock(int pressTicks, Settings settings)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(LIT, false));
		this.pressTicks = pressTicks;
	}
	
	public RedstoneDopedBlock(Settings settings)
	{
		this(40, settings);
	}

	@Override
	public MapCodec<? extends RedstoneDopedBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager)
	{
		stateManager.add(LIT);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit)
	{
		if(state.get(LIT).booleanValue())
			return ActionResult.CONSUME;
		
		BiPredicate<World, BlockPos> include = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return blockState.isOf(this);
		};
		
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		BlockSearch.search(world, pos, checkList, include, 256, true);
		
		for(BlockPos foundPos : checkList)
		{
			world.setBlockState(foundPos, state.with(LIT, true), Block.NOTIFY_ALL);
			world.updateNeighborsAlways(foundPos, this);
			world.scheduleBlockTick(foundPos, this, pressTicks);
		}
		
		world.emitGameEvent((Entity) player, GameEvent.BLOCK_ACTIVATE, pos);
		world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS);
		return ActionResult.success(world.isClient);
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(moved || state.isOf(newState.getBlock()))
			return;

		if(state.get(LIT).booleanValue())
			world.updateNeighborsAlways(pos, this);

		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		if(!state.get(LIT).booleanValue())
			return;
		
		world.setBlockState(pos, state.with(LIT, false), Block.NOTIFY_ALL);
		world.updateNeighborsAlways(pos, this);
	}
	
	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction)
	{
		return state.get(LIT) != false ? 15 : 0;
	}

	@Override
	public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction)
	{
		return state.get(LIT) != false ? 15 : 0;
	}

	@Override
	public boolean emitsRedstonePower(BlockState state)
	{
		return true;
	}
}