package space.world;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
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
		BlockPos blockPos = context.getOrigin();
		StructureWorldAccess structureWorldAccess = context.getWorld();
		Random random = context.getRandom();
		BlockState blockState = structureWorldAccess.getBlockState(blockPos.down(6 + random.nextInt(8)));
		float chance = 0.1f;
		
		if(structureWorldAccess.getBiome(blockPos).isIn(StarflightWorldGeneration.MORE_SCATTER))
			chance = 0.8f;
		
		// Random chance of generating a stray basalt rock.
		if(random.nextInt(4) == 0)
			blockState = Blocks.SMOOTH_BASALT.getDefaultState();
		
		// Skip generating at random or if the block found is not stone related.
		if(random.nextFloat() > chance || !blockState.isIn(BlockTags.PICKAXE_MINEABLE))
			return true;
		
		while(blockPos.getY() > structureWorldAccess.getBottomY() + 3 && !(structureWorldAccess.getBlockState(blockPos.down()).isSideSolidFullSquare(structureWorldAccess, blockPos, Direction.UP)))
			blockPos = blockPos.down();
		
		if(blockPos.getY() <= structureWorldAccess.getBottomY() + 3)
			return false;
		
		int rockSize = 2;
		
		for(int i = 0; i < 8; i++)
		{
			if(random.nextInt(4) == 0)
				rockSize++;
			else
				break;
		}
		
		for(int i = 0; i < 3 + (rockSize / 2); i++)
		{
			int j = random.nextInt(rockSize);
			int k = random.nextInt(rockSize);
			int l = random.nextInt(rockSize);
			float f = (float) (j + k + l) * 0.333f + 0.5f;
			
			for(BlockPos blockPos2 : BlockPos.iterate(blockPos.add(-j, -k, -l), blockPos.add(j, k, l)))
			{
				if(!(blockPos2.getSquaredDistance(blockPos) <= (double) (f * f)))
					continue;
				
				structureWorldAccess.setBlockState(blockPos2, blockState, Block.NOTIFY_LISTENERS);
			}
			
			blockPos = blockPos.add(random.nextInt(rockSize) - random.nextInt(rockSize), -random.nextInt(rockSize) / 2, random.nextInt(rockSize) - random.nextInt(rockSize));
		}
		
		return true;
	}
}