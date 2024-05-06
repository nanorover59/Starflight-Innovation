package space.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
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
import net.minecraft.world.World;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import space.StarflightMod;
import space.block.SolidLeverBlock;
import space.block.StarflightBlocks;

public class BioDomeGenerator
{
	private static final int SHELL_RADIUS = 48;
	private static final int CENTER_RADIUS = 6;
	private static final int CENTER_HEIGHT = 24;
	private static final int LEVEL_HEIGHT = 6;
	private static final Block BRICKS = Blocks.STONE_BRICKS;
	private static final Block BRICK_STAIRS = Blocks.STONE_BRICK_STAIRS;
	private static final Block SLAB = StarflightBlocks.STRUCTURAL_ALUMINUM_SLAB;
	private static final Identifier LOOT_TABLE = new Identifier(StarflightMod.MOD_ID, "chests/moonshaft");
	private static final Identifier AIRLOCK = new Identifier(StarflightMod.MOD_ID, "sliding_airlock");
	private static final Identifier OXYGEN_SOURCE = new Identifier(StarflightMod.MOD_ID, "biodome_oxygen_source");
	
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
	
	private static BlockState getBrickType(World world)
	{
		if(world.getDimensionKey().getValue().getPath().equals("mars"))
			return StarflightBlocks.REDSLATE_BRICKS.getDefaultState();
		
		return Blocks.STONE_BRICKS.getDefaultState();
	}
	
