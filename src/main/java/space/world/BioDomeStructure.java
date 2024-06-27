package space.world;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class BioDomeStructure extends Structure
{
	public static final MapCodec<BioDomeStructure> CODEC = BioDomeStructure.createCodec(BioDomeStructure::new);

	public BioDomeStructure(Structure.Config config)
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
		return BioDomeStructure.getStructurePosition(context, Heightmap.Type.WORLD_SURFACE, collector -> BioDomeStructure.addPieces((StructurePiecesCollector) collector, context));
	}

	private static void addPieces(StructurePiecesCollector collector, Structure.Context context)
	{
		BlockPos blockPos = new BlockPos(context.chunkPos().getStartX(), 0, context.chunkPos().getStartZ());
		BioDomeGenerator.addPieces(context, blockPos, collector);
	}
}