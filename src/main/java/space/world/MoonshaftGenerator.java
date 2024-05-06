package space.world;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BiomeTags;
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
import space.block.SimpleFacingBlock;
import space.block.StarflightBlocks;
import space.block.StorageCubeBlock;
import space.entity.AncientHumanoidEntity;
import space.entity.StarflightEntities;

public class MoonshaftGenerator
{
	private static final Block SOLID_BLOCK = StarflightBlocks.RIVETED_ALUMINUM;
	private static final Block FRAME_BLOCK = StarflightBlocks.ALUMINUM_FRAME;
	private static final Block SLAB = StarflightBlocks.STRUCTURAL_ALUMINUM_SLAB;
	private static final Identifier LOOT_TABLE = new Identifier(StarflightMod.MOD_ID, "chests/moonshaft");
	private static final Identifier SOLAR_ROOF = new Identifier(StarflightMod.MOD_ID, "moonshaft_solar_roof");
	private static final Identifier EXTRACTOR_SHELF = new Identifier(StarflightMod.MOD_ID, "extractor_shelf");
	
	public static MoonshaftGenerator.MoonshaftPart pickPiece(StructurePiecesHolder holder, Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength, boolean allowCrossing)
	{
		int i = random.nextInt(5);
		
		if(allowCrossing && (i >= 3 || chainLength == 2))
		{
			int up = 0;
			int down = 0;
			
			if(random.nextBoolean())
			{
				up += random.nextInt(4);
				down = random.nextInt(4);
			}
			
			BlockBox blockBox = MoonshaftCrossing.getBoundingBox(holder, random, x, y, z, up, down, orientation);
			
			if(blockBox != null)
				return new MoonshaftCrossing(chainLength, blockBox, up, down, orientation);
		}
		else
		{
			BlockBox blockBox = MoonshaftCorridor.getBoundingBox(holder, random, x, y, z, orientation);
			
			if(blockBox != null)
				return new MoonshaftCorridor(chainLength, random, blockBox, orientation);
		}
		
		return null;
	}

	public static MoonshaftPart pieceGenerator(StructurePiece start, StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength, boolean allowCrossing)
	{
		if(chainLength > 8 || Math.abs(x - start.getBoundingBox().getMinX()) > 128 || Math.abs(z - start.getBoundingBox().getMinZ()) > 128)
			return null;
		
		MoonshaftPart moonshaftPart = MoonshaftGenerator.pickPiece(holder, random, x, y, z, orientation, chainLength + 1, allowCrossing);
		
		if(moonshaftPart != null)
		{
			holder.addPiece(moonshaftPart);
			moonshaftPart.fillOpenings(start, holder, random);
		}
		
		return moonshaftPart;
	}
	
	private static BlockState getBrickType(World world)
	{
		if(world.getDimensionKey().getValue().getPath().equals("mars"))
			return StarflightBlocks.REDSLATE_BRICKS.getDefaultState();
		
		return Blocks.STONE_BRICKS.getDefaultState();
	}
	
