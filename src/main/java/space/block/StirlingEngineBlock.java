package space.block;

import java.util.Random;

import org.jetbrains.annotations.Nullable;

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
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.StirlingEngineBlockEntity;
import space.energy.EnergyNet;

public class StirlingEngineBlock extends BlockWithEntity implements EnergyBlock
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty LIT = Properties.LIT;

	public StirlingEngineBlock(Settings settings)
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

	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new StirlingEngineBlockEntity(pos, state);
	}

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
		
		if(blockEntity instanceof StirlingEngineBlockEntity)
			player.openHandledScreen((StirlingEngineBlockEntity) blockEntity);
	}

	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
	{
		if((Boolean) state.get(LIT))
		{
			double d = (double) pos.getX() + 0.5D;
			double e = (double) pos.getY();
			double f = (double) pos.getZ() + 0.5D;
			
			if(random.nextDouble() < 0.1D)
				world.playSound(d, e, f, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);

			Direction direction = (Direction) state.get(FACING);
			Direction.Axis axis = direction.getAxis();
			double h = random.nextDouble() * 0.6D - 0.3D;
			double i = axis == Direction.Axis.X ? (double) direction.getOffsetX() * 0.52D : h;
			double j = random.nextDouble() * 6.0D / 16.0D;
			double k = axis == Direction.Axis.Z ? (double) direction.getOffsetZ() * 0.52D : h;
			world.addParticle(ParticleTypes.SMOKE, d + i, e + j, f + k, 0.0D, 0.0D, 0.0D);
			world.addParticle(ParticleTypes.FLAME, d + i, e + j, f + k, 0.0D, 0.0D, 0.0D);
		}
	}

	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
	}

	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		if(itemStack.hasCustomName())
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);

			if(blockEntity instanceof StirlingEngineBlockEntity)
				((StirlingEngineBlockEntity) blockEntity).setCustomName(itemStack.getName());
		}
		
		addNode(world, pos);
	}
	
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(!state.isOf(newState.getBlock()))
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if(blockEntity instanceof StirlingEngineBlockEntity)
			{
				if(world instanceof ServerWorld)
					ItemScatterer.spawn(world, (BlockPos) pos, (Inventory) ((StirlingEngineBlockEntity) blockEntity));

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

	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return (BlockState) state.with(FACING, rotation.rotate((Direction) state.get(FACING)));
	}

	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.rotate(mirror.getRotation((Direction) state.get(FACING)));
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : checkType(type, StarflightBlocks.STIRLING_ENGINE_BLOCK_ENTITY, StirlingEngineBlockEntity::serverTick);
	}

	@Override
	public double getPowerOutput(WorldAccess world, BlockPos pos, BlockState state)
	{
		BlockEntity blockEntity = world.getBlockEntity(pos);
		
		if(blockEntity != null && blockEntity instanceof StirlingEngineBlockEntity)
			return ((StirlingEngineBlockEntity) blockEntity).isBurning() ? 10.0D : 0.0D;
		
		return 0.0D;
	}

	@Override
	public double getPowerDraw(WorldAccess world, BlockPos pos, BlockState state)
	{
		return 0;
	}

	@Override
	public boolean isSideInput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}

	@Override
	public boolean isSideOutput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return true;
	}

	@Override
	public void addNode(World world, BlockPos pos)
	{
		EnergyNet.addProducer(world, pos);
	}
}