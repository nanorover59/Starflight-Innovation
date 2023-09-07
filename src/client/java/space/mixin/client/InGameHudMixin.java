package space.mixin.client;

import java.awt.Color;
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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import space.StarflightMod;
import space.entity.RocketEntity;
import space.item.SpaceSuitItem;
import space.item.StarflightItems;

@Environment(value=EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin
{
	@Shadow @Final private MinecraftClient client;
	
	private static final Identifier ROCKET_HUD = new Identifier(StarflightMod.MOD_ID, "textures/gui/starflight_hud.png");
	
	@Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;F)V", at = @At("HEAD"), cancellable = true)
	public void renderInject(DrawContext context, float tickDelta, CallbackInfo info)
	{
		if(client.player != null && !client.options.hudHidden)
		{
			TextRenderer textRenderer = client.textRenderer;
			int scaledWidth = this.client.getWindow().getScaledWidth();
            int scaledHeight = this.client.getWindow().getScaledHeight();
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, new Identifier(StarflightMod.MOD_ID, "textures/gui/starflight_hud.png"));
			
			if(client.player.hasVehicle() && client.player.getVehicle() instanceof RocketEntity)
			{
				RocketEntity rocketEntity = (RocketEntity) client.player.getVehicle();
	            int barHeight = 128;
	            
	            // Numerical Display
	            int displayX = 8;
	            int displayY = 8;
	            float velocity = (float) (Math.sqrt(Math.pow(rocketEntity.getTrackedVelocity().x(), 2.0) + Math.pow(rocketEntity.getTrackedVelocity().y(), 2.0) + Math.pow(rocketEntity.getTrackedVelocity().z(), 2.0)) * 20.0);
	            DecimalFormat df = new DecimalFormat("#.#");
	            context.drawTexture(ROCKET_HUD, displayX, displayY, 32, 0, 80, 32, 256, 256);
	            
	            // Throttle
	            float throttleLevel = (float) MathHelper.lerp(tickDelta, rocketEntity.throttlePrevious, rocketEntity.throttle);
	            int throttleX = 8;
	            int throttleY = scaledHeight - barHeight - 8;
	            int throttleOffset = (int) Math.round(barHeight * (1.0 - throttleLevel));
	            context.drawTexture(ROCKET_HUD, throttleX, throttleY, 0, 0, 16, barHeight, 256, 256);
	            context.drawTexture(ROCKET_HUD, throttleX, throttleY + throttleOffset, 0, barHeight + throttleOffset, 16, (int) Math.round(barHeight * throttleLevel), 256, 256);
	            
	            // Fuel Gauges
	            float oxygenLevel = rocketEntity.getOxygenLevel();
	            float hydrogenLevel = rocketEntity.getHydrogenLevel();
	            int fuelX = scaledWidth - 24;
	            int gaugeY = throttleY;
	            context.drawTexture(ROCKET_HUD, fuelX, gaugeY, 16, 0, 16, barHeight, 256, 256); 
	            int oxygenOffset = (int) Math.round(barHeight * (1.0 - oxygenLevel));
	            int hydrogenOffset = (int) Math.round(barHeight * (1.0 - hydrogenLevel));
	            context.drawTexture(ROCKET_HUD, fuelX + 8, gaugeY + oxygenOffset, 24, barHeight + oxygenOffset, 8, (int) Math.round(barHeight * oxygenLevel), 256, 256);
	            context.drawTexture(ROCKET_HUD, fuelX, gaugeY + hydrogenOffset, 16, barHeight + hydrogenOffset, 8, (int) Math.round(barHeight * hydrogenLevel), 256, 256);
	            
	            // Heading
	            //int headingX = 8;
	            //int headingY = 32;
	            //context.drawTexture(ROCKET_HUD, headingX, headingY, 32, 32, 80, 80, 256, 256);
	            
	            // Draw Text
	            context.drawTextWithShadow(textRenderer, "A: " + df.format(rocketEntity.getAltitude()) + "m", displayX + 8, displayY + 3, Color.BLUE.getRGB());
	            context.drawTextWithShadow(textRenderer, "V: " + df.format(velocity) + "m/s", displayX + 8, displayY + 13, velocity > 10.0f ? Color.RED.getRGB() : Color.BLUE.getRGB());
	            
	            if(rocketEntity.getUserInput())
	            	context.drawTextWithShadow(textRenderer, "User Control", displayX + 8, displayY + 22, Color.BLUE.getRGB());
	            
	            info.cancel();
			}
			else if(!client.player.isCreative() && !client.player.isSpectator())
			{
				for(ItemStack stack : client.player.getArmorItems())
				{
					if(stack.getItem() == StarflightItems.SPACE_SUIT_CHESTPLATE)
					{
						float oxygenLevel = (float) (stack.getNbt().getDouble("oxygen") / SpaceSuitItem.MAX_OXYGEN);
						int barHeight = 64;
						int oxygenX = scaledWidth - 24;
						int oxygenY = 8;
						int oxygenOffset = (int) Math.round(barHeight * (1.0 - oxygenLevel));
						context.drawTexture(ROCKET_HUD, oxygenX, oxygenY, 240, 0, 16, barHeight, 256, 256);
						context.drawTexture(ROCKET_HUD, oxygenX, oxygenY + oxygenOffset, 240, barHeight + oxygenOffset, 16, (int) Math.round(barHeight * oxygenLevel), 256, 256);

						break;
					}
				}
			}
		}
	}
}