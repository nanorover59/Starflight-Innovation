package space.world;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class LargeAeroplanktonStructure extends Structure
{
	public static final MapCodec<LargeAeroplanktonStructure> CODEC = LargeAeroplanktonStructure.createCodec(LargeAeroplanktonStructure::new);

	public LargeAeroplanktonStructure(Structure.Config config)
	{
		super(config);
	}
	
	@Override
	public StructureType<?> getType()
	{
		return StarflightWorldGeneration.LARGE_AEROPLANKTON_TYPE;
	}

	@Override
	public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context)
	{
		return LargeAeroplanktonStructure.getStructurePosition(context, Heightmap.Type.OCEAN_FLOOR_WG, collector -> LargeAeroplanktonStructure.addPieces((StructurePiecesCollector) collector, context));
	}

	private static void addPieces(StructurePiecesCollector collector, Structure.Context context)
	{
		BlockPos blockPos = new BlockPos(context.chunkPos().getStartX(), 0, context.chunkPos().getStartZ());
		LargeAeroplanktonGenerator.addPieces(context, blockPos, collector);
	}
}