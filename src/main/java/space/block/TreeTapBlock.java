package space.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import space.client.StarflightModClient;
import space.item.StarflightItems;

public class TreeTapBlock extends WallMountedBlock implements Waterloggable
{
	public static final BooleanProperty RUBBER_SAP = BooleanProperty.of("rubber_sap");
	protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(4.0, 0.0, 8.0, 12.0, 11.0, 16.0);
	protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(4.0, 0.0, 0.0, 12.0, 11.0, 8.0);
	protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(8.0, 0.0, 4.0, 16.0, 11.0, 12.0);
	protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 4.0, 8.0, 11.0, 12.0);
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	public TreeTapBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(FACING, Direction.NORTH).with(RUBBER_SAP, false).with(WATERLOGGED, false)));
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext context)
	{
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.tree_tap.description"));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, FACE, RUBBER_SAP, WATERLOGGED);
	}

	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	private VoxelShape shapeFromDirection(Direction direction)
	{
		switch(direction)
		{
		case EAST:
			return EAST_SHAPE;
		case WEST:
			return WEST_SHAPE;
		case SOUTH:
			return SOUTH_SHAPE;
		default:
			return NORTH_SHAPE;
		}
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		Direction direction = state.get(FACING);
		return shapeFromDirection(direction);
	}

	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		Direction direction = state.get(FACING);
		return shapeFromDirection(direction);
	}

	public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos)
	{
		Direction direction = state.get(FACING);
		return shapeFromDirection(direction);
	}
	
	@Override
	public FluidState getFluidState(BlockState state)
	{
		if(state.get(WATERLOGGED).booleanValue())
			return Fluids.WATER.getStill(false);
		
		return super.getFluidState(state);
	}

	public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos)
	{
		return true;
	}

	@Override
	public boolean hasRandomTicks(BlockState state)
	{
		return !state.get(RUBBER_SAP).booleanValue();
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		Direction direction = state.get(FACING).getOpposite();
		BlockPos blockPos = pos.offset(direction);
		return world.getBlockState(blockPos).getBlock() == StarflightBlocks.RUBBER_LOG;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(!state.get(RUBBER_SAP).booleanValue() || world.getBlockState(pos).getBlock() != StarflightBlocks.TREE_TAP)
			return ActionResult.CONSUME;

		ItemStack itemStack = new ItemStack(StarflightItems.RUBBER_SAP, 1);
		ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + world.random.nextDouble(), (double) pos.getY() + world.random.nextDouble(), (double) pos.getZ() + world.random.nextDouble(), itemStack);
		itemEntity.setToDefaultPickupDelay();
		world.spawnEntity(itemEntity);
		world.setBlockState(pos, world.getBlockState(pos).with(RUBBER_SAP, false), Block.NOTIFY_LISTENERS);
		return ActionResult.success(world.isClient);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		if(state.get(RUBBER_SAP).booleanValue() || world.getBlockState(pos).getBlock() != StarflightBlocks.TREE_TAP)
			return;

		world.setBlockState(pos, world.getBlockState(pos).with(RUBBER_SAP, true), Block.NOTIFY_LISTENERS);
	}
	
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		if(state.get(WATERLOGGED).booleanValue())
			world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));

		return state;
	}
}