package space.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.OxygenInletValveBlockEntity;

public class OxygenInletValveBlock extends BlockWithEntity implements FluidUtilityBlock
{
	public OxygenInletValveBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public String getFluidName()
	{
		return "oxygen";
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
		return new OxygenInletValveBlockEntity(pos, state);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(state.hasBlockEntity() && !state.isOf(newState.getBlock()))
			world.removeBlockEntity(pos);
	}
}
