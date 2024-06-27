package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

@Mixin(Entity.class)
public abstract class EntityMixin
{
	/**
	 * Inject into the tickPortal() function for entities to travel between "surface" and "sky" dimensions.
	 */
	@Inject(method = "tickPortal()V", at = @At("TAIL"), cancellable = true)
	public void tickPortalInject(CallbackInfo info)
	{
		Entity entity = (Entity) (Object) this;
		
		if(entity != null && !entity.hasVehicle() && entity.getServer() != null && entity.getPortalCooldown() == 0)
		{
			World world = entity.getWorld();
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
			
			if(data != null)
			{
				ServerWorld next = null;
				Vec3d arrivalPos = null;
				int topThreshold = world.getTopY() - 4;
				int bottomThreshold = world.getBottomY() + 4;
				
				if(data.isSky() && data.getPlanet().getSurface() != null && entity.getBlockY() < bottomThreshold)
				{
					next = entity.getServer().getWorld(data.getPlanet().getSurface().getWorldKey());
					arrivalPos = new Vec3d(entity.getX(), topThreshold - 4.0, entity.getZ());
				}
				else if(!data.isOrbit() && !data.isSky() && data.getPlanet().getSky() != null && entity.getBlockY() > topThreshold)
				{
					next = entity.getServer().getWorld(data.getPlanet().getSky().getWorldKey());
					arrivalPos = new Vec3d(entity.getX(), bottomThreshold + 4.0, entity.getZ());
				}
				
				if(next != null)
				{
					TeleportTarget target = new TeleportTarget(next, arrivalPos, entity.getVelocity(), entity.getYaw(), entity.getPitch(), false, TeleportTarget.NO_OP);
					Entity transferred = entity.teleportTo(target);
					
					if(transferred != null)
						transferred.setPortalCooldown(60);
				}
			}
		}
	}
}