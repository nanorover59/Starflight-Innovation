package space.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.FluidContainerBlockEntity;

public class FluidPipeBlock extends BlockWithEntity implements Waterloggable, FluidUtilityBlock
{
	public static final BooleanProperty NORTH = BooleanProperty.of("north");
	public static final BooleanProperty EAST = BooleanProperty.of("east");
	public static final BooleanProperty SOUTH = BooleanProperty.of("south");
	public static final BooleanProperty WEST = BooleanProperty.of("west");
	public static final BooleanProperty UP = BooleanProperty.of("up");
	public static final BooleanProperty DOWN = BooleanProperty.of("down");
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	public FluidPipeBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(NORTH, false).with(EAST, false).with(SOUTH, false).with(WEST, false).with(UP, false).with(DOWN, false).with(WATERLOGGED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager)
	{
		stateManager.add(NORTH);
		stateManager.add(EAST);
		stateManager.add(SOUTH);
		stateManager.add(WEST);
		stateManager.add(UP);
		stateManager.add(DOWN);
		stateManager.add(WATERLOGGED);
	}

	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return Block.createCuboidShape(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
	}

	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return Block.createCuboidShape(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
	}

	public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos)
	{
		return Block.createCuboidShape(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
	}

	public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos)
	{
		return !(Boolean) state.get(WATERLOGGED);
	}

	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type)
	{
		return false;
	}

	@Override
	public FluidState getFluidState(BlockState state)
	{
		if(state.get(WATERLOGGED).booleanValue())
			return Fluids.WATER.getStill(false);
		
		return super.getFluidState(state);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		Direction[] directions = new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN };

		for(Direction d : directions)
			state = updateStateForConnection(world, pos.offset(d), world.getBlockState(pos.offset(d)), state, d);

		world.setBlockState(pos, state);
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify)
	{
		Direction[] directions = new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN };

		for(Direction d : directions)
			state = updateStateForConnection(world, pos.offset(d), world.getBlockState(pos.offset(d)), state, d);

		world.setBlockState(pos, state);
	}

	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{

	}
	
	@Override
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		BlockPos blockPos = context.getBlockPos();
		FluidState fluidState = context.getWorld().getFluidState(blockPos);
		return this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER && fluidState.isStill());
	}

	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		if(state.get(WATERLOGGED).booleanValue())
			world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));

		return updateStateForConnection(world, neighborPos, neighborState, state, direction);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new FluidContainerBlockEntity(null, pos, state);
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return null;
	}

	private BlockState updateStateForConnection(WorldAccess world, BlockPos neighborPos, BlockState neighborState, BlockState initialState, Direction direction)
	{
		BlockState result = initialState;

		if(canConnect(world, neighborPos, neighborState, direction))
		{
			if(direction == Direction.NORTH)
				result = result.with(NORTH, true);
			else if(direction == Direction.EAST)
				result = result.with(EAST, true);
			else if(direction == Direction.SOUTH)
				result = result.with(SOUTH, true);
			else if(direction == Direction.WEST)
				result = result.with(WEST, true);
			else if(direction == Direction.UP)
				result = result.with(UP, true);
			else if(direction == Direction.DOWN)
				result = result.with(DOWN, true);
		}
		else
		{
			if(direction == Direction.NORTH)
				result = result.with(NORTH, false);
			else if(direction == Direction.EAST)
				result = result.with(EAST, false);
			else if(direction == Direction.SOUTH)
				result = result.with(SOUTH, false);
			else if(direction == Direction.WEST)
				result = result.with(WEST, false);
			else if(direction == Direction.UP)
				result = result.with(UP, false);
			else if(direction == Direction.DOWN)
				result = result.with(DOWN, false);
		}

		return result;
	}

	private boolean canConnect(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		if(state.getBlock() instanceof FluidUtilityBlock)
			return ((FluidUtilityBlock) state.getBlock()).getFluidName().contains(getFluidName()) && ((FluidUtilityBlock) state.getBlock()).canPipeConnectToSide(world, pos, state, direction);
		else
			return state.getBlock() == this;
	}

	public boolean isConnected(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		if((direction == Direction.NORTH && state.get(NORTH)) || (direction == Direction.EAST && state.get(EAST)) || (direction == Direction.SOUTH && state.get(SOUTH)) || (direction == Direction.WEST && state.get(WEST)) || (direction == Direction.UP && state.get(UP)) || (direction == Direction.DOWN && state.get(DOWN)))
			return true;

		return false;
	}

	@Override
	public String getFluidName()
	{
		return "null";
	}

	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return true;
	}
}
