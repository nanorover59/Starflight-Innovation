package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;
import space.planet.PlanetDimensionData;
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
		if(this.getWorld().getRegistryKey() != World.OVERWORLD && this.getWorld().getRegistryKey() != World.NETHER && this.getWorld().getRegistryKey() != World.END)
		{
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(this.getWorld());

			if(data != null && data.overridePhysics())
			{
				double airMultiplier = AirUtil.getAirResistanceMultiplier(this.getWorld(), data, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance.
				this.setVelocity(this.getVelocity().multiply(1.0 / 0.98));
				
				if(!this.hasNoGravity() && !this.isOnGround())
		            this.setVelocity(this.getVelocity().add(0.0, 0.04 - (0.04 * data.getGravity()), 0.0));
				
				this.setVelocity(this.getVelocity().multiply((float) (1.0 / (1.0 + (0.02 * airMultiplier)))));
			}
		}
	}
}