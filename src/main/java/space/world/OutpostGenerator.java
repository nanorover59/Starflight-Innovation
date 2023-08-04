package space.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.SlabType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import space.block.StarflightBlocks;

public class OutpostGenerator
{
	private static final int SHELL_RADIUS = 32;
	private static final int CENTER_RADIUS = 8;
	private static final int CENTER_HEIGHT = 24;
	private static final int LEVEL_HEIGHT = 6;
	private static final int TUNNEL_RADIUS = 5;
	private static final Block BRICKS = Blocks.STONE_BRICKS;
	private static final Block SLAB = StarflightBlocks.STRUCTURAL_ALUMINUM_SLAB;
	
	public static void addPieces(Structure.Context context, BlockPos pos, StructurePiecesHolder holder)
	{
		ChunkRandom random = context.random();
		int radius = 64;
		pos = pos.add(8, 0, 8);
		int surfaceY = context.chunkGenerator().getHeightOnGround(pos.getX(), pos.getZ(), Type.WORLD_SURFACE_WG, context.world(), context.noiseConfig());
		BlockPos center = pos.up(surfaceY);
		int chunkRadius = (((int) radius) >> 4);
		
		for(int x = -chunkRadius; x < chunkRadius; x++)
		{
			for(int z = -chunkRadius; z < chunkRadius; z++)
			{
				BlockPos startPos = new BlockPos(pos.getX() + (x << 4), 0, pos.getZ() + (z << 4));
				
				if(MathHelper.hypot(x, z) <= chunkRadius)
					holder.addPiece(new Piece(startPos, center.getX(), center.getY(), center.getZ()));
			}
		}
	}
	
	public static class Piece extends StructurePiece
	{
		private final int centerX;
		private final int centerY;
		private final int centerZ;
		
		public Piece(BlockPos start, int x, int y, int z)
		{
			super(StarflightWorldGeneration.OUTPOST_PIECE, 0, new BlockBox(start));
			this.centerX = x;
			this.centerY = y;
			this.centerZ = z;
		}

		public Piece(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.OUTPOST_PIECE, nbt);
			this.centerX = nbt.getInt("x");
			this.centerY = nbt.getInt("y");
			this.centerZ = nbt.getInt("z");
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			nbt.putInt("x", this.centerX);
			nbt.putInt("y", this.centerY);
			nbt.putInt("z", this.centerZ);
		}
		
