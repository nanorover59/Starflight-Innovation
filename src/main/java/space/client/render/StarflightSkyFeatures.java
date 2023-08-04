package space.client.render;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilder.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class StarflightSkyFeatures
{
	public static VertexBuffer stars;
	public static VertexBuffer milkyWay;
	
	@Nullable public static ShaderEffect bloomShader;
	
	public static void initializeBuffers()
	{
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		stars = buildStars(stars, bufferBuilder);
		milkyWay = buildMilkyWay(milkyWay, bufferBuilder);
	}
	
	private static VertexBuffer buildMilkyWay(VertexBuffer vertexBuffer, BufferBuilder bufferBuilder)
	{
		if(vertexBuffer != null)
			vertexBuffer.close();
		
		vertexBuffer = new VertexBuffer();
		BuiltBuffer builtBuffer = wrapAroundSky(bufferBuilder, 64, 100.0f, 0.125f);
		vertexBuffer.bind();
		vertexBuffer.upload(builtBuffer);
		return vertexBuffer;
	}
	
	/**
	 * Used to properly render the milky way wrapped around the sky.
	 */
	private static BuiltBuffer wrapAroundSky(BufferBuilder bufferBuilder, int segments, float radius, float textureRatio)
	{
		float height = radius * (float) Math.tan(Math.PI / segments) * (float) segments * textureRatio;
		
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		
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
			
			bufferBuilder.vertex(-height, y1, z1).texture(u1, 0.0f).next();
			bufferBuilder.vertex(height, y1, z1).texture(u1, 1.0f).next();
			bufferBuilder.vertex(height, y2, z2).texture(u2, 1.0f).next();
			bufferBuilder.vertex(-height, y2, z2).texture(u2, 0.0f).next();
		}
		
		return bufferBuilder.end();
	}
	
	private static VertexBuffer buildStars(VertexBuffer vertexBuffer, BufferBuilder bufferBuilder)
	{
		if(vertexBuffer != null)
			vertexBuffer.close();
		
		vertexBuffer = new VertexBuffer();
		BuiltBuffer builtBuffer = renderStars(bufferBuilder);
		vertexBuffer.bind();
		vertexBuffer.upload(builtBuffer);
		return vertexBuffer;
	}
	
	/**
	 * Render textured stars to a vertex buffer. Used by the sky render inject.
	 */
	private static BuiltBuffer renderStars(BufferBuilder buffer)
	{
		Random random = Random.create(2048L);
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

		for(int i = 0; i < 4000; i++)
		{
			int frame = 0;

			while(random.nextFloat() < 0.2 && frame < 3)
				frame++;
			
			double d = random.nextFloat() * 2.0f - 1.0f;
			double e = random.nextFloat() * 2.0f - 1.0f;
			double f = random.nextFloat() * 2.0f - 1.0f;
			double g = 0.75f - (frame * 0.025f) + random.nextFloat() * 0.1f; // Star size.
			double h = d * d + e * e + f * f;

			if(h < 0.01 || h > 1.0)
				continue;

			h = 1.0 / Math.sqrt(h);
			double j = (d *= h) * 100.0;
			double k = (e *= h) * 100.0;
			double l = (f *= h) * 100.0;
			double m = Math.atan2(d, f);
			double n = Math.sin(m);
			double o = Math.cos(m);
			double p = Math.atan2(Math.sqrt(d * d + f * f), e);
			double q = Math.sin(p);
			double r = Math.cos(p);
			double s = random.nextDouble() * Math.PI * 2.0;
			double t = Math.sin(s);
			double u = Math.cos(s);
			
			double interval = 1.0f / 4.0f;
			float startFrame = (float) (frame * interval);
			float endFrame = (float) (startFrame + interval);

			for(int v = 0; v < 4; v++)
			{
				double x = (double) ((v & 2) - 1) * g;
				double y = (double) ((v + 1 & 2) - 1) * g;
				double aa = x * u - y * t;
				double ac = y * u + x * t;
				double ad = aa * q + 0.0 * r;
				double ae = 0.0 * q - aa * r;
				double af = ae * n - ac * o;
				double ah = ac * n + ae * o;
				buffer.vertex(j + af, k + ad, l + ah).texture(v == 0 || v == 3 ? endFrame : startFrame, v < 2 ? 0.0f : 1.0f).next();
			}
		}
		
		return buffer.end();
	}
	
	public static void loadBloomShader(MinecraftClient client)
	{
		if(bloomShader != null)
			bloomShader.close();
		
		Logger logger = LogUtils.getLogger();
        Identifier identifier = new Identifier("shaders/post/bloom.json");
        
        try
        {
        	bloomShader = new ShaderEffect(client.getTextureManager(), client.getResourceManager(), client.getFramebuffer(), identifier);
        	bloomShader.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
        }
        catch (IOException iOException)
        {
        	logger.warn("Failed to load shader: {}", (Object) identifier, (Object) iOException);
            bloomShader = null;
        }
        catch (JsonSyntaxException jsonSyntaxException)
        {
        	logger.warn("Failed to parse shader: {}", (Object) identifier, (Object) jsonSyntaxException);
            bloomShader = null;
        }
	}
}