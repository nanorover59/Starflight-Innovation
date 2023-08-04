package space.block;

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
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.ElectricFurnaceBlockEntity;
import space.energy.EnergyNet;
import space.util.StarflightEffects;

public class ElectricFurnaceBlock extends BlockWithEntity implements EnergyBlock
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty LIT = Properties.LIT;

	public ElectricFurnaceBlock(Settings settings)
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
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new ElectricFurnaceBlockEntity(pos, state);
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
		
		if(blockEntity instanceof ElectricFurnaceBlockEntity)
			player.openHandledScreen((ElectricFurnaceBlockEntity) blockEntity);
	}
	
	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
	{
		if((Boolean) state.get(LIT))
		{
			double d = (double) pos.getX() + 0.5;
			double e = (double) pos.getY();
			double f = (double) pos.getZ() + 0.5;
			
			if(random.nextFloat() < 0.1f)
				world.playSound(d, e, f, StarflightEffects.CURRENT_SOUND_EVENT, SoundCategory.BLOCKS, 0.1f, 0.5f - 0.1f * random.nextFloat(), true);
		}
	}

	public BlockState getPlacementState(ItemPlacementContext context)
	{
		return (BlockState) this.getDefaultState().with(FACING, context.getPlayerFacing().getOpposite());
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		if(itemStack.hasCustomName())
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);

			if(blockEntity instanceof ElectricFurnaceBlockEntity)
				((ElectricFurnaceBlockEntity) blockEntity).setCustomName(itemStack.getName());
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
			if(blockEntity instanceof ElectricFurnaceBlockEntity)
			{
				if(world instanceof ServerWorld)
				{
					ItemScatterer.spawn(world, (BlockPos) pos, (Inventory) ((ElectricFurnaceBlockEntity) blockEntity));
					((ElectricFurnaceBlockEntity) blockEntity).getRecipesUsedAndDropExperience((ServerWorld) world, Vec3d.ofCenter(pos));
				}

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
		return world.isClient ? null : checkType(type, StarflightBlocks.ELECTRIC_FURNACE_BLOCK_ENTITY, ElectricFurnaceBlockEntity::serverTick);
	}

	@Override
	public double getPowerOutput(World world, BlockPos pos, BlockState state)
	{
		return 0.0;
	}

	@Override
	public double getPowerDraw(World world, BlockPos pos, BlockState state)
	{
		BlockEntity blockEntity = world.getBlockEntity(pos);
		
		if(blockEntity != null && blockEntity instanceof ElectricFurnaceBlockEntity)
			return ((ElectricFurnaceBlockEntity) blockEntity).hasValidItem() ? 10.0 : 0.0;
		
		return 0.0;
	}

	@Override
	public boolean isSideInput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return true;
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
}