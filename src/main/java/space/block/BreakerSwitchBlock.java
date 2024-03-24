package space.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.util.BlockSearch;

public class BreakerSwitchBlock extends Block implements EnergyBlock
{
	public static final DirectionProperty FACING = Properties.FACING;
	public static final BooleanProperty LIT = Properties.LIT;

	public BreakerSwitchBlock(AbstractBlock.Settings settings)
	{
		super(settings);
		this.setDefaultState((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(LIT, false));
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
	{
		if(world.isClient)
			return;
		
		BlockSearch.energyConnectionSearch(world, pos);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(world.isClient)
			return;
		
		if(newState.isOf(state.getBlock()) && !newState.get(LIT))
		{
			BlockSearch.energyConnectionSearch(world, pos.offset(newState.get(FACING)));
			BlockSearch.energyConnectionSearch(world, pos.offset(newState.get(FACING).getOpposite()));
		}
		else
			BlockSearch.energyConnectionSearch(world, pos);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
		if(world.isClient)
			return;
		
		world.setBlockState(pos, state.with(LIT, world.isReceivingRedstonePower(pos)), Block.NOTIFY_ALL);
		//BlockSearch.energyConnectionSearch(world, pos);
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
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, LIT);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
	}
	
	@Override
	public boolean isPassThrough(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return state.get(LIT) && (direction == state.get(FACING) || direction == state.get(FACING).getOpposite());
	}
	
	@Override
	public boolean canConnectToCables(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction == state.get(FACING) || direction == state.get(FACING).getOpposite();
	}
}