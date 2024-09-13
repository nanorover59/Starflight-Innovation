package space.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.HydrogenInletValveBlockEntity;

public class HydrogenInletValveBlock extends BlockWithEntity implements FluidUtilityBlock
{
	public HydrogenInletValveBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public String getFluidName()
	{
		return "hydrogen";
	}
	
	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return true;
	}
	
	@Override
    public BlockRenderType getRenderType(BlockState state)
	{
        return BlockRenderType.MODEL;
    }

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new HydrogenInletValveBlockEntity(pos, state);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(state.hasBlockEntity() && !state.isOf(newState.getBlock()))
			world.removeBlockEntity(pos);
	}
}
