package space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.BoatEntity.Location;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends Entity
{
	@Shadow Location location;
	@Shadow float velocityDecay;
	@Shadow float yawVelocity;
	@Shadow abstract void setPaddleMovings(boolean leftMoving, boolean rightMoving);
	@Shadow boolean pressingForward;
	@Shadow boolean pressingLeft;
	@Shadow boolean pressingRight;
	
	protected BoatEntityMixin(EntityType<? extends BoatEntity> type, World world)
	{
		super(type, world);
	}
	
	/**
	 * Modified vanilla boat entity physics to account for different gravity and air resistance values.
	 */
	@Inject(method = "updateVelocity()V", at = @At("TAIL"), cancellable = true)
	public void updateVelocityInject(CallbackInfo info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());

			if(currentPlanet != null && this.location == Location.IN_AIR)
			{
				double airMultiplier = AirUtil.getAirResistanceMultiplier(world, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance.
				double d = (float) ((1.0 / this.velocityDecay) * (1.0 / (1.0 + ((1.0 - this.velocityDecay) * airMultiplier))));
				this.setVelocity(this.getVelocity().x * d, this.getVelocity().y, this.getVelocity().z * d);
				
				if(!(this.hasNoGravity() || PlanetList.isOrbit(this.world.getRegistryKey())))
		            this.setVelocity(this.getVelocity().add(0.0, 0.04 - (0.04 * currentPlanet.getSurfaceGravity()), 0.0));
				
				this.yawVelocity *= d;
			}
		}
	}
	
	@Inject(method = "updatePaddles()V", at = @At("HEAD"), cancellable = true)
	public void updatePaddlesInject(CallbackInfo info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());

			if(currentPlanet != null && this.location == Location.IN_AIR)
			{
				this.setPaddleMovings(this.pressingRight && !this.pressingLeft || this.pressingForward, this.pressingLeft && !this.pressingRight || this.pressingForward);
				info.cancel();
			}
		}
	}
}
