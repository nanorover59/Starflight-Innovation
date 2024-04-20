package space.client.render;

import java.awt.Color;
import java.text.DecimalFormat;

import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import space.StarflightMod;
import space.entity.RocketEntity;
import space.item.SpaceSuitItem;
import space.item.StarflightItems;

public class StarflightHUD
{
	private static final Identifier ROCKET_HUD = new Identifier(StarflightMod.MOD_ID, "textures/gui/starflight_hud.png");
	
	public static void renderPlayerHUD(MinecraftClient client, DrawContext context, float tickDelta)
	{
		int scaledWidth = client.getWindow().getScaledWidth();
		
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
	
	public static void renderSpacecraftHUD(MinecraftClient client, DrawContext context, float tickDelta)
	{
		TextRenderer textRenderer = client.textRenderer;
		int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
		RocketEntity rocketEntity = (RocketEntity) client.player.getVehicle();
		DecimalFormat df = new DecimalFormat("#.#");
        int hudColor = 0xC800FF00;
        int throttleColor = 0xC8960928;
        int oxygenColor = 0xC8707CFe;
        int hydrogenColor = 0xC8FF4800;
        int divisions = 5;
        int attitudeWidth = 240;
        int attitudeHeight = 180;
        int pitchXGap = 96;
        float hudScale = 0.5f;
        
        if(rocketEntity.clientQuaternion == null || rocketEntity.clientQuaternionPrevious == null)
        	return;
        
        Quaternionf quaternion = new Quaternionf(rocketEntity.clientQuaternionPrevious).slerp(rocketEntity.clientQuaternion, tickDelta).normalize();
        Vector3f angles = new Vector3f();
        quaternion.getEulerAnglesYXZ(angles);
        float pitch;
        float yaw;
        
        switch(rocketEntity.getForwardDirection())
        {
        	case NORTH:
        		pitch = angles.x() * MathHelper.DEGREES_PER_RADIAN;
                yaw = angles.z() * -MathHelper.DEGREES_PER_RADIAN;
        		break;
        	case EAST:
        		pitch = angles.z() * MathHelper.DEGREES_PER_RADIAN;
                yaw = angles.x() * MathHelper.DEGREES_PER_RADIAN;
        		break;
        	case SOUTH:
        		pitch = angles.x() * -MathHelper.DEGREES_PER_RADIAN;
                yaw = angles.z() * MathHelper.DEGREES_PER_RADIAN;
        		break;
        	default:
        		pitch = angles.z() * -MathHelper.DEGREES_PER_RADIAN;
                yaw = angles.x() * -MathHelper.DEGREES_PER_RADIAN;
        		break;
        }
        
        quaternion.getEulerAnglesZXY(angles);
        float roll = angles.y() * -MathHelper.DEGREES_PER_RADIAN;
        //Vector3f upRotated = new Vector3f(0.0f, 0.0f, 1.0f).rotate(quaternion);
        //float heading = Math.atan2(upRotated.x(), upRotated.z()) * MathHelper.DEGREES_PER_RADIAN;
        scaledWidth /= hudScale;
		scaledHeight /= hudScale;
		int centerX = scaledWidth / 2;
        int centerY = scaledHeight / 2;
		context.getMatrices().push();
		context.getMatrices().scale(hudScale, hudScale, hudScale);
		drawCrossHairs(context, tickDelta, centerX, centerY, hudColor);
		drawReadouts(context, textRenderer, rocketEntity, tickDelta, 40, 40, hudColor);
		drawThrottle(context, textRenderer, rocketEntity, tickDelta, 40, scaledHeight - 200, throttleColor);
		drawFuelLevels(context, textRenderer, rocketEntity, tickDelta, scaledWidth - 80, scaledHeight - 200, hydrogenColor, oxygenColor);
		context.getMatrices().translate(centerX, centerY, 0.0f);
		context.getMatrices().multiply(new Quaternionf().fromAxisAngleDeg(new Vector3f(0.0f, 0.0f, 1.0f), -yaw));
		context.getMatrices().translate(-centerX, -centerY, 0.0f);
		drawVelocityHeading(context, tickDelta, quaternion, new Vector3f(0.0f, 1.0f, 0.0f).rotate(quaternion).normalize(), new Vector3f(rocketEntity.getTrackedVelocity()).normalize(), rocketEntity.getForwardDirection(), centerX, centerY, hudColor);
		context.getMatrices().push();
		context.getMatrices().translate(yaw * 4.0f, 0.0f, 0.0f);

		for(int i = -180; i <= 180; i += divisions)
		{
			int x = i;

			if(x + yaw < -179)
				x += 360;
			else if(x + yaw > 180)
				x -= 360;
			
			x *= 4;
			x += centerX;

			if(i > -180 && x + yaw * 4 > centerX - attitudeWidth && x + yaw * 4 < centerX + attitudeWidth)
			{
				int length = i % 15 == 0 ? 24 : 16;
				context.drawVerticalLine(x, centerY - attitudeHeight, centerY - attitudeHeight + length, hudColor);
	
				if(i % 15 == 0 && (x + yaw * 4 < centerX - 30 || x + yaw * 4 > centerX + 30))
					context.drawCenteredTextWithShadow(textRenderer, df.format(i), x, centerY - attitudeHeight - 16, hudColor);
			}
		}
		
		context.getMatrices().pop();
		context.getMatrices().push();
		context.getMatrices().translate(roll * 4.0f, 0.0f, 0.0f);

		for(int i = -180; i <= 180; i += divisions)
		{
			int x = -i;

			if(x + roll < -179)
				x += 360;
			else if(x + roll > 180)
				x -= 360;
			
			x *= 4;
			x += centerX;

			if(i > -180 && x + roll * 4 > centerX - attitudeWidth && x + roll * 4 < centerX + attitudeWidth)
			{
				int length = i % 15 == 0 ? 24 : 16;
				drawDashedVerticalLine(context, x, centerY + attitudeHeight - length, centerY + attitudeHeight, 4, hudColor);
	
				if(i % 15 == 0 && (x + roll * 4 < centerX - 30 || x + roll * 4 > centerX + 30))
					context.drawCenteredTextWithShadow(textRenderer, df.format(i), x, centerY + attitudeHeight + 16, hudColor);
			}
		}
		
		context.getMatrices().pop();
		context.drawBorder(centerX - 18, centerY - attitudeHeight - 20, 36, 16, hudColor);
		context.drawCenteredTextWithShadow(textRenderer, df.format(-yaw), centerX, centerY - attitudeHeight - 16, hudColor);
		context.drawBorder(centerX - 18, centerY + attitudeHeight + 12, 36, 16, hudColor);
		context.drawCenteredTextWithShadow(textRenderer, df.format(-roll), centerX, centerY + attitudeHeight + 16, hudColor);
		context.getMatrices().push();
		context.getMatrices().translate(0.0f, pitch * 4.0f, 0.0f);

		for(int i = -180; i <= 180; i += divisions)
		{
			int y = -i;

			if(y + pitch < -179)
				y += 360;
			else if(y + pitch > 180)
				y -= 360;
			
			y *= 4;
			y += centerY;

			if(i > -180 && y + pitch * 4 > centerY - attitudeHeight + 32 && y + pitch * 4 < centerY + attitudeHeight - 32)
			{
				int length = i % 15 == 0 ? 96 : 48;
				
				if(i < 0)
				{
					drawDashedHorizontalLine(context, centerX - pitchXGap - length, centerX - pitchXGap, y, 4, hudColor);
					drawDashedHorizontalLine(context, centerX + pitchXGap, centerX + pitchXGap + length, y, 4, hudColor);
				}
				else
				{
					context.drawHorizontalLine(centerX - pitchXGap - length, centerX - pitchXGap, y, hudColor);
					context.drawHorizontalLine(centerX + pitchXGap, centerX + pitchXGap + length, y, hudColor);
				}
	
				if(i % 15 == 0)
				{
					if(y + pitch * 4 < centerY - 14 || y + pitch * 4 > centerY + 14)
						context.drawCenteredTextWithShadow(textRenderer, df.format(i), centerX - pitchXGap - 120, y, hudColor);
					
					context.drawCenteredTextWithShadow(textRenderer, df.format(i), centerX + pitchXGap + 120, y, hudColor);
				}
			}
		}

		context.getMatrices().pop();
		
		context.drawBorder(centerX - pitchXGap - 138, centerY - 4, 36, 16, hudColor);
		context.drawCenteredTextWithShadow(textRenderer, df.format(pitch), centerX - pitchXGap - 120, centerY, hudColor);
		
		context.getMatrices().pop();
	}
	
	public static void drawDashedVerticalLine(DrawContext context, int x, int y1, int y2, int interval, int hudColor)
	{
		boolean b = false;
		
		for(int y = y1; y < y2; y += interval)
		{
			if(b)
				context.drawVerticalLine(x, y + interval, y, hudColor);
				
			b = !b;
		}
	}
	
	public static void drawDashedHorizontalLine(DrawContext context, int x1, int x2, int y, int interval, int hudColor)
	{
		boolean b = false;
		
		for(int x = x1; x < x2; x += interval)
		{
			if(b)
				context.drawHorizontalLine(x - interval, x, y, hudColor);
				
			b = !b;
		}
	}
	
	public static void drawReadouts(DrawContext context, TextRenderer textRenderer, RocketEntity rocketEntity, float tickDelta, int x, int y, int hudColor)
	{
		float altitude = rocketEntity.getAltitude();
		float velocity = rocketEntity.getTrackedVelocity().length() * 20.0f;
		
		if(rocketEntity.getTrackedVelocity().y() < 0.0f)
		{
			if(velocity > 10.0f)
				hudColor = Color.RED.getRGB();
			else if(velocity > 5.0f)
				hudColor = Color.YELLOW.getRGB();
		}
		
		DecimalFormat df = new DecimalFormat("#.#");
		context.drawTextWithShadow(textRenderer, "ALT: " + df.format(altitude) + "m", x, y, hudColor);
		context.drawTextWithShadow(textRenderer, "VEL: " + df.format(velocity) + "m/s", x, y + 16, hudColor);
	}
	
	public static void drawThrottle(DrawContext context, TextRenderer textRenderer, RocketEntity rocketEntity, float tickDelta, int x, int y, int throttleColor)
	{
		int barWidth = 16;
		int barHeight = 160;
		float throttleLevel = (float) MathHelper.lerp(tickDelta, rocketEntity.throttlePrevious, rocketEntity.throttle);
		int throttleOffset = (int) Math.round(barHeight * (1.0 - throttleLevel));
		DecimalFormat df = new DecimalFormat("#");
		context.fill(x, y, x + barWidth, y + barHeight, 0x80000017);
		context.fill(x, y + throttleOffset, x + barWidth, y + barHeight, throttleColor);
		context.drawCenteredTextWithShadow(textRenderer, "T", x + (barWidth / 2), y + barHeight + 4, throttleColor);
		context.drawCenteredTextWithShadow(textRenderer, df.format(throttleLevel * 100.0f) + "%", x + (barWidth / 2) , y + barHeight + 20, throttleColor);
	}
	
	public static void drawFuelLevels(DrawContext context, TextRenderer textRenderer, RocketEntity rocketEntity, float tickDelta, int x, int y, int hydrogenColor, int oxygenColor)
	{
		int barWidth = 16;
		int barHeight = 160;
		float hydrogenLevel = rocketEntity.getHydrogenLevel();
		float oxygenLevel = rocketEntity.getOxygenLevel();
        int hydrogenOffset = (int) Math.round(barHeight * (1.0 - hydrogenLevel));
        int oxygenOffset = (int) Math.round(barHeight * (1.0 - oxygenLevel));
        DecimalFormat df = new DecimalFormat("#");
        context.fill(x, y, x + barWidth, y + barHeight, 0x80000017);
		context.fill(x, y + hydrogenOffset, x + barWidth, y + barHeight, hydrogenColor);
		context.fill(x + (barWidth * 2), y, x +  + (barWidth * 3), y + barHeight, 0x80000017);
		context.fill(x + (barWidth * 2), y + oxygenOffset, x + (barWidth * 3), y + barHeight, oxygenColor);
		context.drawCenteredTextWithShadow(textRenderer, "H2", x + (barWidth / 2), y + barHeight + 4, hydrogenColor);
		context.drawCenteredTextWithShadow(textRenderer, df.format(hydrogenLevel * 100.0f) + "%", x + (barWidth / 2) , y + barHeight + 20, hydrogenColor);
		context.drawCenteredTextWithShadow(textRenderer, "O2", x + (int) (barWidth * 2.5), y + barHeight + 4, oxygenColor);
		context.drawCenteredTextWithShadow(textRenderer, df.format(oxygenLevel * 100.0f) + "%", x + (int) (barWidth * 2.5), y + barHeight + 20, oxygenColor);
	}
	
	public static void drawCrossHairs(DrawContext context, float tickDelta, int x, int y, int hudColor)
	{
		context.drawHorizontalLine(x - 16, x - 48, y, hudColor);
		context.drawHorizontalLine(x + 17, x + 49, y, hudColor);
		
		for(int i = 0; i <= 16; i++)
		{
			context.drawHorizontalLine(x - 16 + i, x - 16 + i, y + i, hudColor);
			context.drawHorizontalLine(x + 17 - i, x + 17 - i, y + i, hudColor);
		}
	}
	
	public static void drawVelocityHeading(DrawContext context, float tickDelta, Quaternionf referenceRotation, Vector3f forward, Vector3f velocity, Direction forwardDirection, int x, int y, int hudColor)
	{
		Quaternionf quaternion = new Quaternionf().rotateTo(velocity, forward).mul(referenceRotation);
        Vector3f angles = new Vector3f();
        quaternion.getEulerAnglesYXZ(angles);
        float vPitch;
        float vYaw;
        
        switch(forwardDirection)
        {
        	case NORTH:
        		vPitch = angles.x() * MathHelper.DEGREES_PER_RADIAN;
        		vYaw = angles.z() * -MathHelper.DEGREES_PER_RADIAN;
        		break;
        	case EAST:
        		vPitch = angles.z() * MathHelper.DEGREES_PER_RADIAN;
        		vYaw = angles.x() * MathHelper.DEGREES_PER_RADIAN;
        		break;
        	case SOUTH:
        		vPitch = angles.x() * -MathHelper.DEGREES_PER_RADIAN;
        		vYaw = angles.z() * MathHelper.DEGREES_PER_RADIAN;
        		break;
        	default:
        		vPitch = angles.z() * -MathHelper.DEGREES_PER_RADIAN;
        		vYaw = angles.x() * -MathHelper.DEGREES_PER_RADIAN;
        		break;
        }
        
        if(vPitch < -179)
        	vPitch += 360;
		else if(vPitch > 180)
			vPitch -= 360;
        
        if(vYaw < -179)
        	vYaw += 360;
		else if(vYaw > 180)
			vYaw -= 360;
        
        context.getMatrices().push();
		context.getMatrices().translate(x + vYaw * 4.0f, y + vPitch * 4.0f, 0.0f);
		context.drawBorder(-4, -4, 9, 9, hudColor);
		context.drawHorizontalLine(-9, -5, 0, hudColor);
		context.drawHorizontalLine(5, 9, 0, hudColor);
		context.drawVerticalLine(0, -4, -9, hudColor);
		context.getMatrices().pop();
		quaternion = new Quaternionf().rotateTo(new Vector3f(velocity).negate(), forward).mul(referenceRotation);
        quaternion.getEulerAnglesYXZ(angles);
        
        switch(forwardDirection)
        {
        	case NORTH:
        		vPitch = angles.x() * MathHelper.DEGREES_PER_RADIAN;
        		vYaw = angles.z() * -MathHelper.DEGREES_PER_RADIAN;
        		break;
        	case EAST:
        		vPitch = angles.z() * MathHelper.DEGREES_PER_RADIAN;
        		vYaw = angles.x() * MathHelper.DEGREES_PER_RADIAN;
        		break;
        	case SOUTH:
        		vPitch = angles.x() * -MathHelper.DEGREES_PER_RADIAN;
        		vYaw = angles.z() * MathHelper.DEGREES_PER_RADIAN;
        		break;
        	default:
        		vPitch = angles.z() * -MathHelper.DEGREES_PER_RADIAN;
        		vYaw = angles.x() * -MathHelper.DEGREES_PER_RADIAN;
        		break;
        }
        
		if(vPitch < -179)
			vPitch += 360;
		else if(vPitch > 180)
			vPitch -= 360;

		if(vYaw < -179)
			vYaw += 360;
		else if(vYaw > 180)
			vYaw -= 360;
		
		context.getMatrices().push();
		context.getMatrices().translate(x + vYaw * 4.0f, y + vPitch * 4.0f, 0.0f);
		context.drawBorder(-4, -4, 9, 9, hudColor);
		context.drawHorizontalLine(-9, -5, 4, hudColor);
		context.drawHorizontalLine(5, 9, 4, hudColor);
		context.drawVerticalLine(0, -4, -9, hudColor);
		context.getMatrices().pop();
	}
}