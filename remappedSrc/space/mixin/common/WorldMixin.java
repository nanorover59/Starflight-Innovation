package space.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.planet.Planet;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.IWorldMixin;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable, IWorldMixin
{
	@Shadow @Final MutableWorldProperties properties;
	@Shadow RegistryKey<World> registryKey;
	
	private boolean planetCheck = true;
	private PlanetDimensionData planetData = null;
	
	public PlanetDimensionData getPlanetDimensionData()
	{
		if(isClient())
			return PlanetList.viewpointDimensionData;
		
		if(planetCheck)
		{
			for(Planet planet : PlanetList.getPlanets())
			{
				if(planet.getOrbit() != null && planet.getOrbit().getWorldKey() == registryKey)
					planetData = planet.getOrbit();
				else if(planet.getSurface() != null && planet.getSurface().getWorldKey() == registryKey)
					planetData = planet.getSurface();
				else if(planet.getSky() != null && planet.getSky().getWorldKey() == registryKey)
					planetData = planet.getSky();
			}
			
			planetCheck = false;
		}
		
		return planetData;
	}
	
	public void clearPlanetDimensionData()
	{
		planetCheck = true;
	}
	
	@Override
	public float getSkyAngle(float f)
	{
		if(isClient() && PlanetList.hasViewpoint)
			return (float) ((PlanetList.inOrbit ? PlanetList.sunAngleOrbit : PlanetList.sunAngle) / (Math.PI * 2.0));
		else
		{
			PlanetDimensionData data = getPlanetDimensionData();
			
			if(data != null && data.overrideSky())
			{
				Planet planet = data.getPlanet();
				return (float) ((data.isOrbit() ? planet.sunAngleOrbit : planet.sunAngle) / (Math.PI * 2.0));
			}
		}
		
		return this.getDimension().getSkyAngle(this.getLunarTime());
	}
	
	@Inject(method = "getTimeOfDay()J", at = @At("HEAD"), cancellable = true)
	public void getTimeOfDayInject(CallbackInfoReturnable<Long> info)
	{
		PlanetDimensionData data = getPlanetDimensionData();
        
		if(data != null && data.overrideSky())
		{
			double angle = 0.0;
			
			if(isClient())
				angle = PlanetList.inOrbit ? PlanetList.sunAngleOrbit : PlanetList.sunAngle;
			else
			{
				Planet planet = data.getPlanet();
				angle = data.isOrbit() ? planet.sunAngleOrbit : planet.sunAngle;
			}
			
			angle -= Math.PI / 2.0;
			
			if(angle < 0.0)
				angle += Math.PI * 2.0;
			
			info.setReturnValue(24000L - (long) ((angle / (Math.PI * 2.0)) * 24000.0));
			info.cancel();
		}
    }
	
	@Inject(method = "getThunderGradient(F)F", at = @At("HEAD"), cancellable = true)
	public void getThunderGradientInject(float delta, CallbackInfoReturnable<Float> info)
	{
		PlanetDimensionData data = getPlanetDimensionData();
		
		if(data != null && data.overrideSky() && !data.hasWeather())
		{
			info.setReturnValue(0.0f);
			info.cancel();
		}
	}

	@Inject(method = "getRainGradient(F)F", at = @At("HEAD"), cancellable = true)
	public void getRainGradient(float delta, CallbackInfoReturnable<Float> info)
	{
		PlanetDimensionData data = getPlanetDimensionData();
		
		if(data != null && data.overrideSky() && !data.hasWeather())
		{
			info.setReturnValue(0.0f);
			info.cancel();
		}
	}
}