package space.world;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import space.StarflightMod;

public class OutpostGenerator
{
	private static final Identifier TENT = new Identifier(StarflightMod.MOD_ID, "tent");
	
	public static void addPieces(Structure.Context context, BlockPos pos, StructurePiecesHolder holder)
	{
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		ChunkRandom random = context.random();
		NoiseConfig noiseConfig = context.noiseConfig();	
		int surfaceY = chunkGenerator.getHeightOnGround(pos.getX(), pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG, context.world(),noiseConfig);
		int radius = 32 + random.nextInt(32);
		BlockPos center = pos.add(0, surfaceY, 0);
		int chunkRadius = (((int) (radius * 2.0)) >> 4) + 2;
		int evenTerrain = checkEvenTerrain(context, pos);
		
		if(evenTerrain > 16)
			return;
		
		int componentCount = 1;
		Set<Pair<Integer, Integer>> xzList = new HashSet<Pair<Integer, Integer>>();
		
		while(random.nextInt(4) > 2 && componentCount < 16)
			componentCount++;
		
		for(int i = 0; i < componentCount; i++)
		{
			double r = random.nextInt(chunkRadius);
			double theta = random.nextDouble() * Math.PI * 2.0;
			int x = (int) (Math.cos(theta) * r);
			int z = (int) (Math.sin(theta) * r);
			Pair<Integer, Integer> xz = new Pair<Integer, Integer>(x, z);
			
			if(xzList.contains(xz))
				continue;
			
			BlockPos startPos = new BlockPos(pos.getX() + (x << 4), 0, pos.getZ() + (z << 4));
			holder.addPiece(new Piece(startPos, center.getX(), center.getZ(), radius, 0));
			xzList.add(new Pair<Integer, Integer>(x, z));
		}
	}
	
	private static int checkEvenTerrain(Structure.Context context, BlockPos pos)
	{
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		NoiseConfig noiseConfig = context.noiseConfig();
		int y1 = chunkGenerator.getHeightOnGround(pos.getX(), pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG, context.world(), noiseConfig);
		int y2 = chunkGenerator.getHeightOnGround(pos.getX() + 15, pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG, context.world(), noiseConfig);
		int y3 = chunkGenerator.getHeightOnGround(pos.getX(), pos.getZ() + 15, Heightmap.Type.WORLD_SURFACE_WG, context.world(), noiseConfig);
		int y4 = chunkGenerator.getHeightOnGround(pos.getX() + 15, pos.getZ() + 15, Heightmap.Type.WORLD_SURFACE_WG, context.world(), noiseConfig);
		int d1 = (int) Math.abs(y1 - y2);
		int d2 = (int) Math.abs(y1 - y3);
		int d3 = (int) Math.abs(y1 - y4);
		return (d1 + d2 + d3) / 3;
	}

	public static class Piece extends StructurePiece
	{
		private final int centerX;
		private final int centerZ;
		private final int radius;
		private final int type;
		
		public Piece(BlockPos start, int x, int z, int radius, int type)
		{
			super(StarflightWorldGeneration.OUTPOST_PIECE, 0, new BlockBox(start));
			this.centerX = x;
			this.centerZ = z;
			this.radius = radius;
			this.type = type;
		}

		public Piece(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.OUTPOST_PIECE, nbt);
			this.centerX  = nbt.getInt("x");
			this.centerZ = nbt.getInt("z");
			this.radius = nbt.getInt("radius");
			this.type = nbt.getInt("type");
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			nbt.putInt("x", this.centerX);
			nbt.putInt("z", this.centerZ);
			nbt.putInt("radius", radius);
			nbt.putInt("type", centerX);
		}
		
		@Override
		protected boolean canReplace(BlockState state)
		{
			return true;
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			BlockPos placementPosition = chunkPos.getBlockPos(0, 0, 0);
			int localSurfaceY = world.getTopPosition(Type.OCEAN_FLOOR_WG, placementPosition).getY();
			placementPosition = placementPosition.add(0, localSurfaceY, 0);
			
			for(int i = 0; i < radius / 2; i++)
			{
				world.setBlockState(placementPosition.add(0, i, 0), Blocks.GLOWSTONE.getDefaultState(), Block.REDRAW_ON_MAIN_THREAD);
			}
			
			StructureTemplateManager templateManager = world.toServerWorld().getStructureTemplateManager();
			StructureTemplate template = templateManager.getTemplate(TENT).get();
			StructurePlacementData placementdata = new StructurePlacementData().setRotation(BlockRotation.random(random));
			template.place(world, placementPosition, placementPosition, placementdata, random, Block.NOTIFY_LISTENERS);
			//postPlacement(serverWorld, placementPosition, placementPosition.add(size));
			
			
		}
	}
}