package space.world;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.enums.RailShape;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.tag.BiomeTags;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import space.StarflightMod;
import space.block.StarflightBlocks;
import space.block.StorageCubeBlock;
import space.entity.AncientHumanoidEntity;
import space.entity.StarflightEntities;

public class MoonshaftGenerator
{
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Block SOLID_BLOCK = StarflightBlocks.RIVETED_ALUMINUM;
	private static final Block FRAME_BLOCK = StarflightBlocks.ALUMINUM_FRAME;
	private static final Block SLAB = StarflightBlocks.STRUCTURAL_ALUMINUM_SLAB;
	private static final Identifier LOOT_TABLE = new Identifier(StarflightMod.MOD_ID, "chests/moonshaft");
	
	private static MoonshaftGenerator.MoonshaftPart pickPiece(StructurePiecesHolder holder, Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength, boolean allowCrossing)
	{
		int i = random.nextInt(100);
		
		if(allowCrossing && i >= 95)
		{
			BlockBox blockBox = MoonshaftCrossing.getBoundingBox(holder, random, x, y, z, orientation);
			
			if(blockBox != null)
				return new MoonshaftCrossing(chainLength, blockBox, orientation, random.nextBoolean());
		}
		else
		{
			BlockBox blockBox = MoonshaftCorridor.getBoundingBox(holder, random, x, y, z, orientation);
			
			if(blockBox != null)
				return new MoonshaftCorridor(chainLength, random, blockBox, orientation);
		}
		
		return null;
	}
	
	private static BlockState getBrickType(World world)
	{
		if(world.getDimensionKey().getValue().getPath().equals("mars"))
			return StarflightBlocks.REDSLATE_BRICKS.getDefaultState();
		
		return Blocks.STONE_BRICKS.getDefaultState();
	}

