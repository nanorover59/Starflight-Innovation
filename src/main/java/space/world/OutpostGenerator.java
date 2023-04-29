package space.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
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
	private static final Identifier[] COMPONENTS = {
		new Identifier(StarflightMod.MOD_ID, "tent"),
		new Identifier(StarflightMod.MOD_ID, "tank_farm"),
		new Identifier(StarflightMod.MOD_ID, "oxygen_tank"),
		new Identifier(StarflightMod.MOD_ID, "rocket_2"),
		new Identifier(StarflightMod.MOD_ID, "rocket_3")
	};
	
	public static void addPieces(Structure.Context context, BlockPos pos, StructurePiecesHolder holder)
	{
		/*ChunkRandom random = context.random();
		int radius = 48;
		BlockPos center = pos;
		int chunkRadius = (((int) radius) >> 4);
		
		for(int x = -chunkRadius; x < chunkRadius; x += 2)
		{
			for(int z = -chunkRadius; z < chunkRadius; z += 2)
			{
				int type = random.nextInt(5);
				BlockPos startPos = new BlockPos(pos.getX() + (x << 4), 0, pos.getZ() + (z << 4));
				
				if(MathHelper.hypot(x, z) <= chunkRadius && checkEvenTerrain(context, startPos) < 8)
					holder.addPiece(new Piece(context, startPos, center.getX(), center.getZ(), radius, type));
			}
		}*/
	}
	
	private static int checkEvenTerrain(Structure.Context context, BlockPos pos)
	{
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		NoiseConfig noiseConfig = context.noiseConfig();
		int y1 = chunkGenerator.getHeightOnGround(pos.getX(), pos.getZ(), Heightmap.Type.WORLD_SURFACE, context.world(), noiseConfig);
		int y2 = chunkGenerator.getHeightOnGround(pos.getX() + 15, pos.getZ(), Heightmap.Type.WORLD_SURFACE, context.world(), noiseConfig);
		int y3 = chunkGenerator.getHeightOnGround(pos.getX(), pos.getZ() + 15, Heightmap.Type.WORLD_SURFACE, context.world(), noiseConfig);
		int y4 = chunkGenerator.getHeightOnGround(pos.getX() + 15, pos.getZ() + 15, Heightmap.Type.WORLD_SURFACE, context.world(), noiseConfig);
		int d1 = (int) Math.abs(y1 - y2);
		int d2 = (int) Math.abs(y1 - y3);
		int d3 = (int) Math.abs(y1 - y4);
		return (d1 + d2 + d3) / 3;
	}

	public static class Piece extends StructurePiece
	{
		private final StructureTemplateManager templateManager;
		private final int centerX;
		private final int centerZ;
		private final int radius;
		private final int type;
		
		public Piece(Structure.Context context, BlockPos start, int x, int z, int radius, int type)
		{
			super(StarflightWorldGeneration.OUTPOST_PIECE, 0, new BlockBox(start));
			this.templateManager = context.structureTemplateManager();
			this.centerX = x;
			this.centerZ = z;
			this.radius = radius;
			this.type = type;
		}

		public Piece(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.OUTPOST_PIECE, nbt);
			this.templateManager = context.structureTemplateManager();
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
			nbt.putInt("type", type);
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
			int localSurfaceY = world.getTopPosition(Type.WORLD_SURFACE, placementPosition).getY();
			placementPosition = placementPosition.add(0, localSurfaceY, 0);
			
			/*for(int i = 0; i < radius / 2; i++)
			{
				world.setBlockState(placementPosition.add(0, i, 0), Blocks.GLOWSTONE.getDefaultState(), Block.REDRAW_ON_MAIN_THREAD);
			}*/
			
			StructureTemplate template = templateManager.getTemplate(COMPONENTS[type]).get();
			StructurePlacementData placementdata = new StructurePlacementData().setRotation(BlockRotation.random(random));
			template.place(world, placementPosition, placementPosition.add(7, 0, 7), placementdata, random, Block.NOTIFY_LISTENERS);
			//postPlacement(serverWorld, placementPosition, placementPosition.add(size));
		}
	}
}