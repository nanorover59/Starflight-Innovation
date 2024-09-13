package space.mixin.client;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import space.StarflightMod;
import space.client.render.StarflightSkyFeatures;
import space.planet.PlanetRenderList;
import space.planet.PlanetRenderer;

@Environment(value=EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin
{
	@Shadow @Final private MinecraftClient client;
	@Shadow private VertexBuffer starsBuffer;
	@Shadow abstract void renderEndSky(MatrixStack matrices);
	@Shadow private ClientWorld world;
	@Shadow private VertexBuffer lightSkyBuffer;
	@Shadow private VertexBuffer darkSkyBuffer;
	@Shadow private boolean cloudsDirty = true;
	@Shadow @Nullable private VertexBuffer cloudsBuffer;
	@Shadow private int lastCloudsBlockX = Integer.MIN_VALUE;
	@Shadow private int lastCloudsBlockY = Integer.MIN_VALUE;
	@Shadow private int lastCloudsBlockZ = Integer.MIN_VALUE;
	@Shadow private Vec3d lastCloudsColor = Vec3d.ZERO;
	@Shadow abstract BufferBuilder.BuiltBuffer renderClouds(BufferBuilder builder, double x, double y, double z, Vec3d color);
	@Shadow private int ticks;
	@Shadow @Final private static Identifier SUN;
	@Shadow @Final private static Identifier MOON_PHASES;
	@Shadow @Final private static Identifier CLOUDS;
	
	@Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
	public void renderSkyInject(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo info)
	{
		ArrayList<PlanetRenderer> planetList = PlanetRenderList.getRenderers(true);
		PlanetRenderer viewpointPlanet = PlanetRenderList.getViewpointPlanet();
		
		if(viewpointPlanet != null)
		{
			boolean starrySky = PlanetRenderList.isViewpointInOrbit() || viewpointPlanet.getSurfacePressure() < 0.001d;
			boolean cloudySky = viewpointPlanet.hasCloudCover();
			Vec3d viewpointPlanetPosition = viewpointPlanet.getPosition(tickDelta);
			
			// Find the position of the sun and background stars in angular coordinates.
			Vec3d starPosition = new Vec3d(0.0d, 0.0d, 0.0d);
			Vec3d viewpoint = PlanetRenderList.isViewpointInOrbit() ? viewpointPlanet.getParkingOrbitViewpoint(tickDelta) : viewpointPlanet.getSurfaceViewpoint(tickDelta);
			double azimuthOfViewpoint = Math.atan2(viewpointPlanetPosition.getZ() - viewpoint.getZ(), viewpointPlanetPosition.getX() - viewpoint.getX());
			double azimuthOfStar = Math.atan2(starPosition.getZ() - viewpoint.getZ(), starPosition.getX() - viewpoint.getX());
			double trueAzimuth = azimuthOfViewpoint - azimuthOfStar;
			
			// Setup for sky rendering.
			BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
			BackgroundRenderer.setFogBlack();
			BackgroundRenderer.clearFog();
			RenderSystem.depthMask(false);
			RenderSystem.disableBlend();
			RenderSystem.disableTexture();
			
			// Ensure a black background.
			RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
			RenderSystem.setShader(GameRenderer::getPositionShader);
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
			bufferBuilder.vertex(projectionMatrix, -10.0f, -10.0f, 0.0f).next();
			bufferBuilder.vertex(projectionMatrix,  10.0f, -10.0f, 0.0f).next();
			bufferBuilder.vertex(projectionMatrix,  10.0f,  10.0f, 0.0f).next();
			bufferBuilder.vertex(projectionMatrix, -10.0f,  10.0f, 0.0f).next();
			BufferRenderer.drawWithShader(bufferBuilder.end());
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			
			// Render the stars and milky way.
			Vec3d viewpointVector = viewpoint.subtract(viewpointPlanetPosition);
			double phiViewpoint = Math.atan2(viewpointVector.getZ(), viewpointVector.getX());	
			matrices.push();
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90.0f));
			matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion((float) viewpointPlanet.getPrecession()));
			matrices.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion((float) viewpointPlanet.getObliquity()));
			matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion((float) phiViewpoint));
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(60.0f));
			
			float rainGradient = starrySky ? 0.0f : this.world.getRainGradient(tickDelta);
			float s = cloudySky ? 0.0f : (1.0f - rainGradient);
			float starFactor = starrySky ? 1.0f : this.world.method_23787(tickDelta) * s * 2.0f;

			if(starFactor > 0.0f)
			{
				Matrix4f matrix4f3 = matrices.peek().getPositionMatrix();
				float milkyWayFactor = 0.8f;
				RenderSystem.enableTexture();
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderColor(starFactor * milkyWayFactor, starFactor * milkyWayFactor, starFactor * milkyWayFactor, starFactor * milkyWayFactor);
				RenderSystem.setShaderTexture(0, new Identifier(StarflightMod.MOD_ID, "textures/environment/milky_way.png"));
				StarflightSkyFeatures.milkyWay.bind();
				StarflightSkyFeatures.milkyWay.draw(matrix4f3, projectionMatrix, GameRenderer.getPositionTexShader());
				RenderSystem.setShaderColor(starFactor, starFactor, starFactor, starFactor);
				RenderSystem.setShaderTexture(0, new Identifier(StarflightMod.MOD_ID, "textures/environment/stars.png"));
				StarflightSkyFeatures.stars.bind();
				StarflightSkyFeatures.stars.draw(matrix4f3, projectionMatrix, GameRenderer.getPositionTexShader());
	            VertexBuffer.unbind();
			}
			
			matrices.pop();
			
			// Render all celestial objects.
			float celestialFactor = Math.min(starFactor + 0.3f, 1.0f);
			celestialFactor = Math.max(celestialFactor - rainGradient, 0.0f);
			
			for(PlanetRenderer planetRenderer : planetList)
			{
				if(planetRenderer != null)
					planetRenderer.doRender(bufferBuilder, matrices, tickDelta, celestialFactor, s < 0.95f);
			}
			
			RenderSystem.disableTexture();
			
			// Apply the bloom shader effect for players with fabulous graphics.
			if(MinecraftClient.isFabulousGraphicsOrBetter() && StarflightSkyFeatures.bloomShader != null && starFactor > 0.5f)
			{
				StarflightSkyFeatures.bloomShader.render(tickDelta);
				client.getFramebuffer().beginWrite(false);
			}
			
			// Apply the sky color on atmospheric planets.
			if(!starrySky)
			{
				Vec3d skyRGB = world.getSkyColor(client.gameRenderer.getCamera().getPos(), tickDelta);
				float skyR = (float) skyRGB.getX();
				float skyG = (float) skyRGB.getY();
				float skyB = (float) skyRGB.getZ();
				float v = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(tickDelta) * ((float)Math.PI * 2)) * 2.0f + 0.5f, 0.0f, 1.0f);
				Vec3d vec3d = client.gameRenderer.getCamera().getPos().subtract(2.0, 2.0, 2.0).multiply(0.25);
				Vec3d fogRGB = CubicSampler.sampleColor(vec3d, (x, y, z) -> world.getDimensionEffects().adjustFogColor(Vec3d.unpackRgb(client.world.getBiomeForNoiseGen(x, y, z).value().getFogColor()), v));
				float fogR = (float) fogRGB.getX();
				float fogG = (float) fogRGB.getY();
				float fogB = (float) fogRGB.getZ();
				float rain = world.getRainGradient(tickDelta);
				float thunder = world.getThunderGradient(tickDelta);
				float range = Math.min(128.0f, client.gameRenderer.getViewDistance());
	            
				if(rain > 0.0f)
				{
					float rgFactor = 1.0f - rain * 0.5f;
					float bFactor = 1.0f - rain * 0.4f;
					fogR *= rgFactor;
					fogG *= rgFactor;
					fogB *= bFactor;
				}
				
				if(thunder > 0.0f)
				{
					float factor = 1.0f - rain * 0.5f;
					fogR *= factor;
					fogG *= factor;
					fogB *= factor;
				}
				
				RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
				RenderSystem.setShader(GameRenderer::getPositionColorShader);
				RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
				Matrix4f matrix4f = matrices.peek().getPositionMatrix();
		        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		        bufferBuilder.vertex(matrix4f, 0.0f, 16.0f, 0.0f).color(skyR, skyG, skyB, 0.8f).next();
		        
		        for(int i = 0; i <= 16; i++)
				{
					float theta = (float) i * (float) (Math.PI * 2.0) / 16.0f;
					float sinTheta = MathHelper.sin(theta);
					float cosTheta = MathHelper.cos(theta);
		        	bufferBuilder.vertex(matrix4f, range * cosTheta, 0.0f, range * sinTheta).color(fogR, fogG, fogB, 0.8f).next();
				}
		        
		        BufferRenderer.drawWithShader(bufferBuilder.end());
		        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		        bufferBuilder.vertex(matrix4f, 0.0f, -16.0f, 0.0f).color(fogR, fogG, fogB, 0.8f).next();
		        
		        for(int i = 0; i <= 16; i++)
				{
					float theta = (float) i * (float) (Math.PI * 2.0) / 16.0f;
					float sinTheta = MathHelper.sin(theta);
					float cosTheta = MathHelper.cos(theta);
		        	bufferBuilder.vertex(matrix4f, range * cosTheta, 0.0f, -range * sinTheta).color(fogR, fogG, fogB, 0.8f).next();
				}
		        
		        BufferRenderer.drawWithShader(bufferBuilder.end());
		        
				// Render the sunset effect.
				float skyAngle = (float) trueAzimuth;
				float[] fs = this.world.getDimensionEffects().getFogColorOverride((float) (skyAngle / (Math.PI * 2.0d)), tickDelta);
				
				if(fs != null)
				{
					RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
					matrices.push();
					matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0f));
		            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(MathHelper.sin(this.world.getSkyAngleRadians(tickDelta)) < 0.0f ? 180.0f : 0.0f));
		            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90.0f));
		            matrix4f = matrices.peek().getPositionMatrix();
					bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
					bufferBuilder.vertex(matrix4f, 0.0f, 100.0f, 0.0f).color(fs[0], fs[1], fs[2], fs[3]).next();

					for(int i = 0; i <= 16; i++)
					{
						float theta = (float) i * (float) (Math.PI * 2.0) / 16.0f;
						float sinTheta = MathHelper.sin(theta);
						float cosTheta = MathHelper.cos(theta);
						bufferBuilder.vertex(matrix4f, sinTheta * 120.0f, cosTheta * 120.0f, -cosTheta * 40.0f * fs[3]).color(fs[0], fs[1], fs[2], 0.0f).next();
					}

					BufferRenderer.drawWithShader(bufferBuilder.end());
					matrices.pop();
				}
			}
			
			// End of custom sky rendering.
			RenderSystem.enableTexture();
			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
			info.cancel();
		}
	}
	
	@Inject(method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FDDD)V", at = @At("HEAD"), cancellable = true)
	public void renderCloudsInject(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double d, double e, double f, CallbackInfo info)
	{
		if(PlanetRenderList.getViewpointPlanet() != null && (PlanetRenderList.isViewpointInOrbit() || !PlanetRenderList.getViewpointPlanet().hasLowClouds()))
			info.cancel();
	}
	
	@Inject(method = "renderStars()V", at = @At("HEAD"))
	public void renderStarsInject(CallbackInfo info)
	{
		StarflightSkyFeatures.initializeBuffers();
	}
	
	@Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("TAIL"))
	public void reloadInject(ResourceManager manager, CallbackInfo info)
	{
		StarflightSkyFeatures.loadBloomShader(client);
	}
	
	@Inject(method = "onResized(II)V", at = @At("TAIL"))
	public void onResizedInject(int width, int height, CallbackInfo info)
	{
		if(StarflightSkyFeatures.bloomShader != null)
			StarflightSkyFeatures.bloomShader.setupDimensions(width, height);
	}
}