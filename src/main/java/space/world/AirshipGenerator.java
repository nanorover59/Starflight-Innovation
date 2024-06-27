package space.world;

import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
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
import space.StarflightMod;
import space.block.StarflightBlocks;

public class AirshipGenerator
{
	private static final int BALLOON_LENGTH = 48;
	private static final int BALLOON_WIDTH = 32;
	private static final int BALLOON_HEIGHT = 24;
	private static final int SHIP_LENGTH = 32;
	private static final int SHIP_RADIUS = 8;
	private static final int CONE_Z = 16;
	private static final int PROP_RADIUS = 4;
	private static final RegistryKey<LootTable> LOOT_TABLE = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(StarflightMod.MOD_ID, "chests/moonshaft"));
	
	public static void addPieces(Structure.Context context, BlockPos pos, StructurePiecesHolder holder)
	{
		ChunkRandom random = context.random();
		int radius = 64;
		pos = pos.add(8, 0, 8);
		int surfaceY = context.chunkGenerator().getHeightOnGround(pos.getX(), pos.getZ(), Type.WORLD_SURFACE_WG, context.world(), context.noiseConfig());
		BlockPos center = pos.up(surfaceY);
		int chunkRadius = (((int) radius) >> 4);
		Direction direction = Direction.fromHorizontal(random.nextInt(4));
		
		for(int x = -chunkRadius; x < chunkRadius; x++)
		{
			for(int z = -chunkRadius; z < chunkRadius; z++)
			{
				BlockPos startPos = new BlockPos(pos.getX() + (x << 4), 0, pos.getZ() + (z << 4));
				
				if(MathHelper.hypot(x, z) <= chunkRadius)
					holder.addPiece(new Piece(startPos, center.getX(), center.getY(), center.getZ(), direction));
			}
		}
	}
	
	private static void postPlacement(StructureWorldAccess world, Random random, BlockBox box)
	{
		for(int x = box.getMinX(); x < box.getMaxX(); x++)
		{
			for(int y = box.getMinY(); y < box.getMaxY(); y++)
			{
				for(int z = box.getMinZ(); z < box.getMaxZ(); z++)
				{
					BlockPos pos = new BlockPos(x, y, z);
					BlockState blockState = world.getBlockState(pos);
					
					if(blockState.getBlock() == Blocks.DIRT && world.getBlockState(pos.up()).isAir() && random.nextInt(6) == 0)
						world.setBlockState(pos.up(), Blocks.DEAD_BUSH.getDefaultState(), Block.NOTIFY_LISTENERS);
					else if(blockState.getBlock() == StarflightBlocks.STORAGE_CUBE)
						LootableInventory.setLootTable(world, random, pos, LOOT_TABLE);
				}
			}
		}
	}
	
	public static class Piece extends StructurePiece
	{
		private final int centerX;
		private final int centerY;
		private final int centerZ;
		
		public Piece(BlockPos start, int x, int y, int z, Direction orientation)
		{
			super(StarflightWorldGeneration.OUTPOST_PIECE, 0, new BlockBox(start));
			this.setOrientation(orientation);
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
			BlockPos chunkCenterPos = new BlockPos(8, centerY, 8);
			HashMap<BlockPos, Integer> details = new HashMap<BlockPos, Integer>();
			int balloonGap = BALLOON_HEIGHT + SHIP_RADIUS;
			int forwardBulkhead = (SHIP_LENGTH / 2) - 4;
			double sqpi = SHIP_RADIUS / Math.sqrt(Math.PI);
			BlockRotation rotation = BlockRotation.NONE;
			
			if(getFacing() == Direction.NORTH)
				rotation = BlockRotation.CLOCKWISE_180;
			else if(getFacing() == Direction.EAST)
				rotation = BlockRotation.CLOCKWISE_90;
			else if(getFacing() == Direction.WEST)
				rotation = BlockRotation.COUNTERCLOCKWISE_90;
			
			for(int x = 0; x < 16; x++)
			{
				for(int y = -64; y <= 64; y++)
				{
					for(int z = 0; z < 16; z++)
					{
						BlockPos pos = new BlockPos(startPos.getX() + x, startPos.getY() + y, startPos.getZ() + z);
						BlockPos difference = pos.subtract(center).rotate(rotation);
						BlockPos detailOffset = pos.subtract(getCenter()).rotate(rotation);
						int dx = difference.getX();
						int dy = difference.getY();
						int dz = difference.getZ();
						BlockState state = null;
						double radius = Math.hypot(dx, dy);
						
						// Balloon
						double ellipsoid = ((double) (dx * dx) / (BALLOON_WIDTH * BALLOON_WIDTH)) + ((double) ((dy - balloonGap) * (dy - balloonGap)) / (BALLOON_HEIGHT * BALLOON_HEIGHT)) + ((double) (dz * dz) / (BALLOON_LENGTH * BALLOON_LENGTH));
						
						if(ellipsoid < 1.0 && ellipsoid > 0.85)
							state = StarflightBlocks.REINFORCED_FABRIC.getDefaultState();
						
						// Airship Hull
						double shipRadius;
						boolean shell;
						
						if(dz < SHIP_LENGTH / 2)
						{
							shipRadius = SHIP_RADIUS;
							shell = radius < shipRadius + 0.75 && radius >= shipRadius;
						}
						else
						{
							double theta = Math.acos(1.0 - (2.0 * ((SHIP_LENGTH / 2) + CONE_Z - dz)) / CONE_Z);
							shipRadius = Math.round(sqpi * Math.sqrt(theta - Math.sin(theta * 2.0) / 2.0 + (2.0 / 3.0) * Math.pow(Math.sin(theta), 3))) - 0.5;
							shell = radius < shipRadius + (shipRadius < 1.5 ? 2.0 : 1.0) && radius >= shipRadius - (shipRadius < 2.0 ? 1.0 : 0.0);
						}
						
						if(shell && dz < (SHIP_LENGTH / 2) + CONE_Z + 1 && dz > -(SHIP_LENGTH / 2) - CONE_Z)
							state = (dy == 2 && dz > (SHIP_LENGTH / 2) + 5) || (dy == 3 && dz > SHIP_LENGTH / 2) ? Blocks.TINTED_GLASS.getDefaultState() :  StarflightBlocks.TITANIUM_PANELS.getDefaultState();
						
						// Airship Fins
						if(dy == 0 && dz > -(SHIP_LENGTH / 2) - CONE_Z && radius > shipRadius && radius <= (shipRadius - dz) * 0.5)
							state = StarflightBlocks.TITANIUM_PANELS.getDefaultState();
						
						// Balloon Fins
						double balloonRadius = Math.hypot(dx, dy - balloonGap);
						
						if(dy == balloonGap && dz > -BALLOON_LENGTH && ellipsoid > 1.0 && balloonRadius <= (4 - dz) * 0.3 + (SHIP_LENGTH / 3))
							state = StarflightBlocks.REINFORCED_FABRIC.getDefaultState();
						
						if(dx == 0 && dz > -BALLOON_LENGTH && ellipsoid > 1.0 && balloonRadius <= (4 - dz) * 0.3 + (SHIP_LENGTH / 3))
							state = StarflightBlocks.REINFORCED_FABRIC.getDefaultState();
						
						// Ship Interior
						if(radius < shipRadius && dz > -(SHIP_LENGTH / 2) - CONE_Z)
						{
							// Bulkhead Plates
							{
								if(dz == -(SHIP_LENGTH / 2) - CONE_Z + 1 || dz == forwardBulkhead)
									state = StarflightBlocks.TITANIUM_PANELS.getDefaultState();
								
								// Doors
								if(dx == 0 && dz == forwardBulkhead && (dy == -7 || dy == -3 || dy == 1 || dy == 5))
									details.put(detailOffset, 0);
							}
							
							// Floors
							if(dy == 0)
							{
								state = StarflightBlocks.TITANIUM_PANELS.getDefaultState();
								
								// Ladders
								if(dx == 0 && dz < SHIP_LENGTH / 2 && dz % 16 == 0)
									details.put(detailOffset, 1);
							}
							
							if(dy == 4 || dy == -4)
							{
								if(dz < forwardBulkhead)
									state = StarflightBlocks.TITANIUM_PANELS.getDefaultState();
								else if(dz > forwardBulkhead && dz < forwardBulkhead + 4)
									state = StarflightBlocks.WALKWAY.getDefaultState();
							}
							
							// Seats
							if(dy == 1 && dx != 0 && dx % 2 == 0 && Math.abs(dx) < shipRadius - 1.5 && dz % 2 == 0 && dz > (SHIP_LENGTH / 2) - 4 && dz < (SHIP_LENGTH / 2) + CONE_Z - 4)
								state = Blocks.BIRCH_STAIRS.getDefaultState().with(StairsBlock.FACING, getFacing().getOpposite());
						}
						
						// Propellers
						if(dz > -8 && dz < 8)
						{
							double propRadius = Math.hypot(dx - BALLOON_WIDTH - PROP_RADIUS, dy - balloonGap);
							
							if((int) propRadius == PROP_RADIUS)
								state = StarflightBlocks.TITANIUM_PANELS.getDefaultState();
							
							propRadius = Math.hypot(dx + BALLOON_WIDTH + PROP_RADIUS, dy - balloonGap);
							
							if((int) propRadius == PROP_RADIUS)
								state = StarflightBlocks.TITANIUM_PANELS.getDefaultState();
						}
						
						if(state != null)
							world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
					}
				}
			}
			
			for(Entry<BlockPos, Integer> entry : details.entrySet())
			{
				BlockPos pos = entry.getKey();
				int type = entry.getValue();
				
				if(type == 0)
				{
					this.addBlock(world, StarflightBlocks.TITANIUM_DOOR.getDefaultState(), pos.getX(), pos.getY(), pos.getZ(), chunkBox);
					this.addBlock(world, StarflightBlocks.TITANIUM_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER), pos.getX(), pos.getY() + 1, pos.getZ(), chunkBox);
					this.addBlock(world, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState(), pos.getX(), pos.getY(), pos.getZ() - 1, chunkBox);
					this.addBlock(world, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState(), pos.getX(), pos.getY(), pos.getZ() + 1, chunkBox);
					this.addBlock(world, StarflightBlocks.AIRWAY.getDefaultState(), pos.getX() - 2, pos.getY() + 1, pos.getZ(), chunkBox);
					this.addBlock(world, StarflightBlocks.AIRWAY.getDefaultState(), pos.getX() + 2, pos.getY() + 1, pos.getZ(), chunkBox);
				}
				else if(type == 1)
				{
					this.fillWithOutline(world, chunkBox, pos.getX() - 3, pos.getY() - 7, pos.getZ(), pos.getX() - 3, pos.getY() + 4, pos.getZ(), StarflightBlocks.TITANIUM_FRAME.getDefaultState(), AIR, false);
					this.fillWithOutline(world, chunkBox, pos.getX() + 3, pos.getY() - 7, pos.getZ(), pos.getX() + 3, pos.getY() + 4, pos.getZ(), StarflightBlocks.TITANIUM_FRAME.getDefaultState(), AIR, false);
					this.fillWithOutline(world, chunkBox, pos.getX() - 2, pos.getY() - 7, pos.getZ(), pos.getX() - 2, pos.getY() + 4, pos.getZ(), StarflightBlocks.IRON_LADDER.getDefaultState().with(LadderBlock.FACING, Direction.EAST), AIR, false);
					this.fillWithOutline(world, chunkBox, pos.getX() + 2, pos.getY() - 7, pos.getZ(), pos.getX() + 2, pos.getY() + 4, pos.getZ(), StarflightBlocks.IRON_LADDER.getDefaultState().with(LadderBlock.FACING, Direction.WEST), AIR, false);
				}
			}
			
			postPlacement(world, random, new BlockBox(chunkBox.getMinX(), chunkCenterPos.getY() - 32, chunkBox.getMinZ(), chunkBox.getMaxX(), chunkCenterPos.getY() + 32, chunkBox.getMaxZ()));
		}
	}
}