	private static BlockState getBrickStairsType(World world, Direction direction)
	{
		if(world.getDimensionKey().getValue().getPath().equals("mars"))
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
			BlockPos airlockPos = null;
			Direction airlockDirection = null;
			BlockPos oxygenSourcePos = null;
			Direction oxygenSourceDirection = null;
			BlockPos gardenPos = null;
			Direction gardenDirection = null;
			boolean spiral = false;
			
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
							
							world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
							
							if(y == 0 && (dx == 0 || dz == 0))
							{
								airlockPos = new BlockPos(startPos.getX() + x, startPos.getY() + y, startPos.getZ() + z);
								airlockDirection = Direction.fromVector(dx, 0, dz);
							}
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
							}
						}
						
						if(state != null)
							world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
						
						if(pos.equals(center))
							spiral = true;
						
						// Spawn grid structures.
						if(y == 0 && dx % 8 == 0 && dz % 8 == 0)
						{
							int cellX = dx / 8;
							int cellZ = dz / 8;
							
							if(((getFacing() == Direction.NORTH || getFacing() == Direction.SOUTH) && (cellX == -2 || cellX == 2) && cellZ == 0) || (getFacing() == Direction.EAST || getFacing() == Direction.WEST) && (cellZ == -2 || cellZ == 2) && cellX == 0)
							{
								oxygenSourcePos = new BlockPos(startPos.getX() + x, startPos.getY() + y, startPos.getZ() + z);
								oxygenSourceDirection = Direction.fromVector(dx, 0, dz);
							}
							
							if((cellX == -2 || cellX == 2) && (cellZ == -2 || cellZ == 2))
							{
								gardenPos = new BlockPos(startPos.getX() + x, startPos.getY() + y, startPos.getZ() + z);
								gardenDirection = Direction.random(random);
							}
						}
					}
				}
			}
			
			if(spiral)
			{
				fill(world, chunkBox, -3, chunkCenterPos.getY() - SHELL_RADIUS + 3, -3, -3, chunkCenterPos.getY() + CENTER_HEIGHT - 1, 3);
				fill(world, chunkBox, 3, chunkCenterPos.getY() - SHELL_RADIUS + 3, -3, 3, chunkCenterPos.getY() + CENTER_HEIGHT - 1, 3);
				fill(world, chunkBox, -3, chunkCenterPos.getY() - SHELL_RADIUS + 3, -3, 3, chunkCenterPos.getY() + CENTER_HEIGHT - 1, -3);
				fill(world, chunkBox, -3, chunkCenterPos.getY() - SHELL_RADIUS + 3, 3, 3, chunkCenterPos.getY() + CENTER_HEIGHT - 1, 3);
				fillWithOutline(world, chunkBox, -2, chunkCenterPos.getY() - SHELL_RADIUS + 3, -2, 2, chunkCenterPos.getY() - 1, 2, Blocks.GLASS.getDefaultState(), AIR, false);
				fillWithOutline(world, chunkBox, -2, chunkCenterPos.getY() - SHELL_RADIUS + 3, -2, -2, chunkCenterPos.getY() - 1, -2, StarflightBlocks.TITANIUM_PANELS.getDefaultState(), AIR, false);
				fillWithOutline(world, chunkBox, 2, chunkCenterPos.getY() - SHELL_RADIUS + 3, -2, 2, chunkCenterPos.getY() - 1, -2, StarflightBlocks.TITANIUM_PANELS.getDefaultState(), AIR, false);
				fillWithOutline(world, chunkBox, 2, chunkCenterPos.getY() - SHELL_RADIUS + 3, 2, 2, chunkCenterPos.getY() - 1, 2, StarflightBlocks.TITANIUM_PANELS.getDefaultState(), AIR, false);
				fillWithOutline(world, chunkBox, -2, chunkCenterPos.getY() - SHELL_RADIUS + 3, 2, -2, chunkCenterPos.getY() - 1, 2, StarflightBlocks.TITANIUM_PANELS.getDefaultState(), AIR, false);
				fillWithOutline(world, chunkBox, -1, chunkCenterPos.getY() - SHELL_RADIUS + 3, -1, 1, chunkCenterPos.getY() - 1, 1, Blocks.WATER.getDefaultState(), AIR, false);
				
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
				
				placeDoor(world, chunkBox, 0, centerY + 1, -6);
				this.addBlock(world, StarflightBlocks.PLANETARIUM.getDefaultState().mirror(BlockMirror.FRONT_BACK), 0, centerY + CENTER_HEIGHT - LEVEL_HEIGHT + 1, 0, chunkBox);
			}
			
			if(airlockPos != null)
			{
				BlockRotation rotation = getRotationFromDirection(airlockDirection);
				airlockPos = airlockPos.subtract(new BlockPos(5, 5, 9).rotate(rotation));
				StructureTemplate template = world.toServerWorld().getStructureTemplateManager().getTemplate(AIRLOCK).get();
				StructurePlacementData placementdata = new StructurePlacementData().setRotation(rotation);
				template.place(world, airlockPos, airlockPos, placementdata, random, Block.NOTIFY_LISTENERS);
			}
			
			if(oxygenSourcePos != null)
			{
				BlockRotation rotation = getRotationFromDirection(oxygenSourceDirection);
				oxygenSourcePos = oxygenSourcePos.subtract(new BlockPos(5, 18, 4).rotate(rotation));
				StructureTemplate template = world.toServerWorld().getStructureTemplateManager().getTemplate(OXYGEN_SOURCE).get();
				StructurePlacementData placementdata = new StructurePlacementData().setRotation(rotation);
				template.place(world, oxygenSourcePos, oxygenSourcePos, placementdata, random, Block.NOTIFY_LISTENERS);
			}
			
			if(gardenPos != null)
			{
				ServerWorld serverWorld = world.toServerWorld();
				BlockState bricks = getBrickType(serverWorld);
				int y = chunkCenterPos.getY() + 1;
				fillWithOutline(world, chunkBox, -5, y, -5, 5, y, -5, getBrickStairsType(serverWorld, Direction.NORTH), AIR, false);
				fillWithOutline(world, chunkBox, -5, y, 5, 5, y, 5, getBrickStairsType(serverWorld, Direction.SOUTH), AIR, false);
				fillWithOutline(world, chunkBox, 5, y, -5, 5, y, 5, getBrickStairsType(serverWorld, Direction.WEST), AIR, false);
				fillWithOutline(world, chunkBox, -5, y, -5, -5, y, 5, getBrickStairsType(serverWorld, Direction.EAST), AIR, false);
				fillWithOutline(world, chunkBox, -5, y, -5, -5, y + 1, -5, bricks, AIR, false);
				fillWithOutline(world, chunkBox, 5, y, -5, 5, y + 1, -5, bricks, AIR, false);
				fillWithOutline(world, chunkBox, 5, y, 5, 5, y + 1, 5, bricks, AIR, false);
				fillWithOutline(world, chunkBox, -5, y, 5, -5, y + 1, 5, bricks, AIR, false);
				fillWithOutline(world, chunkBox, -4, y, -4, 4, y, 4, Blocks.DIRT.getDefaultState(), AIR, false);
				
				int type = random.nextInt(2);
				
				if(type == 0)
				{
					fillWithOutline(world, chunkBox, -4, y, -4, -4, y, 4, Blocks.PACKED_ICE.getDefaultState(), AIR, false);
					fillWithOutline(world, chunkBox, 0, y, -4, 0, y, 4, Blocks.PACKED_ICE.getDefaultState(), AIR, false);
					fillWithOutline(world, chunkBox, 4, y, -4, 4, y, 4, Blocks.PACKED_ICE.getDefaultState(), AIR, false);
				}
				else if(type == 1)
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
			
			postPlacement(world, random, new BlockBox(chunkBox.getMinX(), chunkCenterPos.getY() - SHELL_RADIUS, chunkBox.getMinZ(), chunkBox.getMaxX(), chunkCenterPos.getY() + SHELL_RADIUS, chunkBox.getMaxZ()));
		}
		
		public void placeDoor(StructureWorldAccess world, BlockBox chunkBox, int x, int y, int z)
		{
			this.fillWithOutline(world, chunkBox, x - 1, y, z, x + 1, y + 1, z, StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState(), StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState(), false);
			this.fillWithOutline(world, chunkBox, x - 1, y + 2, z, x + 1, y + 2, z, StarflightBlocks.RIVETED_ALUMINUM.getDefaultState(), StarflightBlocks.RIVETED_ALUMINUM.getDefaultState(), false);
			this.addBlock(world, StarflightBlocks.AIRLOCK_DOOR.getDefaultState(), x, y, z, chunkBox);
			this.addBlock(world, StarflightBlocks.AIRLOCK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER), x, y + 1, z, chunkBox);
			this.addBlock(world, StarflightBlocks.LEVER_BLOCK.getDefaultState().with(SolidLeverBlock.FACING, Direction.NORTH), x - 1, y + 1, z, chunkBox);
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