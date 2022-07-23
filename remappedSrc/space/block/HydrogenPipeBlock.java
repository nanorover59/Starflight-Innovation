package space.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.entity.FluidContainerBlockEntity;
import space.block.entity.HydrogenPipeBlockEntity;

public class HydrogenPipeBlock extends FluidPipeBlock
{
	public HydrogenPipeBlock(Settings settings)
	{
		super(settings);
	}

	@Override
	public String getFluidName()
	{
		return "hydrogen";
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new HydrogenPipeBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return (world1, pos, blockState, blockEntity) -> HydrogenPipeBlockEntity.tick(world1, pos, blockState, (FluidContainerBlockEntity) blockEntity);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(state.hasBlockEntity() && !state.isOf(newState.getBlock()))
			world.removeBlockEntity(pos);
	}
}
