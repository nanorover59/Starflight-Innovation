package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin extends Entity
{
	@Shadow abstract double getMaxSpeed();

	protected AbstractMinecartEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}

	/**
	 * Modified vanilla minecart entity physics to account for different gravity and air resistance values.
	 */
	@Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;moveOffRail()V", shift = At.Shift.AFTER))
	public void tickInject(CallbackInfo info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);

			if(data != null && !this.onGround && data.overridePhysics() && !(this.hasNoGravity() || data.isOrbit()))
			{
				double d = this.isTouchingWater() ? 0.005 : 0.04;
				this.setVelocity(this.getVelocity().add(0.0, d - (d * data.getGravity()), 0.0));
			}
		}
	}

	@Inject(method = "moveOffRail()V", at = @At("HEAD"), cancellable = true)
	public void moveOffRailInject(CallbackInfo info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);

			if(data != null && data.overridePhysics() && !this.onGround)
			{
				double airMultiplier = AirUtil.getAirResistanceMultiplier(world, data, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance.
				double d = Math.min(this.getMaxSpeed() / (airMultiplier + Double.MIN_VALUE), 20.0);
				Vec3d vec3d = this.getVelocity();
				this.setVelocity(MathHelper.clamp(vec3d.x, -d, d), vec3d.y, MathHelper.clamp(vec3d.z, -d, d));

				if(this.onGround)
					this.setVelocity(this.getVelocity().multiply(0.5));
				
				this.move(MovementType.SELF, this.getVelocity());

				if(!this.onGround)
					this.setVelocity(this.getVelocity().multiply(1.0 / (1.0 + (0.05 * airMultiplier))));
				
				info.cancel();
			}
		}
	}
}