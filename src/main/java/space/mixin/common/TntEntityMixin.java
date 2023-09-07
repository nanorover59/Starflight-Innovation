package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity
{
	protected TntEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	/**
	 * Modified vanilla TNT entity physics to account for different gravity and air resistance values.
	 */	
	@ModifyArg(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/TntEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal = 0), index = 0)
	public Vec3d modifyVelocity1(Vec3d velocity)
	{
		if(this.getWorld().getRegistryKey() != World.OVERWORLD && this.getWorld().getRegistryKey() != World.NETHER && this.getWorld().getRegistryKey() != World.END)
		{
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(this.getWorld());
			
			if(data != null && data.overridePhysics())
				velocity = velocity.add(0.0, 0.04 - 0.04 * (data.isOrbit() ? 0.0 : data.getGravity()), 0.0);
		}
		
		return velocity;
	}
	
	@ModifyArg(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/TntEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal = 1), index = 0)
	public Vec3d modifyVelocity2(Vec3d velocity)
	{
		if(this.getWorld().getRegistryKey() != World.OVERWORLD && this.getWorld().getRegistryKey() != World.NETHER && this.getWorld().getRegistryKey() != World.END)
		{
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(this.getWorld());
			
			if(data != null && data.overridePhysics() && !this.isOnGround())
			{
				double airMultiplier = AirUtil.getAirResistanceMultiplier(this.getWorld(), data, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance.
				double d = 1.0 / (1.0 + (0.02 * airMultiplier));
				velocity = this.getVelocity().multiply(d);
			}
		}
		
		return velocity;
	}
}