package space.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.BalloonControllerBlockEntity;
import space.client.StarflightModClient;
import space.item.StarflightItems;
import space.util.FluidResourceType;

public class BalloonControllerBlock extends FluidTankControllerBlock implements FluidUtilityBlock
{
	public static final MapCodec<BalloonControllerBlock> CODEC = BalloonControllerBlock.createCodec(BalloonControllerBlock::new);
	public static final IntProperty MODE = ValveBlock.MODE;
	
	public BalloonControllerBlock(Settings settings)
	{
		super(settings, FluidResourceType.HYDROGEN, 0.25);
	}
	
	@Override
	public MapCodec<? extends BalloonControllerBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(MODE);
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext context)
	{
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.balloon.description_1"), Text.translatable("block.space.balloon.description_2"));
	}
	
	@Override
	public FluidResourceType getFluidType()
	{
		return FluidResourceType.HYDROGEN;
	}
	
	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return true;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(world.isClient)
			return ActionResult.PASS;

		if(player.getActiveItem().getItem() == StarflightItems.WRENCH)
		{
			state = (BlockState) state.cycle(MODE);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			return ActionResult.CONSUME;
		}

		return super.onUse(state, world, pos, player, hand, hit);
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new BalloonControllerBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.BALLOON_CONTROLLER_BLOCK_ENTITY, BalloonControllerBlockEntity::serverTick);
	}
}