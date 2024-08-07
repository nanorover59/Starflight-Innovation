package space.world;

import java.util.function.Function;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.MultiNoiseSampler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.CarverContext;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.chunk.AquiferSampler;

public class CraterCarver extends Carver<CraterCarverConfig>
{
	public CraterCarver(Codec<CraterCarverConfig> codec)
	{
        super(codec);
    }

	@Override
	public boolean shouldCarve(CraterCarverConfig craterCarverConfig, Random random)
	{
		return random.nextFloat() <= craterCarverConfig.probability;
	}

	@Override
	public boolean carve(CarverContext carverContext, CraterCarverConfig craterCarverConfig, Chunk chunk, Function<BlockPos, RegistryEntry<Biome>> posToBiome, Random random, AquiferSampler aquiferSampler, ChunkPos chunkPos, CarvingMask carvingMask)
	{
		MultiNoiseSampler sampler = carverContext.getNoiseConfig().getMultiNoiseSampler();
        long noise1 = sampler.sample(chunkPos.getStartX(), 0, chunkPos.getStartZ()).continentalnessNoise();
        long noise2 = sampler.sample(chunkPos.getStartX(), 0, chunkPos.getStartZ()).erosionNoise();
        Random craterRandom = Random.create(combineSeeds(noise1, noise2, chunkPos.x, chunkPos.z));
        
        if(craterRandom.nextFloat() <= craterCarverConfig.probability)
        {
			int craterX = chunkPos.getOffsetX(craterRandom.nextInt(16));
			int craterZ = chunkPos.getOffsetZ(craterRandom.nextInt(16));
			int radius = (int) powerLaw(craterRandom, 2.5f, 4.0f, 64.0f);
			double depthFactor = 0.5 + craterRandom.nextDouble() * 0.25;
			double rimWidth = 0.5 + craterRandom.nextDouble() * 0.15;
			double rimSteepness = 0.25 + craterRandom.nextDouble() * 0.25;
			BlockPos.Mutable mutable = new BlockPos.Mutable();
			RegistryEntry<Biome> biome = posToBiome.apply(new BlockPos(craterX, 64, craterZ));
			boolean ice = biome == null ? false : biome.isIn(StarflightWorldGeneration.ICE_CRATERS);
			
			for(int x = chunk.getPos().getStartX(); x <= chunk.getPos().getEndX(); x++)
			{
				for(int z = chunk.getPos().getStartZ(); z <= chunk.getPos().getEndZ(); z++)
				{
					double r = MathHelper.hypot(x - craterX, z - craterZ);
					
					if(r < radius + 8)
					{
						r /= radius;
						int surfaceY = chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, x, z);
						int depth = getCraterDepth(x, z, craterX, craterZ, radius, rimWidth, rimSteepness, depthFactor);
						BlockState surfaceState = chunk.getBlockState(new BlockPos(x, surfaceY, z));
						
						if(depth < 0)
						{
							for(int y = surfaceY + 4; y >= surfaceY + depth; y--)
							{
								BlockState blockState = AIR;
								mutable.set(x, y, z);
								
								if(ice)
								{
									if(y < surfaceY - 9)
										blockState = craterRandom.nextInt(16) == 0 ? Blocks.BLUE_ICE.getDefaultState() : Blocks.PACKED_ICE.getDefaultState();
									else if(y < surfaceY - 8)
										blockState = Blocks.POWDER_SNOW.getDefaultState();
								}
								
								chunk.setBlockState(mutable, blockState, false);
								chunk.markBlockForPostProcessing(mutable);
								
								if(y == surfaceY + depth)
									chunk.setBlockState(mutable.down(), surfaceState, false);
							}
						}
						else
						{
							for(int y = surfaceY; y <= surfaceY + depth; y++)
							{
								mutable.set(x, y, z);
								chunk.setBlockState(mutable, surfaceState, false);
							}
						}
					}
				}
			}
        }
		
		return true;
	}
	
	@Override
	protected boolean canAlwaysCarveBlock(CraterCarverConfig config, BlockState state)
	{
        return true;
    }
	
	private long combineSeeds(long ... seeds)
	{
		long mix = 0;
		
		for(int i = 0; i < seeds.length; i++)
		{
			long seed = seeds[i];
			seed ^= (seed << 21);
			seed ^= (seed >>> 35);
			seed ^= (seed << 4);
			mix += seed;
		}
		
		mix ^= (mix << 21);
		mix ^= (mix >>> 35);
		mix ^= (mix << 4);
		return mix;
	}
	
	private float powerLaw(Random random, float alpha, float min, float max)
	{
		float u = random.nextFloat();
		return (float) (Math.pow((Math.pow(max, 1.0f - alpha) - Math.pow(min, 1.0f - alpha)) * u + Math.pow(min, 1.0f - alpha), 1.0f / (1.0f - alpha)));
	}
	
	private int getCraterDepth(int x, int z, int craterX, int craterZ, int radius, double rimWidth, double rimSteepness, double depthFactor)
	{
		double r = MathHelper.hypot(x - craterX, z - craterZ) / radius;
		double parabola = r * r - 1.0;
		double rimR = Math.min(r - rimWidth - 1.0, 0.0);
		double rim = rimR * rimR * rimSteepness;
		double shape = smoothMin(parabola, rim, 0.5);
		shape = smoothMax(shape, -depthFactor, 0.5);
		return (int) (shape * radius);
	}
	
	private double smoothMin(double a, double b, double c)
	{
		double h = Math.max(c - Math.abs(a - b), 0.0) / c;
		return Math.min(a, b) - h * h * c * 0.25;
	}
	
	private double smoothMax(double a, double b, double c)
	{
		double h = Math.max(c - Math.abs(a - b), 0.0) / c;
		return Math.max(a, b) + h * h * c * 0.25;
	}
}