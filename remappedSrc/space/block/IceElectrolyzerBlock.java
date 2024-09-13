package space.block;

import java.text.DecimalFormat;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
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
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.IceElectrolyzerBlockEntity;
import space.client.StarflightModClient;
import space.energy.EnergyNet;
import space.util.StarflightEffects;

public class IceElectrolyzerBlock extends BlockWithEntity implements EnergyBlock, FluidUtilityBlock
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	private static final double POWER_DRAW = 80.0;
	
	public IceElectrolyzerBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager)
	{
		stateManager.add(FACING);
		stateManager.add(LIT);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext context)
	{
		DecimalFormat df = new DecimalFormat("#.##");
		tooltip.add(Text.translatable("block.space.energy_consumer").append(String.valueOf(df.format(POWER_DRAW))).append("kJ/s").formatted(Formatting.LIGHT_PURPLE));
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.ice_electrolyzer.description_1"), Text.translatable("block.space.ice_electrolyzer.description_2"));
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
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
		
		if(blockEntity instanceof IceElectrolyzerBlockEntity)
			player.openHandledScreen((IceElectrolyzerBlockEntity) blockEntity);
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
				world.playSound(d, e, f, StarflightEffects.CURRENT_SOUND_EVENT, SoundCategory.BLOCKS, 0.25f, 0.5f - 0.1f * random.nextFloat(), true);
		}
	}

	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		if(itemStack.hasCustomName())
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);

			if(blockEntity instanceof IceElectrolyzerBlockEntity)
				((IceElectrolyzerBlockEntity) blockEntity).setCustomName(itemStack.getName());
		}
		
		if(!world.isClient())
			addNode(world, pos);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(!state.isOf(newState.getBlock()))
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if(blockEntity instanceof IceElectrolyzerBlockEntity)
			{
				if(world instanceof ServerWorld)
					ItemScatterer.spawn(world, (BlockPos) pos, (Inventory) ((IceElectrolyzerBlockEntity) blockEntity));

				world.updateComparators(pos, this);
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
		return new IceElectrolyzerBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.ICE_ELECTROLYZER_BLOCK_ENTITY, IceElectrolyzerBlockEntity::serverTick);
	}

	@Override
	public double getPowerOutput(World world, BlockPos pos, BlockState state)
	{
		return 0;
	}

	@Override
	public double getPowerDraw(World world, BlockPos pos, BlockState state)
	{
		BlockEntity blockEntity = world.getBlockEntity(pos);
		
		if(blockEntity != null && blockEntity instanceof IceElectrolyzerBlockEntity)
			return ((IceElectrolyzerBlockEntity) blockEntity).hasValidItem() ? POWER_DRAW : 0.0;
		
		return 0.0;
	}

	@Override
	public boolean isSideInput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction == (Direction) state.get(FACING).getOpposite() || direction == Direction.UP || direction == Direction.DOWN;
	}

	@Override
	public boolean isSideOutput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}

	@Override
	public void addNode(World world, BlockPos pos)
	{
		EnergyNet.addConsumer(world, pos);
	}
	
	@Override
	public String getFluidName()
	{
		return "hydrogen/oxygen";
	}

	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction == (Direction) state.get(FACING).rotateYClockwise() || direction == (Direction) state.get(FACING).rotateYCounterclockwise();
	}
}