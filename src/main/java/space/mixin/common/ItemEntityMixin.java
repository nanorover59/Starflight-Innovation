 package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity
{
	protected ItemEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}

	/**
	 * Modified vanilla item entity physics to account for different gravity and air resistance values.
	 */	
	@ModifyArg(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal = 0), index = 0)
	public Vec3d modifyVelocity1(Vec3d velocity)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());
			
			if(currentPlanet != null)
				velocity = velocity.add(0.0, 0.04 - 0.04 * (PlanetList.isOrbit(this.world.getRegistryKey()) ? 0.0 : currentPlanet.getSurfaceGravity()), 0.0);
		}
		
		return velocity;
	}
	
	@ModifyArg(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal = 1), index = 0)
	public Vec3d modifyVelocity2(Vec3d velocity)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());
			
			if(currentPlanet != null && !this.onGround)
			{
				double airMultiplier = AirUtil.getAirResistanceMultiplier(world, currentPlanet, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance.
				double d = 1.0 / (1.0 + (0.02 * airMultiplier));
				velocity = this.getVelocity().multiply(d);
			}
		}
		
		return velocity;
	}
}