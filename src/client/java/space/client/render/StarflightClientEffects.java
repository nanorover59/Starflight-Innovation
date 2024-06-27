package space.client.render;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Matrix4f;
import org.slf4j.Logger;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class StarflightClientEffects
{
	public static VertexBuffer stars;
	public static VertexBuffer milkyWay;
	public static float radiation;
	
	@Nullable public static PostEffectProcessor bloomShader;
	@Nullable public static PostEffectProcessor radiationShader;
	
	public static void initializeBuffers()
	{
		stars = buildStars(stars);
		milkyWay = buildMilkyWay(milkyWay);
	}
	
	private static VertexBuffer buildMilkyWay(VertexBuffer vertexBuffer)
	{
		if(vertexBuffer != null)
			vertexBuffer.close();
		
		vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		BuiltBuffer builtBuffer = wrapAroundSky(64, 100.0f, 0.125f);
		vertexBuffer.bind();
		vertexBuffer.upload(builtBuffer);
		return vertexBuffer;
	}
	
	/**
	 * Used to properly render the milky way wrapped around the sky.
	 */
	private static BuiltBuffer wrapAroundSky(int segments, float radius, float textureRatio)
	{
		float height = radius * (float) Math.tan(Math.PI / segments) * (float) segments * textureRatio;
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		
		for(int i = 0; i < segments; i++)
		{
			double theta1 = (i * Math.PI * 2.0) / segments;
			double theta2 = ((i + 1) * Math.PI * 2.0) / segments;
			float y1 = radius * (float) Math.cos(theta1);
			float z1 = radius * (float) Math.sin(theta1);
			float y2 = radius * (float) Math.cos(theta2);
			float z2 = radius * (float) Math.sin(theta2);
			float u1 = (float) i / segments;
			float u2 = (float) (i + 1) / segments;
			
			bufferBuilder.vertex(-height, y1, z1).texture(u1, 0.0f);
			bufferBuilder.vertex(height, y1, z1).texture(u1, 1.0f);
			bufferBuilder.vertex(height, y2, z2).texture(u2, 1.0f);
			bufferBuilder.vertex(-height, y2, z2).texture(u2, 0.0f);
		}
		
		return bufferBuilder.end();
	}
	
	private static VertexBuffer buildStars(VertexBuffer vertexBuffer)
	{
		if(vertexBuffer != null)
			vertexBuffer.close();
		
		vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		BuiltBuffer builtBuffer = renderStars();
		vertexBuffer.bind();
		vertexBuffer.upload(builtBuffer);
		return vertexBuffer;
	}
	
	/**
	 * Render textured stars to a vertex buffer. Used by the sky render inject.
	 */
	private static BuiltBuffer renderStars()
	{
		Random random = Random.create(10842L);
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

		for(int i = 0; i < 4000; i++)
		{
			int frame = 0;

			while(random.nextFloat() < 0.2 && frame < 3)
				frame++;
			
			float d = random.nextFloat() * 2.0f - 1.0f;
			float e = random.nextFloat() * 2.0f - 1.0f;
			float f = random.nextFloat() * 2.0f - 1.0f;
			float g = 0.75f - (frame * 0.025f) + random.nextFloat() * 0.1f; // Star size.
			float h = d * d + e * e + f * f;

			if(h < 0.01 || h > 1.0)
				continue;

			h = 1.0f / Math.sqrt(h);
			float j = (d *= h) * 100.0f;
			float k = (e *= h) * 100.0f;
			float l = (f *= h) * 100.0f;
			float m = Math.atan2(d, f);
			float n = Math.sin(m);
			float o = Math.cos(m);
			float p = Math.atan2(Math.sqrt(d * d + f * f), e);
			float q = Math.sin(p);
			float r = Math.cos(p);
			float s = random.nextFloat() * MathHelper.PI * 2.0f;
			float t = Math.sin(s);
			float u = Math.cos(s);
			
			float interval = 1.0f / 4.0f;
			float startFrame = (float) (frame * interval);
			float endFrame = (float) (startFrame + interval);

			for(int v = 0; v < 4; v++)
			{
				float x = ((v & 2) - 1) * g;
				float y = ((v + 1 & 2) - 1) * g;
				float aa = x * u - y * t;
				float ac = y * u + x * t;
				float ad = aa * q + 0.0f * r;
				float ae = 0.0f * q - aa * r;
				float af = ae * n - ac * o;
				float ah = ac * n + ae * o;
				bufferBuilder.vertex(j + af, k + ad, l + ah).texture(v == 0 || v == 3 ? endFrame : startFrame, v < 2 ? 0.0f : 1.0f);
			}
		}
		
		return bufferBuilder.end();
	}
	
	public static void renderScreenGUIOverlay(MinecraftClient client, DrawContext context, int width, int height, float tickDelta)
	{
		for(int i = 0; i < height / 4; i++)
			context.fill(0, i * 4, width, (i * 4) + 1, -100, 0x108C1860);
		
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, (width / 2), (height / 2), -100.0f).color(0x00000000);

		for(int i = 0; i <= 16; i++)
		{
			float theta = (float) i * (float) (Math.PI * 2.0) / 16.0f;
			float sinTheta = MathHelper.sin(theta);
			float cosTheta = MathHelper.cos(theta);
			float a = 280.0f;
			float b = 200.0f;
			float radius = (a * b) / (float) (MathHelper.hypot(b * cosTheta, a * sinTheta));
			bufferBuilder.vertex(matrix, (width / 2) + (radius * cosTheta), (height / 2) - (radius * sinTheta), -100.0f).color(0x168C1860);
		}

		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		RenderSystem.disableBlend();
	}
	
	public static PostEffectProcessor loadShader(MinecraftClient client, String shaderName)
	{
		Logger logger = LogUtils.getLogger();
        Identifier identifier = Identifier.of("shaders/post/" + shaderName + ".json");
        
        try
        {
        	PostEffectProcessor shader = new PostEffectProcessor(client.getTextureManager(), client.getResourceManager(), client.getFramebuffer(), identifier);
        	shader.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
        	return shader;
        }
        catch (IOException iOException)
        {
        	logger.warn("Failed to load shader: {}", (Object) identifier, (Object) iOException);
        }
        catch (JsonSyntaxException jsonSyntaxException)
        {
        	logger.warn("Failed to parse shader: {}", (Object) identifier, (Object) jsonSyntaxException);
        }
        
        return null;
	}
}