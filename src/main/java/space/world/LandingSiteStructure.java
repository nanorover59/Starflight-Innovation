package space.world;

import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class LandingSiteStructure extends Structure
{
	public static final Codec<LandingSiteStructure> CODEC = LandingSiteStructure.createCodec(LandingSiteStructure::new);

	public LandingSiteStructure(Structure.Config config)
	{
		super(config);
	}
	
	@Override
	public StructureType<?> getType()
	{
		return StarflightWorldGeneration.LANDING_SITE_TYPE;
	}

	@Override
	public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context)
	{
		return LandingSiteStructure.getStructurePosition(context, Heightmap.Type.OCEAN_FLOOR_WG, collector -> LandingSiteStructure.addPieces((StructurePiecesCollector) collector, context));
	}

	private static void addPieces(StructurePiecesCollector collector, Structure.Context context)
	{
		BlockPos blockPos = new BlockPos(context.chunkPos().getStartX(), 0, context.chunkPos().getStartZ());
		LandingSiteGenerator.addPieces(context, blockPos, collector);
	}
}