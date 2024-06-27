package space.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import space.util.BlockSearch;

public class EnergyCableBlock extends Block implements EnergyBlock, Waterloggable
{
	public static final MapCodec<EnergyCableBlock> CODEC = EnergyCableBlock.createCodec(EnergyCableBlock::new);
	public static final BooleanProperty NORTH = BooleanProperty.of("north");
	public static final BooleanProperty EAST = BooleanProperty.of("east");
	public static final BooleanProperty SOUTH = BooleanProperty.of("south");
	public static final BooleanProperty WEST = BooleanProperty.of("west");
	public static final BooleanProperty UP = BooleanProperty.of("up");
	public static final BooleanProperty DOWN = BooleanProperty.of("down");
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	
	public EnergyCableBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(NORTH, false).with(EAST, false).with(SOUTH, false).with(WEST, false).with(UP, false).with(DOWN, false).with(WATERLOGGED, false));
	}
	
	@Override
	public MapCodec<? extends EnergyCableBlock> getCodec()
	{
		return CODEC;
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
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return Block.createCuboidShape(5.0d, 5.0d, 5.0d, 11.0d, 11.0d, 11.0d);
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return Block.createCuboidShape(5.0d, 5.0d, 5.0d, 11.0d, 11.0d, 11.0d);
	}
	
	@Override
	public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos)
	{
		return Block.createCuboidShape(5.0d, 5.0d, 5.0d, 11.0d, 11.0d, 11.0d);
	}
	
	@Override
	public boolean isTransparent(BlockState state, BlockView world, BlockPos pos)
	{
		return !(Boolean) state.get(WATERLOGGED);
	}
	
	@Override
	public boolean canPathfindThrough(BlockState state, NavigationType type)
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
		int connections = 0;
		
		for(Direction d : DIRECTIONS)
		{
			state = updateStateForConnection(world, pos, world.getBlockState(pos.offset(d)), state, d);
			
			if(isConnected(state, d))
				connections++;
		}
		
		world.setBlockState(pos, state);
		
		if(!world.isClient && connections > 1)
			BlockSearch.energyConnectionSearch(world, pos);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(state.isOf(newState.getBlock()))
			return;
		
		int connections = 0;
		
		for(Direction d : DIRECTIONS)
		{
			if(isConnected(state, d))
				connections++;
		}
		
		if(!world.isClient && connections > 1)
			BlockSearch.energyConnectionSearch(world, pos);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
		if(state.get(WATERLOGGED).booleanValue())
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		
		for(Direction d : DIRECTIONS)
			state = updateStateForConnection(world, pos, world.getBlockState(pos.offset(d)), state, d);
		
		world.setBlockState(pos, state);
    }
	
	public BlockState updateStateForConnection(World world, BlockPos neighborPos, BlockState neighborState, BlockState initialState, Direction direction)
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
	
	public boolean isConnected(BlockState state, Direction direction)
	{
		if((direction == Direction.NORTH && state.get(NORTH))
		|| (direction == Direction.EAST && state.get(EAST))
		|| (direction == Direction.SOUTH && state.get(SOUTH))
		|| (direction == Direction.WEST && state.get(WEST))
		|| (direction == Direction.UP && state.get(UP))
		|| (direction == Direction.DOWN && state.get(DOWN)))
			return true;
		
		return false;
	}
	
	public boolean canConnect(World world, BlockPos pos, BlockState state, Direction direction)
	{
		BlockPos offset = pos.offset(direction);
		BlockState offsetState = world.getBlockState(pos.offset(direction));
		
		if(!(offsetState.getBlock() instanceof EnergyBlock))
			return false;
		
		EnergyBlock energyBlock = (EnergyBlock) offsetState.getBlock();
		return energyBlock.canConnectToCables(world, offset, offsetState, direction.getOpposite());
	}
	
	@Override
	public boolean isPassThrough(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return isConnected(state, direction);
	}
	
	@Override
	public boolean canConnectToCables(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return true;
	}
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		BlockState newState = state.getBlock().getDefaultState();
		newState = newState.with(UP, state.get(UP));
		newState = newState.with(DOWN, state.get(DOWN));
		newState = newState.with(WATERLOGGED, state.get(WATERLOGGED));
		
		if(rotation == BlockRotation.NONE)
		{
			newState = newState.with(NORTH, state.get(NORTH));
			newState = newState.with(EAST, state.get(EAST));
			newState = newState.with(SOUTH, state.get(SOUTH));
			newState = newState.with(WEST, state.get(WEST));
		}
		else if(rotation == BlockRotation.CLOCKWISE_180)
		{
			newState = newState.with(NORTH, state.get(SOUTH));
			newState = newState.with(EAST, state.get(WEST));
			newState = newState.with(SOUTH, state.get(NORTH));
			newState = newState.with(WEST, state.get(EAST));
		}
		else if(rotation == BlockRotation.COUNTERCLOCKWISE_90)
		{
			newState = newState.with(NORTH, state.get(EAST));
			newState = newState.with(EAST, state.get(SOUTH));
			newState = newState.with(SOUTH, state.get(WEST));
			newState = newState.with(WEST, state.get(NORTH));
		}
		else if(rotation == BlockRotation.CLOCKWISE_90)
		{
			newState = newState.with(NORTH, state.get(WEST));
			newState = newState.with(EAST, state.get(NORTH));
			newState = newState.with(SOUTH, state.get(EAST));
			newState = newState.with(WEST, state.get(SOUTH));
		}
		
		return newState;
	}
}