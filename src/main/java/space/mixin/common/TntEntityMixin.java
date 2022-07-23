package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.TntEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity
{
	@Shadow abstract int getFuse();
	@Shadow abstract void setFuse(int fuse);
	@Shadow abstract void explode();
	
	protected TntEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}

	/**
	 * Modified vanilla TNT entity physics to account for different gravity and air resistance values.
	 */
	@Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
	public void tickInject(CallbackInfo info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());

			if(currentPlanet != null)
			{
				if(!(this.hasNoGravity() || PlanetList.isOrbit(this.world.getRegistryKey())))
					this.setVelocity(this.getVelocity().add(0.0, -0.04 * currentPlanet.getSurfaceGravity(), 0.0));
				
				double airMultiplier = AirUtil.getAirResistanceMultiplier(world, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance;
				this.move(MovementType.SELF, this.getVelocity());
				this.setVelocity(this.getVelocity().multiply((float) (1.0 / (1.0 + (0.02 * airMultiplier)))));

				if(this.onGround)
					this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
				
				int i = this.getFuse() - 1;
				this.setFuse(i);

				if(i <= 0)
				{
					this.discard();

					if(!this.world.isClient)
						this.explode();
				}
				else
				{
					this.updateWaterState();

					if(this.world.isClient)
						this.world.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
				}
				
				info.cancel();
			}
		}
	}
}
