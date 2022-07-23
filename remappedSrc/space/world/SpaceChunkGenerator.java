package space.world;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.MultiNoiseSampler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep.Carver;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;

public class SpaceChunkGenerator extends ChunkGenerator
{
	public static final Codec<SpaceChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> SpaceChunkGenerator.method_41042(instance).and(RegistryOps.createRegistryCodec(Registry.BIOME_KEY).forGetter(spaceChunkGenerator -> spaceChunkGenerator.biomeRegistry)).apply(instance, instance.stable(SpaceChunkGenerator::new)));
	public final Registry<Biome> biomeRegistry;
	
	public SpaceChunkGenerator(Registry<StructureSet> structureRegistry, Registry<Biome> biomeRegistry)
	{
        super(structureRegistry, Optional.empty(), new FixedBiomeSource(biomeRegistry.getOrCreateEntry(StarflightBiomes.SPACE)));
        this.biomeRegistry = biomeRegistry;
    }

	@Override
	protected Codec<? extends ChunkGenerator> getCodec()
	{
		return CODEC;
	}

	@Override
	public ChunkGenerator withSeed(long seed)
	{
		return this;
	}

	@Override
	public void buildSurface(ChunkRegion chunkRegion, StructureAccessor structureAccessor, Chunk chunk)
	{
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, StructureAccessor structureAccessor, Chunk chunk)
	{
		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public int getHeight(int x, int z, Type heightmap, HeightLimitView world)
	{
		return 0;
	}

	@Override
	public MultiNoiseSampler getMultiNoiseSampler()
	{
		return null;
	}

	@Override
	public void carve(ChunkRegion var1, long var2, BiomeAccess var4, StructureAccessor var5, Chunk var6, Carver var7)
	{
	}

	@Override
	public void populateEntities(ChunkRegion var1)
	{
	}

	@Override
	public int getWorldHeight()
	{
		return 0;
	}

	@Override
	public int getSeaLevel()
	{
		return 0;
	}

	@Override
	public int getMinimumY()
	{
		return 0;
	}

	@Override
	public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world)
	{
		return new VerticalBlockSample(0, new BlockState[0]);
	}

	@Override
	public void getDebugHudText(List<String> var1, BlockPos var2)
	{
		
	}
}