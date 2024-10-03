package space.world;

import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.structure.Structure;
import space.StarflightMod;
import space.block.LightColumnBlock;
import space.block.SolidLeverBlock;
import space.block.StarflightBlocks;
import space.block.StorageCubeBlock;
import space.entity.AncientHumanoidEntity;
import space.entity.StarflightEntities;

public class BioDomeGenerator
{
	private static final int SHELL_RADIUS = 48;
	private static final int CENTER_RADIUS = 6;
	private static final int CENTER_HEIGHT = 36;
	private static final int LEVEL_HEIGHT = 6;
	private static final Block SLAB = StarflightBlocks.STRUCTURAL_ALUMINUM_SLAB;
	private static final RegistryKey<LootTable> LOOT_TABLE = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(StarflightMod.MOD_ID, "chests/biodome"));
	private static final Identifier AIRLOCK = Identifier.of(StarflightMod.MOD_ID, "sliding_airlock");
	private static final Identifier OXYGEN_SOURCE = Identifier.of(StarflightMod.MOD_ID, "biodome_oxygen");
	
	public static void addPieces(Structure.Context context, BlockPos pos, StructurePiecesHolder holder)
	{
		ChunkRandom random = context.random();
		int radius = 64;
		//pos = pos.add(8, 0, 8);
		int surfaceY = context.chunkGenerator().getHeightOnGround(pos.getX(), pos.getZ(), Type.WORLD_SURFACE_WG, context.world(), context.noiseConfig());
		
		for(int i = 0; i < 8; i++)
		{
			double theta = (i * Math.PI * 2.0) / 8.0;
			int x = (int) (pos.getX() + (radius + 8) * Math.cos(theta));
			int z = (int) (pos.getZ() + (radius + 8) * Math.sin(theta));
			int sy = context.chunkGenerator().getHeightOnGround(x, z, Heightmap.Type.OCEAN_FLOOR_WG, context.world(), context.noiseConfig());
			VerticalBlockSample sample = context.chunkGenerator().getColumnSample(pos.getX(), pos.getZ(), context.world(), context.noiseConfig());
			
			if(sy < surfaceY - 16 || sy > surfaceY + 16 || !sample.getState(surfaceY + 4).isAir())
				return;
		}
		
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
	
	private static BlockState getBrickType(World world)
	{
		if(world.getDimensionEntry().getIdAsString().contains("mars"))
			return StarflightBlocks.REDSLATE_BRICKS.getDefaultState();
		
		return Blocks.STONE_BRICKS.getDefaultState();
	}
	
	private static BlockState getBrickStairsType(World world, Direction direction)
	{
		if(world.getDimensionEntry().getIdAsString().contains("mars"))
			return StarflightBlocks.REDSLATE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, direction);
		
		return Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, direction);
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
		private static final BlockState AIR = Blocks.AIR.getDefaultState();
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
			ServerWorld serverWorld = world.toServerWorld();
			BlockPos center = new BlockPos(centerX, centerY, centerZ);
			BlockPos startPos = chunkPos.getBlockPos(0, centerY, 0);
			BlockPos chunkCenterPos = new BlockPos(8, centerY, 8);
			HashMap<BlockPos, Integer> details = new HashMap<BlockPos, Integer>();
			
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
							state = StarflightBlocks.TITANIUM_GLASS.getDefaultState();
							
							if(pos.getY() == centerY)
								state = StarflightBlocks.IRON_ROUND_DECO.getDefaultState();
							else if(pos.getY() < centerY)
								state = StarflightBlocks.STRUCTURAL_IRON.getDefaultState();
							
							world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
							
							if(y == 0 && (dx == 0 || dz == 0))
								details.put(pos, 1);
						}
						else if((int) distance < SHELL_RADIUS)
						{
							state = Blocks.AIR.getDefaultState();
							
							if(y < 0 && (y == -1 || y % LEVEL_HEIGHT == 0))
								state = getBrickType(serverWorld);
							else if(y == 0)							
								state = Blocks.DIRT.getDefaultState();
							
							double dxz = MathHelper.hypot(dx, dz);
							
							if((int) dxz <= CENTER_RADIUS && y <= CENTER_HEIGHT + 1)
							{
								if((int) dxz == CENTER_RADIUS)
								{
									if(y > 0)
									{
										if(y == CENTER_HEIGHT + 1)
											state = StarflightBlocks.SOLAR_PANEL.getDefaultState();
										else if(y % LEVEL_HEIGHT == 0)
											state = StarflightBlocks.RIVETED_ALUMINUM.getDefaultState();
										else
											state = Blocks.GLASS.getDefaultState();
									}
									else
										state = StarflightBlocks.STRUCTURAL_IRON.getDefaultState();
								}
								else if((int) dxz < CENTER_RADIUS)
								{
									if(y == CENTER_HEIGHT + 1)
										state = StarflightBlocks.SOLAR_PANEL.getDefaultState();
									else if(y == CENTER_HEIGHT)
										state = StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState();
									else if(y % LEVEL_HEIGHT == 0)
										state = y > 0 ? StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState() : StarflightBlocks.STRUCTURAL_IRON.getDefaultState();
									else
										state = Blocks.AIR.getDefaultState();
								}
							}
							else if(y < 0 && y % LEVEL_HEIGHT != 0 && ((dx - 4) % 8 == 0 || (dz - 4) % 8 == 0))
							{
								boolean door = ((y - 1) % LEVEL_HEIGHT == 0 || (y - 2) % LEVEL_HEIGHT == 0 || (y - 3) % LEVEL_HEIGHT == 0)
											&& (dx % 8 == 0 || dz % 8 == 0 || (dx - 1) % 8 == 0 || (dz - 1) % 8 == 0 || (dx + 1) % 8 == 0 || (dz + 1) % 8 == 0);
								
								if(!door)
									state = getBrickType(serverWorld);
							}
							
							//else if(y < 0 && y % LEVEL_HEIGHT != 0 && Math.abs(dx) > 4 && Math.abs(dz) > 4 && (dx - 4) % 8 == 0 && (dz - 4) % 8 == 0)
							//	state = StarflightBlocks.RIVETED_ALUMINUM.getDefaultState();
							
							// Spawn the tower.
							if(pos.equals(center))
								details.put(center, 0);
							
							// Spawn grid structures.
							if((int) distance < SHELL_RADIUS - 4 && !(dx == 0 && dz == 0) && dx % 16 == 0 && dz % 16 == 0)
							{
								int cellX = dx / 16;
								int cellZ = dz / 16;
								double cellDistance = Math.hypot(cellX, cellZ);
								
								if(y == 0)
								{
									if(Direction.fromVector(cellX, 0, cellZ) == getFacing() && cellDistance == 2)
										details.put(pos, 2);
									else if(!details.containsKey(pos))
										details.put(pos, 3);
								}
								else if(y < 0 && y % LEVEL_HEIGHT == 0 && !details.containsKey(pos))
									details.put(pos, 4);
							}
						}
						else if(y > 0 && distance - SHELL_RADIUS - 8 < y)
							state = Blocks.AIR.getDefaultState();
						
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
					// Tower and Spiral Staircase
					BlockState verticalLight = StarflightBlocks.LIGHT_COLUMN.getDefaultState().with(LightColumnBlock.AXIS, Axis.Y);
					fill(world, chunkBox, -3, chunkCenterPos.getY() - SHELL_RADIUS + 1, -3, -3, chunkCenterPos.getY() + CENTER_HEIGHT - 1, 3);
					fill(world, chunkBox, 3, chunkCenterPos.getY() - SHELL_RADIUS + 1, -3, 3, chunkCenterPos.getY() + CENTER_HEIGHT - 1, 3);
					fill(world, chunkBox, -3, chunkCenterPos.getY() - SHELL_RADIUS + 1, -3, 3, chunkCenterPos.getY() + CENTER_HEIGHT - 1, -3);
					fill(world, chunkBox, -3, chunkCenterPos.getY() - SHELL_RADIUS + 1, 3, 3, chunkCenterPos.getY() + CENTER_HEIGHT - 1, 3);
					
					int startY = chunkCenterPos.getY() - SHELL_RADIUS + 1;
					int endY = chunkCenterPos.getY() + CENTER_HEIGHT - 8;
					fillWithOutline(world, chunkBox, -2, startY, -2, 2, endY, 2, StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState(), AIR, false);
					fillWithOutline(world, chunkBox, -2, startY, -2, -2, endY, -2, verticalLight, AIR, false);
					fillWithOutline(world, chunkBox, 2, startY, -2, 2, endY, -2, verticalLight, AIR, false);
					fillWithOutline(world, chunkBox, 2, startY, 2, 2, endY, 2, verticalLight, AIR, false);
					fillWithOutline(world, chunkBox, -2, startY, 2, -2, endY, 2, verticalLight, AIR, false);
					//fillWithOutline(world, chunkBox, -1, chunkCenterPos.getY() - SHELL_RADIUS + 1, -1, 1, chunkCenterPos.getY() - 1, 1, Blocks.WATER.getDefaultState(), AIR, false);
					
					for(int y = -SHELL_RADIUS; y < 0; y++)
					{
						if(y % LEVEL_HEIGHT != 0)
							continue;
						
						fill(world, chunkBox, -CENTER_RADIUS, centerY + y + 1, -1, -CENTER_RADIUS, centerY + y + 4, 1);
						fill(world, chunkBox, CENTER_RADIUS, centerY + y + 1, -1, CENTER_RADIUS, centerY + y + 4, 1);
						fill(world, chunkBox, -1, centerY + y + 1, -CENTER_RADIUS, 1, centerY + y + 4, -CENTER_RADIUS);
						fill(world, chunkBox, -1, centerY + y + 1, CENTER_RADIUS, 1, centerY + y + 4, CENTER_RADIUS);
						fill(world, chunkBox, -CENTER_RADIUS, centerY + y + 2, -2, -CENTER_RADIUS, centerY + y + 3, 2);
						fill(world, chunkBox, CENTER_RADIUS, centerY + y + 2, -2, CENTER_RADIUS, centerY + y + 3, 2);
						fill(world, chunkBox, -2, centerY + y + 2, -CENTER_RADIUS, 2, centerY + y + 3, -CENTER_RADIUS);
						fill(world, chunkBox, -2, centerY + y + 2, CENTER_RADIUS, 2, centerY + y + 3, CENTER_RADIUS);
					}
					
					BlockState bottomSlab = SLAB.getDefaultState();
					BlockState topSlab = SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP);
					Direction spiralDirection = Direction.fromHorizontal(random.nextInt(4));
					BlockPos spiralStart = new BlockPos(centerX, centerY - SHELL_RADIUS + 1, centerZ);
					BlockPos spiralOffset = new BlockPos(3, 0, 3);

					for(int i = 0; i <= spiralDirection.getHorizontal(); i++)
						spiralOffset = spiralOffset.rotate(BlockRotation.CLOCKWISE_90);

					BlockPos spiralPos = new BlockPos(spiralStart.getX() - spiralOffset.getX(), spiralStart.getY(), spiralStart.getZ() - spiralOffset.getZ());
					
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
					
					addDoor(world, chunkBox, 0, centerY + 1, -6);
					//this.addBlock(world, StarflightBlocks.PLANETARIUM.getDefaultState().mirror(BlockMirror.FRONT_BACK), 0, centerY + CENTER_HEIGHT - LEVEL_HEIGHT + 1, 0, chunkBox);
					BlockRotation rotation = getRotationFromDirection(getFacing());
					BlockPos placementPos = pos.add(new BlockPos(-2, CENTER_HEIGHT - 12, -2).rotate(rotation));
					StructureTemplate template = world.toServerWorld().getStructureTemplateManager().getTemplate(OXYGEN_SOURCE).get();
					StructurePlacementData placementData = new StructurePlacementData().setRotation(rotation);
					template.place(world, placementPos, placementPos, placementData, random, Block.NOTIFY_LISTENERS);
				}
				else if(type == 1)
				{
					// Airlock
					BlockRotation rotation = getRotationFromDirection(Direction.fromVector(pos.getX() - center.getX(), 0, pos.getZ() - center.getZ()));
					pos = pos.subtract(new BlockPos(5, 5, 3).rotate(rotation));
					StructureTemplate template = world.toServerWorld().getStructureTemplateManager().getTemplate(AIRLOCK).get();
					StructurePlacementData placementdata = new StructurePlacementData().setRotation(rotation);
					template.place(world, pos, pos, placementdata, random, Block.NOTIFY_LISTENERS);
				}
				else if(type == 2)
				{
					// Oxygen Source
					/*Direction direction = Direction.fromVector(pos.getX() - center.getX(), 0, pos.getZ() - center.getZ());
					BlockRotation rotation = getRotationFromDirection(direction);
					BlockPos placementPos = pos.subtract(new BlockPos(5, 18, 4).rotate(rotation));
					StructureTemplate template = world.toServerWorld().getStructureTemplateManager().getTemplate(OXYGEN_SOURCE_UPPER).get();
					StructurePlacementData placementData = new StructurePlacementData().setRotation(rotation);
					template.place(world, placementPos, placementPos, placementData, random, Block.NOTIFY_LISTENERS);
					placementPos = pos.subtract(new BlockPos(5, 30, 4).rotate(rotation));
					template = world.toServerWorld().getStructureTemplateManager().getTemplate(OXYGEN_SOURCE_LOWER).get();
					template.place(world, placementPos, placementPos, placementData, random, Block.NOTIFY_LISTENERS);*/
					
				}
				else if(type == 3)
				{
					// Gardens
					int y = chunkCenterPos.getY() + 1;
					fillWithOutline(world, chunkBox, -5, y, -5, 5, y, -5, getBrickStairsType(serverWorld, Direction.NORTH), AIR, false);
					fillWithOutline(world, chunkBox, -5, y, 5, 5, y, 5, getBrickStairsType(serverWorld, Direction.SOUTH), AIR, false);
					fillWithOutline(world, chunkBox, 5, y, -5, 5, y, 5, getBrickStairsType(serverWorld, Direction.WEST), AIR, false);
					fillWithOutline(world, chunkBox, -5, y, -5, -5, y, 5, getBrickStairsType(serverWorld, Direction.EAST), AIR, false);
					fillWithOutline(world, chunkBox, -5, y, -5, -5, y + 1, -5, getBrickType(serverWorld), AIR, false);
					fillWithOutline(world, chunkBox, 5, y, -5, 5, y + 1, -5, getBrickType(serverWorld), AIR, false);
					fillWithOutline(world, chunkBox, 5, y, 5, 5, y + 1, 5, getBrickType(serverWorld), AIR, false);
					fillWithOutline(world, chunkBox, -5, y, 5, -5, y + 1, 5, getBrickType(serverWorld), AIR, false);
					fillWithOutline(world, chunkBox, -4, y, -4, 4, y, 4, Blocks.DIRT.getDefaultState(), AIR, false);
					
					int gardenType = random.nextInt(2);
					
					if(gardenType == 0)
					{
						fillWithOutline(world, chunkBox, -4, y, -4, -4, y, 4, Blocks.PACKED_ICE.getDefaultState(), AIR, false);
						fillWithOutline(world, chunkBox, 0, y, -4, 0, y, 4, Blocks.PACKED_ICE.getDefaultState(), AIR, false);
						fillWithOutline(world, chunkBox, 4, y, -4, 4, y, 4, Blocks.PACKED_ICE.getDefaultState(), AIR, false);
					}
					else if(gardenType == 1)
					{
						int treeCount = 1 + random.nextInt(3);
						
						for(int i = 0; i < treeCount; i++)
						{
							int x = random.nextInt(5) - random.nextInt(5);
							int z = random.nextInt(5) - random.nextInt(5);
							fillWithOutline(world, chunkBox, x, y + 1, z, x, y + 4 + random.nextInt(4), z, Blocks.STRIPPED_OAK_LOG.getDefaultState(), AIR, false);
						}
					}
				}
				else if(type == 4)
				{
					// Underground Rooms
					int y = pos.getY();
					fillWithOutline(world, chunkBox, -4, y + 1, -4, 4, y + 5, -4, StarflightBlocks.RIVETED_ALUMINUM.getDefaultState(), AIR, false);
					fillWithOutline(world, chunkBox, -4, y + 1, 4, 4, y + 5, 4, StarflightBlocks.RIVETED_ALUMINUM.getDefaultState(), AIR, false);
					fillWithOutline(world, chunkBox, -4, y + 1, -4, -4, y + 5, 4, StarflightBlocks.RIVETED_ALUMINUM.getDefaultState(), AIR, false);
					fillWithOutline(world, chunkBox, 4, y + 1, -4, 4, y + 5, 4, StarflightBlocks.RIVETED_ALUMINUM.getDefaultState(), AIR, false);
				}
			}
			
			postPlacement(world, random, new BlockBox(chunkBox.getMinX(), chunkCenterPos.getY() - SHELL_RADIUS, chunkBox.getMinZ(), chunkBox.getMaxX(), chunkCenterPos.getY() + SHELL_RADIUS, chunkBox.getMaxZ()));
		}
		
		private void addDoor(StructureWorldAccess world, BlockBox chunkBox, int x, int y, int z)
		{
			fillWithOutline(world, chunkBox, x - 1, y, z, x + 1, y + 1, z, StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState(), StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState(), false);
			fillWithOutline(world, chunkBox, x - 1, y + 2, z, x + 1, y + 2, z, StarflightBlocks.RIVETED_ALUMINUM.getDefaultState(), StarflightBlocks.RIVETED_ALUMINUM.getDefaultState(), false);
			addBlock(world, StarflightBlocks.AIRLOCK_DOOR.getDefaultState(), x, y, z, chunkBox);
			addBlock(world, StarflightBlocks.AIRLOCK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER), x, y + 1, z, chunkBox);
			addBlock(world, StarflightBlocks.LEVER_BLOCK.getDefaultState().with(SolidLeverBlock.FACING, Direction.NORTH), x - 1, y + 1, z, chunkBox);
		}
		
		private void addDirectionalDoor(StructureWorldAccess world, BlockBox chunkBox, int x, int y, int z, Direction direction)
		{
			addBlock(world, StarflightBlocks.AIRLOCK_DOOR.getDefaultState().with(DoorBlock.FACING, direction), x, y, z, chunkBox);
			addBlock(world, StarflightBlocks.AIRLOCK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, direction), x, y + 1, z, chunkBox);
		}
		
		private void addLoot(StructureWorldAccess world, BlockBox chunkBox, int x, int y, int z)
		{
			Direction direction = world.getRandom().nextInt(4) == 0 ? Direction.UP : Direction.fromHorizontal(world.getRandom().nextInt(4));
			addBlock(world, StarflightBlocks.STORAGE_CUBE.getDefaultState().with(StorageCubeBlock.FACING, direction), x, y, z, chunkBox);
		}
		
		private void addMob(StructureWorldAccess world, Random random, int x, int y, int z)
		{
			BlockPos.Mutable blockPos = this.offsetPos(x, y, z);
			
			if(world.getBlockState(blockPos).isAir() && !world.getBlockState(((BlockPos) blockPos).down()).isAir())
			{
				AncientHumanoidEntity entity = new AncientHumanoidEntity(StarflightEntities.ANCIENT_HUMANOID, world.toServerWorld());
				entity.initEquipment(random, world.getLocalDifficulty(blockPos));
				entity.setPosition(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
				world.spawnEntity(entity);
			}
		}
		
		private BlockRotation getRotationFromDirection(Direction direction)
		{
			switch(direction)
			{
			case NORTH:
				return BlockRotation.NONE;
			case EAST:
				return BlockRotation.CLOCKWISE_90;
			case SOUTH:
				return BlockRotation.CLOCKWISE_180;
			case WEST:
				return BlockRotation.COUNTERCLOCKWISE_90;
			default:
				return BlockRotation.NONE;
			}
		}
	}
}