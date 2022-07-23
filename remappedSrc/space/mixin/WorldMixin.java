package space.mixin;

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
import net.minecraft.world.dimension.DimensionType;
import space.planet.Planet;
import space.planet.PlanetList;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable
{
	@Shadow RegistryKey<World> registryKey;
	@Shadow DimensionType dimension;
	@Shadow @Final protected MutableWorldProperties properties;
	
	@Override
	public float getSkyAngle(float tickDelta)
	{
		Planet p = PlanetList.getPlanetForWorld(registryKey);
		
		if(p != null)
			return (float) (p.getSunAngleXZ(PlanetList.isOrbit(registryKey)) / (Math.PI * 2.0));
		
		return dimension.getSkyAngle(properties.getTimeOfDay());
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
