package space.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class SolidLeverBlock extends Block
{
	public static final DirectionProperty FACING = Properties.FACING;
	public static final BooleanProperty POWERED = Properties.POWERED;

	public SolidLeverBlock(AbstractBlock.Settings settings)
	{
		super(settings);
		this.setDefaultState((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(FACING, Direction.UP)).with(POWERED, false));
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, POWERED);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(world.isClient)
		{
			state.cycle(POWERED);
			return ActionResult.SUCCESS;
		}

		BlockState blockState = (BlockState) state.cycle(POWERED);
		world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
		this.updateNeighbors(blockState, world, pos);
		float f = blockState.get(POWERED) != false ? 0.6f : 0.5f;
		world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3f, f);
		world.emitGameEvent((Entity) player, blockState.get(POWERED) != false ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
		return ActionResult.CONSUME;
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(moved || state.isOf(newState.getBlock()))
			return;
		
		if(state.get(POWERED).booleanValue())
			this.updateNeighbors(state, world, pos);
		
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction)
	{
		return state.get(POWERED) != false ? 15 : 0;
	}

	@Override
	public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction)
	{
		return state.get(POWERED).booleanValue() && state.get(FACING) == direction ? 15 : 0;
	}

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

	private void updateNeighbors(BlockState state, World world, BlockPos pos)
	{
		world.updateNeighborsAlways(pos, this);
		world.updateNeighborsAlways(pos.offset(state.get(FACING).getOpposite()), this);
	}
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return (BlockState) state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
	}
}