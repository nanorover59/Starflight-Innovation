package space.mixin.client;

import java.text.DecimalFormat;

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
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import space.StarflightMod;
import space.entity.RocketEntity;

@Environment(value=EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin
{
	@Shadow @Final private MinecraftClient client;
	
	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At("HEAD"), cancellable = true)
	public void renderInject(MatrixStack matrices, float tickDelta, CallbackInfo info)
	{
		if(client.player != null && !client.options.hudHidden && client.player.hasVehicle() && client.player.getVehicle() instanceof RocketEntity)
		{
			RocketEntity rocketEntity = (RocketEntity) client.player.getVehicle();
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, new Identifier(StarflightMod.MOD_ID, "textures/gui/rocket_hud.png"));
            int scaledWidth = this.client.getWindow().getScaledWidth();
            int scaledHeight = this.client.getWindow().getScaledHeight();
            int barHeight = 148;
            
            // Numerical Display
            int displayX = 8;
            int displayY = 8;
            boolean down = rocketEntity.getTrackedVelocity().getY() < 0.0;
            float vy = Math.abs(rocketEntity.getTrackedVelocity().getY()) * 20.0f;
            float vxz = (float) (Math.sqrt(Math.pow(rocketEntity.getTrackedVelocity().getX(), 2.0) + Math.pow(rocketEntity.getTrackedVelocity().getZ(), 2.0)) * 20.0);
            DecimalFormat df = new DecimalFormat("#.#");
            DrawableHelper.drawTexture(matrices, displayX, displayY, 24, 0, 100, 62, 256, 256);
            
            // Throttle
            float throttle = (float) MathHelper.lerp(tickDelta, rocketEntity.throttlePrevious, rocketEntity.throttle);
            int throttleX = 16;
            int throttleY = scaledHeight - barHeight - 8;
            DrawableHelper.drawTexture(matrices, throttleX, throttleY, 0, 0, 12, barHeight, 256, 256);
            DrawableHelper.drawTexture(matrices, throttleX - 3, throttleY + (int) Math.round(barHeight * (1.0 - throttle)) - 4, 0, barHeight, 18, 8, 256, 256);
            
            // Fuel Gauges
            float oxygenLevel = rocketEntity.getOxygenLevel();
            float hydrogenLevel = rocketEntity.getHydrogenLevel();
            int oxygenX = scaledWidth - 16;
            int hydrogenX = scaledWidth - 32;
            int gaugeY = throttleY;
            DrawableHelper.drawTexture(matrices, oxygenX, gaugeY, 0, 0, 12, barHeight, 256, 256);
            DrawableHelper.drawTexture(matrices, hydrogenX, gaugeY, 0, 0, 12, barHeight, 256, 256);    
            int oxygenOffset = (int) Math.round(barHeight * (1.0 - oxygenLevel));
            int hydrogenOffset = (int) Math.round(barHeight * (1.0 - hydrogenLevel));
            DrawableHelper.drawTexture(matrices, oxygenX + 3, gaugeY + oxygenOffset, 12, oxygenOffset, 6, (int) Math.round(barHeight * oxygenLevel), 256, 256);
            DrawableHelper.drawTexture(matrices, hydrogenX + 3, gaugeY + hydrogenOffset, 18, hydrogenOffset, 6, (int) Math.round(barHeight * hydrogenLevel), 256, 256);
            
            // Draw Text
            client.textRenderer.drawWithShadow(matrices, "ALT: " + df.format(rocketEntity.getAltitude()) + "m", displayX + 8, displayY + 8, 0x6abe30);
            client.textRenderer.drawWithShadow(matrices, "VY: " + df.format(vy) + "m/s", displayX + 8, displayY + 20, vy > 10.0f && down ? 0xdc3222 : 0x6abe30);
            client.textRenderer.drawWithShadow(matrices, "VXZ: " + df.format(vxz) + "m/s", displayX + 8, displayY + 32, vxz > 10.0f ? 0xdc3222 : 0x6abe30);
            
            if(rocketEntity.getUserInput())
            	client.textRenderer.drawWithShadow(matrices, "User Control", displayX + 8, displayY + 44, 0x6abe30);
            
            info.cancel();
		}
	}
}