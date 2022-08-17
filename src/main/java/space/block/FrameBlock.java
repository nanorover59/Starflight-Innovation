package space.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

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
	public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction)
	{
		return false;
	}
	
	@Override
	public FluidState getFluidState(BlockState state)
	{
		return state.get(WATERLOGGED).booleanValue() ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos)
	{
		return true;
	}
}