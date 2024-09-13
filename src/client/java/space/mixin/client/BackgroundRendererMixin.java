package space.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.MathHelper;
import space.planet.Planet;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin
{
	@Inject(method = "Lnet/minecraft/client/render/BackgroundRenderer;applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZF)V", at = @At("TAIL"))
	private static void applyFogMixin(Camera camera, FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo info)
	{
		Planet viewpointPlanet = PlanetList.getClient().getViewpointPlanet();
		PlanetDimensionData dimensionData = PlanetList.getClient().getViewpointDimensionData();
		
		if(viewpointPlanet != null && dimensionData != null)
		{
			MinecraftClient client = MinecraftClient.getInstance();
			float fogStart = RenderSystem.getShaderFogStart();
			float fogEnd = RenderSystem.getShaderFogEnd();
			float fogFactor = 1.0f;
			
			if(dimensionData.isSky() && viewpointPlanet.hasCloudCover())
				fogFactor = MathHelper.clamp((float) (camera.getPos().getY() - client.world.getBottomY()) / 128.0f, 0.1f, 1.0f);
			else if(dimensionData.isCloudy())
				fogFactor = MathHelper.clamp((float) (client.world.getTopY() - camera.getPos().getY()) / 256.0f, 0.1f, 1.0f);
			else if(dimensionData.getPlanet().getName().equals("mars") && client.world.isRaining())
				fogFactor = MathHelper.clamp((float) (camera.getPos().getY() - client.world.getBottomY()) / 320.0f, 0.15f, 1.0f) / client.world.getRainGradient(tickDelta);
			
			if(fogFactor < 1.0f)
			{
				RenderSystem.setShaderFogStart(fogStart * fogFactor);
				RenderSystem.setShaderFogEnd(fogEnd * fogFactor);
			}
		}
	}
}