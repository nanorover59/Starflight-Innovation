package space.block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.ExtractorBlockEntity;
import space.item.StarflightItems;
import space.util.BlockSearch;
import space.util.FluidResourceType;
import space.util.StarflightSoundEvents;

public class ExtractorBlock extends BlockWithEntity implements EnergyBlock, FluidUtilityBlock
{
	public static final MapCodec<ExtractorBlock> CODEC = ExtractorBlock.createCodec(ExtractorBlock::new);
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	
	public ExtractorBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
	}
	
	@Override
	public MapCodec<? extends ExtractorBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager)
	{
		stateManager.add(FACING);
		stateManager.add(LIT);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_consumer", df.format(getInput())).formatted(Formatting.LIGHT_PURPLE));
		textList.add(Text.translatable("block.space.extractor.description"));
		StarflightItems.hiddenItemTooltip(tooltip, textList);
	}
	
	@Override
	public long getInput()
	{
		return 32;
	}
	
	@Override
	public long getEnergyCapacity()
	{
		return 64;
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit)
	{
		if(!world.isClient)
		{
			NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

			if(screenHandlerFactory != null)
				player.openHandledScreen(screenHandlerFactory);
		}

		return ActionResult.SUCCESS;
	}
	
	public void openScreen(World world, BlockPos pos, PlayerEntity player)
	{
		BlockEntity blockEntity = world.getBlockEntity(pos);
		
		if(blockEntity instanceof ExtractorBlockEntity)
			player.openHandledScreen((ExtractorBlockEntity) blockEntity);
	}
	
	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
	{
		if((Boolean) state.get(LIT))
		{
			double d = (double) pos.getX() + 0.5;
			double e = (double) pos.getY();
			double f = (double) pos.getZ() + 0.5;
			
			if(random.nextDouble() < 0.1)
				world.playSound(d, e, f, StarflightSoundEvents.CURRENT_SOUND_EVENT, SoundCategory.BLOCKS, 0.25f, 0.5f - 0.1f * random.nextFloat(), true);
		}
	}

	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		super.onPlaced(world, pos, state, placer, itemStack);
		
		if(!world.isClient)
		{
			BlockSearch.energyConnectionSearch(world, pos);
			BlockSearch.fluidConnectionSearch(world, pos, FluidResourceType.WATER);
		}
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(!state.isOf(newState.getBlock()))
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);
			
			if(blockEntity instanceof ExtractorBlockEntity)
			{
				if(world instanceof ServerWorld)
					ItemScatterer.spawn(world, (BlockPos) pos, (Inventory) ((ExtractorBlockEntity) blockEntity));

				world.updateComparators(pos, this);
			}
			
			if(!world.isClient)
			{
				BlockSearch.energyConnectionSearch(world, pos);
				BlockSearch.fluidConnectionSearch(world, pos, FluidResourceType.WATER);
			}

			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}

	public boolean hasComparatorOutput(BlockState state)
	{
		return true;
	}

	public int getComparatorOutput(BlockState state, World world, BlockPos pos)
	{
		return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return (BlockState) state.with(FACING, rotation.rotate((Direction) state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.rotate(mirror.getRotation((Direction) state.get(FACING)));
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new ExtractorBlockEntity(pos, state);
	}
	
	@Override
	public boolean isInput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction != (Direction) state.get(FACING);
	}
	
	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidResourceType fluidType)
	{
		return fluidType == FluidResourceType.WATER && direction != state.get(FACING).getOpposite();
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.EXTRACTOR_BLOCK_ENTITY, ExtractorBlockEntity::serverTick);
	}
}