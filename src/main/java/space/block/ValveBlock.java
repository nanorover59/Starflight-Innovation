package space.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.ValveBlockEntity;
import space.item.StarflightItems;
import space.util.FluidResourceType;

public class ValveBlock extends BlockWithEntity implements FluidUtilityBlock
{
	public static final MapCodec<ValveBlock> CODEC = ValveBlock.createCodec(ValveBlock::new);
	public static final IntProperty MODE = IntProperty.of("mode", 0, 2);
	
	public ValveBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public MapCodec<? extends ValveBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(MODE);
	}
	
	@Override
    public BlockRenderType getRenderType(BlockState state)
	{
        return BlockRenderType.MODEL;
    }
	
	@Override
	public FluidResourceType getFluidType()
	{
		return FluidResourceType.ANY;
	}
	
	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return true;
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(player.getActiveItem().getItem() != StarflightItems.WRENCH)
			return ActionResult.PASS;
		
		if(world.isClient)
			return ActionResult.SUCCESS;

		state = (BlockState) state.cycle(MODE);
		world.setBlockState(pos, state, Block.NOTIFY_ALL);
		return ActionResult.CONSUME;
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(state.hasBlockEntity() && !state.isOf(newState.getBlock()))
			world.removeBlockEntity(pos);
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new ValveBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.VALVE_BLOCK_ENTITY, ValveBlockEntity::serverTick);
	}
}
