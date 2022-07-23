package space.world;

import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class CraterStructure extends Structure
{
	public static final Codec<CraterStructure> CODEC = CraterStructure.createCodec(CraterStructure::new);

	public CraterStructure(Structure.Config config)
	{
		super(config);
	}

	@Override
	public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context)
	{
		return CraterStructure.getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, collector -> CraterStructure.addPieces((StructurePiecesCollector) collector, context));
	}

	@Override
	public StructureType<?> getType()
	{
		return StarflightWorldGeneration.CRATER_TYPE;
	}

	private static void addPieces(StructurePiecesCollector collector, Structure.Context context)
	{
		BlockPos blockPos = new BlockPos(context.chunkPos().getStartX(), 0, context.chunkPos().getStartZ());
		CraterGenerator.addPieces(context, blockPos, collector);
	}
}
