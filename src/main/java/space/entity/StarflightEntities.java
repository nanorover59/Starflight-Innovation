package space.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import space.StarflightMod;

public class StarflightEntities
{
	public static final EntityType<MovingCraftEntity> MOVING_CRAFT = registerEntity(Identifier.of(StarflightMod.MOD_ID, "moving_craft"), EntityType.Builder.<MovingCraftEntity>create(MovingCraftEntity::new, SpawnGroup.MISC).dimensions(0.5f, 0.5f));
	public static final EntityType<RocketEntity> ROCKET = registerEntity(Identifier.of(StarflightMod.MOD_ID, "rocket"), EntityType.Builder.<RocketEntity>create(RocketEntity::new, SpawnGroup.MISC).dimensions(0.5f, 0.5f).maxTrackingRange(12));
	public static final EntityType<LinearPlatformEntity> LINEAR_PLATFORM = registerEntity(Identifier.of(StarflightMod.MOD_ID, "linear_platform"), EntityType.Builder.<LinearPlatformEntity>create(LinearPlatformEntity::new, SpawnGroup.MISC).dimensions(0.5f, 0.5f).maxTrackingRange(12));
	public static final EntityType<AirshipEntity> AIRSHIP = registerEntity(Identifier.of(StarflightMod.MOD_ID, "airship"), EntityType.Builder.<AirshipEntity>create(AirshipEntity::new, SpawnGroup.MISC).dimensions(0.5f, 0.5f).maxTrackingRange(12));
	public static final EntityType<PlasmaBallEntity> PLASMA_BALL = registerEntity(Identifier.of(StarflightMod.MOD_ID, "plasma_ball"), EntityType.Builder.<PlasmaBallEntity>create(PlasmaBallEntity::new, SpawnGroup.MISC).dimensions(0.25f, 0.25f).maxTrackingRange(8));
	public static final EntityType<DustEntity> DUST = registerEntity(Identifier.of(StarflightMod.MOD_ID, "dust"), EntityType.Builder.<DustEntity>create(DustEntity::new, SpawnGroup.MONSTER).dimensions(1.0f, 2.8f).maxTrackingRange(8));
	public static final EntityType<CeruleanEntity> CERULEAN = registerEntity(Identifier.of(StarflightMod.MOD_ID, "cerulean"), EntityType.Builder.<CeruleanEntity>create(CeruleanEntity::new, SpawnGroup.MONSTER).dimensions(0.5f, 1.5f).maxTrackingRange(8));
	public static final EntityType<AncientHumanoidEntity> ANCIENT_HUMANOID = registerEntity(Identifier.of(StarflightMod.MOD_ID, "ancient_humanoid"), EntityType.Builder.<AncientHumanoidEntity>create(AncientHumanoidEntity::new, SpawnGroup.MONSTER).dimensions(0.5f, 1.8f).maxTrackingRange(8));
	public static final EntityType<SolarSpectreEntity> SOLAR_SPECTRE = registerEntity(Identifier.of(StarflightMod.MOD_ID, "solar_spectre"), EntityType.Builder.<SolarSpectreEntity>create(SolarSpectreEntity::new, SpawnGroup.MONSTER).dimensions(3.0f, 3.0f).eyeHeight(1.5f).maxTrackingRange(12));
	public static final EntityType<SolarEyesEntity> SOLAR_EYES = registerEntity(Identifier.of(StarflightMod.MOD_ID, "solar_eyes"), EntityType.Builder.<SolarEyesEntity>create(SolarEyesEntity::new, SpawnGroup.AMBIENT).dimensions(0.75f, 0.75f));
	public static final EntityType<StratofishEntity> STRATOFISH = registerEntity(Identifier.of(StarflightMod.MOD_ID, "stratofish"), EntityType.Builder.<StratofishEntity>create(StratofishEntity::new, SpawnGroup.CREATURE).dimensions(1.5f, 0.75f).eyeHeight(0.375f).maxTrackingRange(12));
	public static final EntityType<CloudSharkEntity> CLOUD_SHARK = registerEntity(Identifier.of(StarflightMod.MOD_ID, "cloud_shark"), EntityType.Builder.<CloudSharkEntity>create(CloudSharkEntity::new, SpawnGroup.MONSTER).dimensions(3.0f, 1.5f).eyeHeight(1.5f).maxTrackingRange(12));
	public static final EntityType<CaveLampreyEntity> CAVE_LAMPREY = registerEntity(Identifier.of(StarflightMod.MOD_ID, "cave_lamprey"), EntityType.Builder.<CaveLampreyEntity>create(CaveLampreyEntity::new, SpawnGroup.MONSTER).dimensions(0.75f, 0.5f).eyeHeight(0.25f).maxTrackingRange(8));
	public static final EntityType<BlockShellEntity> BLOCK_SHELL = registerEntity(Identifier.of(StarflightMod.MOD_ID, "block_shell"), EntityType.Builder.<BlockShellEntity>create(BlockShellEntity::new, SpawnGroup.MONSTER).dimensions(0.75f, 0.75f).maxTrackingRange(8));

	public static void initializeEntities()
	{
		FabricDefaultAttributeRegistry.register(DUST, DustEntity.createDustAttributes());
		FabricDefaultAttributeRegistry.register(CERULEAN, CeruleanEntity.createCeruleanAttributes());
		FabricDefaultAttributeRegistry.register(ANCIENT_HUMANOID, AncientHumanoidEntity.createAncientHumanoidAttributes());
		FabricDefaultAttributeRegistry.register(SOLAR_SPECTRE, SolarSpectreEntity.createSolarSpectreAttributes());
		FabricDefaultAttributeRegistry.register(SOLAR_EYES, SolarEyesEntity.createSolarEyesAttributes());
		FabricDefaultAttributeRegistry.register(STRATOFISH, StratofishEntity.createStratofishAttributes());
		FabricDefaultAttributeRegistry.register(CLOUD_SHARK, CloudSharkEntity.createCloudSharkAttributes());
		FabricDefaultAttributeRegistry.register(CAVE_LAMPREY, CaveLampreyEntity.createCaveLampreyAttributes());
		FabricDefaultAttributeRegistry.register(BLOCK_SHELL, BlockShellEntity.createBlockShellAttributes());
		
		SpawnRestriction.register(DUST, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, DustEntity::canDustSpawn);
		SpawnRestriction.register(CERULEAN, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, CeruleanEntity::canCeruleanSpawn);
		SpawnRestriction.register(SOLAR_SPECTRE, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SolarSpectreEntity::canSolarSpectreSpawn);
		SpawnRestriction.register(SOLAR_EYES, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SolarEyesEntity::canSolarEyesSpawn);
		SpawnRestriction.register(STRATOFISH, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, StratofishEntity::canStratofishSpawn);
		SpawnRestriction.register(CLOUD_SHARK, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, CloudSharkEntity::canCloudSharkSpawn);
		SpawnRestriction.register(CAVE_LAMPREY, SpawnLocationTypes.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, CaveLampreyEntity::canCaveLampreySpawn);
	}
	
	private static <T extends Entity> EntityType<T> registerEntity(Identifier id, Builder<T> type)
	{
        return Registry.register(Registries.ENTITY_TYPE, id, type.build());
    }
}