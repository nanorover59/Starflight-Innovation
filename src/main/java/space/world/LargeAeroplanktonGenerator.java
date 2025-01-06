package space.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CaveVines;
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
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import space.block.StarflightBlocks;
import space.util.PerlinNoise;

public class LargeAeroplanktonGenerator
{
	public static void addPieces(Structure.Context context, BlockPos pos, StructurePiecesHolder holder)
	{
		ChunkRandom random = context.random();
		int radius = random.nextBetweenExclusive(16, 32);
		double thicknessFactor = 0.25 + random.nextDouble() * 0.25;
		BlockPos center = pos.add(random.nextInt(16), 128, random.nextInt(16));
		
		if(random.nextInt(8) == 0)
			radius *= 2;
		
		int chunkRadius = (radius >> 4) + 2;
		
		for(int x = -chunkRadius; x <= chunkRadius; x++)
		{
			for(int z = -chunkRadius; z <= chunkRadius; z++)
			{
				if(MathHelper.hypot(x, z) <= chunkRadius)
				{
					BlockPos startPos = new BlockPos(pos.getX() + (x << 4), 0, pos.getZ() + (z << 4));
					holder.addPiece(new Piece(startPos, center.getX(), center.getY(), center.getZ(), radius, thicknessFactor));
				}
			}
		}
	}
	
	public static class Piece extends StructurePiece
	{
		private final int centerX;
		private final int centerY;
		private final int centerZ;
		private final int radius;
		private final double thicknessFactor;
		
		public Piece(BlockPos start, int x, int y, int z, int radius, double thicknessFactor)
		{
			super(StarflightWorldGeneration.VOLCANO_PIECE, 0, new BlockBox(start.getX(), 0, start.getZ(), start.getX() + 16, 128, start.getZ() + 16));
			this.centerX = x;
			this.centerY = y;
			this.centerZ = z;
			this.radius = radius;
			this.thicknessFactor = thicknessFactor;
		}

		public Piece(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.VOLCANO_PIECE, nbt);
			this.centerX  = nbt.getInt("x");
			this.centerY  = nbt.getInt("y");
			this.centerZ = nbt.getInt("z");
			this.radius = nbt.getInt("radius");
			this.thicknessFactor = nbt.getDouble("thicknessFactor");
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			nbt.putInt("x", this.centerX);
			nbt.putInt("y", this.centerY);
			nbt.putInt("z", this.centerZ);
			nbt.putInt("radius", radius);
			nbt.putDouble("thicknessFactor", thicknessFactor);
		}
		
		@Override
		protected boolean canReplace(BlockState state)
		{
			return true;
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			PerlinNoise noise1 = new PerlinNoise(centerX, centerY, centerZ);
			PerlinNoise noise2 = new PerlinNoise(centerX * 2, centerY * 2, centerZ * 2);
			PerlinNoise noise3 = new PerlinNoise(centerX * 3, centerY * 3, centerZ * 3);
			BlockPos.Mutable mutable = new BlockPos.Mutable();
			
			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					mutable.set(chunkPos.getStartX() + x, centerY, chunkPos.getStartZ() + z);
					double distanceFromCenter = MathHelper.hypot(mutable.getX() - centerX, mutable.getZ() - centerZ);
					double r = distanceFromCenter / radius;
					
					if(r >= 1.0)
						continue;
					
					int yOffset = (int) (MathHelper.square(1.0 - MathHelper.square(r)) * thicknessFactor * radius);
					double nx = ((double) ((mutable.getX() - centerX)) / radius) * 5.0;
					double nz = ((double) ((mutable.getZ() - centerZ)) / radius) * 5.0;
					int noise1Offset = (int) (((noise1.get(nx, nz) + 1.0) / 2.0) * yOffset);
					int noise2Offset = (int) (((noise2.get(nx, nz) + 1.0) / 2.0) * yOffset);
					int noise3Offset = (int) (noise3.get(nx * 2.0, nz * 2.0) * distanceFromCenter * 0.5);
					world.setBlockState(mutable.up(yOffset), StarflightBlocks.AEROPLANKTON.getDefaultState(), Block.NOTIFY_LISTENERS);
					world.setBlockState(mutable.down(yOffset), StarflightBlocks.AEROPLANKTON.getDefaultState(), Block.NOTIFY_LISTENERS);
					
					for(int i = -yOffset; i < -yOffset + noise1Offset; i++)
						world.setBlockState(mutable.up(i), StarflightBlocks.AEROPLANKTON.getDefaultState(), Block.NOTIFY_LISTENERS);
					
					for(int i = yOffset - noise2Offset; i < yOffset; i++)
						world.setBlockState(mutable.up(i), StarflightBlocks.AEROPLANKTON.getDefaultState(), Block.NOTIFY_LISTENERS);
					
					for(int i = -yOffset - noise3Offset; i < -yOffset; i++)
						world.setBlockState(mutable.up(i), StarflightBlocks.AEROPLANKTON.getDefaultState(), Block.NOTIFY_LISTENERS);
					
					if(random.nextInt(6) == 0 && noise3Offset > 0)
					{
						int vineLength = random.nextBetween(1, 6);
						int startY = -yOffset - noise3Offset - vineLength;
						int endY = -yOffset - noise3Offset;
						
						for(int i = startY; i < endY; i++)
						{
							BlockState vineState = (i == startY) ? StarflightBlocks.AEROPLANKTON_VINES_BOTTOM.getDefaultState() : StarflightBlocks.AEROPLANKTON_VINES.getDefaultState();
							
							if(random.nextInt(12) == 0)
								vineState = vineState.with(CaveVines.BERRIES, true);
							
							world.setBlockState(mutable.up(i), vineState, Block.NOTIFY_LISTENERS);
						}
					}
				}
			}
		}
	}
}