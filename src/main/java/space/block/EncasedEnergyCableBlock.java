package space.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
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

public class EncasedEnergyCableBlock extends EnergyCableBlock
{
	public static final MapCodec<EncasedEnergyCableBlock> CODEC = EncasedEnergyCableBlock.createCodec(EncasedEnergyCableBlock::new);
	public static final DirectionProperty FACING = Properties.FACING;
	
	public EncasedEnergyCableBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(FACING, Direction.UP)));
	}
	
	@Override
	public MapCodec<? extends EncasedEnergyCableBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		super.appendProperties(builder);
		builder.add(FACING);
	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return Block.createCuboidShape(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return Block.createCuboidShape(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
	}
	
	@Override
	public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos)
	{
		return Block.createCuboidShape(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
	}
	
	@Override
	public boolean isTransparent(BlockState state, BlockView world, BlockPos pos)
	{
		return false;
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
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		return (BlockState) this.getDefaultState().with(FACING, context.getPlayerLookDirection().getOpposite());
	}
	
	@Override
	public FluidState getFluidState(BlockState state)
	{
		return Fluids.EMPTY.getDefaultState();
	}
	
	@Override
	public boolean canConnectToCables(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction == state.get(FACING) || direction == state.get(FACING).getOpposite();
	}
}