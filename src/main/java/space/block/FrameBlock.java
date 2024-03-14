package space.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class FrameBlock extends Block implements Waterloggable
{
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	public FrameBlock(AbstractBlock.Settings settings)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(WATERLOGGED);
	}
	
	@Override
	public boolean isTransparent(BlockState state, BlockView world, BlockPos pos)
	{
		return !(Boolean) state.get(WATERLOGGED);
	}
	
	@Override
	public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction)
	{
		if(stateFrom.isOf(this))
			return true;
		
		return super.isSideInvisible(state, stateFrom, direction);
	}

	@Override
	public FluidState getFluidState(BlockState state)
	{
		if(state.get(WATERLOGGED).booleanValue())
			return Fluids.WATER.getStill(false);

		return super.getFluidState(state);
	}
	
	@Override
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		BlockPos blockPos = context.getBlockPos();
		FluidState fluidState = context.getWorld().getFluidState(blockPos);
		return this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER && fluidState.isStill());
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		if(state.get(WATERLOGGED).booleanValue())
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));

		return state;
	}
}