package space.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction.Location;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import space.StarflightMod;

public class StarflightEntities
{
	public static final EntityType<MovingCraftEntity> MOVING_CRAFT = FabricEntityTypeBuilder.create(SpawnGroup.MISC, (EntityType.EntityFactory<MovingCraftEntity>) MovingCraftEntity::new).dimensions(EntityDimensions.changing(0.5f, 0.5f)).build();
	public static final EntityType<RocketEntity> ROCKET = FabricEntityTypeBuilder.create(SpawnGroup.MISC, (EntityType.EntityFactory<RocketEntity>) RocketEntity::new).dimensions(EntityDimensions.changing(0.5f, 0.5f)).build();
	public static final EntityType<DustEntity> DUST = FabricEntityTypeBuilder.create(SpawnGroup.MISC, (EntityType.EntityFactory<DustEntity>) DustEntity::new).dimensions(EntityDimensions.changing(1.0f, 2.8f)).build();
	public static final EntityType<CeruleanEntity> CERULEAN = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, (EntityType.EntityFactory<CeruleanEntity>) CeruleanEntity::new).dimensions(EntityDimensions.fixed(0.5f, 1.5f)).build();
	
	// Entity Tags
	public static final TagKey<EntityType<?>> NO_OXYGEN_ENTITY_TAG = TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier(StarflightMod.MOD_ID, "no_oxygen"));
	
	public static void initializeEntities()
	{
		Registry.register(Registry.ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "moving_craft"), MOVING_CRAFT);
		Registry.register(Registry.ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "rocket"), ROCKET);
		
		registerMobEntity(DUST, "dust", DustEntity.createDustAttributes(), Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES);
		registerMobEntity(CERULEAN, "cerulean", CeruleanEntity.createCeruleanAttributes(), Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES);
	}
	
	private static <T extends MobEntity> void registerMobEntity(EntityType<T> entityType, String name, DefaultAttributeContainer.Builder attributes, Location location, Heightmap.Type heightmap)
	{
		Registry.register(Registry.ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, name), entityType);
		FabricDefaultAttributeRegistry.register(entityType, attributes);
	}
}