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
import net.minecraft.util.math.Direction;
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
import space.block.StarflightBlocks;

public class CraterGenerator
{
	public static void addPieces(Structure.Context context, BlockPos pos, StructurePiecesHolder holder)
	{
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		ChunkRandom random = context.random();
		NoiseConfig noiseConfig = context.noiseConfig();	
		int surfaceY = chunkGenerator.getHeightOnGround(pos.getX(), pos.getZ(), Heightmap.Type.OCEAN_FLOOR_WG, context.world(), noiseConfig);
		int radius = 24 + random.nextInt(24);
		double depthFactor = 0.5 + random.nextDouble() * 0.25;
		double rimWidth = 0.5 + random.nextDouble() * 0.15;
		double rimSteepness = 0.25 + random.nextDouble() * 0.25;
		BlockPos center = pos.add(random.nextInt(16), surfaceY, random.nextInt(16));
		int chunkRadius = (((int) (radius * 2.0)) >> 4) + 2;
		
		if(surfaceY < 32)
			return;
		
		for(int x = -chunkRadius; x <= chunkRadius; x++)
		{
			for(int z = -chunkRadius; z <= chunkRadius; z++)
			{
				if(MathHelper.hypot(x, z) <= chunkRadius)
				{
					BlockPos startPos = new BlockPos(pos.getX() + (x << 4), 0, pos.getZ() + (z << 4));
					holder.addPiece(new Piece(startPos, center.getX(), center.getY(), center.getZ(), radius, depthFactor, rimWidth, rimSteepness));
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
		private final double depthFactor;
		private final double rimWidth;
		private final double rimSteepness;
		
		public Piece(BlockPos start, int x, int y, int z, int radius, double depthFactor, double rimWidth, double rimSteepness)
		{
			super(StarflightWorldGeneration.CRATER_PIECE, 0, new BlockBox(start));
			this.centerX = x;
			this.centerY = y;
			this.centerZ = z;
			this.radius = radius;
			this.depthFactor = depthFactor;
			this.rimWidth = rimWidth;
			this.rimSteepness = rimSteepness;
		}

		public Piece(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.CRATER_PIECE, nbt);
			this.centerX  = nbt.getInt("x");
			this.centerY  = nbt.getInt("y");
			this.centerZ = nbt.getInt("z");
			this.radius = nbt.getInt("radius");
			this.depthFactor = nbt.getDouble("depthFactor");
			this.rimWidth = nbt.getDouble("rimWidth");
			this.rimSteepness = nbt.getDouble("rimSteepness");
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			nbt.putInt("x", this.centerX);
			nbt.putInt("y", this.centerY);
			nbt.putInt("z", this.centerZ);
			nbt.putInt("radius", radius);
			nbt.putDouble("depthFactor", depthFactor);
			nbt.putDouble("rimWidth", rimWidth);
			nbt.putDouble("rimSteepness", rimSteepness);
		}
		
		@Override
		protected boolean canReplace(BlockState state)
		{
			return false;
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			BlockPos.Mutable mutable = new BlockPos.Mutable();
			BlockPos startPos = chunkPos.getBlockPos(0, 0, 0);
			BlockPos center = new BlockPos(centerX, centerY, centerZ);
			boolean hasIce = world.getBiome(center).isIn(StarflightWorldGeneration.ICE_CRATERS);
			int iceY = MathHelper.clamp(center.getY() + getCraterDepth(0) / 2, 32, 56);
			
			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					mutable.set(startPos.getX() + x, 0, startPos.getZ() + z);
					int localSurfaceY = world.getTopPosition(Type.OCEAN_FLOOR, mutable).getY();
					mutable.setY(localSurfaceY);
					
					while(!world.getBlockState(mutable).isSideSolidFullSquare(world, startPos, Direction.UP) && mutable.getY() > 0)
						mutable.setY(mutable.getY() - 1);
					
					mutable.up();
					double r = MathHelper.hypot(mutable.getX() - centerX, mutable.getZ() - centerZ) / radius;
					localSurfaceY = mutable.getY();
					int y = localSurfaceY + getCraterDepth(r);
					BlockState surfaceState = world.getBlockState(mutable);
					
					if(hasIce && y <= iceY && surfaceState.getBlock() == StarflightBlocks.REGOLITH)
						surfaceState = StarflightBlocks.ICY_REGOLITH.getDefaultState();
					
					if(y < localSurfaceY)
					{
						for(int i = y; i < localSurfaceY + 6; i++)
						{
							mutable.setY(i);
							
							if(world.getBlockState(mutable).getBlock() != Blocks.AIR)
							{
								if(hasIce && i == iceY)
									world.setBlockState(mutable, surfaceState, Block.NOTIFY_LISTENERS);
								else if(hasIce && i < iceY)
									world.setBlockState(mutable, random.nextBoolean() ? Blocks.ICE.getDefaultState() : Blocks.PACKED_ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
								else
									world.setBlockState(mutable, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
							}
						}
						
						mutable.setY(y - 1);
						
						if(world.getBlockState(mutable).isSideSolidFullSquare(world, startPos, Direction.UP))
							world.setBlockState(mutable, surfaceState, Block.NOTIFY_LISTENERS);
					}
					else
					{
						for(int i = y; i > localSurfaceY; i--)
						{
							mutable.setY(i);
							world.setBlockState(mutable, surfaceState, Block.NOTIFY_LISTENERS);
						}
					}
				}
			} 
		}
		
		private int getCraterDepth(double r)
		{
			double parabola = r * r - 1.0;
			double rimR = Math.min(r - rimWidth - 1.0, 0.0);
			double rim = rimR * rimR * rimSteepness;
			double shape = smoothMin(parabola, rim, 0.5);
			shape = smoothMax(shape, -depthFactor, 0.5);
			return (int) (shape * radius);
		}
		
		private double smoothMin(double a, double b, double c)
		{
			double h = Math.max(c - Math.abs(a - b), 0.0) / c;
			return Math.min(a, b) - h * h * c * 0.25;
		}
		
		private double smoothMax(double a, double b, double c)
		{
			double h = Math.max(c - Math.abs(a - b), 0.0) / c;
			return Math.max(a, b) + h * h * c * 0.25;
		}
	}
}