package space.block;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.LivingEntity;
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
import space.energy.EnergyNet;

public class SolarPanelBlock extends Block implements EnergyBlock, Waterloggable
{
	public static double NOMINAL_OUTPUT = 2.5;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
	
	public SolarPanelBlock(Settings settings)
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
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return SHAPE;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return SHAPE;
	}
	
	@Override
	public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos)
	{
		return SHAPE;
	}

	@Override
	public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos)
	{
		return !(Boolean) state.get(WATERLOGGED);
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
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		if(!world.isClient())
		{
			addNode(world, pos);
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			EnergyNet.updateEnergyNodes(world, pos, checkList);
		}
	}
	
	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify)
	{
		if(!world.isClient())
		{
			addNode(world, pos);
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			EnergyNet.updateEnergyNodes(world, pos, checkList);
		}
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(!world.isClient())
		{
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			EnergyNet.updateEnergyNodes(world, pos, checkList);
		}
	}
	
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		if(state.get(WATERLOGGED).booleanValue())
			world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));

		return state;
	}
	
	@Override
	public double getPowerOutput(WorldAccess world, BlockPos pos, BlockState state)
	{
		if(!world.getDimension().hasSkyLight() || !world.isSkyVisible(pos.up()))
			return 0;
		else
		{
			// Calculate the output of this solar panel at Earth's distance to to the sun taking the sky angle into account.
			float f = world.getSkyAngle(1.0f);
			float highLimit1 = 0.05f;
			float highLimit2 = 1.0f - highLimit1;
			float lowLimit1 = 0.25f;
			float lowLimit2 = 1.0f - lowLimit1;
			
			if(f < highLimit1 || f > highLimit2)
				return NOMINAL_OUTPUT;
			else if(f < lowLimit1)
				return NOMINAL_OUTPUT * Math.pow(1.0 - ((f - highLimit1) / (lowLimit1 - highLimit1)), 1.0 / 3.0);
			else if(f > lowLimit2)
				return NOMINAL_OUTPUT * Math.pow(1.0 - ((f - highLimit2) / (lowLimit2 - highLimit2)), 1.0 / 3.0);
			return 0.0;
		}
	}

	@Override
	public double getPowerDraw(WorldAccess world, BlockPos pos, BlockState state)
	{
		return 0;
	}

	@Override
	public boolean isSideInput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}

	@Override
	public boolean isSideOutput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction == Direction.DOWN || world.getBlockState(pos.offset(direction)).getBlock() instanceof SolarPanelBlock || world.getBlockState(pos.offset(direction)).isFullCube(world, pos.offset(direction));
	}

	@Override
	public void addNode(World world, BlockPos pos)
	{
		EnergyNet.addProducer(world, pos);
	}
}