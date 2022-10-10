package space.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import space.planet.PlanetRenderList;

@Environment(value=EnvType.CLIENT)
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin
{
	@Inject(method = "render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V", at = @At("HEAD"), cancellable = true)
	private static void injected(Camera camera, float tickDelta, ClientWorld world, int i, float f, CallbackInfo info)
	{
		if(PlanetRenderList.isViewpointInOrbit() || (PlanetRenderList.getViewpointPlanet() != null && PlanetRenderList.getViewpointPlanet().getSurfacePressure() < 0.001))
			info.cancel();
	}
}