	static MoonshaftPart pieceGenerator(StructurePiece start, StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength, boolean allowCrossing)
	{
		if(chainLength > 8 || Math.abs(x - start.getBoundingBox().getMinX()) > 80 || Math.abs(z - start.getBoundingBox().getMinZ()) > 80)
			return null;
		
		MoonshaftPart moonshaftPart = MoonshaftGenerator.pickPiece(holder, random, x, y, z, orientation, chainLength + 1, allowCrossing);
		
		if(moonshaftPart != null)
		{
			holder.addPiece(moonshaftPart);
			moonshaftPart.fillOpenings(start, holder, random);
		}
		
		return moonshaftPart;
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
			Block block = this.getBlockAt(world, x, y, z, box).getBlock();
			return !block.equals(SOLID_BLOCK) && !block.equals(FRAME_BLOCK);
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
			int i = Math.max(this.boundingBox.getMinX() - 1, box.getMinX());
			int j = Math.max(this.boundingBox.getMinY() - 1, box.getMinY());
			int k = Math.max(this.boundingBox.getMinZ() - 1, box.getMinZ());
			int l = Math.min(this.boundingBox.getMaxX() + 1, box.getMaxX());
			int m = Math.min(this.boundingBox.getMaxY() + 1, box.getMaxY());
			int n = Math.min(this.boundingBox.getMaxZ() + 1, box.getMaxZ());
			BlockPos.Mutable mutable = new BlockPos.Mutable((i + l) / 2, (j + m) / 2, (k + n) / 2);

			if(world.getBiome(mutable).isIn(BiomeTags.MINESHAFT_BLOCKING))
				return true;

			for(int o = i; o <= l; o++)
			{
				for(int p = k; p <= n; p++)
				{
					if(world.getBlockState(mutable.set(o, j, p)).getMaterial().isLiquid())
						return true;

					if(!world.getBlockState(mutable.set(o, m, p)).getMaterial().isLiquid())
						continue;

					return true;
				}
			}

			for(int o = i; o <= l; o++)
			{
				for(int p = j; p <= m; p++)
				{
					if(world.getBlockState(mutable.set(o, p, k)).getMaterial().isLiquid())
						return true;

					if(!world.getBlockState(mutable.set(o, p, n)).getMaterial().isLiquid())
						continue;
					
					return true;
				}
			}

			for(int o = k; o <= n; o++)
			{
				for(int p = j; p <= m; p++)
				{
					if(world.getBlockState(mutable.set(i, p, o)).getMaterial().isLiquid())
						return true;

					if(!world.getBlockState(mutable.set(l, p, o)).getMaterial().isLiquid())
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
			this.hasRails = random.nextBoolean();
			this.length = this.getFacing().getAxis() == Direction.Axis.Z ? boundingBox.getBlockCountZ() / 5 : boundingBox.getBlockCountX() / 5;
		}

		@Nullable
		public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation)
		{
			int i = random.nextInt(3) + 2;
			
			while(i > 0)
			{
				int j = i * 5;
				
				BlockBox blockBox = switch(orientation)
				{
					default -> new BlockBox(0, 0, -(j - 1), 2, 2, 0);
					case SOUTH -> new BlockBox(0, 0, 0, 2, 2, j - 1);
					case WEST -> new BlockBox(-(j - 1), 0, 0, 0, 2, 2);
					case EAST -> new BlockBox(0, 0, 0, j - 1, 2, 2);
				};
				
				blockBox = blockBox.offset(x, y, z);
				
				if(holder.getIntersecting(blockBox) == null)
					return blockBox;
				
				i--;
			}
			
			return null;
		}

		@Override
		public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random)
		{
			int i = this.getChainLength();
			int j = random.nextInt(4);
			Direction direction = this.getFacing();
			
			if(direction != null)
			{
				switch(direction)
				{
					default:
					{
						if(j <= 1)
						{
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, direction, i, true);
							break;
						}
						if(j == 2)
						{
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), Direction.WEST, i, true);
							break;
						}
						MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), Direction.EAST, i, true);
						break;
					}
					case SOUTH:
					{
						if(j <= 1)
						{
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, direction, i, true);
							break;
						}
						if(j == 2)
						{
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() - 3, Direction.WEST, i, true);
							break;
						}
						MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() - 3, Direction.EAST, i, true);
						break;
					}
					case WEST:
					{
						if(j <= 1)
						{
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), direction, i, true);
							break;
						}
						if(j == 2)
						{
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i, true);
							break;
						}
						MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i, true);
						break;
					}
					case EAST:
					{
						if(j <= 1)
						{
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), direction, i, true);
							break;
						}
						if(j == 2)
						{
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() - 3, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i, true);
							break;
						}
						MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() - 3, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i, true);
					}
				}
			}

			if(i >= 8)
				return;

			if(direction == Direction.NORTH || direction == Direction.SOUTH)
			{
				int k = this.boundingBox.getMinZ() + 7;
				
				while(k + 7 <= this.boundingBox.getMaxZ())
				{
					int l = random.nextInt(5);
					
					if(l == 0)
						MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), k, Direction.WEST, i + 1, true);
					else if(l == 1)
						MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), k, Direction.EAST, i + 1, true);
					
					k += 7;
				}
			}
			else
			{
				int k = this.boundingBox.getMinX() + 7;
				
				while(k + 7 <= this.boundingBox.getMaxX())
				{
					int l = random.nextInt(5);
					
					if(l == 0)
						MoonshaftGenerator.pieceGenerator(start, holder, random, k, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i + 1, true);
					else if(l == 1)
						MoonshaftGenerator.pieceGenerator(start, holder, random, k, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i + 1, true);
					
					k += 7;
				}
			}
		}

		@Override
		protected boolean addChest(StructureWorldAccess world, BlockBox boundingBox, Random random, int x, int y, int z, Identifier lootTableId)
		{
			BlockPos.Mutable blockPos = this.offsetPos(x, y, z);
			
			if(boundingBox.contains(blockPos) && world.getBlockState(blockPos).isAir() && !world.getBlockState(((BlockPos) blockPos).down()).isAir())
			{
				BlockState blockState = (BlockState) Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, random.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
				this.addBlock(world, blockState, x, y, z, boundingBox);
				ChestMinecartEntity chestMinecartEntity = new ChestMinecartEntity(world.toServerWorld(), (double) blockPos.getX() + 0.5, (double) blockPos.getY() + 0.5, (double) blockPos.getZ() + 0.5);
				chestMinecartEntity.setLootTable(lootTableId, random.nextLong());
				world.spawnEntity(chestMinecartEntity);
				return true;
			}
			
			return false;
		}
		
		private void addMob(StructureWorldAccess world, int x, int y, int z)
		{
			BlockPos.Mutable blockPos = this.offsetPos(x, y, z);
			
			if(world.getBlockState(blockPos).isAir() && !world.getBlockState(((BlockPos) blockPos).down()).isAir())
			{
				AncientHumanoidEntity entity = new AncientHumanoidEntity(StarflightEntities.ANCIENT_HUMANOID, world.toServerWorld());
				entity.initEquipment();
				entity.setPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());
				world.spawnEntity(entity);
			}
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			if(this.cannotGenerate(world, chunkBox))
				return;
			
			BlockState brickState = getBrickType(world.toServerWorld());
			int maxZ = this.length * 5 - 1;
			int i;
			boolean b = true;
			this.fillWithOutline(world, chunkBox, 0, 0, 0, 2, 2, maxZ, AIR, AIR, false);
			this.fillWithOutline(world, chunkBox, 0, -1, 0, 2, -1, maxZ, brickState, brickState, false);
			this.fillWithOutlineUnderSeaLevel(world, chunkBox, random, 0.8f, 0, 2, 0, 2, 2, maxZ, AIR, AIR, false, false);
			
			for(i = 0; i < this.length; i++)
			{
				int j = 2 + i * 5;
				
				if(b)
					this.generateSupports(world, chunkBox, 0, 0, j, 2, 2, random);
				
				b = !b;
				
				if(random.nextInt(100) == 0)
					this.addChest(world, chunkBox, random, 2, 0, j - 1, LOOT_TABLE);
				
				if(random.nextInt(100) == 0)
					this.addChest(world, chunkBox, random, 0, 0, j + 1, LOOT_TABLE);
				
				if(random.nextInt(80) == 0)
					this.addMob(world, 0, 0, j);
				
				int q = j - 1 + random.nextInt(3);
				BlockPos.Mutable blockPos = this.offsetPos(1, 0, q);
				
				if(!chunkBox.contains(blockPos) || !this.isUnderSeaLevel(world, 1, 0, q, chunkBox))
					continue;
			}
			
			for(i = 0; i <= 2; i++)
			{
				for(int j = 0; j <= i; j++)
					this.tryPlaceFloor(world, chunkBox, SOLID_BLOCK.getDefaultState(), i, -1, j);
			}
			
			this.fillSupportBeam(world, chunkBox, 0, -1, 2);
			
			if(this.length > 1)
				this.fillSupportBeam(world, chunkBox, 0, -1, maxZ - 2);
			
			if(this.hasRails)
			{
				BlockState blockState2 = (BlockState) Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, RailShape.NORTH_SOUTH);
				
				for(int j = 0; j <= i; j++)
				{
					BlockState blockState3 = this.getBlockAt(world, 1, -1, j, chunkBox);
					
					if(blockState3.isAir() || !blockState3.isOpaqueFullCube(world, this.offsetPos(1, -1, j)))
						continue;
					
					float f = this.isUnderSeaLevel(world, 1, 0, j, chunkBox) ? 0.7f : 0.9f;
					this.addBlockWithRandomThreshold(world, chunkBox, random, f, 1, 0, j, blockState2);
				}
			}
		}

		private void fillSupportBeam(StructureWorldAccess world, BlockBox box, int x, int y, int z)
		{
			if(this.getBlockAt(world, x, y, z, box).getMaterial().blocksMovement())
				this.fillSupportBeam(world, FRAME_BLOCK.getDefaultState(), x, y, z, box);
			
			if(this.getBlockAt(world, x + 2, y, z, box).getMaterial().blocksMovement())
				this.fillSupportBeam(world, FRAME_BLOCK.getDefaultState(), x + 2, y, z, box);
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

		protected void fillSupportBeam(StructureWorldAccess world, BlockState state, int x, int y, int z, BlockBox box)
		{
			BlockPos.Mutable mutable = this.offsetPos(x, y, z);
			
			if(!box.contains(mutable))
				return;
			
			int i = mutable.getY();
			int j = 1;
			boolean bl = true;
			boolean bl2 = true;
			
			while(bl || bl2)
			{
				boolean bl3;
				BlockState blockState;
				
				if(bl)
				{
					mutable.setY(i - j);
					blockState = world.getBlockState(mutable);
					bl3 = this.canReplace(blockState) && !blockState.isOf(Blocks.LAVA);
					
					if(!bl3 && this.isUpsideSolidFullSquare(world, mutable, blockState))
					{
						MoonshaftCorridor.fillColumn(world, state, mutable, i - j + 1, i);
						return;
					}
					
					bl = j <= 20 && bl3 && mutable.getY() > world.getBottomY() + 1;
				}
				
				if(bl2)
				{
					mutable.setY(i + j);
					blockState = world.getBlockState(mutable);
					bl3 = this.canReplace(blockState);
					
					if(!bl3 && this.sideCoversSmallSquare(world, mutable, blockState))
					{
						MoonshaftCorridor.fillColumn(world, Blocks.CHAIN.getDefaultState(), mutable, i + 1, i + j);
						return;
					}
					
					bl2 = j <= 50 && bl3 && mutable.getY() < world.getTopY() - 1;
				}
				
				j++;
			}
		}

		private static void fillColumn(StructureWorldAccess world, BlockState state, BlockPos.Mutable pos, int startY, int endY)
		{
			for(int i = startY; i < endY; i++)
				world.setBlockState(pos.setY(i), state, Block.NOTIFY_LISTENERS);
		}

		private boolean isUpsideSolidFullSquare(WorldView world, BlockPos pos, BlockState state)
		{
			return state.isSideSolidFullSquare(world, pos, Direction.UP);
		}

		private boolean sideCoversSmallSquare(WorldView world, BlockPos pos, BlockState state)
		{
			return Block.sideCoversSmallSquare(world, pos, Direction.DOWN) && !(state.getBlock() instanceof FallingBlock);
		}

		private void generateSupports(StructureWorldAccess world, BlockBox boundingBox, int minX, int minY, int z, int maxY, int maxX, Random random)
		{
			if(!this.isSolidCeiling(world, boundingBox, minX, maxX, maxY, z))
				return;
			
			this.fillWithOutline(world, boundingBox, minX, minY - 1, z, maxX, minY - 1, z, SOLID_BLOCK.getDefaultState(), AIR, true);
			this.fillWithOutline(world, boundingBox, minX, maxY + 1, z, maxX, maxY + 1, z, SOLID_BLOCK.getDefaultState(), AIR, true);
			this.fillWithOutline(world, boundingBox, minX - 1, minY, z, minX - 1, maxY, z, SOLID_BLOCK.getDefaultState(), AIR, true);
			this.fillWithOutline(world, boundingBox, maxX + 1, minY, z, maxX + 1, maxY, z, SOLID_BLOCK.getDefaultState(), AIR, true);
		}
	}

	public static class MoonshaftCrossing extends MoonshaftPart
	{
		private final Direction direction;
		private final boolean twoFloors;
		private final boolean lootRoom;

		public MoonshaftCrossing(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.MOONSHAFT_CROSSING, nbt);
			this.twoFloors = nbt.getBoolean("twoFloors");
			this.lootRoom = nbt.getBoolean("lootRoom");
			this.direction = Direction.fromHorizontal(nbt.getInt("direction"));
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			super.writeNbt(context, nbt);
			nbt.putBoolean("twoFloors", this.twoFloors);
			nbt.putBoolean("lootRoom", this.lootRoom);
			nbt.putInt("direction", this.direction.getHorizontal());
		}

		public MoonshaftCrossing(int chainLength, BlockBox boundingBox, @Nullable Direction orientation, boolean lootRoom)
		{
			super(StarflightWorldGeneration.MOONSHAFT_CROSSING, chainLength, boundingBox);
			this.direction = orientation;
			this.twoFloors = boundingBox.getBlockCountY() > 5;
			this.lootRoom = lootRoom;
		}

		@Nullable
		public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation)
		{
			int i = random.nextBoolean() ? (4 * random.nextBetween(1, 5)) : 4;
			
			BlockBox blockBox = switch(orientation)
			{
				default -> new BlockBox(-1, 0, -4, 3, i, 0);
				case SOUTH -> new BlockBox(-1, 0, 0, 3, i, 4);
				case WEST -> new BlockBox(-4, 0, -1, 0, i, 3);
				case EAST -> new BlockBox(0, 0, -1, 4, i, 3);
			};
			
			blockBox = blockBox.offset(x, y, z);
			
			if(holder.getIntersecting(blockBox) != null)
				return null;
			
			return blockBox;
		}

		@Override
		public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random)
		{
			int i = this.getChainLength();
			
			switch(this.direction)
			{
				default:
				{
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i, false);
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, Direction.WEST, i, false);
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, Direction.EAST, i, false);
					break;
				}
				case SOUTH:
				{
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i, false);
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, Direction.WEST, i, false);
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, Direction.EAST, i, false);
					break;
				}
				case WEST:
				{
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i, false);
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i, false);
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, Direction.WEST, i, false);
					break;
				}
				case EAST:
				{
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i, false);
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i, false);
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, Direction.EAST, i, false);
				}
			}
			
			if(this.twoFloors && !this.lootRoom)
			{
				int height = boundingBox.getBlockCountY();
				MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY() + height - 3, this.boundingBox.getMinZ() - 1, Direction.NORTH, i, false);
				MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + height - 3, this.boundingBox.getMinZ() + 1, Direction.WEST, i, false);
				MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + height - 3, this.boundingBox.getMinZ() + 1, Direction.EAST, i, false);
				MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY() + height - 3, this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i, false);
			}
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			if(this.cannotGenerate(world, chunkBox))
				return;
			
			BlockState brickState = getBrickType(world.toServerWorld());
			
			if(this.lootRoom)
			{
				this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() - 3, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 3, this.boundingBox.getMaxX() + 3, this.boundingBox.getMaxY(), this.boundingBox.getMaxZ() + 3, AIR, AIR, false);
				this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() - 3, this.boundingBox.getMinY() - 1, this.boundingBox.getMinZ() - 3, this.boundingBox.getMaxX() + 3, this.boundingBox.getMinY() - 1, this.boundingBox.getMaxZ() + 3, brickState, brickState, false);
				
				for(int i = this.boundingBox.getMinX() - 3; i <= this.boundingBox.getMaxX() + 3; i += 2)
				{
					BlockPos blockPos = new BlockPos(i, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 3);
					
					if(random.nextBoolean() && !world.getBlockState(blockPos).getMaterial().blocksMovement() && world.getBlockState(blockPos.add(0, 0, -1)).getMaterial().blocksMovement())
					{
						world.setBlockState(blockPos, StarflightBlocks.STORAGE_CUBE.getDefaultState().with(StorageCubeBlock.FACING, Direction.UP), Block.NOTIFY_LISTENERS);
						LootableContainerBlockEntity.setLootTable(world, random, blockPos, LOOT_TABLE);
					}
					
					blockPos = new BlockPos(i, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 3);
					
					if(random.nextBoolean() && !world.getBlockState(blockPos).getMaterial().blocksMovement() && world.getBlockState(blockPos.add(0, 0, 1)).getMaterial().blocksMovement())
					{
						world.setBlockState(blockPos, StarflightBlocks.STORAGE_CUBE.getDefaultState().with(StorageCubeBlock.FACING, Direction.UP), Block.NOTIFY_LISTENERS);
						LootableContainerBlockEntity.setLootTable(world, random, blockPos, LOOT_TABLE);
					}
				}
				
				for(int i = this.boundingBox.getMinZ() - 3; i <= this.boundingBox.getMaxZ() + 3; i += 2)
				{
					BlockPos blockPos = new BlockPos(this.boundingBox.getMinX() - 3, this.boundingBox.getMinY(), i);
					
					if(random.nextBoolean() && !world.getBlockState(blockPos).getMaterial().blocksMovement() && world.getBlockState(blockPos.add(-1, 0, 0)).getMaterial().blocksMovement())
					{
						world.setBlockState(blockPos, StarflightBlocks.STORAGE_CUBE.getDefaultState().with(StorageCubeBlock.FACING, Direction.UP), Block.NOTIFY_LISTENERS);
						LootableContainerBlockEntity.setLootTable(world, random, blockPos, LOOT_TABLE);
					}
					
					blockPos = new BlockPos(this.boundingBox.getMaxX() + 3, this.boundingBox.getMinY(), i);
					
					if(random.nextBoolean() && !world.getBlockState(blockPos).getMaterial().blocksMovement() && world.getBlockState(blockPos.add(1, 0, 0)).getMaterial().blocksMovement())
					{
						world.setBlockState(blockPos, StarflightBlocks.STORAGE_CUBE.getDefaultState().with(StorageCubeBlock.FACING, Direction.UP), Block.NOTIFY_LISTENERS);
						LootableContainerBlockEntity.setLootTable(world, random, blockPos, LOOT_TABLE);
					}
				}
				
				return;
			}
			else
			{
				this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMinZ(), this.boundingBox.getMaxX(), this.boundingBox.getMaxY(), this.boundingBox.getMaxZ(), AIR, AIR, false);
				this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX(), this.boundingBox.getMinY() - 1, this.boundingBox.getMinZ(), this.boundingBox.getMaxX(), this.boundingBox.getMinY() - 1, this.boundingBox.getMaxZ(), brickState, brickState, false);
			}
			
			if(this.twoFloors)
			{
				// Spiral Staircase
				for(int j = this.boundingBox.getMinX(); j <= this.boundingBox.getMaxX(); j++)
				{
					for(int k = this.boundingBox.getMinZ(); k <= this.boundingBox.getMaxZ(); k++)
						this.tryPlaceFloor(world, chunkBox, StarflightBlocks.WALKWAY.getDefaultState(), j, this.boundingBox.getMaxY() - 3, k);
				}

				this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, this.boundingBox.getMaxX() - 1, this.boundingBox.getMaxY(), this.boundingBox.getMaxZ() - 1, AIR, AIR, false);

				BlockState bottomSlab = SLAB.getDefaultState();
				BlockState topSlab = SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP);
				Direction spiralDirection = this.direction;
				BlockPos spiralOffset = new BlockPos(1, 0, 1);

				for(int i = 0; i <= spiralDirection.getHorizontal(); i++)
					spiralOffset = spiralOffset.rotate(BlockRotation.CLOCKWISE_90);

				BlockPos spiralPos = new BlockPos(this.boundingBox.getMinX() + 2 - spiralOffset.getX(), this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 2 - spiralOffset.getZ());
				this.fillWithOutline(world, boundingBox, this.boundingBox.getMinX() + 2, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 2, this.boundingBox.getMinX() + 2, this.boundingBox.getMaxY(), this.boundingBox.getMinZ() + 2, FRAME_BLOCK.getDefaultState(), AIR, false);
				
				while(spiralPos.getY() < this.boundingBox.getMaxY() - 2)
				{
					world.setBlockState(spiralPos, bottomSlab, 0);
					world.setBlockState(spiralPos.offset(spiralDirection, 1), topSlab, 0);
					spiralPos = spiralPos.up().offset(spiralDirection, 2);
					spiralDirection = spiralDirection.rotateYClockwise();
				}
			}
			else
			{
				this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, this.boundingBox.getMaxY());
				this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() - 1, this.boundingBox.getMaxY());
				this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMaxX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, this.boundingBox.getMaxY());
				this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMaxX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() - 1, this.boundingBox.getMaxY());
			}
		}

		private void generateCrossingPillar(StructureWorldAccess world, BlockBox boundingBox, int x, int minY, int z, int maxY)
		{
			if(!this.getBlockAt(world, x, maxY + 1, z, boundingBox).isAir())
				this.fillWithOutline(world, boundingBox, x, minY, z, x, maxY, z, FRAME_BLOCK.getDefaultState(), AIR, false);
		}
	}

	public static class MoonshaftRoom extends MoonshaftPart
	{
		private final List<BlockBox> entrances = Lists.newLinkedList();

		public MoonshaftRoom(int chainLength, Random random, int x, int z)
		{
			super(StarflightWorldGeneration.MOONSHAFT_ROOM, chainLength, new BlockBox(x, 50, z, x + 9, 60, z + 9));
		}

		public MoonshaftRoom(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.MOONSHAFT_ROOM, nbt);
			BlockBox.CODEC.listOf().parse(NbtOps.INSTANCE, nbt.getList("entrances", NbtElement.INT_ARRAY_TYPE)).resultOrPartial(LOGGER::error).ifPresent(this.entrances::addAll);
		}
		
		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			super.writeNbt(context, nbt);
			BlockBox.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.entrances).resultOrPartial(LOGGER::error).ifPresent(nbtElement -> nbt.put("entrances", (NbtElement) nbtElement));
		}
		
		@Override
		public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random)
		{
			BlockBox blockBox;
			MoonshaftPart mineshaftPart;
			int k;
			int i = this.getChainLength();
			int j = this.boundingBox.getBlockCountY() - 2;
			
			if(j <= 0)
				j = 1;
			
			for(k = 0; k < this.boundingBox.getBlockCountX() && (k += random.nextInt(this.boundingBox.getBlockCountX())) + 3 <= this.boundingBox.getBlockCountX(); k += 4)
			{
				mineshaftPart = MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + k, this.boundingBox.getMinY() + random.nextInt(j) + 1, this.boundingBox.getMinZ() - 1, Direction.NORTH, i, true);
				
				if(mineshaftPart == null)
					continue;
				
				blockBox = mineshaftPart.getBoundingBox();
				this.entrances.add(new BlockBox(blockBox.getMinX(), blockBox.getMinY(), this.boundingBox.getMinZ(), blockBox.getMaxX(), blockBox.getMaxY(), this.boundingBox.getMinZ() + 1));
			}
			
			for(k = 0; k < this.boundingBox.getBlockCountX() && (k += random.nextInt(this.boundingBox.getBlockCountX())) + 3 <= this.boundingBox.getBlockCountX(); k += 4)
			{
				mineshaftPart = MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + k, this.boundingBox.getMinY() + random.nextInt(j) + 1, this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i, true);
				
				if(mineshaftPart == null)
					continue;
				
				blockBox = mineshaftPart.getBoundingBox();
				this.entrances.add(new BlockBox(blockBox.getMinX(), blockBox.getMinY(), this.boundingBox.getMaxZ() - 1, blockBox.getMaxX(), blockBox.getMaxY(), this.boundingBox.getMaxZ()));
			}
			
			for(k = 0; k < this.boundingBox.getBlockCountZ() && (k += random.nextInt(this.boundingBox.getBlockCountZ())) + 3 <= this.boundingBox.getBlockCountZ(); k += 4)
			{
				mineshaftPart = MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + random.nextInt(j) + 1, this.boundingBox.getMinZ() + k, Direction.WEST, i, true);
				
				if(mineshaftPart == null)
					continue;
				
				blockBox = mineshaftPart.getBoundingBox();
				this.entrances.add(new BlockBox(this.boundingBox.getMinX(), blockBox.getMinY(), blockBox.getMinZ(), this.boundingBox.getMinX() + 1, blockBox.getMaxY(), blockBox.getMaxZ()));
			}
			
			for(k = 0; k < this.boundingBox.getBlockCountZ() && (k += random.nextInt(this.boundingBox.getBlockCountZ())) + 3 <= this.boundingBox.getBlockCountZ(); k += 4)
			{
				MoonshaftPart structurePiece = MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + random.nextInt(j) + 1, this.boundingBox.getMinZ() + k, Direction.EAST, i, true);
				
				if(structurePiece == null)
					continue;
				
				blockBox = structurePiece.getBoundingBox();
				this.entrances.add(new BlockBox(this.boundingBox.getMaxX() - 1, blockBox.getMinY(), blockBox.getMinZ(), this.boundingBox.getMaxX(), blockBox.getMaxY(), blockBox.getMaxZ()));
			}
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			if(this.cannotGenerate(world, chunkBox))
				return;
			
			this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX(), this.boundingBox.getMinY() + 1, this.boundingBox.getMinZ(), this.boundingBox.getMaxX(), Math.min(this.boundingBox.getMinY() + 3, this.boundingBox.getMaxY()), this.boundingBox.getMaxZ(), AIR, AIR, false);
			
			for(BlockBox blockBox : this.entrances)
				this.fillWithOutline(world, chunkBox, blockBox.getMinX(), blockBox.getMaxY() - 2, blockBox.getMinZ(), blockBox.getMaxX(), blockBox.getMaxY(), blockBox.getMaxZ(), AIR, AIR, false);
			
			this.fillHalfEllipsoid(world, chunkBox, this.boundingBox.getMinX(), this.boundingBox.getMinY() + 4, this.boundingBox.getMinZ(), this.boundingBox.getMaxX(), this.boundingBox.getMaxY(), this.boundingBox.getMaxZ(), AIR, false);
		}

		@Override
		public void translate(int x, int y, int z)
		{
			super.translate(x, y, z);
			
			for(BlockBox blockBox : this.entrances)
				blockBox = blockBox.offset(x, y, z);
		}
	}
}