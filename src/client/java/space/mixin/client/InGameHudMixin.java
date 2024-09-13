package space.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.gui.SpaceNavigationScreen;
import space.client.render.StarflightHUD;
import space.entity.AirshipEntity;
import space.entity.RocketEntity;

@Environment(value=EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin
{
	@Shadow @Final private MinecraftClient client;
	
	@Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("HEAD"), cancellable = true)
	public void renderInject(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info)
	{
		if(client.player != null && !client.options.hudHidden)
		{
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, Identifier.of(StarflightMod.MOD_ID, "textures/gui/starflight_hud.png"));
			
			if(client.player.hasVehicle() && !(client.currentScreen instanceof SpaceNavigationScreen))
			{
				if(client.player.getVehicle() instanceof RocketEntity)
				{
					StarflightHUD.renderSpacecraftHUD(client, context, tickCounter.getTickDelta(false));
		            info.cancel();
				}
				else if(client.player.getVehicle() instanceof AirshipEntity)
				{
					StarflightHUD.renderAirshipHUD(client, context, tickCounter.getTickDelta(false));
		            info.cancel();
				}
			}
			else if(!client.player.isCreative() && !client.player.isSpectator())
				StarflightHUD.renderPlayerHUD(client, context, tickCounter.getTickDelta(false));
		}
	}
}