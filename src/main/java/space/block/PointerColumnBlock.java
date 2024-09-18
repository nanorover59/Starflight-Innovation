package space.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class PointerColumnBlock extends Block
{
	public static final DirectionProperty FACING = Properties.FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	public static final BooleanProperty PRESSED = BooleanProperty.of("pressed");
	
	private static final int TICKS = 80;

	public PointerColumnBlock(AbstractBlock.Settings settings)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.UP).with(LIT, false).with(PRESSED, false));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, LIT, PRESSED);
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit)
	{
		if(state.get(LIT).booleanValue() || player.isHolding(state.getBlock().asItem()))
			return ActionResult.PASS;
		
		BlockState newState = state.with(LIT, true).with(PRESSED, true);
		world.setBlockState(pos, newState, Block.NOTIFY_ALL);
		updateOthers(newState, world, pos);
		world.emitGameEvent((Entity) player, GameEvent.BLOCK_ACTIVATE, pos);
		world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS);
		return ActionResult.success(world.isClient);
	}
	
	@Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
    {
        if(world.isClient)
        	return;
        
        if(state.get(LIT) && state.get(PRESSED))
        {
        	BlockState newState = state.with(LIT, false).with(PRESSED, false);
        	world.setBlockState(pos,newState, Block.NOTIFY_ALL);
        	updateOthers(newState, world, pos);
        }
    }
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(world.isClient)
			return;
		
		if(newState.getBlock() == state.getBlock() && newState.get(PRESSED))
			world.scheduleBlockTick(pos, this, TICKS);
		
		if(!moved)
		{
			for(Direction direction : Direction.values())
				world.updateNeighborsAlways(pos.offset(direction), this);
		}
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
		if(world.isClient)
			return;
		
		if(!state.get(PRESSED) && fromPos.equals(pos.offset(state.get(FACING).getOpposite())) && world.getBlockState(fromPos).getBlock() != state.getBlock())
		{
			BlockState newState = state.with(LIT, world.getEmittedRedstonePower(fromPos, state.get(FACING).getOpposite()) > 0).with(PRESSED, false);
			world.setBlockState(pos, newState, Block.NOTIFY_ALL);
			updateOthers(newState, world, pos);
		}
    }
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		return this.getDefaultState().with(FACING, context.getSide().getOpposite());
	}
	
	@Override
	protected boolean emitsRedstonePower(BlockState state)
	{
		return true;
	}
	
	@Override
	protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction)
	{
		if(direction == state.get(FACING))
			return 0;
		else
			return state.get(LIT) ? 15 : 0;
	}
	
	@Override
	protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction)
	{
		if(direction == state.get(FACING).getOpposite())
			return state.get(LIT) ? 15 : 0;
		else
			return 0;
	}
	
	private void updateOthers(BlockState state, World world, BlockPos pos)
	{
		Mutable mutable = pos.mutableCopy();
		
		for(int i = 0; i < 256; i++)
		{
			mutable.set(mutable.offset(state.get(FACING)));
			BlockState otherState = world.getBlockState(mutable);
			
			if(otherState.getBlock() == state.getBlock() && otherState.get(FACING) == state.get(FACING))
				world.setBlockState(mutable, state, Block.NOTIFY_ALL);
			else
				break;
		}
		
		mutable = pos.mutableCopy();
		
		for(int i = 0; i < 256; i++)
		{
			mutable.set(mutable.offset(state.get(FACING).getOpposite()));
			BlockState otherState = world.getBlockState(mutable);
			
			if(otherState.getBlock() == state.getBlock() && otherState.get(FACING) == state.get(FACING))
				world.setBlockState(mutable, state, Block.NOTIFY_ALL);
			else
				break;
		}
	}
}