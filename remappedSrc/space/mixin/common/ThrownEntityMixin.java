package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(ThrownEntity.class)
public abstract class ThrownEntityMixin extends Entity
{
	public ThrownEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	/**
	 * Modified vanilla thrown entity physics to account for different gravity and air resistance values.
	 */
	@Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/ThrownEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal = 0), cancellable = true)
	public void tickInject(CallbackInfo info)
	{
		if(this.method_48926().getRegistryKey() != World.OVERWORLD && this.method_48926().getRegistryKey() != World.NETHER && this.method_48926().getRegistryKey() != World.END)
		{
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(this.method_48926());
			
			if(data != null && data.overridePhysics())
			{
				Vec3d position = this.getPos();
				Vec3d velocity = this.getVelocity();
				double airMultiplier = AirUtil.getAirResistanceMultiplier(this.method_48926(), data, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance.
				double d = this.isTouchingWater() ? 0.8f : 1.0 / (1.0 + (0.01 * airMultiplier));
				this.setVelocity(velocity.multiply(d));

				if(!this.hasNoGravity())
				{
					double gravity = data.isOrbit() ? 0.0f : (float) (0.04 * data.getGravity());
					this.addVelocity(0.0, -gravity, 0.0);
				}
				
				this.setPosition(position.x + velocity.x, position.y + velocity.y, position.z + velocity.z);
				this.checkBlockCollision();
				info.cancel();
			}
		}
	}
}