package space.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.nbt.NbtCompound;
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
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import space.StarflightMod;
import space.block.StarflightBlocks;
import space.entity.AncientHumanoidEntity;
import space.entity.StarflightEntities;

public class LandingSiteGenerator
{
	private static final int SPREAD_CHUNKS = 4;
	private static final Identifier LOOT_TABLE = new Identifier(StarflightMod.MOD_ID, "chests/surface_outpost");
	
	private static final Identifier[] STRUCTURES = {
		new Identifier(StarflightMod.MOD_ID, "rocket_1"),
		new Identifier(StarflightMod.MOD_ID, "rocket_2"),
		new Identifier(StarflightMod.MOD_ID, "rocket_3"),
		new Identifier(StarflightMod.MOD_ID, "tent"),
		new Identifier(StarflightMod.MOD_ID, "comfy_canister"),
		new Identifier(StarflightMod.MOD_ID, "tilted_greenhouse"),
		new Identifier(StarflightMod.MOD_ID, "curious_capsule"),
		new Identifier(StarflightMod.MOD_ID, "peculiar_pod"),
	};
	
	public static void addPieces(Structure.Context context, BlockPos pos, StructurePiecesHolder holder)
	{
		ChunkRandom random = context.random();
		
		int count = 1 + random.nextInt(3);
		Set<BlockPos> set = new HashSet<BlockPos>();
		
		for(int i = 0; i < count; i++)
		{
			BlockPos offset = pos.add((random.nextInt(SPREAD_CHUNKS) - random.nextInt(SPREAD_CHUNKS)) * 16, 0, (random.nextInt(SPREAD_CHUNKS) - random.nextInt(SPREAD_CHUNKS)) * 16);
			
			if(set.contains(offset))
				continue;
			
			int structureID = set.size() == 0 ? random.nextInt(3) : random.nextInt(STRUCTURES.length - 1);
			holder.addPiece(new Piece(offset, structureID));
			set.add(offset);
		}
	}

	public static class Piece extends StructurePiece
	{
		private final int structureID;
		
		public Piece(BlockPos start, int structureID)
		{
			super(StarflightWorldGeneration.LANDING_SITE_PIECE, 0, new BlockBox(start));
			this.structureID = structureID;
		}

		public Piece(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.LANDING_SITE_PIECE, nbt);
			this.structureID  = nbt.getInt("structureID");
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			nbt.putInt("structureID", this.structureID);
		}
		
		@Override
		protected boolean canReplace(BlockState state)
		{
			return true;
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			BlockBox box = BlockBox.create(chunkPos.getStartPos().add(-16, 0, -16), chunkPos.getStartPos().add(16, world.getHeight(), 16));
			ArrayList<BlockBox> boxes = new ArrayList<BlockBox>();
			placeStructureInBounds(world, random, box, boxes, STRUCTURES[structureID]);
			spawnMobs(world, random, box, boxes);
		}
		
		private void placeStructureInBounds(StructureWorldAccess world, Random random, BlockBox bounds, ArrayList<BlockBox> boxes, Identifier templateID)
		{
			for(int i = 0; i < 32; i++)
			{
				BlockRotation rotation = BlockRotation.random(random);
				StructureTemplate template = world.toServerWorld().getStructureTemplateManager().getTemplate(templateID).get();
				StructurePlacementData placementdata = new StructurePlacementData().setRotation(rotation);
				BlockPos pos = new BlockPos(bounds.getMinX() + random.nextInt(bounds.getBlockCountX()), 0, bounds.getMinZ() + random.nextInt(bounds.getBlockCountZ()));
				pos = world.getTopPosition(Type.WORLD_SURFACE, pos);
				BlockBox box1 = template.calculateBoundingBox(placementdata, pos).expand(1);
				
				if(bounds.contains(new BlockPos(box1.getMinX(), box1.getMinY(), box1.getMinZ())) && bounds.contains(new BlockPos(box1.getMaxX(), box1.getMaxY(), box1.getMaxZ())))
				{
					if(boxes.isEmpty())
					{
						template.place(world, pos, pos, placementdata, random, Block.NOTIFY_LISTENERS);
						postPlacement(world, random, box1);
						boxes.add(box1);
						return;
					}
					
					for(BlockBox box2 : boxes)
					{
						if(!box1.intersects(box2))
						{
							template.place(world, pos, pos, placementdata, random, Block.NOTIFY_LISTENERS);
							postPlacement(world, random, box1);
							boxes.add(box1);
							return;
						}
					}
				}
			}
		}
		
		private void postPlacement(StructureWorldAccess world, Random random, BlockBox box)
		{
			for(int x = box.getMinX(); x < box.getMaxX(); x++)
			{
				for(int y = box.getMinY(); y < box.getMaxY(); y++)
				{
					for(int z = box.getMinZ(); z < box.getMaxZ(); z++)
					{
						BlockPos pos = new BlockPos(x, y, z);
						BlockState blockState = world.getBlockState(pos);
						
						if(blockState.getBlock() == Blocks.DIRT && random.nextInt(4) == 0)
							world.setBlockState(pos.up(), Blocks.DEAD_BUSH.getDefaultState(), Block.NOTIFY_LISTENERS);
						else if(blockState.getBlock() == StarflightBlocks.STORAGE_CUBE)
							LootableInventory.setLootTable(world, random, pos, LOOT_TABLE);
					}
				}
			}
		}
		
		private void spawnMobs(StructureWorldAccess world, Random random, BlockBox bounds, ArrayList<BlockBox> boxes)
		{
			for(int count = random.nextInt(4); count > 0; count--)
			{
				boolean b = false;
				BlockPos pos = new BlockPos(bounds.getMinX() + random.nextInt(bounds.getBlockCountX()), 0, bounds.getMinZ() + random.nextInt(bounds.getBlockCountZ()));
				pos = world.getTopPosition(Type.WORLD_SURFACE, pos);
				
				for(BlockBox exclusion : boxes)
				{
					if(exclusion.contains(pos))
					{
						b = true;
						break;
					}
				}
				
				if(!b && world.getBlockState(pos).isAir() && !world.getBlockState(pos.down()).isAir())
				{
					AncientHumanoidEntity entity = new AncientHumanoidEntity(StarflightEntities.ANCIENT_HUMANOID, world.toServerWorld());
					entity.initEquipment(random, world.getLocalDifficulty(pos));
					entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					world.spawnEntity(entity);
				}
			}
		}
	}
}