package space.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import space.block.entity.EncasedOxygenPipeALBlockEntity;
import space.block.entity.FluidContainerBlockEntity;
import space.block.entity.OxygenPipeBlockEntity;

public class EncasedOxygenPipeALBlock extends OxygenPipeBlock
{
	protected EncasedOxygenPipeALBlock(Settings settings)
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
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new EncasedOxygenPipeALBlockEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return (world1, pos, blockState, blockEntity) -> OxygenPipeBlockEntity.tick(world1, pos, blockState, (FluidContainerBlockEntity) blockEntity);
	}
}