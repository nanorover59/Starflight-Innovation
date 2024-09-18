package space.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import space.particle.StarflightParticleTypes;

public class LunalightOreBlock extends RedstoneOreBlock
{
	public LunalightOreBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	protected void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player)
	{
		light(state, world, pos);
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity)
	{
		if(!world.isClient && !entity.bypassesSteppingEffects())
			light(state, world, pos);
	}
	
	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(!world.isClient)
			light(state, world, pos);

		return stack.getItem() instanceof BlockItem && new ItemPlacementContext(player, hand, stack, hit).canPlace() ? ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION : ItemActionResult.SUCCESS;
	}

	private static void light(BlockState state, World world, BlockPos pos)
	{
		if(!state.get(LIT))
			world.setBlockState(pos, state.with(LIT, Boolean.valueOf(true)), Block.NOTIFY_ALL);
	}
	
	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
	{
		if(state.get(LIT))
			spawnParticles(world, pos);
	}
	
	private static void spawnParticles(World world, BlockPos pos)
	{
		double d = 0.8;
		Random random = world.random;

		for(Direction direction : Direction.values())
		{
			BlockPos blockPos = pos.offset(direction);
			
			if(!world.getBlockState(blockPos).isOpaqueFullCube(world, blockPos) && random.nextInt(4) == 0)
			{
				Direction.Axis axis = direction.getAxis();
				double e = axis == Direction.Axis.X ? 0.5 + d * (double) direction.getOffsetX() : (double) random.nextFloat();
				double f = axis == Direction.Axis.Y ? 0.5 + d * (double) direction.getOffsetY() : (double) random.nextFloat();
				double g = axis == Direction.Axis.Z ? 0.5 + d * (double) direction.getOffsetZ() : (double) random.nextFloat();
				world.addParticle(StarflightParticleTypes.EYE, (double) pos.getX() + e, (double) pos.getY() + f, (double) pos.getZ() + g, 0.0, 0.0, 0.0);
			}
		}
	}
}