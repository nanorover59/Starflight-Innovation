package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin extends Entity
{
	@Shadow abstract boolean isNoClip();
	@Shadow abstract protected float getDragInWater();
	
	public PersistentProjectileEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	/**
	 * Modified vanilla persistent projectile entity physics to account for different gravity and air resistance values.
	 */
	@Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal = 0), cancellable = true)
	public void tickInject(CallbackInfo info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());
			
			if(currentPlanet != null)
			{
				Vec3d position = this.getPos();
				Vec3d velocity = this.getVelocity();
				double airMultiplier = AirUtil.getAirResistanceMultiplier(world, currentPlanet, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance.
				double d = this.isTouchingWater() ? this.getDragInWater() : 1.0 / (1.0 + (0.01 * airMultiplier));
				this.setVelocity(velocity.multiply(d));

				if(!this.hasNoGravity() && !this.isNoClip())
				{
					double gravity = PlanetList.isOrbit(this.world.getRegistryKey()) ? 0.0f : (float) (0.04 * currentPlanet.getSurfaceGravity());
					this.addVelocity(0.0, -gravity, 0.0);
				}
				
				this.setPosition(position.x + velocity.x, position.y + velocity.y, position.z + velocity.z);
				this.checkBlockCollision();
				info.cancel();
			}
		}
	}
}