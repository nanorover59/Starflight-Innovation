package space.world;

import com.mojang.serialization.Codec;

import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructurePiecesGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class CraterFeature extends StructureFeature<DefaultFeatureConfig>
{
	public CraterFeature(Codec<DefaultFeatureConfig> codec)
	{
	    super(codec, StructureGeneratorFactory.simple(StructureGeneratorFactory.checkForBiomeOnTop(Heightmap.Type.WORLD_SURFACE_WG), CraterFeature::addPieces));
	}
	
	@Override
	public GenerationStep.Feature getGenerationStep()
	{
        return GenerationStep.Feature.SURFACE_STRUCTURES;
    }
	
	private static void addPieces(StructurePiecesCollector collector, StructurePiecesGenerator.Context<DefaultFeatureConfig> context)
	{
        BlockPos blockPos = new BlockPos(context.chunkPos().getStartX(), 0, context.chunkPos().getStartZ());
        CraterGenerator.addPieces(context, blockPos, collector);
    }
}
