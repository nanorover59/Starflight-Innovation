package space.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;

@Environment(value=EnvType.CLIENT)
@Mixin(DimensionEffects.class)
public class DimensionEffectsMixin
{
	@Inject(method = "getFogColorOverride(FF)[F", at = @At("HEAD"), cancellable = true)
	private void getFogColorOverrideInject(float skyAngle, float tickDelta, CallbackInfoReturnable<float[]> info)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		RegistryKey<World> worldKey = client.world.getRegistryKey();
		Planet planet = PlanetList.getPlanetForWorld(worldKey);
		
		if(planet != null && (PlanetList.isOrbit(worldKey) || planet.getSurfacePressure() == 0.0))
		{
			info.setReturnValue(null);
			info.cancel();
		}
	}
}