		@Override
		protected boolean canReplace(BlockState state)
		{
			return true;
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			BlockPos center = new BlockPos(centerX, centerY, centerZ);
			BlockPos startPos = chunkPos.getBlockPos(0, centerY, 0);
			boolean spiral = false;
			//world.setBlockState(startPos, Blocks.GLOWSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
			
			for(int x = 0; x < 16; x++)
			{
				for(int y = -SHELL_RADIUS; y <= SHELL_RADIUS; y++)
				{
					for(int z = 0; z < 16; z++)
					{
						BlockPos pos = new BlockPos(startPos.getX() + x, startPos.getY() + y, startPos.getZ() + z);
						int dx = pos.getX() - center.getX();
						int dy = pos.getY() - center.getY();
						int dz = pos.getZ() - center.getZ();
						double distance = Math.sqrt(pos.getSquaredDistance(center));
						BlockState state = null;
						
						if((int) distance == SHELL_RADIUS)
						{
							state = Blocks.GLASS.getDefaultState();
							
							if(pos.getY() == centerY)
								state = StarflightBlocks.RIVETED_ALUMINUM.getDefaultState();
							else if(pos.getY() < centerY)
								state = BRICKS.getDefaultState();
							
							int rxy = (int) MathHelper.hypot(dx, dy);
							int ryz = (int) MathHelper.hypot(dy, dz);
							
							if(rxy == TUNNEL_RADIUS || ryz == TUNNEL_RADIUS)
								state = StarflightBlocks.RIVETED_ALUMINUM.getDefaultState();
							else if(rxy < TUNNEL_RADIUS || ryz < TUNNEL_RADIUS)
								state = StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState();
							
							world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
						}
						else if((int) distance < SHELL_RADIUS)
						{
							state = Blocks.AIR.getDefaultState();
							
							if(y < 0 && (y == -1 || y % LEVEL_HEIGHT == 0))
								state = BRICKS.getDefaultState();
							else if(y == 0)
								state = Blocks.DIRT.getDefaultState();
							
							double dxz = MathHelper.hypot(dx, dz);
							
							if((int) dxz <= CENTER_RADIUS && y <= CENTER_HEIGHT)
							{
								if((int) dxz == CENTER_RADIUS)
								{
									if(y > 0)
									{
										if(y % LEVEL_HEIGHT == 0)
											state = StarflightBlocks.RIVETED_ALUMINUM.getDefaultState();
										else
											state = Blocks.GLASS.getDefaultState();
									}
									else
										state = BRICKS.getDefaultState();
								}
								else if((int) dxz < CENTER_RADIUS)
								{
									if(y == CENTER_HEIGHT)
										state = Blocks.GLASS.getDefaultState();
									else if(y % LEVEL_HEIGHT == 0)
										state = y > 0 ? StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState() : BRICKS.getDefaultState();
									else
										state = Blocks.AIR.getDefaultState();
								}
									
								
								//if(y <= 0)
								//	state = BRICKS.getDefaultState();
								//else
								//	state = Blocks.GLASS.getDefaultState();
							}
						}
						else
						{
							if(Math.abs(dz) < SHELL_RADIUS + 8)
							{
								int r = (int) MathHelper.hypot(dx, dy);
								
								if(r == TUNNEL_RADIUS)
									state = StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState();
								else if(r < TUNNEL_RADIUS)
									state = Blocks.AIR.getDefaultState();
							}
							else if(Math.abs(dz) == SHELL_RADIUS + 8)
							{
								int r = (int) MathHelper.hypot(dx, dy);
								
								if(r == TUNNEL_RADIUS)
									state = StarflightBlocks.RIVETED_ALUMINUM.getDefaultState();
								else if(r < TUNNEL_RADIUS)
									state = StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState();
							}
							
							if(Math.abs(dx) < SHELL_RADIUS + 8)
							{
								int r = (int) MathHelper.hypot(dz, dy);
								
								if(r == TUNNEL_RADIUS)
									state = StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState();
								else if(r < TUNNEL_RADIUS)
									state = Blocks.AIR.getDefaultState();
							}
							else if(Math.abs(dx) == SHELL_RADIUS + 8)
							{
								int r = (int) MathHelper.hypot(dz, dy);
								
								if(r == TUNNEL_RADIUS)
									state = StarflightBlocks.RIVETED_ALUMINUM.getDefaultState();
								else if(r < TUNNEL_RADIUS)
									state = StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState();
							}
						}
						
						if(state != null)
							world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
						
						if(pos.equals(center))
							spiral = true;
					}
				}
			}
			
			if(spiral)
			{
				fillWithOutline(world, chunkBox, centerX - 3, centerY - SHELL_RADIUS + 2, centerZ - 3, centerX + 3, centerY + CENTER_HEIGHT - 1, centerZ - 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
				fillWithOutline(world, chunkBox, centerX - 3, centerY - SHELL_RADIUS + 2, centerZ + 3, centerX + 3, centerY + CENTER_HEIGHT - 1, centerZ + 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
				fillWithOutline(world, chunkBox, centerX - 3, centerY - SHELL_RADIUS + 2, centerZ - 3, centerX - 3, centerY + CENTER_HEIGHT - 1, centerZ + 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
				fillWithOutline(world, chunkBox, centerX + 3, centerY - SHELL_RADIUS + 2, centerZ - 3, centerX + 3, centerY + CENTER_HEIGHT - 1, centerZ + 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
				
				BlockState bottomSlab = SLAB.getDefaultState();
				BlockState topSlab = SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP);
				Direction spiralDirection = Direction.fromHorizontal(random.nextInt(4));
				BlockPos spiralStart = new BlockPos(centerX, centerY - SHELL_RADIUS + 1, centerZ);
				BlockPos spiralOffset = new BlockPos(3, 0, 3);

				for(int i = 0; i <= spiralDirection.getHorizontal(); i++)
					spiralOffset = spiralOffset.rotate(BlockRotation.CLOCKWISE_90);

				BlockPos spiralPos = new BlockPos(spiralStart.getX() - spiralOffset.getX(), spiralStart.getY() + 2, spiralStart.getZ() - spiralOffset.getZ());
				
				while(spiralPos.getY() < centerY + CENTER_HEIGHT - LEVEL_HEIGHT)
				{
					world.setBlockState(spiralPos, bottomSlab, Block.NOTIFY_LISTENERS);
					world.setBlockState(spiralPos.offset(spiralDirection, 1), topSlab, Block.NOTIFY_LISTENERS);
					world.setBlockState(spiralPos.up().offset(spiralDirection, 2), bottomSlab, Block.NOTIFY_LISTENERS);
					world.setBlockState(spiralPos.up().offset(spiralDirection, 3), topSlab, Block.NOTIFY_LISTENERS);
					world.setBlockState(spiralPos.up(2).offset(spiralDirection, 4), bottomSlab, Block.NOTIFY_LISTENERS);
					world.setBlockState(spiralPos.up(2).offset(spiralDirection, 5), topSlab, Block.NOTIFY_LISTENERS);
					spiralPos = spiralPos.up(3).offset(spiralDirection, 6);
					spiralDirection = spiralDirection.rotateYClockwise();
				}
			}
		}
	}
}