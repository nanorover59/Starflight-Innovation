package space.world;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import space.StarflightMod;

public class LandingSiteFeature extends Feature<DefaultFeatureConfig>
{
	private static final Identifier[] VEHICLES = {
		new Identifier(StarflightMod.MOD_ID, "rocket_2"),
		new Identifier(StarflightMod.MOD_ID, "rocket_3")
	};
	
	private static final Identifier[] HABS = {
		new Identifier(StarflightMod.MOD_ID, "tent"),
		new Identifier(StarflightMod.MOD_ID, "tilted_greenhouse"),
		new Identifier(StarflightMod.MOD_ID, "curious_capsule"),
		new Identifier(StarflightMod.MOD_ID, "peculiar_pod"),
	};
	
	public LandingSiteFeature(Codec<DefaultFeatureConfig> configCodec)
	{
		super(configCodec);
	}
	
	private static int checkEvenTerrain(FeatureContext<DefaultFeatureConfig> context, int x, int z)
	{
		StructureWorldAccess world = context.getWorld();
		int y1 = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
		int y2 = world.getTopY(Heightmap.Type.WORLD_SURFACE, x + 32, z);
		int y3 = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z + 32);
		int y4 = world.getTopY(Heightmap.Type.WORLD_SURFACE, x + 32, z + 32);
		int d1 = (int) Math.abs(y1 - y2);
		int d2 = (int) Math.abs(y1 - y3);
		int d3 = (int) Math.abs(y1 - y4);
		return (d1 + d2 + d3) / 3;
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context)
	{
		StructureWorldAccess world = context.getWorld();
		Random random = context.getRandom();
		ChunkPos chunkPos = new ChunkPos(context.getOrigin());
		float chance = 0.0025f;
		int xOffset = random.nextBoolean() ? 0 : 15;
		int zOffset = random.nextBoolean() ? 0 : 15;
		BlockPos pos1 = chunkPos.getStartPos().add(xOffset, 0, zOffset);
		pos1 = world.getTopPosition(Type.WORLD_SURFACE, pos1);
		BlockPos pos2 = chunkPos.getStartPos().add(xOffset > 0 ? 0 : 15, 0, zOffset > 0 ? 0 : 15);
		pos2 = world.getTopPosition(Type.WORLD_SURFACE, pos2);
		
		if(random.nextFloat() > chance || checkEvenTerrain(context, chunkPos.getStartX(), chunkPos.getStartZ()) > 4)
			return true;
		
		StructureTemplate template = world.toServerWorld().getStructureTemplateManager().getTemplate(VEHICLES[random.nextInt(VEHICLES.length)]).get();
		StructurePlacementData placementdata = new StructurePlacementData().setRotation(BlockRotation.random(random));
		BlockPos pivot = pos1.add(template.getSize().getX() / 2, 0, template.getSize().getZ() / 2);
		template.place(world, pos1, pivot, placementdata, random, Block.NOTIFY_LISTENERS);
		template = world.toServerWorld().getStructureTemplateManager().getTemplate(HABS[random.nextInt(HABS.length)]).get();
		placementdata = new StructurePlacementData().setRotation(BlockRotation.random(random));
		pivot = pos2.add(template.getSize().getX() / 2, 0, template.getSize().getZ() / 2);
		template.place(world, pos2, pivot, placementdata, random, Block.NOTIFY_LISTENERS);
		return true;
	}
}