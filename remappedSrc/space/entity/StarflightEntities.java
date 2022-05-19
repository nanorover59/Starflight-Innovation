package space.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import space.StarflightMod;

public class StarflightEntities
{
	public static final EntityType<MovingCraftEntity> MOVING_CRAFT = FabricEntityTypeBuilder.create(SpawnGroup.MISC, (EntityType.EntityFactory<MovingCraftEntity>) MovingCraftEntity::new).dimensions(EntityDimensions.changing(0.5f, 0.5f)).build();
	public static final EntityType<RocketEntity> ROCKET = FabricEntityTypeBuilder.create(SpawnGroup.MISC, (EntityType.EntityFactory<RocketEntity>) RocketEntity::new).dimensions(EntityDimensions.changing(0.5f, 0.5f)).build();
	
	public static void initializeEntities()
	{
		Registry.register(Registry.ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "moving_craft"), MOVING_CRAFT);
		Registry.register(Registry.ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "rocket"), ROCKET);
	}
}
