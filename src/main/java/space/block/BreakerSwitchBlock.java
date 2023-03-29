package space.block;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.entity.StorageCubeBlockEntity;
import space.energy.EnergyNet;

public class BreakerSwitchBlock extends Block
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
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(state.isOf(newState.getBlock()))
			return;

		BlockEntity blockEntity = world.getBlockEntity(pos);

		if(blockEntity instanceof Inventory)
		{
			ItemScatterer.spawn(world, pos, (Inventory) ((Object) blockEntity));
			world.updateComparators(pos, this);
		}

		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
	{
		BlockEntity blockEntity = world.getBlockEntity(pos);

		if(itemStack.hasCustomName() && blockEntity instanceof StorageCubeBlockEntity)
			((StorageCubeBlockEntity) blockEntity).setCustomName(itemStack.getName());
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
		if(world.isClient)
			return;
		
		world.setBlockState(pos, state.with(LIT, world.isReceivingRedstonePower(pos)), Block.NOTIFY_LISTENERS);
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		EnergyNet.updateEnergyNodes(world, pos, checkList);
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
}