	private static BlockState getBrickSlabType(World world)
	{
		if(world.getDimensionKey().getValue().getPath().equals("mars"))
			return StarflightBlocks.REDSLATE_BRICK_SLAB.getDefaultState();
		
		return Blocks.STONE_BRICK_SLAB.getDefaultState();
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
			int i = Math.max(this.boundingBox.getMinX(), box.getMinX());
			int j = Math.max(this.boundingBox.getMinY(), box.getMinY());
			int k = Math.max(this.boundingBox.getMinZ(), box.getMinZ());
			int l = Math.min(this.boundingBox.getMaxX(), box.getMaxX());
			int m = Math.min(this.boundingBox.getMaxY(), box.getMaxY());
			int n = Math.min(this.boundingBox.getMaxZ(), box.getMaxZ());
			BlockPos.Mutable mutable = new BlockPos.Mutable((i + l) / 2, (j + m) / 2, (k + n) / 2);

			if(world.getBiome(mutable).isIn(BiomeTags.MINESHAFT_BLOCKING) || world.getTopY(Heightmap.Type.OCEAN_FLOOR, this.boundingBox.getCenter().getX(), this.boundingBox.getCenter().getZ()) < this.boundingBox.getMaxY())
				return true;

			for(int o = i; o <= l; o++)
			{
				for(int p = k; p <= n; p++)
				{
					if(world.getBlockState(mutable.set(o, j, p)).isLiquid())
						return true;

					if(!world.getBlockState(mutable.set(o, m, p)).isLiquid())
						continue;

					return true;
				}
			}

			for(int o = i; o <= l; o++)
			{
				for(int p = j; p <= m; p++)
				{
					if(world.getBlockState(mutable.set(o, p, k)).isLiquid())
						return true;

					if(!world.getBlockState(mutable.set(o, p, n)).isLiquid())
						continue;
					
					return true;
				}
			}

			for(int o = k; o <= n; o++)
			{
				for(int p = j; p <= m; p++)
				{
					if(world.getBlockState(mutable.set(i, p, o)).isLiquid())
						return true;

					if(!world.getBlockState(mutable.set(l, p, o)).isLiquid())
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
	
	public static class MoonshaftCore extends MoonshaftPart
	{
		public MoonshaftCore(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.MOONSHAFT_CROSSING, nbt);
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			super.writeNbt(context, nbt);
		}

		public MoonshaftCore(int chainLength, Direction orientation, BlockBox boundingBox)
		{
			super(StarflightWorldGeneration.MOONSHAFT_CROSSING, chainLength, boundingBox);
			this.setOrientation(orientation);
		}

		@Nullable
		public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x, int y, int z, int depth)
		{
			return new BlockBox(0, -depth, 0, 14, 6, 14).offset(x, y, z);
		}

		@Override
		public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random)
		{
			int i = this.getChainLength();
			int height = boundingBox.getBlockCountY();
			
			for(int y = this.boundingBox.getMinY(); y < height; y += 6)
			{
				if(random.nextBoolean())
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 6, y, this.boundingBox.getMinZ(), Direction.NORTH, i, false);
				
				if(random.nextBoolean())
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 6, y, this.boundingBox.getMaxZ(), Direction.SOUTH, i, false);
				
				if(random.nextBoolean())
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), y, this.boundingBox.getMinZ() + 6, Direction.WEST, i, false);
				
				if(random.nextBoolean())
					MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX(), y, this.boundingBox.getMinZ() + 6, Direction.EAST, i, false);
			}
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			// Clear the volume and build floors.
			BlockState brickState = getBrickType(world.toServerWorld());
			this.fill(world, chunkBox, 0, -1, 0, this.boundingBox.getBlockCountX() - 1, this.boundingBox.getBlockCountY() + 8, this.boundingBox.getBlockCountZ() - 1);
			int elevatorY = 0;
			int y;
			
			for(y = -1; y < this.boundingBox.getBlockCountY() + 1; y++)
			{
				if((y + 1) % 6 == 0)
				{
					this.fillWithOutline(world, chunkBox, 0, y, 0, this.boundingBox.getBlockCountX() - 1, y, this.boundingBox.getBlockCountZ() - 1, brickState, brickState, false);
					this.fillWithOutline(world, chunkBox, 3, y, 4, this.boundingBox.getBlockCountX() - 4, y, 4, StarflightBlocks.BUFFER.getDefaultState().with(SimpleFacingBlock.FACING, Direction.NORTH), AIR, false);
					this.fillWithOutline(world, chunkBox, 3, y, this.boundingBox.getBlockCountZ() - 5, this.boundingBox.getBlockCountX() - 4, y, this.boundingBox.getBlockCountZ() - 5, StarflightBlocks.BUFFER.getDefaultState().with(SimpleFacingBlock.FACING, Direction.SOUTH), AIR, false);
					this.fillWithOutline(world, chunkBox, 2, y, 5, 2, y, this.boundingBox.getBlockCountZ() - 6, StarflightBlocks.BUFFER.getDefaultState().with(SimpleFacingBlock.FACING, Direction.EAST), AIR, false);
					this.fillWithOutline(world, chunkBox, this.boundingBox.getBlockCountX() - 3, y, 5, this.boundingBox.getBlockCountX() - 3, y, this.boundingBox.getBlockCountZ() - 6, StarflightBlocks.BUFFER.getDefaultState().with(SimpleFacingBlock.FACING, Direction.WEST), AIR, false);
					this.fill(world, chunkBox, 3, y, 5, this.boundingBox.getBlockCountX() - 4, y, this.boundingBox.getBlockCountZ() - 6);
					elevatorY = y;
				}
				
				BlockState trackState = (y - 1) % 6 == 0 ? StarflightBlocks.CALL_TRACK.getDefaultState() : StarflightBlocks.LINEAR_TRACK.getDefaultState();
				this.addBlock(world, trackState, 6, y, 7, chunkBox);
				this.addBlock(world, trackState, 8, y, 7, chunkBox);
				this.addBlock(world, StarflightBlocks.TITANIUM_FRAME.getDefaultState(), 7, y, 7, chunkBox);
				
				if((y - 1) % 6 == 0)
				{
					this.addBlock(world, Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.SOUTH), 6, y, 6, chunkBox);
					this.addBlock(world, Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.SOUTH), 8, y, 6, chunkBox);
					this.addBlock(world, Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.NORTH), 6, y, 8, chunkBox);
					this.addBlock(world, Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.NORTH), 8, y, 8, chunkBox);
				}
			}
			
			// Elevator Platforms
			this.fillWithOutline(world, chunkBox, 3, elevatorY, 5, 5, elevatorY, 9, SOLID_BLOCK.getDefaultState(), SOLID_BLOCK.getDefaultState(), false);
			this.addBlock(world, SOLID_BLOCK.getDefaultState(), 5, elevatorY + 1, 7, chunkBox);
			this.addBlock(world, StarflightBlocks.LINEAR_ACTUATOR.getDefaultState().with(SimpleFacingBlock.FACING, Direction.WEST), 5, elevatorY + 2, 7, chunkBox);
			this.addBlock(world, SOLID_BLOCK.getDefaultState(), 5, elevatorY + 3, 7, chunkBox);
			this.addBlock(world, Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.WEST), 4, elevatorY + 1, 7, chunkBox);
			this.addBlock(world, Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.WEST), 4, elevatorY + 3, 7, chunkBox);
			this.fillWithOutline(world, chunkBox, 9, elevatorY, 5, 11, elevatorY, 9, SOLID_BLOCK.getDefaultState(), SOLID_BLOCK.getDefaultState(), false);
			this.addBlock(world, SOLID_BLOCK.getDefaultState(), 9, elevatorY + 1, 7, chunkBox);
			this.addBlock(world, StarflightBlocks.LINEAR_ACTUATOR.getDefaultState().with(SimpleFacingBlock.FACING, Direction.EAST), 9, elevatorY + 2, 7, chunkBox);
			this.addBlock(world, SOLID_BLOCK.getDefaultState(), 9, elevatorY + 3, 7, chunkBox);
			this.addBlock(world, Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.EAST), 10, elevatorY + 1, 7, chunkBox);
			this.addBlock(world, Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.EAST), 10, elevatorY + 3, 7, chunkBox);
			
			// Outer Walls
			y -= 3;
			this.fillWithOutline(world, chunkBox, 0, 0, 0, 0, y, this.boundingBox.getBlockCountZ() - 1, brickState, brickState, false);
			this.fillWithOutline(world, chunkBox, this.boundingBox.getBlockCountX() - 1, 0, 0, this.boundingBox.getBlockCountX() - 1, y, this.boundingBox.getBlockCountZ() - 1, brickState, brickState, false);
			this.fillWithOutline(world, chunkBox, 0, 0, 0, this.boundingBox.getBlockCountX() - 1, y, 0, brickState, brickState, false);
			this.fillWithOutline(world, chunkBox, 0, 0, this.boundingBox.getBlockCountZ() - 1, this.boundingBox.getBlockCountX() - 1, y, this.boundingBox.getBlockCountZ() - 1, brickState, brickState, false);
			this.fillWithOutline(world, chunkBox, 0, 0, 0, 0, y, 0, SOLID_BLOCK.getDefaultState(), SOLID_BLOCK.getDefaultState(), false);
			this.fillWithOutline(world, chunkBox, this.boundingBox.getBlockCountX() - 1, 0, 0, this.boundingBox.getBlockCountX() - 1, y, 0, SOLID_BLOCK.getDefaultState(), SOLID_BLOCK.getDefaultState(), false);
			this.fillWithOutline(world, chunkBox, 0, 0, this.boundingBox.getBlockCountZ() - 1, 0, y, this.boundingBox.getBlockCountZ() - 1, SOLID_BLOCK.getDefaultState(), SOLID_BLOCK.getDefaultState(), false);
			this.fillWithOutline(world, chunkBox, this.boundingBox.getBlockCountX() - 1, 0, this.boundingBox.getBlockCountZ() - 1, this.boundingBox.getBlockCountX() - 1, y, this.boundingBox.getBlockCountZ() - 1, SOLID_BLOCK.getDefaultState(), SOLID_BLOCK.getDefaultState(), false);
			
			// Bottom Floor
			this.fillWithOutline(world, chunkBox, 0, -2, 0, this.boundingBox.getBlockCountX() - 1, -2, this.boundingBox.getBlockCountZ() - 1, brickState, brickState, false);
			this.fillWithOutline(world, chunkBox, 3, -2, 5, 5, -2, 9, StarflightBlocks.BUFFER.getDefaultState(), StarflightBlocks.BUFFER.getDefaultState(), false);
			this.fillWithOutline(world, chunkBox, 9, -2, 5, 11, -2, 9, StarflightBlocks.BUFFER.getDefaultState(), StarflightBlocks.BUFFER.getDefaultState(), false);
			
			// Entrances
			for(y = this.boundingBox.getBlockCountY() - 12; y < this.boundingBox.getBlockCountY(); y++)
			{
				if(y % 6 == 0)
				{
					this.fill(world, chunkBox, 0, y, 5, 0, y + 2, 9);
					this.fill(world, chunkBox, this.boundingBox.getBlockCountX() - 1, y, 5, this.boundingBox.getBlockCountX() - 1, y + 2, 9);
					this.fill(world, chunkBox, 5, y, 0, 9, y + 2, 0);
					this.fill(world, chunkBox, 5, y, this.boundingBox.getBlockCountZ() - 1, 9, y + 2, this.boundingBox.getBlockCountZ() - 1);
				}
			}
			
			// Solar Roof
			y = this.boundingBox.getBlockCountY() + 6;
			
			for(int i = 0; i < 5; i++)
			{
				this.fillWithOutline(world, chunkBox, 0, y, i * 3, this.boundingBox.getBlockCountX() - 1, y, (i + 1) * 3, SOLID_BLOCK.getDefaultState(), SOLID_BLOCK.getDefaultState(), false);
				this.fillWithOutline(world, chunkBox, 0, y + 1, i * 3, this.boundingBox.getBlockCountX() - 1, y + 1, (i + 1) * 3, StarflightBlocks.SOLAR_PANEL.getDefaultState(), StarflightBlocks.SOLAR_PANEL.getDefaultState(), false);
				this.fillWithOutline(world, chunkBox, 0, this.boundingBox.getBlockCountY() - 1, i * 3, 0, y - 1, i * 3, FRAME_BLOCK.getDefaultState(), FRAME_BLOCK.getDefaultState(), false);
				this.fillWithOutline(world, chunkBox, this.boundingBox.getBlockCountX() - 1, this.boundingBox.getBlockCountY() - 1, i * 3, this.boundingBox.getBlockCountX() - 1, y - 1, i * 3, FRAME_BLOCK.getDefaultState(), FRAME_BLOCK.getDefaultState(), false);
				y++;
			}
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
			this.hasRails = chainLength > 2 && length > 8;
		}

		@Nullable
		public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation)
		{
			int i = (random.nextInt(3) + 3) * 3;
			
			if(random.nextInt(4) == 0)
				i *= 2;
			
			if(random.nextInt(4) == 0)
				i *= 2;
			
			while(i > 3)
			{
				BlockBox blockBox = switch(orientation)
				{
					default -> new BlockBox(0, 0, -i, 2, 2, 0);
					case SOUTH -> new BlockBox(0, 0, 0, 2, 2, i);
					case WEST -> new BlockBox(-i, 0, 0, 0, 2, 2);
					case EAST -> new BlockBox(0, 0, 0, i, 2, 2);
				};
				
				BlockBox blockBoxIntersect = switch(orientation)
				{
					default -> new BlockBox(-1, -1, -i + 1, 3, 3, -1);
					case SOUTH -> new BlockBox(-1, -1, 1, 3, 3, i - 1);
					case WEST -> new BlockBox(-i + 1, -1, -1, -1, 3, 3);
					case EAST -> new BlockBox(1, -1, -1, i - 1, 3, 3);
				};
				
				blockBox = blockBox.offset(x, y, z);
				blockBoxIntersect = blockBoxIntersect.offset(x, y, z);
				
				if(holder.getIntersecting(blockBoxIntersect) == null)
					return blockBox;
				
				i--;
			}
			
			return null;
		}

		@Override
		public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random)
		{
			int i = this.getChainLength();
			Direction direction = this.getFacing();
			
			if(direction != null)
			{
				switch(direction)
				{
					default:
					{
						if(i < 3 || random.nextBoolean())
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, direction, i, !this.hasRails);
						else
						{
							if(random.nextBoolean())
								MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), Direction.EAST, i, false);
						
							if(random.nextBoolean())
								MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), Direction.WEST, i, false);
						}
						
						break;
					}
					case SOUTH:
					{
						if(i < 3 || random.nextBoolean())
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, direction, i, !this.hasRails);
						else
						{
							if(random.nextBoolean())
								MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() - 2, Direction.EAST, i, false);
						
							if(random.nextBoolean())
								MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() - 2, Direction.WEST, i, false);
						}
						
						break;
					}
					case WEST:
					{
						if(i < 3 || random.nextBoolean())
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), direction, i, !this.hasRails);
						else
						{
							if(random.nextBoolean())
								MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i, false);
						
							if(random.nextBoolean())
								MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i, false);
						}
						
						break;
					}
					case EAST:
					{
						if(i < 3 || random.nextBoolean())
							MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), direction, i, !this.hasRails);
						else
						{
							if(random.nextBoolean())
								MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() - 2, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i, false);
						
							if(random.nextBoolean())
								MoonshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() - 2, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i, false);
						}
						
						break;
					}
				}
			}
		}

		@Override
		protected boolean addChest(StructureWorldAccess world, BlockBox boundingBox, Random random, int x, int y, int z, Identifier lootTableId)
		{
			BlockPos.Mutable blockPos = this.offsetPos(x, y, z);
			
			if(boundingBox.contains(blockPos) && world.getBlockState(blockPos).isAir() && !world.getBlockState(((BlockPos) blockPos).down()).isAir())
			{
				world.setBlockState(blockPos, StarflightBlocks.STORAGE_CUBE.getDefaultState().with(StorageCubeBlock.FACING, Direction.UP), Block.NOTIFY_LISTENERS);
				LootableInventory.setLootTable(world, random, blockPos, LOOT_TABLE);
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
			
			int minZ = 0;
			int maxZ = Math.max(this.boundingBox.getDimensions().getX(), this.boundingBox.getDimensions().getZ());
			
			if(hasRails)
			{
				this.fill(world, chunkBox, -1, -1, minZ, 3, 4, maxZ);
				this.fillWithOutline(world, chunkBox, 1, 4, minZ, 1, 4, maxZ, StarflightBlocks.LINEAR_TRACK.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Z), AIR, false);
				this.addBlock(world, StarflightBlocks.CALL_TRACK.getDefaultState(), 1, 4, minZ, chunkBox);
				this.addBlock(world, StarflightBlocks.CALL_TRACK.getDefaultState(), 1, 4, maxZ, chunkBox);
				
				for(int z = 0; z < maxZ; z ++)
				{
					if(z % 12 != 0)
						continue;
					
					this.fillWithOutline(world, chunkBox, -2, 4, z, 0, 4, z, SOLID_BLOCK.getDefaultState(), AIR, false);
					this.fillWithOutline(world, chunkBox, 2, 4, z, 4, 4, z, SOLID_BLOCK.getDefaultState(), AIR, false);
					
					int upY = 5;
					
					while(this.getBlockAt(world, -2, upY, z, chunkBox).isAir() && upY < 64)
						upY++;
					
					if(upY < 64)
					{
						int y = 5;
						
						while(this.getBlockAt(world, -2, y, z, chunkBox).isAir() && y < 64)
						{
							this.addBlock(world, FRAME_BLOCK.getDefaultState(), -2, y, z, chunkBox);
							y++;
						}
					}
					
					upY = 5;
					
					while(this.getBlockAt(world, 4, upY, z, chunkBox).isAir() && upY < 64)
						upY++;
					
					if(upY < 64)
					{
						int y = 5;
						
						while(this.getBlockAt(world, 4, y, z, chunkBox).isAir() && y < 64)
						{
							this.addBlock(world, FRAME_BLOCK.getDefaultState(), 4, y, z, chunkBox);
							y++;
						}
					}
					
					int y = 3;

					while(this.getBlockAt(world, -2, y, z, chunkBox).isAir() && y > -64)
					{
						this.addBlock(world, FRAME_BLOCK.getDefaultState(), -2, y, z, chunkBox);
						y--;
					}

					y = 3;

					while(this.getBlockAt(world, 4, y, z, chunkBox).isAir() && y > -64)
					{
						this.addBlock(world, FRAME_BLOCK.getDefaultState(), 4, y, z, chunkBox);
						y--;
					}
				}
			}
			else
			{
				BlockState brickState = getBrickType(world.toServerWorld());
				this.fill(world, chunkBox, 0, 0, minZ, 2, 2, maxZ);
				this.fillWithOutline(world, chunkBox, 0, -1, minZ, 2, -1, maxZ, brickState, brickState, hasRails);
				
				for(int z = 0; z < maxZ; z++)
				{
					if(z % 12 == 0)
					{
						this.generateSupports(world, chunkBox, 0, 0, z, 2, 2, random);
						
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
						
						if(this.getBlockAt(world, -1, -1, z, chunkBox).isAir())
							this.addBlock(world, SOLID_BLOCK.getDefaultState(), -1, -1, z, chunkBox);
						
						if(this.getBlockAt(world, 3, -1, z, chunkBox).isAir())
							this.addBlock(world, SOLID_BLOCK.getDefaultState(), 3, -1, z, chunkBox);
						
						int y = -2;

						while(this.getBlockAt(world, -1, y, z, chunkBox).isAir() && y > -64)
						{
							this.addBlock(world, FRAME_BLOCK.getDefaultState(), -1, y, z, chunkBox);
							y--;
						}

						y = -2;

						while(this.getBlockAt(world, 3, y, z, chunkBox).isAir() && y > -64)
						{
							this.addBlock(world, FRAME_BLOCK.getDefaultState(), 3, y, z, chunkBox);
							y--;
						}
					}
					
					if(random.nextInt(100) == 0)
						this.addChest(world, chunkBox, random, 2, 0, z, LOOT_TABLE);
					
					if(random.nextInt(100) == 0)
						this.addChest(world, chunkBox, random, 0, 0, z, LOOT_TABLE);
					
					if(random.nextInt(60) == 0)
						this.addMob(world, random, 0, 0, z);
				}
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

		private void generateSupports(StructureWorldAccess world, BlockBox boundingBox, int minX, int minY, int z, int maxY, int maxX, Random random)
		{
			this.fillWithOutline(world, boundingBox, minX, minY - 1, z, maxX, minY - 1, z, SOLID_BLOCK.getDefaultState(), AIR, true);
			this.fillWithOutline(world, boundingBox, minX, maxY + 1, z, maxX, maxY + 1, z, SOLID_BLOCK.getDefaultState(), AIR, true);
			this.fillWithOutline(world, boundingBox, minX - 1, minY, z, minX - 1, maxY, z, SOLID_BLOCK.getDefaultState(), AIR, true);
			this.fillWithOutline(world, boundingBox, maxX + 1, minY, z, maxX + 1, maxY, z, SOLID_BLOCK.getDefaultState(), AIR, true);
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

		public MoonshaftCrossing(int chainLength, BlockBox boundingBox, int up, int down, @Nullable Direction orientation)
		{
			super(StarflightWorldGeneration.MOONSHAFT_CROSSING, chainLength, boundingBox);
			this.direction = orientation;
			this.up = up;
			this.down = down;
		}

		@Nullable
		public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x, int y, int z, int up, int down, Direction orientation)
		{
			BlockBox blockBox = new BlockBox(0, -down * 6, 0, 2, 3 + up * 6, 2).offset(x, y, z);

			//if(holder.getIntersecting(blockBox.expand(-1)) != null)
			//	return null;
			
			return blockBox;
		}

		@Override
		public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random)
		{
			int i = this.getChainLength();
			
			for(int j = 0; j < up + down + 1; j++)
			{
				randomCorridor(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY() + j * 6, this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
				randomCorridor(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY() + j * 6, this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
				randomCorridor(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + j * 6, this.boundingBox.getMinZ(), Direction.WEST, i);
				randomCorridor(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + j * 6, this.boundingBox.getMinZ(), Direction.EAST, i);
			}
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			if(this.cannotGenerate(world, chunkBox) || world.getBlockState(new BlockPos(this.boundingBox.getCenter().getX(), this.boundingBox.getMinY(), this.boundingBox.getCenter().getZ())).isAir())
				return;
			
			BlockState brickState = getBrickType(world.toServerWorld());
			this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxX() + 1, this.boundingBox.getMaxY(), this.boundingBox.getMaxZ() + 1, AIR, AIR, false);
			this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() - 1, this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() - 1, this.boundingBox.getMaxZ() + 1, brickState, brickState, false);
			
			if(up > 0 || down > 0)
			{
				// Spiral Staircase
				for(int i = 1; i <= up + down; i++)
				{
					for(int j = this.boundingBox.getMinX() - 1; j <= this.boundingBox.getMaxX() + 1; j++)
					{
						for(int k = this.boundingBox.getMinZ() - 1; k <= this.boundingBox.getMaxZ() + 1; k++)
							this.tryPlaceFloor(world, chunkBox, StarflightBlocks.WALKWAY.getDefaultState(), j, this.boundingBox.getMinY() + i * 6 - 1, k);
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
				this.fillWithOutline(world, boundingBox, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, this.boundingBox.getMinX() + 1, this.boundingBox.getMaxY(), this.boundingBox.getMinZ() + 1, FRAME_BLOCK.getDefaultState(), AIR, false);
				
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
				this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxY());
				this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, this.boundingBox.getMaxY());
				this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, this.boundingBox.getMaxY());
				this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, this.boundingBox.getMaxY());
				int lootCount = random.nextInt(4);
				
				for(int i = 0; i < lootCount; i++)
				{
					BlockPos lootPos = new BlockPos(random.nextBetween(this.boundingBox.getMinX(), this.boundingBox.getMaxX()), this.boundingBox.getMinY(), random.nextBetween(this.boundingBox.getMinZ(), this.boundingBox.getMaxZ()));
					
					if(world.getBlockState(lootPos).isAir())
					{
						world.setBlockState(lootPos, StarflightBlocks.STORAGE_CUBE.getDefaultState().with(StorageCubeBlock.FACING, Direction.UP), Block.NOTIFY_LISTENERS);
						LootableInventory.setLootTable(world, random, lootPos, LOOT_TABLE);
					}
				}
			}
		}
		
		private void randomCorridor(StructurePiece start, StructurePiecesHolder holder, Random random, int x, int y, int z, Direction direction, int chainLength)
		{
			if(direction != this.direction.getOpposite() && random.nextBoolean())
				MoonshaftGenerator.pieceGenerator(start, holder, random, x, y, z, direction, chainLength, false);
		}

		private void generateCrossingPillar(StructureWorldAccess world, BlockBox boundingBox, int x, int minY, int z, int maxY)
		{
			if(!this.getBlockAt(world, x, maxY + 1, z, boundingBox).isAir())
				this.fillWithOutline(world, boundingBox, x, minY, z, x, maxY, z, FRAME_BLOCK.getDefaultState(), AIR, false);
		}
	}
}