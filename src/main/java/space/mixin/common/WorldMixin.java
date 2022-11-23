package space.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.planet.Planet;
import space.planet.PlanetList;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable
{
	@Shadow RegistryKey<World> registryKey;
	@Shadow @Final protected MutableWorldProperties properties;
	
	@Inject(method = "getSkyAngle(F)F", at = @At("HEAD"), cancellable = true)
	public void getSkyAngleInject(CallbackInfoReturnable<Float> info)
	{
		Planet p = PlanetList.getPlanetForWorld(registryKey);
		
		if(p != null)
		{
			info.setReturnValue((float) ((PlanetList.isOrbit(registryKey) ? p.sunAngleOrbit : p.sunAngle) / (Math.PI * 2.0)));
			info.cancel();
		}
	}
	
	@Inject(method = "getTimeOfDay()J", at = @At("HEAD"), cancellable = true)
	public void getTimeOfDayInject(CallbackInfoReturnable<Long> info)
	{
        Planet p = PlanetList.getPlanetForWorld(registryKey);
        
		if(p != null)
		{
			double angle = PlanetList.isOrbit(registryKey) ? p.sunAngleOrbit : p.sunAngle;
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
		Planet p = PlanetList.getPlanetForWorld(registryKey);
		
		if(p != null && (!p.hasWeather() || PlanetList.isOrbit(registryKey)))
		{
			info.setReturnValue(0.0f);
			info.cancel();
		}
	}

	@Inject(method = "getRainGradient(F)F", at = @At("HEAD"), cancellable = true)
	public void getRainGradient(float delta, CallbackInfoReturnable<Float> info)
	{
		Planet p = PlanetList.getPlanetForWorld(registryKey);
		
		if(p != null && (!p.hasWeather() || PlanetList.isOrbit(registryKey)))
		{
			info.setReturnValue(0.0f);
			info.cancel();
		}
	}
}