package space.world;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.CarverContext;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.structure.Structure;
import space.block.StarflightBlocks;

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
		int craterX = chunkPos.getOffsetX(random.nextInt(16));
		int craterZ = chunkPos.getOffsetZ(random.nextInt(16));
		int radius = (int) powerLaw(random, 2.5f, 6.0f, 32.0f);
		double depthFactor = 0.6 + random.nextDouble() * 0.2;
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		Registry<Structure> structureRegistry = carverContext.getRegistryManager().get(RegistryKeys.STRUCTURE);
		Map<Structure, LongSet> structureReferences = chunk.getStructureReferences();
		for(Structure structure : structureReferences.keySet())
		{
			if(structureRegistry.getEntry(structure).isIn(StarflightWorldGeneration.NO_CRATERS))
				return false;
		}
		
		boolean hasIce = posToBiome.apply(new BlockPos(craterX, 64, craterZ)).isIn(StarflightWorldGeneration.ICE_CRATERS);
		
		for(int i = 0; i < 16; i++)
		{
			for(int j = 0; j < 16; j++)
			{
				int x = chunk.getPos().getOffsetX(i);
				int z = chunk.getPos().getOffsetZ(j);
				double r = MathHelper.squaredHypot(x - craterX, z - craterZ);

				if(r < radius * radius)
				{
					r /= radius * radius;
					int targetDepth = (int) (smoothMin(1.0 - r, depthFactor, 0.5) * radius);
					int depth = 0;
					boolean captureSurface = true;
					ArrayList<BlockState> surface = new ArrayList<BlockState>();
					mutable.set(x, 256, z);
					
					while(depth < targetDepth && mutable.getY() > chunk.getBottomY())
					{
						mutable.move(Direction.DOWN);
						
						if(chunk.getBlockState(mutable).blocksMovement() && !carvingMask.get(x, mutable.getY(), z))
						{
							if(chunk.getBlockState(mutable).isIn(StarflightBlocks.WORLD_STONE_BLOCK_TAG))
								captureSurface = false;
							else if(captureSurface)
								surface.add(chunk.getBlockState(mutable));
							
							BlockState state = AIR;
							
							if(hasIce)
							{
								if(depth == 4)
									state = Blocks.SNOW_BLOCK.getDefaultState();
								else if(depth > 4)
									state = random.nextInt(4) == 0 ? Blocks.BLUE_ICE.getDefaultState() : Blocks.PACKED_ICE.getDefaultState();
							}
							
							chunk.setBlockState(mutable, state, false);
							carvingMask.set(x, mutable.getY(), z);
							depth++;
						}
					}
					
					for(BlockState surfaceState : surface)
					{
						mutable.move(Direction.DOWN);
						chunk.setBlockState(mutable, surfaceState, false);
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
	
	private float powerLaw(Random random, float alpha, float min, float max)
	{
		float u = random.nextFloat();
		return (float) (Math.pow((Math.pow(max, 1.0f - alpha) - Math.pow(min, 1.0f - alpha)) * u + Math.pow(min, 1.0f - alpha), 1.0f / (1.0f - alpha)));
	}
	
	private double smoothMin(double a, double b, double c)
	{
		double h = Math.max(c - Math.abs(a - b), 0.0) / c;
		return Math.min(a, b) - h * h * c * 0.25;
	}
}