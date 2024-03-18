package space.mixin.client;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import space.planet.ClientPlanet;
import space.planet.ClientPlanetList;
import space.planet.PlanetDimensionData;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin
{
	@Inject(method = "Lnet/minecraft/client/render/BackgroundRenderer;applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZF)V", at = @At("TAIL"))
	private static void applyFogMixin(Camera camera, FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo info)
	{
		ClientPlanet viewpointPlanet = ClientPlanetList.getViewpointPlanet();
		PlanetDimensionData dimensionData = ClientPlanetList.getViewpointDimensionData();
		
		if(viewpointPlanet != null && dimensionData != null)
		{
			MinecraftClient client = MinecraftClient.getInstance();
			float fogStart = RenderSystem.getShaderFogStart();
			float fogEnd = RenderSystem.getShaderFogEnd();
			float fogFactor = 1.0f;
			
			if(dimensionData.isSky() && viewpointPlanet.hasCloudCover)
				fogFactor = MathHelper.clamp((float) Math.abs(client.world.getBottomY() - camera.getPos().getY()) / 128.0f, 0.1f, 1.0f);
			else if(dimensionData.isCloudy())
				fogFactor = MathHelper.clamp((float) Math.abs(client.world.getTopY() - camera.getPos().getY()) / 256.0f, 0.1f, 1.0f);
			
			if(fogFactor < 1.0f)
			{
				//System.out.println(fogFactor);
				RenderSystem.setShaderFogStart(fogStart * fogFactor);
				RenderSystem.setShaderFogEnd(fogEnd * fogFactor);
			}
		}
	}
}