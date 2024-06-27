package space.world;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;

import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import space.world.MoonshaftGenerator.MoonshaftCore;

public class MoonshaftStructure extends Structure
{
	public static final MapCodec<MoonshaftStructure> CODEC = MoonshaftStructure.createCodec(MoonshaftStructure::new);

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
		ChunkPos chunkPos = context.chunkPos();
        StructurePiecesCollector structurePiecesCollector = new StructurePiecesCollector();
        int y = this.addPieces(structurePiecesCollector, context);
        return Optional.of(new Structure.StructurePosition(new BlockPos(chunkPos.getCenterX(), y, chunkPos.getCenterZ()), Either.right(structurePiecesCollector)));
	}

	private int addPieces(StructurePiecesCollector collector, Structure.Context context)
	{
		ChunkPos chunkPos = context.chunkPos();
		ChunkRandom chunkRandom = context.random();
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		int x = chunkPos.getStartX();
		int z = chunkPos.getStartZ();
		int y = chunkGenerator.getHeight(x, z, Heightmap.Type.OCEAN_FLOOR, context.world(), context.noiseConfig());
		int maxLevels = (y - chunkGenerator.getMinimumY()) / 12;
		int depth = 24 + chunkRandom.nextInt(maxLevels) * 6;
		BlockBox blockBox = MoonshaftCore.getBoundingBox(collector, chunkRandom, x, y, z, depth);
		MoonshaftCore moonshaftPart = new MoonshaftCore(0, Direction.fromHorizontal(chunkRandom.nextInt(4)), blockBox);
		collector.addPiece(moonshaftPart);
		moonshaftPart.fillOpenings(moonshaftPart, collector, chunkRandom);
		return y;
	}
}