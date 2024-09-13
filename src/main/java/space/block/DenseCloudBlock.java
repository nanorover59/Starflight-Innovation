package space.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DenseCloudBlock extends Block
{
	public static final MapCodec<DenseCloudBlock> CODEC = DenseCloudBlock.createCodec(DenseCloudBlock::new);

	public MapCodec<DenseCloudBlock> getCodec()
	{
		return CODEC;
	}

	public DenseCloudBlock(AbstractBlock.Settings settings)
	{
		super(settings);
	}
	
	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return VoxelShapes.empty();
	}
	
	@Override
	protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return VoxelShapes.empty();
	}

	@Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type)
	{
        return true;
    }
	
	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity)
	{
		if(entity instanceof LivingEntity && entity.getEyePos().distanceTo(pos.toCenterPos()) < 0.6)
			((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 20, 0));
	}
}