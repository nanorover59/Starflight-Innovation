package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.world.World;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin extends Entity
{
	public ExperienceOrbEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}

	/**
	 * Modified vanilla experience orb entity physics to account for different gravity and air resistance values.
	 */
	@Inject(method = "tick()V", at = @At("TAIL"), cancellable = true)
	public void tickInject(CallbackInfo info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);

			if(data != null && data.overridePhysics() && !this.onGround)
			{
				double airMultiplier = AirUtil.getAirResistanceMultiplier(world, data, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance.
				this.setVelocity(this.getVelocity().multiply(1.02));
				
				if(!this.hasNoGravity())
		            this.setVelocity(this.getVelocity().add(0.0, 0.03 - (0.04 * data.getGravity()), 0.0));
				
				this.setVelocity(this.getVelocity().multiply((float) (1.0 / (1.0 + (0.02 * airMultiplier)))));
			}
		}
	}
}