package space.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public class StarflightEntities
{
	public static final EntityType<MovingCraftEntity> MOVING_CRAFT = registerEntity(Identifier.of(StarflightMod.MOD_ID, "moving_craft"), EntityType.Builder.<MovingCraftEntity>create(MovingCraftEntity::new, SpawnGroup.MISC).dimensions(0.5f, 0.5f));
	public static final EntityType<RocketEntity> ROCKET = registerEntity(Identifier.of(StarflightMod.MOD_ID, "rocket"), EntityType.Builder.<RocketEntity>create(RocketEntity::new, SpawnGroup.MISC).dimensions(0.5f, 0.5f));
	public static final EntityType<LinearPlatformEntity> LINEAR_PLATFORM = registerEntity(Identifier.of(StarflightMod.MOD_ID, "linear_platform"), EntityType.Builder.<LinearPlatformEntity>create(LinearPlatformEntity::new, SpawnGroup.MISC).dimensions(0.5f, 0.5f));
	public static final EntityType<AirshipEntity> AIRSHIP_ENTITY = registerEntity(Identifier.of(StarflightMod.MOD_ID, "airship"), EntityType.Builder.<AirshipEntity>create(AirshipEntity::new, SpawnGroup.MISC).dimensions(0.5f, 0.5f));
	public static final EntityType<PlasmaBallEntity> PLASMA_BALL = registerEntity(Identifier.of(StarflightMod.MOD_ID, "plasma_ball"), EntityType.Builder.<PlasmaBallEntity>create(PlasmaBallEntity::new, SpawnGroup.MISC).dimensions(0.25f, 0.25f));
	public static final EntityType<DustEntity> DUST = registerEntity(Identifier.of(StarflightMod.MOD_ID, "dust"), EntityType.Builder.<DustEntity>create(DustEntity::new, SpawnGroup.MONSTER).dimensions(1.0f, 2.8f));
	public static final EntityType<CeruleanEntity> CERULEAN = registerEntity(Identifier.of(StarflightMod.MOD_ID, "cerulean"), EntityType.Builder.<CeruleanEntity>create(CeruleanEntity::new, SpawnGroup.MONSTER).dimensions(0.5f, 1.5f));
	public static final EntityType<AncientHumanoidEntity> ANCIENT_HUMANOID = registerEntity(Identifier.of(StarflightMod.MOD_ID, "ancient_humanoid"), EntityType.Builder.<AncientHumanoidEntity>create(AncientHumanoidEntity::new, SpawnGroup.MONSTER).dimensions(0.5f, 1.8f));
	public static final EntityType<SolarSpectreEntity> SOLAR_SPECTRE = registerEntity(Identifier.of(StarflightMod.MOD_ID, "solar_spectre"), EntityType.Builder.<SolarSpectreEntity>create(SolarSpectreEntity::new, SpawnGroup.MONSTER).dimensions(2.5f, 2.5f));
	public static final EntityType<SolarEyesEntity> SOLAR_EYES = registerEntity(Identifier.of(StarflightMod.MOD_ID, "solar_eyes"), EntityType.Builder.<SolarEyesEntity>create(SolarEyesEntity::new, SpawnGroup.AMBIENT).dimensions(0.5f, 0.5f));
	public static final EntityType<StratofishEntity> STRATOFISH = registerEntity(Identifier.of(StarflightMod.MOD_ID, "stratofish"), EntityType.Builder.<StratofishEntity>create(StratofishEntity::new, SpawnGroup.CREATURE).dimensions(1.5f, 0.5f));

	// Entity Tags
	public static final TagKey<EntityType<?>> NO_OXYGEN_ENTITY_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "no_oxygen"));

	public static void initializeEntities()
	{
		FabricDefaultAttributeRegistry.register(DUST, DustEntity.createDustAttributes());
		FabricDefaultAttributeRegistry.register(CERULEAN, CeruleanEntity.createCeruleanAttributes());
		FabricDefaultAttributeRegistry.register(ANCIENT_HUMANOID, AncientHumanoidEntity.createAncientHumanoidAttributes());
		FabricDefaultAttributeRegistry.register(SOLAR_SPECTRE, SolarSpectreEntity.createSolarSpectreAttributes());
		FabricDefaultAttributeRegistry.register(SOLAR_EYES, SolarEyesEntity.createSolarEyesAttributes());
		FabricDefaultAttributeRegistry.register(STRATOFISH, StratofishEntity.createStratofishAttributes());
	}
	
	private static <T extends Entity> EntityType<T> registerEntity(Identifier id, Builder<T> type)
	{
        return Registry.register(Registries.ENTITY_TYPE, id, type.build());
    }
}