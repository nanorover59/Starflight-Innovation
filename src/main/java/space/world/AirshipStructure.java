package space.world;

import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class AirshipStructure extends Structure
{
	public static final Codec<AirshipStructure> CODEC = AirshipStructure.createCodec(AirshipStructure::new);

	public AirshipStructure(Structure.Config config)
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
		return AirshipStructure.getStructurePosition(context, Heightmap.Type.MOTION_BLOCKING, collector -> AirshipStructure.addPieces((StructurePiecesCollector) collector, context));
	}

	private static void addPieces(StructurePiecesCollector collector, Structure.Context context)
	{
		BlockPos blockPos = new BlockPos(context.chunkPos().getStartX(), 64 + context.random().nextInt(64), context.chunkPos().getStartZ());
		AirshipGenerator.addPieces(context, blockPos, collector);
	}
}