package space.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.entity.TargetingComputerBlockEntity;
import space.item.TargetingCardItem;

public class TargetingComputerBlock extends BlockWithEntity
{
	public static final MapCodec<TargetingComputerBlock> CODEC = TargetingComputerBlock.createCodec(TargetingComputerBlock::new);
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty LIT = Properties.LIT;

	public TargetingComputerBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
	}

	@Override
	public MapCodec<? extends TargetingComputerBlock> getCodec()
	{
		return CODEC;
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
		builder.add(LIT);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new TargetingComputerBlockEntity(pos, state);
	}
	
	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit)
	{
		if(!world.isClient && world.getBlockEntity(pos) instanceof TargetingComputerBlockEntity blockEntity)
		{
			blockEntity.dropCard();
			world.setBlockState(pos, state.with(LIT, false), Block.NOTIFY_ALL);
		}
		
		return ActionResult.success(world.isClient);
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(!state.get(LIT) && stack.getItem() instanceof TargetingCardItem && world.getBlockEntity(pos) instanceof TargetingComputerBlockEntity blockEntity)
		{
			if(blockEntity.getStack().isEmpty())
			{
				blockEntity.setStack(stack.copyAndEmpty());
				world.setBlockState(pos, state.with(LIT, true), Block.NOTIFY_ALL);
				return ItemActionResult.success(world.isClient);
			}
		}

		return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
	
	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(!state.isOf(newState.getBlock()))
		{
			if(world.getBlockEntity(pos) instanceof TargetingComputerBlockEntity blockEntity)
				blockEntity.dropCard();

			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}

	public BlockState getPlacementState(ItemPlacementContext context)
	{
		return (BlockState) this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	public boolean hasComparatorOutput(BlockState state)
	{
		return true;
	}

	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos)
	{
		return state.get(LIT) ? 15 : 0;
	}

	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return (BlockState) state.with(FACING, rotation.rotate((Direction) state.get(FACING)));
	}

	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.rotate(mirror.getRotation((Direction) state.get(FACING)));
	}
}
