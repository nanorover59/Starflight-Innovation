package space.world;

import java.util.ArrayList;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import space.StarflightMod;
import space.block.StarflightBlocks;

public class ArtifactGenerator
{
	private static final int LARGE_SIZE_LIMIT = 32;
	static final BlockPos DEFAULT_POSITION = new BlockPos(4, 0, 15);
	static final Map<Identifier, Integer> TEMPLATES = Map.of(Identifier.of(StarflightMod.MOD_ID, "rocket_1"), 0);
	static final Map<RegistryKey<LootTable>, Integer> LOOT_TABLES = Map.of(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(StarflightMod.MOD_ID, "chests/moonshaft")), 0);
	
	public static ArtifactGenerator.Piece addParts(StructureTemplateManager structureTemplateManager, BlockPos pos, BlockRotation rotation, StructurePiecesHolder holder, Random random, int type)
	{
		ArrayList<Identifier> templates = new ArrayList<Identifier>();
		
		for(Identifier identifier : TEMPLATES.keySet())
		{
			if(TEMPLATES.get(identifier) == type)
				templates.add(identifier);
		}
		
		ArtifactGenerator.Piece piece = new ArtifactGenerator.Piece(structureTemplateManager, Util.getRandom(templates, random), pos, rotation, type);
		holder.addPiece(piece);
		return piece;
	}

	public static class Piece extends SimpleStructurePiece
	{
		private final int type;

		public Piece(StructureTemplateManager manager, Identifier identifier, BlockPos pos, BlockRotation rotation, int type)
		{
			super(StarflightWorldGeneration.ARTIFACT_PIECE, 0, manager, identifier, identifier.toString(), createPlacementData(rotation), pos);
			this.type = type;
		}

		public Piece(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.ARTIFACT_PIECE, nbt, context.structureTemplateManager(), id -> createPlacementData(BlockRotation.valueOf(nbt.getString("rotation"))));
			this.type = nbt.getInt("type");
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			super.writeNbt(context, nbt);
			nbt.putInt("type", this.type);
			nbt.putString("rotation", this.placementData.getRotation().name());
		}

		private static StructurePlacementData createPlacementData(BlockRotation rotation)
		{
			return new StructurePlacementData().setRotation(rotation).setMirror(BlockMirror.NONE).setPosition(ArtifactGenerator.DEFAULT_POSITION).addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS);
		}

		@Override
		protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox)
		{
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			if(this.isTooLargeForNormalGeneration())
				super.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot);
			else
			{
				int i = world.getTopY();
				int j = 0;
				Vec3i vec3i = this.template.getSize();
				int k = vec3i.getX() * vec3i.getZ();
				
				if(k == 0)
					j = world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, this.pos.getX(), this.pos.getZ());
				else
				{
					BlockPos blockPos = this.pos.add(vec3i.getX() - 1, 0, vec3i.getZ() - 1);

					for(BlockPos blockPos2 : BlockPos.iterate(this.pos, blockPos))
					{
						int l = world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, blockPos2.getX(), blockPos2.getZ());
						j += l;
						i = Math.min(i, l);
					}

					j /= k;
				}

				this.setY(j);
				super.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot);
				
				ArrayList<RegistryKey<LootTable>> lootTables = new ArrayList<RegistryKey<LootTable>>();
				
				for(RegistryKey<LootTable> key : LOOT_TABLES.keySet())
				{
					if(LOOT_TABLES.get(key) == type)
						lootTables.add(key);
				}
				
				BlockPos blockPos = this.pos.add(vec3i.getX() - 1, 0, vec3i.getZ() - 1);
				
				for(BlockPos blockPos2 : BlockPos.iterate(this.pos, blockPos))
				{
					BlockState blockState = world.getBlockState(blockPos2);
					
					if(!lootTables.isEmpty() && blockState.getBlock() == StarflightBlocks.STORAGE_CUBE)
						LootableInventory.setLootTable(world, random, blockPos2, Util.getRandom(lootTables, random));
				}
			}
		}

		public boolean isTooLargeForNormalGeneration()
		{
			Vec3i vec3i = this.template.getSize();
			return vec3i.getX() > LARGE_SIZE_LIMIT || vec3i.getY() > LARGE_SIZE_LIMIT;
		}

		public void setY(int y)
		{
			this.pos = new BlockPos(this.pos.getX(), y, this.pos.getZ());
		}
	}
}