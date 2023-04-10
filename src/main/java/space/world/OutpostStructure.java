package space.world;

import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class OutpostStructure extends Structure
{
	public static final Codec<OutpostStructure> CODEC = OutpostStructure.createCodec(OutpostStructure::new);

	public OutpostStructure(Structure.Config config)
	{
		super(config);
	}
	
	@Override
	public StructureType<?> getType()
	{
		return StarflightWorldGeneration.OUTPOST_TYPE;
	}

	@Override
	public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context)
	{
		return OutpostStructure.getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, collector -> OutpostStructure.addPieces((StructurePiecesCollector) collector, context));
	}

	private static void addPieces(StructurePiecesCollector collector, Structure.Context context)
	{
		BlockPos blockPos = new BlockPos(context.chunkPos().getStartX(), 0, context.chunkPos().getStartZ());
		OutpostGenerator.addPieces(context, blockPos, collector);
	}
}