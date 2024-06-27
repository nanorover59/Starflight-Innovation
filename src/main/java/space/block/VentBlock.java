package space.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.PumpBlockEntity;
import space.block.entity.VentBlockEntity;
import space.client.StarflightModClient;
import space.util.FluidResourceType;

public class VentBlock extends BlockWithEntity implements FluidUtilityBlock
{
	public static final MapCodec<VentBlock> CODEC = VentBlock.createCodec(VentBlock::new);
	public static final DirectionProperty FACING = Properties.FACING;
	public static final IntProperty VENT_STATE = IntProperty.of("vent_state", 0, 2);

	public VentBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.UP).with(VENT_STATE, 0));
	}
	
	@Override
	protected MapCodec<? extends VentBlock> getCodec()
	{
		return CODEC;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.vent.description_1"), Text.translatable("block.space.vent.description_2"), Text.translatable("block.space.vent.description_3"));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager)
	{
		stateManager.add(FACING);
		stateManager.add(VENT_STATE);
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
		
		VentBlockEntity blockEntity = (VentBlockEntity) world.getBlockEntity(pos);
		blockEntity.updateWaterState(world, pos);
		PumpBlockEntity.intakeFilterSearch(world, pos, true);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
		if(world.isClient)
			return;
		
		VentBlockEntity blockEntity = (VentBlockEntity) world.getBlockEntity(pos);
		blockEntity.updateWaterState(world, pos);
		PumpBlockEntity.intakeFilterSearch(world, pos, true);
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
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
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
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new VentBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.VENT_BLOCK_ENTITY, VentBlockEntity::serverTick);
	}
}