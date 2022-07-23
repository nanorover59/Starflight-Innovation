package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity
{
	public FallingBlockEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	/**
	 * Modified vanilla falling block entity physics to account for different gravity and air resistance values.
	 */
	@Inject(method = "tick()V", at = @At("TAIL"), cancellable = true)
	public void tickInject(CallbackInfo info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());

			if(currentPlanet != null)
			{
				double airMultiplier = AirUtil.getAirResistanceMultiplier(world, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance.
				this.setVelocity(this.getVelocity().multiply(1.0 / 0.98));
				
				if(!this.hasNoGravity() && !this.onGround)
		            this.setVelocity(this.getVelocity().add(0.0, 0.04 - (0.04 * currentPlanet.getSurfaceGravity()), 0.0));
				
				this.setVelocity(this.getVelocity().multiply((float) (1.0 / (1.0 + (0.02 * airMultiplier)))));
			}
		}
	}
}
