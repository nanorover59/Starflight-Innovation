package space.world;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BulbBlock;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import space.StarflightMod;
import space.block.PointerColumnBlock;
import space.block.StarflightBlocks;
import space.block.StorageCubeBlock;
import space.entity.AncientHumanoidEntity;
import space.entity.StarflightEntities;

public class MoonshaftGenerator
{
	private static final Block SOLID_BLOCK = StarflightBlocks.STRUCTURAL_IRON;
	private static final Block FRAME_BLOCK = StarflightBlocks.IRON_FRAME;
	private static final Block SLAB = StarflightBlocks.STRUCTURAL_IRON_SLAB;
	private static final RegistryKey<LootTable> LOOT_TABLE = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(StarflightMod.MOD_ID, "chests/moonshaft"));
	
	public static MoonshaftGenerator.MoonshaftPart pickCorridorPiece(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength)
	{
		BlockBox blockBox = MoonshaftCorridor.getBoundingBox(holder, random, x, y, z, orientation);
		
		if(blockBox != null)
			return new MoonshaftCorridor(chainLength, random, blockBox, orientation);
		
		return null;
	}
	
	public static MoonshaftGenerator.MoonshaftPart pickCrossingPiece(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength)
	{
		int up = 0;
		int down = 0;
		
		for(int i = 0; i < 2; i++)
		{
			if(random.nextInt(4) == 0)
				up++;
			else
				break;
		}

		for(int i = 0; i < 8; i++)
		{
			if(random.nextInt(4) == 0)
				down++;
			else
				break;
		}
		
		BlockBox blockBox = MoonshaftCrossing.getBoundingBox(holder, random, x, y, z, up, down);
		
		if(blockBox == null)
		{
			up = 0;
			down = 0;
			blockBox = MoonshaftCrossing.getBoundingBox(holder, random, x, y, z, 0, 0);
		}

		if(blockBox != null)
			return new MoonshaftCrossing(chainLength, blockBox, up, down, orientation);

		return null;
	}

	public static MoonshaftPart pieceGenerator(StructurePiece start, StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int type, int chainLength)
	{
		if(chainLength > 8 || Math.abs(x - start.getBoundingBox().getMinX()) > 128 || Math.abs(z - start.getBoundingBox().getMinZ()) > 128)
			return null;
		
		MoonshaftPart moonshaftPart = null;
		
		switch(type)
		{
			case 0:
				moonshaftPart = MoonshaftGenerator.pickCorridorPiece(holder, random, x, y, z, orientation, chainLength + 1);
				break;
			case 1:
				moonshaftPart = MoonshaftGenerator.pickCrossingPiece(holder, random, x, y, z, orientation, chainLength + 1);
				break;
			default:
				break;
		}
		
		if(moonshaftPart != null)
		{
			holder.addPiece(moonshaftPart);
			moonshaftPart.fillOpenings(start, holder, random);
		}
		
		return moonshaftPart;
	}
	
	private static BlockState getBrickType(World world)
	{
		if(world.getDimensionEntry().getIdAsString().contains("mars"))
			return StarflightBlocks.REDSLATE_BRICKS.getDefaultState();
		
		return Blocks.STONE_BRICKS.getDefaultState();
	}
	
	static abstract class MoonshaftPart extends StructurePiece
	{
		public MoonshaftPart(StructurePieceType structurePieceType, int chainLength, BlockBox box)
		{
			super(structurePieceType, chainLength, box);
		}

		public MoonshaftPart(StructurePieceType structurePieceType, NbtCompound nbtCompound)
		{
			super(structurePieceType, nbtCompound);
		}

		@Override
		protected boolean canAddBlock(WorldView world, int x, int y, int z, BlockBox box)
		{
			return true;
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
		}

		protected boolean isSolidCeiling(BlockView world, BlockBox boundingBox, int minX, int maxX, int y, int z)
		{
			for(int i = minX; i <= maxX; i++)
			{
				if(!this.getBlockAt(world, i, y + 1, z, boundingBox).isAir())
					continue;
				
				return false;
			}
			
			return true;
		}

		protected boolean cannotGenerate(WorldAccess world, BlockBox box)
		{
			if(this.getChainLength() == 0)
				return false;
			
			int i = Math.max(this.boundingBox.getMinX(), box.getMinX());
			int j = Math.max(this.boundingBox.getMinY(), box.getMinY());
			int k = Math.max(this.boundingBox.getMinZ(), box.getMinZ());
			int l = Math.min(this.boundingBox.getMaxX(), box.getMaxX());
			int m = Math.min(this.boundingBox.getMaxY(), box.getMaxY());
			int n = Math.min(this.boundingBox.getMaxZ(), box.getMaxZ());
			BlockPos.Mutable mutable = new BlockPos.Mutable((i + l) / 2, (j + m) / 2, (k + n) / 2);

			if(world.getTopY(Heightmap.Type.OCEAN_FLOOR, this.boundingBox.getCenter().getX(), this.boundingBox.getCenter().getZ()) < this.boundingBox.getMinY())
				return true;

			for(int o = i; o <= l; o++)
			{
				for(int p = k; p <= n; p++)
				{
					BlockState minState = world.getBlockState(mutable.set(o, j, p));
					BlockState maxState = world.getBlockState(mutable.set(o, m, p));
					
					if(minState.isLiquid())
						return true;

					if(!maxState.isLiquid())
						continue;

					return true;
				}
			}

			for(int o = i; o <= l; o++)
			{
				for(int p = j; p <= m; p++)
				{
					BlockState minState = world.getBlockState(mutable.set(o, p, k));
					BlockState maxState = world.getBlockState(mutable.set(o, p, n));
					
					if(minState.isLiquid())
						return true;

					if(!maxState.isLiquid())
						continue;

					return true;
				}
			}

			for(int o = k; o <= n; o++)
			{
				for(int p = j; p <= m; p++)
				{
					BlockState minState = world.getBlockState(mutable.set(i, p, o));
					BlockState maxState = world.getBlockState(mutable.set(l, p, o));
					
					if(minState.isLiquid())
						return true;

					if(!maxState.isLiquid())
						continue;

					return true;
				}
			}

			return false;
		}

		protected void tryPlaceFloor(StructureWorldAccess world, BlockBox box, BlockState state, int x, int y, int z)
		{
			if(!this.isUnderSeaLevel(world, x, y, z, box))
				return;
			
			BlockPos.Mutable blockPos = this.offsetPos(x, y, z);
			BlockState blockState = world.getBlockState(blockPos);
			
			if(!blockState.isSideSolidFullSquare(world, blockPos, Direction.UP))
				world.setBlockState(blockPos, state, Block.NOTIFY_LISTENERS);
		}
    }
	
	public static class MoonshaftCorridor extends MoonshaftPart
	{
		private final boolean hasRails;
		private final int length;

		public MoonshaftCorridor(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.MOONSHAFT_CORRIDOR, nbt);
			this.hasRails = nbt.getBoolean("hasRails");
			this.length = nbt.getInt("length");
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			super.writeNbt(context, nbt);
			nbt.putBoolean("hasRails", this.hasRails);
			nbt.putInt("length", this.length);
		}

		public MoonshaftCorridor(int chainLength, Random random, BlockBox boundingBox, Direction orientation)
		{
			super(StarflightWorldGeneration.MOONSHAFT_CORRIDOR, chainLength, boundingBox);
			this.setOrientation(orientation);
			this.length = this.getFacing().getAxis() == Direction.Axis.Z ? boundingBox.getBlockCountZ() / 3 : boundingBox.getBlockCountX() / 3;
			this.hasRails = false;
		}

		@Nullable
		public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation)
		{
			int i = (2 + random.nextInt(5)) * 5;
			
			if(random.nextInt(4) == 0)
				i *= 2;
			
			while(i > 0)
			{
				BlockBox blockBox = switch(orientation)
				{
					default -> new BlockBox(0, 0, -i, 2, 2, -1);
					case SOUTH -> new BlockBox(0, 0, 1, 2, 2, i);
					case WEST -> new BlockBox(-i, 0, 0, -1, 2, 2);
					case EAST -> new BlockBox(1, 0, 0, i, 2, 2);
				};
				
				blockBox = blockBox.offset(x, y, z);
				
				if(holder.getIntersecting(blockBox.expand(2)) == null)
					return blockBox;
				
				i--;
			}
			
			return null;
		}

		@Override
		public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random)
		{
			int i = this.getChainLength();
			int offset = 2;
			Direction direction = this.getFacing();
			
			if(direction != null)
			{
				switch(direction)
				{
					default:
					{
						if(i < 3 || random.nextBoolean())
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMinZ() - offset, direction, 1, i);
						
						break;
					}
					case SOUTH:
					{
						if(i < 3 || random.nextBoolean())
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + offset, direction, 1, i);

						break;
					}
					case WEST:
					{
						if(i < 3 || random.nextBoolean())
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - offset, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), direction, 1, i);
						
						break;
					}
					case EAST:
					{
						if(i < 3 || random.nextBoolean())
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + offset, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), direction, 1, i);
						
						break;
					}
				}
			}
		}

		@Override
		protected boolean addChest(StructureWorldAccess world, BlockBox boundingBox, Random random, int x, int y, int z, RegistryKey<LootTable> lootTable)
		{
			BlockPos.Mutable blockPos = this.offsetPos(x, y, z);
			
			if(boundingBox.contains(blockPos) && world.getBlockState(blockPos).isAir() && !world.getBlockState(((BlockPos) blockPos).down()).isAir())
			{
				world.setBlockState(blockPos, StarflightBlocks.STORAGE_CUBE.getDefaultState().with(StorageCubeBlock.FACING, Direction.UP), Block.NOTIFY_LISTENERS);
				LootableInventory.setLootTable(world, random, blockPos, lootTable);
				return true;
			}
			
			return false;
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

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			if(this.cannotGenerate(world, chunkBox))
				return;
			
			int minZ = -1;
			int maxZ = Math.max(this.boundingBox.getDimensions().getX(), this.boundingBox.getDimensions().getZ());
			boolean light = false;
			BlockState brickState = getBrickType(world.toServerWorld());
			chunkBox = chunkBox.expand(2);
			this.fill(world, chunkBox, -1, 0, minZ, 3, 2, maxZ);
			
			if(this.getBlockAt(world, 1, -1, minZ, chunkBox).getBlock() == StarflightBlocks.BUFFER)
				minZ++;
			
			this.fillWithOutline(world, chunkBox, -1, -1, minZ, 3, -1, maxZ, brickState, brickState, false);

			for(int z = 0; z < maxZ; z++)
			{
				if(z > 0 && z % 5 == 0)
				{
					this.generateSupports(world, chunkBox, 0, 0, z, 2, 2, light, random);
					light = !light;
					int upY = 0;

					while(this.getBlockAt(world, -1, upY, z, chunkBox).isAir() && upY < 64)
						upY++;

					if(upY < 64)
					{
						int y = 0;

						while(this.getBlockAt(world, -1, y, z, chunkBox).isAir() && y < 64)
						{
							this.addBlock(world, FRAME_BLOCK.getDefaultState(), -1, y, z, chunkBox);
							y++;
						}
					}
					else
					{
						int y = -2;

						while(this.getBlockAt(world, -1, y, z, chunkBox).isAir() && y > -64)
						{
							this.addBlock(world, FRAME_BLOCK.getDefaultState(), -1, y, z, chunkBox);
							y--;
						}
					}

					upY = 0;

					while(this.getBlockAt(world, 3, upY, z, chunkBox).isAir() && upY < 64)
						upY++;

					if(upY < 64)
					{
						int y = 0;

						while(this.getBlockAt(world, 3, y, z, chunkBox).isAir() && y < 64)
						{
							this.addBlock(world, FRAME_BLOCK.getDefaultState(), 3, y, z, chunkBox);
							y++;
						}
					}
					else
					{
						int y = -2;

						while(this.getBlockAt(world, 3, y, z, chunkBox).isAir() && y > -64)
						{
							this.addBlock(world, FRAME_BLOCK.getDefaultState(), 3, y, z, chunkBox);
							y--;
						}
					}

					if(this.getBlockAt(world, -1, -1, z, chunkBox).isAir())
						this.addBlock(world, SOLID_BLOCK.getDefaultState(), -1, -1, z, chunkBox);

					if(this.getBlockAt(world, 3, -1, z, chunkBox).isAir())
						this.addBlock(world, SOLID_BLOCK.getDefaultState(), 3, -1, z, chunkBox);
				}

				if(random.nextInt(120) == 0)
					this.addChest(world, chunkBox, random, -1, 0, z, LOOT_TABLE);

				if(random.nextInt(120) == 0)
					this.addChest(world, chunkBox, random, 3, 0, z, LOOT_TABLE);

				if(random.nextInt(120) == 0)
					this.addMob(world, random, 0, 0, z);
			}
		}

		@Override
		protected void fillDownwards(StructureWorldAccess world, BlockState state, int x, int y, int z, BlockBox box)
		{
			BlockPos.Mutable mutable = this.offsetPos(x, y, z);
			
			if(!box.contains(mutable))
				return;
			
			int i = mutable.getY();
			
			while(this.canReplace(world.getBlockState(mutable)) && mutable.getY() > world.getBottomY() + 1)
				mutable.move(Direction.DOWN);
			
			if(!this.isUpsideSolidFullSquare(world, mutable, world.getBlockState(mutable)))
				return;
			
			while(mutable.getY() < i)
			{
				mutable.move(Direction.UP);
				world.setBlockState(mutable, state, Block.NOTIFY_LISTENERS);
			}
		}

		private boolean isUpsideSolidFullSquare(WorldView world, BlockPos pos, BlockState state)
		{
			return state.isSideSolidFullSquare(world, pos, Direction.UP);
		}

		private void generateSupports(StructureWorldAccess world, BlockBox boundingBox, int minX, int minY, int z, int maxY, int maxX, boolean light, Random random)
		{
			this.fillWithOutline(world, boundingBox, minX - 1, minY - 1, z, maxX + 1, minY - 1, z, SOLID_BLOCK.getDefaultState(), AIR, false);
			this.fillWithOutline(world, boundingBox, minX - 1, maxY + 1, z, maxX + 1, maxY + 1, z, SOLID_BLOCK.getDefaultState(), AIR, true);
			
			if(light)
			{
				this.addBlock(world, StarflightBlocks.IRON_BULB.getDefaultState().with(BulbBlock.LIT, true), minX - 1, minY - 1, z, boundingBox);
				this.addBlock(world, StarflightBlocks.IRON_BULB.getDefaultState().with(BulbBlock.LIT, true), maxX + 1, minY - 1, z, boundingBox);
			}
		}
	}

	public static class MoonshaftCrossing extends MoonshaftPart
	{
		private final Direction direction;
		private final int up;
		private final int down;

		public MoonshaftCrossing(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.MOONSHAFT_CROSSING, nbt);
			this.direction = Direction.fromHorizontal(nbt.getInt("direction"));
			this.up = nbt.getInt("up");
			this.down = nbt.getInt("down");
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			super.writeNbt(context, nbt);
			nbt.putInt("direction", this.direction.getHorizontal());
			nbt.putInt("up", up);
			nbt.putInt("down", down);
		}

		public MoonshaftCrossing(int chainLength, BlockBox boundingBox, int up, int down, Direction orientation)
		{
			super(StarflightWorldGeneration.MOONSHAFT_CROSSING, chainLength, boundingBox);
			this.direction = orientation;
			this.up = up;
			this.down = down;
		}

		@Nullable
		public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x, int y, int z, int up, int down)
		{
			BlockBox box = new BlockBox(0, -down * 6, 0, 2, 3 + up * 6, 2).offset(x, y, z);
			
			if(holder.getIntersecting(box.offset(-1, 0, -1)) == null)
				return box;
			
			return null;
		}

		@Override
		public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random)
		{
			int i = this.getChainLength();
			
			for(int j = 0; j < up + down + 1; j++)
			{
				randomCorridor(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY() + j * 6, this.boundingBox.getMinZ() - 2, Direction.NORTH, i);
				randomCorridor(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY() + j * 6, this.boundingBox.getMaxZ() + 2, Direction.SOUTH, i);
				randomCorridor(start, holder, random, this.boundingBox.getMinX() - 2, this.boundingBox.getMinY() + j * 6, this.boundingBox.getMinZ(), Direction.WEST, i);
				randomCorridor(start, holder, random, this.boundingBox.getMaxX() + 2, this.boundingBox.getMinY() + j * 6, this.boundingBox.getMinZ(), Direction.EAST, i);
				
				if(i == 0 && j >= down / 2)
					break;
			}
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			if(this.cannotGenerate(world, chunkBox))
				return;
			
			BlockState brickState = getBrickType(world.toServerWorld());
			chunkBox = chunkBox.expand(2);
			this.fill(world, chunkBox, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxX() + 1, this.boundingBox.getMaxY(), this.boundingBox.getMaxZ() + 1);
			this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() - 1, this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() - 1, this.boundingBox.getMaxZ() + 1, brickState, brickState, false);
			int elevatorY = 0;
			
			if(up + down > 2)
			{
				// Elevator
				if(this.chainLength == 0)
				{
					this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() - 2, this.boundingBox.getMinY() - 2, this.boundingBox.getMinZ() - 2, this.boundingBox.getMaxX() + 2, this.boundingBox.getMaxY() - 6, this.boundingBox.getMaxZ() + 2, StarflightBlocks.STRUCTURAL_IRON.getDefaultState(), AIR, false);
					this.fill(world, chunkBox, this.boundingBox.getMinX() - 2, this.boundingBox.getMaxY() - 10, this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxX() + 2, this.boundingBox.getMaxY() - 7, this.boundingBox.getMaxZ() + 1);
					this.fill(world, chunkBox, this.boundingBox.getMinX() - 1, this.boundingBox.getMaxY() - 10, this.boundingBox.getMinZ() - 2, this.boundingBox.getMaxX() + 1, this.boundingBox.getMaxY() - 7, this.boundingBox.getMaxZ() + 2);
				}
				else
				{
					this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() - 2, this.boundingBox.getMinY() - 2, this.boundingBox.getMinZ() - 2, this.boundingBox.getMaxX() + 2, this.boundingBox.getMaxY(), this.boundingBox.getMaxZ() + 2, StarflightBlocks.STRUCTURAL_IRON.getDefaultState(), AIR, false);
					this.fill(world, chunkBox, this.boundingBox.getMinX() - 2, this.boundingBox.getMaxY() - (up * 6) - 4, this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxX() + 2, this.boundingBox.getMaxY() - (up * 6) - 1, this.boundingBox.getMaxZ() + 1);
					this.fill(world, chunkBox, this.boundingBox.getMinX() - 1, this.boundingBox.getMaxY() - (up * 6) - 4, this.boundingBox.getMinZ() - 2, this.boundingBox.getMaxX() + 1, this.boundingBox.getMaxY() - (up * 6) - 1, this.boundingBox.getMaxZ() + 2);
				}
				
				BlockPos trackOffset = new BlockPos(-3, 0, 0);
				BlockPos xpOffset = new BlockPos(3, 0, 0);
				BlockPos zpOffset = new BlockPos(0, 0, 3);
				BlockPos zmOffset = new BlockPos(0, 0, -3);
				
				for(int j = 0; j <= this.direction.getHorizontal(); j++)
				{
					trackOffset = trackOffset.rotate(BlockRotation.CLOCKWISE_90);
					xpOffset = xpOffset.rotate(BlockRotation.CLOCKWISE_90);
					zpOffset = zpOffset.rotate(BlockRotation.CLOCKWISE_90);
					zmOffset = zmOffset.rotate(BlockRotation.CLOCKWISE_90);
				}
				
				for(int i = 0; i < ((up + down + 1) * 6) - 3; i++)
				{
					int y = this.boundingBox.getMinY() + i - 1;
					boolean exit = this.getChainLength() > 0 || i < (down + 1) * 3 || i >= (up + down) * 6;
					
					if(i % 6 == 0 && exit)
					{
						for(int j = -2;  j < 3; j++)
						{
							world.setBlockState(new BlockPos(this.boundingBox.getMinX() + j + 1, y, this.boundingBox.getMinZ() + 4), StarflightBlocks.BUFFER.getDefaultState().with(FacingBlock.FACING, Direction.NORTH), Block.NOTIFY_LISTENERS);
							world.setBlockState(new BlockPos(this.boundingBox.getMinX() + j + 1, y, this.boundingBox.getMinZ() - 2), StarflightBlocks.BUFFER.getDefaultState().with(FacingBlock.FACING, Direction.SOUTH), Block.NOTIFY_LISTENERS);
						}
						
						for(int j = -2;  j < 3; j++)
						{
							world.setBlockState(new BlockPos(this.boundingBox.getMinX() + 4, y, this.boundingBox.getMinZ() + j + 1), StarflightBlocks.BUFFER.getDefaultState().with(FacingBlock.FACING, Direction.WEST), Block.NOTIFY_LISTENERS);
							world.setBlockState(new BlockPos(this.boundingBox.getMinX() - 2, y, this.boundingBox.getMinZ() + j + 1), StarflightBlocks.BUFFER.getDefaultState().with(FacingBlock.FACING, Direction.EAST), Block.NOTIFY_LISTENERS);
						}
					}
					
					BlockPos trackPos = new BlockPos(this.boundingBox.getMinX() + trackOffset.getX() + 1, y, this.boundingBox.getMinZ() + trackOffset.getZ() + 1);
					BlockPos xpPos = new BlockPos(this.boundingBox.getMinX() + xpOffset.getX() + 1, y, this.boundingBox.getMinZ() + xpOffset.getZ() + 1);
					BlockPos zpPos = new BlockPos(this.boundingBox.getMinX() + zpOffset.getX() + 1, y, this.boundingBox.getMinZ() + zpOffset.getZ() + 1);
					BlockPos zmPos = new BlockPos(this.boundingBox.getMinX() + zmOffset.getX() + 1, y, this.boundingBox.getMinZ() + zmOffset.getZ() + 1);
					
					if((i - 2) % 6 == 0 && exit)
					{
						world.setBlockState(trackPos, StarflightBlocks.CALL_TRACK.getDefaultState(), Block.NOTIFY_LISTENERS);
						world.setBlockState(trackPos.offset(this.direction.rotateYClockwise()), StarflightBlocks.POINTER_COLUMN.getDefaultState().with(PointerColumnBlock.FACING, this.direction.rotateYCounterclockwise()), Block.NOTIFY_LISTENERS);
						world.setBlockState(trackPos.offset(this.direction.rotateYCounterclockwise()), StarflightBlocks.POINTER_COLUMN.getDefaultState().with(PointerColumnBlock.FACING, this.direction.rotateYClockwise()), Block.NOTIFY_LISTENERS);
						world.setBlockState(trackPos.offset(this.direction.rotateYClockwise()).down(), StarflightBlocks.STRUCTURAL_IRON.getDefaultState(), Block.NOTIFY_LISTENERS);
						world.setBlockState(trackPos.offset(this.direction.rotateYCounterclockwise()).down(), StarflightBlocks.STRUCTURAL_IRON.getDefaultState(), Block.NOTIFY_LISTENERS);
						
						if(i < (down + 1) * 3)
						{
							world.setBlockState(xpPos, StarflightBlocks.IRON_BULB.getDefaultState().with(BulbBlock.LIT, true), Block.NOTIFY_LISTENERS);
							world.setBlockState(zpPos, StarflightBlocks.IRON_BULB.getDefaultState().with(BulbBlock.LIT, true), Block.NOTIFY_LISTENERS);
							world.setBlockState(zmPos, StarflightBlocks.IRON_BULB.getDefaultState().with(BulbBlock.LIT, true), Block.NOTIFY_LISTENERS);
						}
						
						elevatorY = y;
					}
					else
						world.setBlockState(trackPos, StarflightBlocks.LINEAR_TRACK.getDefaultState(), Block.NOTIFY_LISTENERS);
				}
				
				this.fill(world, chunkBox, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() - 1, this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() - 1, this.boundingBox.getMaxZ() + 1);
				this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() - 2, this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() - 2, this.boundingBox.getMaxZ() + 1, StarflightBlocks.BUFFER.getDefaultState(), StarflightBlocks.BUFFER.getDefaultState(), false);
				this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() - 1, elevatorY - 2, this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxX() + 1, elevatorY - 2, this.boundingBox.getMaxZ() + 1, StarflightBlocks.STRUCTURAL_IRON.getDefaultState(), StarflightBlocks.STRUCTURAL_IRON.getDefaultState(), false);
				BlockPos trackPos = new BlockPos(this.boundingBox.getMinX() + trackOffset.getX() + 1, elevatorY, this.boundingBox.getMinZ() + trackOffset.getZ() + 1);
				world.setBlockState(trackPos.offset(this.direction), StarflightBlocks.LINEAR_ACTUATOR.getDefaultState().with(FacingBlock.FACING, this.direction), Block.NOTIFY_LISTENERS);
				world.setBlockState(trackPos.offset(this.direction).down(), StarflightBlocks.STRUCTURAL_IRON.getDefaultState(), Block.NOTIFY_LISTENERS);
			}
			else if(up > 0 || down > 0)
			{
				// Spiral Staircase
				for(int i = 1; i <= up + down; i++)
				{
					for(int j = this.boundingBox.getMinX() - 1; j <= this.boundingBox.getMaxX() + 1; j++)
					{
						for(int k = this.boundingBox.getMinZ() - 1; k <= this.boundingBox.getMaxZ() + 1; k++)
							this.tryPlaceFloor(world, chunkBox, SOLID_BLOCK.getDefaultState(), j, this.boundingBox.getMinY() + i * 6 - 1, k);
					}
				}

				this.fill(world, chunkBox, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMinZ(), this.boundingBox.getMaxX(), this.boundingBox.getMaxY(), this.boundingBox.getMaxZ());
				BlockState bottomSlab = SLAB.getDefaultState();
				BlockState topSlab = SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP);
				Direction spiralDirection = this.direction;
				BlockPos spiralOffset = new BlockPos(1, 0, 1);

				for(int i = 0; i <= spiralDirection.getHorizontal(); i++)
					spiralOffset = spiralOffset.rotate(BlockRotation.CLOCKWISE_90);

				BlockPos spiralPos = new BlockPos(this.boundingBox.getMinX() + 1 - spiralOffset.getX(), this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1 - spiralOffset.getZ());
				this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, this.boundingBox.getMinX() + 1, this.boundingBox.getMaxY(), this.boundingBox.getMinZ() + 1, FRAME_BLOCK.getDefaultState(), AIR, false);
				
				while(spiralPos.getY() < this.boundingBox.getMaxY() - 3)
				{
					world.setBlockState(spiralPos, bottomSlab, Block.NOTIFY_LISTENERS);
					world.setBlockState(spiralPos.offset(spiralDirection, 1), topSlab, Block.NOTIFY_LISTENERS);
					spiralPos = spiralPos.up().offset(spiralDirection, 2);
					spiralDirection = spiralDirection.rotateYClockwise();
				}
			}
			else
			{
				// Single Level
				this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxY());
				this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, this.boundingBox.getMaxY());
				this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxY());
				this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, this.boundingBox.getMaxY());
				world.setBlockState(new BlockPos(this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() - 1, this.boundingBox.getMinZ() - 1), StarflightBlocks.IRON_BULB.getDefaultState().with(BulbBlock.LIT, true), Block.NOTIFY_LISTENERS);
				world.setBlockState(new BlockPos(this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() - 1, this.boundingBox.getMinZ() - 1), StarflightBlocks.IRON_BULB.getDefaultState().with(BulbBlock.LIT, true), Block.NOTIFY_LISTENERS);
				world.setBlockState(new BlockPos(this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() - 1, this.boundingBox.getMaxZ() + 1), StarflightBlocks.IRON_BULB.getDefaultState().with(BulbBlock.LIT, true), Block.NOTIFY_LISTENERS);
				world.setBlockState(new BlockPos(this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() - 1, this.boundingBox.getMaxZ() + 1), StarflightBlocks.IRON_BULB.getDefaultState().with(BulbBlock.LIT, true), Block.NOTIFY_LISTENERS);
			}
		}
		
		private void randomCorridor(StructurePiece start, StructurePiecesHolder holder, Random random, int x, int y, int z, Direction direction, int chainLength)
		{
			if(direction != this.direction.getOpposite() && random.nextBoolean())
				MoonshaftGenerator.pieceGenerator(start, holder, random, x, y, z, direction, 0, chainLength);
		}

		private void generateCrossingPillar(StructureWorldAccess world, BlockBox boundingBox, int x, int minY, int z, int maxY)
		{
			if(!this.getBlockAt(world, x, maxY + 1, z, boundingBox).isAir())
				this.fillWithOutline(world, boundingBox, x, minY, z, x, maxY, z, FRAME_BLOCK.getDefaultState(), AIR, false);
		}
	}
}