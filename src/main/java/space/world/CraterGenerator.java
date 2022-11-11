package space.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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

public class CraterGenerator
{
	public static void addPieces(Structure.Context context, BlockPos pos, StructurePiecesHolder holder)
	{
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		ChunkRandom random = context.random();
		NoiseConfig noiseConfig = context.noiseConfig();
		int surfaceY = chunkGenerator.getHeightOnGround(pos.getX(), pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG, context.world(),noiseConfig);
		int depth = 8 + random.nextInt(24);
		BlockPos center = pos.add(random.nextInt(16), surfaceY - depth, random.nextInt(16));
		float widthFactor = (depth / 3) + random.nextFloat();
		boolean followTerrain = random.nextInt(3) == 0;
		int chunkRadius = (((int) Math.ceil(widthFactor * Math.sqrt(depth) * 1.5)) >> 4) + 2;
		
		if(!checkEvenTerrain(context, pos))
		{
			if(random.nextBoolean())
				followTerrain = true;
			else
				return;
		}
		
		for(int x = -chunkRadius; x <= chunkRadius; x++)
		{
			for(int z = -chunkRadius; z <= chunkRadius; z++)
			{
				if(MathHelper.hypot(x, z) <= chunkRadius)
				{
					BlockPos startPos = new BlockPos(pos.getX() + (x << 4), 0, pos.getZ() + (z << 4));
					holder.addPiece(new Piece(startPos, center.getX(), center.getZ(), 0, depth, surfaceY, false));
				}
			}
		}
	}
	
	private static boolean checkEvenTerrain(Structure.Context context, BlockPos pos)
	{
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		NoiseConfig noiseConfig = context.noiseConfig();
		int y1 = chunkGenerator.getHeightOnGround(pos.getX(), pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG, context.world(), noiseConfig);
		int y2 = chunkGenerator.getHeightOnGround(pos.getX() + 15, pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG, context.world(), noiseConfig);
		int y3 = chunkGenerator.getHeightOnGround(pos.getX(), pos.getZ() + 15, Heightmap.Type.WORLD_SURFACE_WG, context.world(), noiseConfig);
		int y4 = chunkGenerator.getHeightOnGround(pos.getX() + 15, pos.getZ() + 15, Heightmap.Type.WORLD_SURFACE_WG, context.world(), noiseConfig);
		double average = (y1 + y2 + y3 + y4) / 4;
		int d1 = (int) Math.abs(average - y1);
		int d2 = (int) Math.abs(average - y2);
		int d3 = (int) Math.abs(average - y3);
		int d4 = (int) Math.abs(average - y4);
		return d1 < 3 && d2 < 3 && d3 < 3 && d4 < 3;
	}

	public static class Piece extends StructurePiece
	{
		private final int centerX;
		private final int centerZ;
		private final int radius;
		private final int depth;
		private final int surfaceY;
		private boolean followTerrain;
		
		public Piece(BlockPos start, int x, int z, int radius, int depth, int surfaceY, boolean folowTerrain)
		{
			super(StarflightWorldGeneration.CRATER_PIECE, 0, new BlockBox(start));
			this.centerX = x;
			this.centerZ = z;
			this.radius = radius;
			this.depth = depth;
			this.surfaceY = surfaceY;
			this.followTerrain = folowTerrain;
		}

		public Piece(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.CRATER_PIECE, nbt);
			this.centerX  = nbt.getInt("x");
			this.centerZ = nbt.getInt("z");
			this.radius = nbt.getInt("radius");
			this.depth = nbt.getInt("depth");
			this.surfaceY = nbt.getInt("surfaceY");
			this.followTerrain = nbt.getBoolean("followTerrain");
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			nbt.putInt("x", this.centerX);
			nbt.putInt("z", this.centerZ);
			nbt.putInt("radius", radius);
			nbt.putInt("depth", depth);
			nbt.putInt("surfaceY", radius);
			nbt.putBoolean("followTerrain", followTerrain);
		}
		
		@Override
		protected boolean canReplace(BlockState state)
		{
			return true;
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			final BlockPos.Mutable mutable = new BlockPos.Mutable();
			BlockPos startPos = chunkPos.getBlockPos(0, surfaceY, 0);

			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					mutable.set(startPos.getX() + x, 0, startPos.getZ() + z);
					int localSurfaceY = world.getTopPosition(Type.OCEAN_FLOOR_WG, mutable).down().getY();
					mutable.setY(localSurfaceY);
					BlockState surfaceState = world.getBlockState(mutable);
					
					if(!followTerrain)
						localSurfaceY = surfaceY;
					
					double ds = MathHelper.hypot(mutable.getX() - centerX, mutable.getZ() - centerZ);
					
					if(ds < 8.0)
						ds = 8.0;
					
					int offset = (int) (Math.pow(depth / 2.0 - ds, 2.0) / depth);
					int bottomY = localSurfaceY - depth + offset;
					int y = bottomY;
					mutable.setY(y);
					
					while(y < localSurfaceY || !world.getBlockState(mutable).isAir())
					{
						if(y == bottomY + 1 && world.getBlockState(mutable.down()).getMaterial().blocksMovement())
							world.setBlockState(mutable.down(), surfaceState, Block.NOTIFY_LISTENERS);

						world.setBlockState(mutable, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
						mutable.setY(++y);
					}
				}
			} 
		}
	}
}