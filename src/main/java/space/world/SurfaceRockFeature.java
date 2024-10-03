package space.world;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SurfaceRockFeature extends Feature<DefaultFeatureConfig>
{
	public SurfaceRockFeature(Codec<DefaultFeatureConfig> configCodec)
	{
		super(configCodec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context)
	{
		StructureWorldAccess structureWorldAccess = context.getWorld();
		Random random = context.getRandom();
		BlockPos blockPos = structureWorldAccess.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, context.getOrigin());
		Mutable mutable = blockPos.down(2 + random.nextInt(16)).mutableCopy();
		BlockState rockState = null;
		
		while(rockState == null && mutable.getY() > structureWorldAccess.getBottomY())
		{
			BlockState blockState = structureWorldAccess.getBlockState(mutable);
			
			if(blockState.isOpaque() && blockState.isIn(BlockTags.PICKAXE_MINEABLE))
				rockState = blockState;
			
			mutable.setY(mutable.getY() - 1);
		}
		
		if(rockState == null || structureWorldAccess.getBlockState(blockPos.up()).isOpaque())
			return true;
		
		int rockSize = (int) powerLaw(random, 2.5f, 1.0f, 5.0f);
		int u = Math.min(rockSize, 4);
		
		for(int i = 0; i < rockSize; i++)
		{
			int j = random.nextInt(u);
			int k = random.nextInt(u);
			int l = random.nextInt(u);
			float f = (float) (j + k + l) * 0.333f;
			
			for(BlockPos blockPos2 : BlockPos.iterate(blockPos.add(-j, -k, -l), blockPos.add(j, k, l)))
			{
				if(blockPos2.getSquaredDistance(blockPos) > (double) (f * f))
					continue;
				
				structureWorldAccess.setBlockState(blockPos2, rockState, Block.NOTIFY_ALL);
			}
			
			blockPos = blockPos.add(-u + random.nextInt(u) + 1, -u + random.nextInt(u) + 1, -u + random.nextInt(u) + 1);
		}
		
		return true;
	}
	
	private float powerLaw(Random random, float alpha, float min, float max)
	{
		float u = random.nextFloat();
		return (float) (Math.pow((Math.pow(max, 1.0f - alpha) - Math.pow(min, 1.0f - alpha)) * u + Math.pow(min, 1.0f - alpha), 1.0f / (1.0f - alpha)));
	}
}