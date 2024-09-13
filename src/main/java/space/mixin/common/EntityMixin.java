package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(Entity.class)
public abstract class EntityMixin
{
	/**
	 * Modified vanilla entity physics to account for different gravity values.
	 */
	@Inject(method = "getFinalGravity()D", at = @At("RETURN"), cancellable = true)
	public void getFinalGravityInject(CallbackInfoReturnable<Double> info)
	{
		Entity entity = (Entity) (Object) this;
		
		if(entity != null)
		{
			World world = entity.getWorld();
			
			if(world.getRegistryKey() != World.OVERWORLD && world.getRegistryKey() != World.NETHER && world.getRegistryKey() != World.END)
			{
				PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
				
				if(data != null && data.overridePhysics())
					info.setReturnValue(info.getReturnValue() * data.getGravity());
			}
		}
	}
	
	/**
	 * Modified vanilla entity physics to account for different air resistance values.
	 */
	@Inject(method = "tick()V", at = @At("TAIL"), cancellable = true)
	public void tickInject(CallbackInfo info)
	{
		Entity entity = (Entity) (Object) this;
		
		if(entity != null && !(entity instanceof LivingEntity))
		{
			World world = entity.getWorld();
			
			if(world.getRegistryKey() != World.OVERWORLD && world.getRegistryKey() != World.NETHER && world.getRegistryKey() != World.END)
			{
				PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
	
				if(data != null && data.overridePhysics())
				{
					double airMultiplier = AirUtil.getAirResistanceMultiplier(world, data, entity.getBlockPos()); // Atmospheric pressure multiplier for air resistance.
					entity.setVelocity(entity.getVelocity().multiply((1.0 / 0.98) * (1.0 / (1.0 + (0.02 * airMultiplier)))));
				} 
			}
		}
	}
	
	/**
	 * Inject into the tickPortal() function for entities to travel between "surface" and "sky" dimensions.
	 */
	@Inject(method = "tickPortalTeleportation()V", at = @At("HEAD"), cancellable = true)
	public void tickPortalTeleportationInject(CallbackInfo info)
	{
		Entity entity = (Entity) (Object) this;
		
		if(entity != null)
		{
			World world = entity.getWorld();
			
			if(!world.isClient() && !entity.hasVehicle() && entity.getServer() != null && entity.getPortalCooldown() == 0)
			{
				PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
				
				if(data != null)
				{
					ServerWorld next = null;
					Vec3d arrivalPos = null;
					int topThreshold = world.getTopY();
					int bottomThreshold = world.getBottomY();
					Vec3d velocity = entity.getVelocity();
					
					if(data.isSky() && data.getPlanet().getSurface() != null && entity.getBlockY() < bottomThreshold && velocity.getY() < 0.0)
					{
						next = entity.getServer().getWorld(data.getPlanet().getSurface().getWorldKey());
						arrivalPos = new Vec3d(entity.getX(), topThreshold, entity.getZ());
					}
					else if(!data.isOrbit() && !data.isSky() && data.getPlanet().getSky() != null && entity.getBlockY() > topThreshold && velocity.getY() > 0.0)
					{
						next = entity.getServer().getWorld(data.getPlanet().getSky().getWorldKey());
						arrivalPos = new Vec3d(entity.getX(), bottomThreshold, entity.getZ());
					}
					
					if(next != null)
					{
						TeleportTarget target = new TeleportTarget(next, arrivalPos, entity.getVelocity(), entity.getYaw(), entity.getPitch(), false, TeleportTarget.NO_OP);
						Entity transferred = entity.teleportTo(target);
						
						if(transferred != null)
						{
							transferred.setPortalCooldown(60);
							transferred.setVelocity(velocity);
							transferred.velocityDirty = true;
							info.cancel();
						}
					}
				}
			}
		}
	}
}