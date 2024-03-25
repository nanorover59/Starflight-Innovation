package space.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.MutableText;
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
import space.util.BlockSearch;
import space.util.FluidResourceType;
import space.util.StarflightEffects;

public class BalloonControllerBlock extends BlockWithEntity implements FluidUtilityBlock
{
	public static final MapCodec<BalloonControllerBlock> CODEC = BalloonControllerBlock.createCodec(BalloonControllerBlock::new);
	public static final IntProperty MODE = ValveBlock.MODE;
	private final double capacity;
	
	public BalloonControllerBlock(Settings settings)
	{
		super(settings);
		this.capacity = 4.0;
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
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
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
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(state.isOf(newState.getBlock()))
			return;
		
		BalloonControllerBlockEntity balloonController = (BalloonControllerBlockEntity) world.getBlockEntity(pos);
		
		if(balloonController.getStoredFluid() > capacity)
		{
			for(Direction direction : Direction.values())
			{
				if(world.getBlockState(pos.offset(direction.getOpposite())).getBlock() == StarflightBlocks.FLUID_TANK_INSIDE)
					StarflightEffects.sendOutgas(world, pos, pos.offset(direction), true);
			}
		}
		
		if(balloonController.getStoredFluid() > capacity)
			world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.0F, World.ExplosionSourceType.BLOCK);
		
		if(state.hasBlockEntity())
			world.removeBlockEntity(pos);
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

		BlockEntity blockEntity = world.getBlockEntity(pos);

		if(blockEntity instanceof BalloonControllerBlockEntity)
		{
			BalloonControllerBlockEntity balloonController = (BalloonControllerBlockEntity) blockEntity;

			if(balloonController.getStorageCapacity() == 0)
			{
				int result = initializeBalloon(world, pos, capacity, balloonController);
				MutableText text = Text.translatable("");

				if(result < 4)
					text.append(Text.translatable("block.space.fluid_tank_" + result));

				if(text != Text.EMPTY)
					player.sendMessage(text, true);

				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.PASS;
	}
	
	public int initializeBalloon(World world, BlockPos position, BalloonControllerBlockEntity balloonController)
	{
		return initializeBalloon(world, position, capacity, balloonController);
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

	protected static int initializeBalloon(World world, BlockPos position, double capacity, BalloonControllerBlockEntity balloonController)
	{
		boolean valid = false;
		balloonController.setStorageCapacity(0);
		balloonController.setStoredFluid(0);

		for(Direction direction : Direction.values())
		{
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>(); // Check list to avoid ensure each block is only checked once.
			ArrayList<BlockPos> actionList = new ArrayList<BlockPos>(); // List of all fluid tank controller and interface blocks found.
			
			BiPredicate<World, BlockPos> include = (w, p) -> {
				return !world.getBlockState(p).isIn(StarflightBlocks.BALLOON_BLOCK_TAG);
			};
			
			BlockSearch.search(world, position.offset(direction), checkList, include, BlockSearch.MAX_VOLUME, true);
			
			if(checkList.size() > 0 && checkList.size() < BlockSearch.MAX_VOLUME)
			{
				double cx = 0;
				double cy = 0;
				double cz = 0;
				int count = 0;

				for(BlockPos p : checkList)
				{
					if(world.getBlockState(p).isAir())
					{
						world.setBlockState(p, StarflightBlocks.FLUID_TANK_INSIDE.getDefaultState(), Block.FORCE_STATE);
						cx += p.getX();
						cy += p.getY();
						cz += p.getZ();
						count++;
						
						for(Direction direction1 : Direction.values())
						{
							BlockPos offset = p.offset(direction1);
							BlockEntity blockEntity = world.getBlockEntity(offset);

							if(blockEntity != null && blockEntity instanceof BalloonControllerBlockEntity)
								actionList.add(offset);
						}
					}
				}
				
				cx /= count;
				cy /= count;
				cz /= count;
				
				// Check for excess balloon controllers.
				for(BlockPos p : actionList)
				{
					if(p.equals(position))
						continue;
					
					BlockState blockState = world.getBlockState(p);
					
					if(blockState.getBlock() instanceof BalloonControllerBlock)
						return 2;
				}
				
				balloonController.setStorageCapacity(capacity * count);
				balloonController.setCenterOfMass(new BlockPos((int) cx, (int) cy, (int) cz));
				balloonController.markDirty();
				valid = true;
			}
		}

		return valid ? 0 : 1;
	}
}