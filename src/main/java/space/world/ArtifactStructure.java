package space.world;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class ArtifactStructure extends Structure
{
	public static final MapCodec<ArtifactStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(configCodecBuilder(instance), Codec.INT.fieldOf("artifact_type").forGetter(artifactStructure -> artifactStructure.type)).apply(instance, ArtifactStructure::new));
	public final int type;

	public ArtifactStructure(Structure.Config config, int type)
	{
		super(config);
		this.type = type;
	}
	
	@Override
	public StructureType<?> getType()
	{
		return StarflightWorldGeneration.ARTIFACT_TYPE;
	}

	@Override
	public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context)
	{
		Heightmap.Type type = Heightmap.Type.OCEAN_FLOOR_WG;
		return getStructurePosition(context, type, collector -> this.addPieces(collector, context));
	}

	private void addPieces(StructurePiecesCollector collector, Structure.Context context)
	{
		BlockRotation blockRotation = BlockRotation.random(context.random());
		BlockPos blockPos = new BlockPos(context.chunkPos().getStartX(), 90, context.chunkPos().getStartZ());
		ArtifactGenerator.Piece piece = ArtifactGenerator.addParts(context.structureTemplateManager(), blockPos, blockRotation, collector, context.random(), type);
		
		if(piece.isTooLargeForNormalGeneration())
		{
			BlockBox blockBox = piece.getBoundingBox();	
			piece.setY(Structure.getAverageCornerHeights(context, blockBox.getMinX(), blockBox.getBlockCountX(), blockBox.getMinZ(), blockBox.getBlockCountZ()));
		}
	}
}