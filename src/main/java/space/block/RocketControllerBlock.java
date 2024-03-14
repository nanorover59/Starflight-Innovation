package space.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.entity.RocketControllerBlockEntity;
import space.client.gui.RocketControllerScreen;

public class RocketControllerBlock extends BlockWithEntity
{
	public static final MapCodec<RocketControllerBlock> CODEC = RocketControllerBlock.createCodec(RocketControllerBlock::new);
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	protected RocketControllerBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)));
	}

	@Override
	public MapCodec<? extends RocketControllerBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new RocketControllerBlockEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return validateTicker(type, StarflightBlocks.ROCKET_CONTROLLER_BLOCK_ENTITY, (world1, pos, blockState, blockEntity) -> RocketControllerBlockEntity.tick(world1, pos, blockState, blockEntity));
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(world.isClient)
		{
			MinecraftClient minecraft = MinecraftClient.getInstance();
        	minecraft.setScreen(new RocketControllerScreen(world.getRegistryKey().getValue().toString(), pos));
		}
		else
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);
			
			if(blockEntity != null && blockEntity instanceof RocketControllerBlockEntity)
			{
				RocketControllerBlockEntity rocketController = (RocketControllerBlockEntity) blockEntity;
				rocketController.sendDisplayData((ServerPlayerEntity) player);
			}
		}
		
		return ActionResult.SUCCESS;
	}

	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	public boolean hasComparatorOutput(BlockState state)
	{
		return true;
	}

	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos)
	{
		return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
	}

	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return (BlockState) state.with(FACING, rotation.rotate((Direction) state.get(FACING)));
	}

	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.rotate(mirror.getRotation((Direction) state.get(FACING)));
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}
}
