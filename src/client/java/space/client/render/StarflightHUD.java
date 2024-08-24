package space.client.render;

import java.awt.Color;
import java.text.DecimalFormat;

import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import space.StarflightMod;
import space.client.gui.SpaceNavigationScreen;
import space.entity.RocketEntity;
import space.item.StarflightItems;

public class StarflightHUD
{
	private static final Identifier ROCKET_HUD = Identifier.of(StarflightMod.MOD_ID, "textures/gui/starflight_hud.png");
	
	public static void renderPlayerHUD(MinecraftClient client, DrawContext context, float tickDelta)
	{
		int scaledWidth = client.getWindow().getScaledWidth();
		
		for(ItemStack stack : client.player.getArmorItems())
		{
			if(stack.getItem() == StarflightItems.SPACE_SUIT_CHESTPLATE)
			{
				float oxygenLevel = stack.get(StarflightItems.OXYGEN) / stack.get(StarflightItems.MAX_OXYGEN);
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
		//DecimalFormat df = new DecimalFormat("#.#");
        int hudColor = 0xC800FF00;
        int throttleColor = 0xC8960928;
        int oxygenColor = 0xC8707CFe;
        int hydrogenColor = 0xC8FF4800;
        float hudScale = 0.5f;
        
        if(rocketEntity.clientQuaternion == null || rocketEntity.clientQuaternionPrevious == null)
        	return;
        
        Quaternionf quaternion = new Quaternionf(rocketEntity.clientQuaternionPrevious).slerp(rocketEntity.clientQuaternion, tickDelta);
        scaledWidth /= hudScale;
		scaledHeight /= hudScale;
		int centerX = scaledWidth / 2;
        int centerY = scaledHeight / 2;
		context.getMatrices().push();
		context.getMatrices().scale(hudScale, hudScale, hudScale);
		drawReadouts(context, textRenderer, rocketEntity, tickDelta, 40, 40, hudColor);
		drawThrottle(context, textRenderer, rocketEntity, tickDelta, 40, scaledHeight - 200, throttleColor);
		drawFuelLevels(context, textRenderer, rocketEntity, tickDelta, scaledWidth - 80, scaledHeight - 200, hydrogenColor, oxygenColor);
		drawNavBall(context, tickDelta, rocketEntity.getForwardDirection(), quaternion, new Vector3f(rocketEntity.getTrackedVelocity()), 200, 200);
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
	
	public static void drawNavBall(DrawContext context, float tickDelta, Direction direction, Quaternionf rotation, Vector3f velocity, int x, int y)
	{
		// Draw to a frame buffer and then scale it to create a pixel art appearance.
		int frameWidth = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
		int frameHeight = MinecraftClient.getInstance().getWindow().getFramebufferHeight();
		float ratio = (float) frameHeight / (float) frameWidth;
		Framebuffer navBallBuffer = new SimpleFramebuffer(800, (int) (800 * ratio), true, MinecraftClient.IS_SYSTEM_MAC);
		navBallBuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		navBallBuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
		navBallBuffer.beginWrite(false);
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		context.getMatrices().push();
		context.getMatrices().translate(x, y, 0.0f);
		context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
		context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction.asRotation()));
		context.getMatrices().multiply(rotation.invert());
		context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
		context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
		Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
		int colorTop = 0xff0088ff;
		int colorBottom = 0xffff8800;
		int stacks = 32;
		int slices = 32;
		float radius = 48.0f;
		
		// Central Sphere
		for(int i = 0; i < stacks; i++)
		{
			float theta1 = (float) (i * Math.PI / stacks);
			float theta2 = (float) ((i + 1) * Math.PI / stacks);
			int color = i >= stacks / 2 ? colorTop : colorBottom;
			BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
			
			for(int j = 0; j <= slices; j++)
			{
				float phi = (float) (j * 2.0 * Math.PI / slices);
				float x0 = (float) (Math.cos(phi));
				float y0 = (float) (Math.sin(phi));
				float x1 = (float) (Math.sin(theta1) * x0);
				float y1 = (float) Math.cos(theta1);
				float z1 = (float) (Math.sin(theta1) * y0);
				float x2 = (float) (Math.sin(theta2) * x0);
				float y2 = (float) Math.cos(theta2);
				float z2 = (float) (Math.sin(theta2) * y0);
				bufferBuilder.vertex(matrix4f, radius * x1, radius * y1, radius * z1).color(color);
				bufferBuilder.vertex(matrix4f, radius * x2, radius * y2, radius * z2).color(color);
			}
			
			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		}

		radius += 0.05f;
		
		// Latitude Lines
		for(int lat = 0; lat <= 180; lat += 45)
		{
			float theta1 = Math.toRadians(lat - 1.0f);
			float theta2 = Math.toRadians(lat + 1.0f);
			BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
			
			for(int i = 0; i <= slices; i++)
			{
				float phi = (float) (i * 2.0 * Math.PI / slices);
				float x0 = (float) (Math.cos(phi));
				float y0 = (float) (Math.sin(phi));
				float x1 = (float) (Math.sin(theta1) * x0);
				float y1 = (float) Math.cos(theta1);
				float z1 = (float) (Math.sin(theta1) * y0);
				float x2 = (float) (Math.sin(theta2) * x0);
				float y2 = (float) Math.cos(theta2);
				float z2 = (float) (Math.sin(theta2) * y0);
				bufferBuilder.vertex(matrix4f, radius * x1, radius * y1, radius * z1).color(0xffffffff);
				bufferBuilder.vertex(matrix4f, radius * x2, radius * y2, radius * z2).color(0xffffffff);
			}
			
			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		}
		
		radius += 0.05f;
		
		// Longitude Lines
		for(int lon = 0; lon < 360; lon += 45)
		{
			context.getMatrices().push();
			context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
			context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(lon));
			Matrix4f lonMatrix4f = context.getMatrices().peek().getPositionMatrix();
			context.getMatrices().pop();
			float theta1 = Math.toRadians(89.0f);
			float theta2 = Math.toRadians(91.0f);
			int color = lon == 0 ? 0xffdf7126 : 0xffffffff;
			BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
			
			for(int i = 1; i <= slices - 1; i++)
			{
				float phi = (float) (i * Math.PI / slices);
				float x0 = (float) (Math.cos(phi));
				float y0 = (float) (Math.sin(phi));
				float x1 = (float) (Math.sin(theta1) * x0);
				float y1 = (float) Math.cos(theta1);
				float z1 = (float) (Math.sin(theta1) * y0);
				float x2 = (float) (Math.sin(theta2) * x0);
				float y2 = (float) Math.cos(theta2);
				float z2 = (float) (Math.sin(theta2) * y0);
				bufferBuilder.vertex(lonMatrix4f, radius * x1, radius * y1, radius * z1).color(color);
				bufferBuilder.vertex(lonMatrix4f, radius * x2, radius * y2, radius * z2).color(color);
			}
			
			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		}
		
		radius += 0.05f;
		Matrix3f matrix3f = new Matrix3f();
		matrix4f.get3x3(matrix3f);
		velocity.mul(matrix3f).normalize().mul(-radius);
		context.getMatrices().pop();
		context.getMatrices().push();
		context.getMatrices().translate(x, y, 0.0f);
		matrix4f = context.getMatrices().peek().getPositionMatrix();
		context.getMatrices().pop();
		
		// Prograde and Retrograde Markers
		float size = 8.0f;
		RenderSystem.disableDepthTest();
		
		if(velocity.z() > 0)
		{
			float x0 = velocity.x();
			float y0 = velocity.y();
			float z0 = velocity.z();
			RenderSystem.setShader(GameRenderer::getPositionTexProgram);
	        RenderSystem.setShaderTexture(0, Identifier.of(StarflightMod.MOD_ID, "textures/gui/navball.png"));
	        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
	        bufferBuilder.vertex(matrix4f, x0 - size, y0 + size, z0).texture(0.0f, 0.5f);
	        bufferBuilder.vertex(matrix4f, x0 + size, y0 + size, z0).texture(0.5f, 0.5f);
	        bufferBuilder.vertex(matrix4f, x0 + size, y0 - size, z0).texture(0.5f, 0.0f);
	        bufferBuilder.vertex(matrix4f, x0 - size, y0 - size, z0).texture(0.0f, 0.0f);
			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		}
		else
		{
			velocity.negate();
			float x0 = velocity.x();
			float y0 = velocity.y();
			float z0 = velocity.z();
			RenderSystem.setShader(GameRenderer::getPositionTexProgram);
	        RenderSystem.setShaderTexture(0, Identifier.of(StarflightMod.MOD_ID, "textures/gui/navball.png"));
	        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
	        bufferBuilder.vertex(matrix4f, x0 - size, y0 + size, z0).texture(0.5f, 0.5f);
	        bufferBuilder.vertex(matrix4f, x0 + size, y0 + size, z0).texture(1.0f, 0.5f);
	        bufferBuilder.vertex(matrix4f, x0 + size, y0 - size, z0).texture(1.0f, 0.0f);
	        bufferBuilder.vertex(matrix4f, x0 - size, y0 - size, z0).texture(0.5f, 0.0f);
			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		}
		
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, Identifier.of(StarflightMod.MOD_ID, "textures/gui/navball.png"));
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f, -size * 2.0f, size, -radius).texture(0.0f, 1.0f);
        bufferBuilder.vertex(matrix4f, size * 2.0f, size, -radius).texture(1.0f, 1.0f);
        bufferBuilder.vertex(matrix4f, size * 2.0f, -size, -radius).texture(1.0f, 0.5f);
        bufferBuilder.vertex(matrix4f, -size * 2.0f, -size, -radius).texture(0.0f, 0.5f);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		
		RenderSystem.enableDepthTest();
		navBallBuffer.endWrite();
		MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		navBallBuffer.draw(frameWidth, frameHeight, false);
		RenderSystem.disableBlend();
		navBallBuffer.delete();
	}
}