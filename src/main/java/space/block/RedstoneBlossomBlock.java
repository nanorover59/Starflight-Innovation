package space.block;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class RedstoneBlossomBlock extends FlowerBlock
{
	public static final DirectionProperty FACING = Properties.FACING;
	private static final Map<Direction, VoxelShape> FACING_TO_SHAPE = Maps.newEnumMap(
		ImmutableMap.of(
		Direction.UP,
		Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 12.0, 12.0),
		Direction.DOWN,
		Block.createCuboidShape(4.0, 4.0, 4.0, 12.0, 16.0, 12.0),
		Direction.NORTH,
		Block.createCuboidShape(4.0, 4.0, 4.0, 12.0, 12.0, 16.0),
		Direction.SOUTH,
		Block.createCuboidShape(4.0, 4.0, 0.0, 12.0, 12.0, 12.0),
		Direction.WEST,
		Block.createCuboidShape(4.0, 4.0, 4.0, 16.0, 12.0, 12.0),
		Direction.EAST,
		Block.createCuboidShape(0.0, 4.0, 4.0, 12.0, 12.0, 12.0)));

	public RedstoneBlossomBlock(RegistryEntry<StatusEffect> stewEffect, float effectLengthInSeconds, Settings settings)
	{
		super(stewEffect, effectLengthInSeconds, settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.UP));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return (VoxelShape) FACING_TO_SHAPE.get(state.get(FACING));
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		if(rotation != null)
			return (BlockState) state.with(FACING, rotation.rotate(state.get(FACING)));
		else
			return state;
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		if(mirror != null)
			return state.rotate(mirror.getRotation(state.get(FACING)));
		else
			return state;
	}
	
	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		BlockState blockState = super.getPlacementState(context);
		WorldView worldView = context.getWorld();
		BlockPos blockPos = context.getBlockPos();
		Direction[] directions = context.getPlacementDirections();
		
		for(Direction direction : directions)
		{
			blockState = blockState.with(FACING, direction.getOpposite());
			
			if(blockState.canPlaceAt(worldView, blockPos))
				return blockState;
		}

		return null;
	}
	
	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		Direction direction = state.get(FACING);
		BlockPos blockPos = pos.offset(direction.getOpposite());
		BlockState blockState = world.getBlockState(blockPos);
		return blockState.isIn(BlockTags.DIRT) || blockState.getBlock() == Blocks.MUD || blockState.getBlock() == StarflightBlocks.ARES_MOSS_BLOCK;
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(world.isClient)
			return;
		
		if(!moved)
		{
			for(Direction direction : Direction.values())
				world.updateNeighborsAlways(pos.offset(direction), this);
		}
	}
	
	@Override
	protected boolean emitsRedstonePower(BlockState state)
	{
		return true;
	}
	
	@Override
	protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction)
	{
		return 15;
	}
	
	@Override
	protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction)
	{
		return direction == state.get(FACING).getOpposite() ? state.getWeakRedstonePower(world, pos, direction) : 0;
	}
}