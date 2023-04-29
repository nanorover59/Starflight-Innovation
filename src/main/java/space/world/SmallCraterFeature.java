package space.world;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SmallCraterFeature extends Feature<DefaultFeatureConfig>
{
	public SmallCraterFeature(Codec<DefaultFeatureConfig> configCodec)
	{
		super(configCodec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context)
	{
		StructureWorldAccess world = context.getWorld();
		Random random = context.getRandom();
		RegistryEntry<Biome> biome = world.getBiome(context.getOrigin());
		float chance = 0.0f;
		
		if(biome.isIn(StarflightWorldGeneration.LIGHT_CRATERING))
			chance = 0.005f;
		else if(biome.isIn(StarflightWorldGeneration.MEDIUM_CRATERING))
			chance = 0.05f;
		else if(biome.isIn(StarflightWorldGeneration.HEAVY_CRATERING))
			chance = 0.15f;
		
		if(random.nextFloat() > chance)
			return true;
		
		int radius = 3 + random.nextInt(5);
		int gap = 7 - radius;
		double depthFactor = 0.3 + random.nextDouble() * 0.5;
		double rimWidth = 0.5 + random.nextDouble() * 0.15;
		double rimSteepness = 0.25 + random.nextDouble() * 0.25;
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		ChunkPos chunkPos = new ChunkPos(context.getOrigin());
		BlockPos startPos = chunkPos.getStartPos().add(7 + (gap > 0 ? random.nextInt(gap) - random.nextInt(gap) : 0), 0, 7 + (gap > 0 ? random.nextInt(gap) - random.nextInt(gap) : 0));
		startPos = startPos.up(world.getTopPosition(Type.OCEAN_FLOOR, startPos).getY());
		
		for(int x = -16; x < 16; x++)
		{
			for(int z = -16; z < 16; z++)
			{
				mutable.set(startPos.getX() + x, 0, startPos.getZ() + z);
				int localSurfaceY = world.getTopPosition(Type.OCEAN_FLOOR, mutable).getY() - 1;
				mutable.setY(localSurfaceY);
				BlockState surfaceState = world.getBlockState(mutable);	
				double r = MathHelper.hypot(mutable.getX() - startPos.getX(), mutable.getZ() - startPos.getZ()) / radius;
				double parabola = r * r - 1.0;
				double rimR = Math.min(r - rimWidth - 1.0, 0.0);
				double rim = rimR * rimR * rimSteepness;
				double shape = Math.min(parabola, rim);
				shape = Math.max(shape, -depthFactor);
				int y = localSurfaceY + (int) (shape * radius);
				
				if(y < localSurfaceY)
				{
					for(int i = y; i <= localSurfaceY; i++)
					{
						mutable.setY(i);
						world.setBlockState(mutable, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
					}
					
					mutable.setY(y - 1);
					
					if(world.getBlockState(mutable).getMaterial().blocksMovement())
						world.setBlockState(mutable, surfaceState, Block.NOTIFY_LISTENERS);
				}
				else
				{
					for(int i = y; i > localSurfaceY; i--)
					{
						mutable.setY(i);
						world.setBlockState(mutable, surfaceState, Block.NOTIFY_LISTENERS);
					}
				}
			}
		}
			
		return true;
	}
}