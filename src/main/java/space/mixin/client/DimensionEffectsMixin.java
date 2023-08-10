package space.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.world.dimension.DimensionType;
import space.client.StarflightModClient;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

@Environment(value=EnvType.CLIENT)
@Mixin(DimensionEffects.class)
public class DimensionEffectsMixin
{
	@Inject(method = "byDimensionType(Lnet/minecraft/world/dimension/DimensionType;)Lnet/minecraft/client/render/DimensionEffects;", at = @At("HEAD"), cancellable = true)
	private static void byDimensionTypeInject(DimensionType dimensionType, CallbackInfoReturnable<DimensionEffects> info)
	{
		DimensionEffects dimensionEffect = StarflightModClient.getDimensionEffect(dimensionType.effects());
		
		if(dimensionEffect != null)
		{
			info.setReturnValue(dimensionEffect);
			info.cancel();
		}
	}
	
	@Inject(method = "getFogColorOverride(FF)[F", at = @At("HEAD"), cancellable = true)
	private void getFogColorOverrideInject(float skyAngle, float tickDelta, CallbackInfoReturnable<float[]> info)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(client.world);
		
		if(data != null && data.overrideSky() && (data.isOrbit() || data.getPressure() == 0.0))
		{
			info.setReturnValue(null);
			info.cancel();
		}
	}
}