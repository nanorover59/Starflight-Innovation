package space.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class EncasedEnergyCableBlock extends EnergyCableBlock
{
	public EncasedEnergyCableBlock(Settings settings)
	{
		super(settings);
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
	public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos)
	{
		return false;
	}
	
	@Override
	public FluidState getFluidState(BlockState state)
	{
		return Fluids.EMPTY.getDefaultState();
	}
}
