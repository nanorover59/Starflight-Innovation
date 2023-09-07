package space.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import space.block.entity.LeakBlockEntity;
import space.particle.StarflightParticleTypes;

public class LeakBlock extends BlockWithEntity
{
	public LeakBlock(Settings settings)
	{
		super(settings);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.INVISIBLE;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return VoxelShapes.empty();
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
	{
		if(world.isClient)
			return;

		BlockEntity blockEntity = world.getBlockEntity(pos);

		if(blockEntity instanceof LeakBlockEntity)
		{

		}
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
	{
		double vx = 0.0;
		double vy = 0.0;
		double vz = 0.0;
		
		if(world.getBlockState(pos.east()).getBlock() == Blocks.AIR)
			vx += 1.0;
		
		if(world.getBlockState(pos.west()).getBlock() == Blocks.AIR)
			vx -= 1.0;
		
		if(world.getBlockState(pos.up()).getBlock() == Blocks.AIR)
			vy += 1.0;
		
		if(world.getBlockState(pos.down()).getBlock() == Blocks.AIR)
			vy -= 1.0;
		
		if(world.getBlockState(pos.south()).getBlock() == Blocks.AIR)
			vz += 1.0;
		
		if(world.getBlockState(pos.north()).getBlock() == Blocks.AIR)
			vz -= 1.0;
		
		double vf = 1.0 - random.nextDouble() * 0.1;
		int i = random.nextBetween(1, 4);
		
		for(int j = 0; j < i; j++)
		{
			double xOffset = 0.5 + random.nextDouble() - random.nextDouble();
			double yOffset = 0.5 + random.nextDouble() - random.nextDouble();
			double zOffset = 0.5 + random.nextDouble() - random.nextDouble();
			world.addParticle(StarflightParticleTypes.AIR_FILL, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, vx * vf, vy * vf, vz * vf);
		}
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new LeakBlockEntity(pos, state);
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : checkType(type, StarflightBlocks.LEAK_BLOCK_ENTITY, LeakBlockEntity::serverTick);
	}
}