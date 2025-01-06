package space.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CaveVines;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import space.item.StarflightItems;

public class AeroplanktonVinesHeadBlock extends AbstractPlantStemBlock implements Fertilizable, CaveVines
{
	public static final MapCodec<AeroplanktonVinesHeadBlock> CODEC = createCodec(AeroplanktonVinesHeadBlock::new);

	@Override
	public MapCodec<AeroplanktonVinesHeadBlock> getCodec()
	{
		return CODEC;
	}

	public AeroplanktonVinesHeadBlock(AbstractBlock.Settings settings)
	{
		super(settings, Direction.DOWN, SHAPE, false, 0.1);
		this.setDefaultState(this.stateManager.getDefaultState().with(AGE, Integer.valueOf(0)).with(BERRIES, Boolean.valueOf(false)));
	}

	@Override
	protected int getGrowthLength(Random random)
	{
		return 1;
	}

	@Override
	protected boolean chooseStemState(BlockState state)
	{
		return state.isAir();
	}

	@Override
	protected Block getPlant()
	{
		return StarflightBlocks.AEROPLANKTON_VINES;
	}

	@Override
	protected BlockState copyState(BlockState from, BlockState to)
	{
		return to.with(BERRIES, (Boolean) from.get(BERRIES));
	}

	@Override
	protected BlockState age(BlockState state, Random random)
	{
		return super.age(state, random).with(BERRIES, Boolean.valueOf(random.nextFloat() < 0.11F));
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit)
	{
		if(state.get(BERRIES))
		{
			Block.dropStack(world, pos, new ItemStack(StarflightItems.VURNBERRY, 1));
			float f = MathHelper.nextBetween(world.random, 0.8F, 1.2F);
			world.playSound(null, pos, SoundEvents.BLOCK_CAVE_VINES_PICK_BERRIES, SoundCategory.BLOCKS, 1.0F, f);
			BlockState blockState = state.with(BERRIES, Boolean.valueOf(false));
			world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
			world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, blockState));
			return ActionResult.success(world.isClient);
		}
		else
			return ActionResult.PASS;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		super.appendProperties(builder);
		builder.add(BERRIES);
	}

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state)
	{
		return !(Boolean) state.get(BERRIES);
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state)
	{
		return true;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state)
	{
		world.setBlockState(pos, state.with(BERRIES, Boolean.valueOf(true)), Block.NOTIFY_LISTENERS);
	}
}