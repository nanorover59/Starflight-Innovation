package space.world;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import space.world.MoonshaftGenerator.MoonshaftRoom;

public class MoonshaftStructure extends Structure
{
	public static final Codec<MoonshaftStructure> CODEC = MoonshaftStructure.createCodec(MoonshaftStructure::new);

	public MoonshaftStructure(Config config)
	{
		super(config);
	}
	
	@Override
	public StructureType<?> getType()
	{
		return StarflightWorldGeneration.MOONSHAFT_TYPE;
	}

	@Override
	public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context)
	{
		context.random().nextDouble();
		ChunkPos chunkPos = context.chunkPos();
		BlockPos blockPos = new BlockPos(chunkPos.getCenterX(), 50, chunkPos.getStartZ());
		StructurePiecesCollector structurePiecesCollector = new StructurePiecesCollector();
		int i = this.addPieces(structurePiecesCollector, context);
		return Optional.of(new Structure.StructurePosition(blockPos.add(0, i, 0), Either.right(structurePiecesCollector)));
	}

	private int addPieces(StructurePiecesCollector collector, Structure.Context context)
	{
		ChunkPos chunkPos = context.chunkPos();
		ChunkRandom chunkRandom = context.random();
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		MoonshaftRoom moonshaftRoom = new MoonshaftGenerator.MoonshaftRoom(0, chunkRandom, chunkPos.getOffsetX(2), chunkPos.getOffsetZ(2));
		collector.addPiece(moonshaftRoom);
		moonshaftRoom.fillOpenings(moonshaftRoom, collector, chunkRandom);
		int topY = 40;
		int baseY = chunkGenerator.getMinimumY();
		BlockBox blockBox = collector.getBoundingBox();
        int i = topY - baseY + 1 - blockBox.getBlockCountY();
        int j = i > 1 ? baseY + chunkRandom.nextInt(i) : baseY;
        int k = j - blockBox.getMinY();
        collector.shift(k);
        return k;
	}
}