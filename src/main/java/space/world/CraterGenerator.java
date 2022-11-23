package space.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesHolder;
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

public class CraterGenerator
{
	public static void addPieces(Structure.Context context, BlockPos pos, StructurePiecesHolder holder)
	{
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		ChunkRandom random = context.random();
		NoiseConfig noiseConfig = context.noiseConfig();
		int surfaceY = chunkGenerator.getHeightOnGround(pos.getX(), pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG, context.world(),noiseConfig);
		int depth = 8 + random.nextInt(12);
		
		if(random.nextDouble() < 0.25)
			depth += random.nextInt(12);
		
		BlockPos center = pos.add(random.nextInt(16), surfaceY - depth, random.nextInt(16));
		int chunkRadius = (((int) (depth * 2.0)) >> 4) + 2;
		int evenTerrain = checkEvenTerrain(context, pos);
		
		if(surfaceY < 32 || evenTerrain > 16)
			return;
		
		for(int x = -chunkRadius; x <= chunkRadius; x++)
		{
			for(int z = -chunkRadius; z <= chunkRadius; z++)
			{
				if(MathHelper.hypot(x, z) <= chunkRadius)
				{
					BlockPos startPos = new BlockPos(pos.getX() + (x << 4), 0, pos.getZ() + (z << 4));
					holder.addPiece(new Piece(startPos, center.getX(), center.getZ(), 0, depth, surfaceY));
				}
			}
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
		private final int depth;
		private final int surfaceY;
		
		public Piece(BlockPos start, int x, int z, int radius, int depth, int surfaceY)
		{
			super(StarflightWorldGeneration.CRATER_PIECE, 0, new BlockBox(start));
			this.centerX = x;
			this.centerZ = z;
			this.depth = depth;
			this.surfaceY = surfaceY;
		}

		public Piece(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.CRATER_PIECE, nbt);
			this.centerX  = nbt.getInt("x");
			this.centerZ = nbt.getInt("z");
			this.depth = nbt.getInt("depth");
			this.surfaceY = nbt.getInt("surfaceY");
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			nbt.putInt("x", this.centerX);
			nbt.putInt("z", this.centerZ);
			nbt.putInt("depth", depth);
			nbt.putInt("surfaceY", surfaceY);
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
					int localSurfaceY = world.getTopPosition(Type.OCEAN_FLOOR_WG, mutable).getY();
					mutable.setY(localSurfaceY);
					BlockState surfaceState = world.getBlockState(mutable);
					BlockState subSurfaceState = surfaceState;
					int subSurfaceOffset = 0;
					
					while(subSurfaceState.getBlock() == surfaceState.getBlock())
					{
						subSurfaceOffset++;
						subSurfaceState = world.getBlockState(mutable.down(subSurfaceOffset));
					}
					
					double ds = MathHelper.hypot(mutable.getX() - centerX, mutable.getZ() - centerZ);
					
					if(ds < 12.0)
						ds = 12.0;
					
					double widthFactor = 2.0;
					int offset = (int) (Math.pow((depth * widthFactor) / 2.0 - ds, 2.0) / (depth * widthFactor));
					int bottomY = localSurfaceY - depth + offset;
					int y = localSurfaceY;
					mutable.setY(y);
					
					while(y >= bottomY)
					{
						if(y == bottomY && world.getBlockState(mutable.down()).getMaterial().blocksMovement())
						{
							world.setBlockState(mutable.down(), surfaceState, Block.NOTIFY_LISTENERS);
							
							if(subSurfaceState.getMaterial().blocksMovement())
							{
								subSurfaceOffset = world.getRandom().nextBetween(2, 4);
								
								for(int i = 0; i < subSurfaceOffset; i++)
									world.setBlockState(mutable.down(i), subSurfaceState, Block.NOTIFY_LISTENERS);
							}
						}

						world.setBlockState(mutable, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
						mutable.setY(--y);
					}
				}
			} 
		}
	}
}