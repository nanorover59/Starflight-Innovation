package space.mixin;

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
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
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
	@Shadow @Nullable private CloudRenderMode lastCloudsRenderMode;
	@Shadow abstract void renderClouds(BufferBuilder builder, double x, double y, double z, Vec3d color);
	@Shadow private int ticks;
	@Shadow @Final private static Identifier SUN;
	@Shadow @Final private static Identifier MOON_PHASES;
	@Shadow @Final private static Identifier CLOUDS;
	
	@Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
	public void renderSkyInject(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo info)
	{
		ArrayList<PlanetRenderer> planetList = PlanetRenderList.getRenderers();
		PlanetRenderer viewpointPlanet = PlanetRenderList.getViewpointPlanet();
		
		if(this.client.world.getDimensionEffects().getSkyType() == DimensionEffects.SkyType.NORMAL && viewpointPlanet != null)
		{
			boolean starrySky = PlanetRenderList.isViewpointInOrbit() || viewpointPlanet.getSurfacePressure() < 0.001D;
			boolean cloudySky = viewpointPlanet.hasCloudCover();
			
			// Find the position of the sun and background stars in angular coordinates.
			Vec3d starPosition = new Vec3d(0.0D, 0.0D, 0.0D);
			Vec3d viewpoint = PlanetRenderList.isViewpointInOrbit() ? viewpointPlanet.getParkingOrbitViewpoint() : viewpointPlanet.getSurfaceViewpoint();
			double azimuthOfViewpoint = Math.atan2(viewpointPlanet.getPosition().getZ() - viewpoint.getZ(), viewpointPlanet.getPosition().getX() - viewpoint.getX());
			double azimuthOfStar = Math.atan2(starPosition.getZ() - viewpoint.getZ(), starPosition.getX() - viewpoint.getX());
			double trueAzimuth = azimuthOfViewpoint - azimuthOfStar;
			
			// Setup for sky rendering.
			runnable.run();
			RenderSystem.disableTexture();
			Vec3d vec3d = starrySky ? new Vec3d(0.0f, 0.0f, 0.0f) : this.world.getSkyColor(this.client.gameRenderer.getCamera().getPos(), tickDelta);
			float skyR = (float) vec3d.x;
			float skyG = (float) vec3d.y;
			float skyB = (float) vec3d.z;
			BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
			RenderSystem.depthMask(false);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
			
			// Ensure a black background.
			RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
			RenderSystem.setShader(GameRenderer::getPositionShader);
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
			bufferBuilder.vertex(projectionMatrix, -10.0f, -10.0f, 0.0f).next();
			bufferBuilder.vertex(projectionMatrix,  10.0f, -10.0f, 0.0f).next();
			bufferBuilder.vertex(projectionMatrix,  10.0f,  10.0f, 0.0f).next();
			bufferBuilder.vertex(projectionMatrix, -10.0f,  10.0f, 0.0f).next();
			bufferBuilder.end();
			BufferRenderer.draw(bufferBuilder);
			
			// Render the stars.
			Vec3d viewpointVector = viewpoint.subtract(viewpointPlanet.getPosition());
			double phiViewpoint = Math.atan2(viewpointVector.getZ(), viewpointVector.getX());	
			matrices.push();
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90.0f));
			matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion((float) viewpointPlanet.getPrecession()));
			matrices.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion((float) viewpointPlanet.getObliquity()));
			matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion((float) phiViewpoint));
			
			float rainGradient = starrySky ? 0.0f : this.world.getRainGradient(tickDelta);
			float s = cloudySky ? 0.0f : (1.0f - rainGradient);
			float starFactor = starrySky ? 1.0f : this.world.method_23787(tickDelta) * s;

			if(starFactor > 0.0f)
			{
				RenderSystem.setShaderColor(starFactor, starFactor, starFactor, starFactor);
				//BackgroundRenderer.clearFog();
				this.starsBuffer.setShader(matrices.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionShader());
				runnable.run();
			}
			
			matrices.pop();
			
			// Render all celestial objects.
			float celestialFactor = Math.min(starFactor + 0.3f, 1.0f);
			celestialFactor = Math.max(celestialFactor - rainGradient, 0.0f);
			
			for(PlanetRenderer planetRenderer : planetList)
			{
				if(planetRenderer != null)
					planetRenderer.doRender(bufferBuilder, matrices, celestialFactor, s < 0.95f);
			}
			
			RenderSystem.disableTexture();
			
			// Apply the sky color on atmospheric planets.
			if(!starrySky)
			{
				RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
				RenderSystem.setShaderColor(skyR, skyG, skyB, 0.75f);
				RenderSystem.setShader(GameRenderer::getPositionShader);
				bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
				bufferBuilder.vertex(projectionMatrix, -10.0f, -10.0f, 0.0f).next();
				bufferBuilder.vertex(projectionMatrix, 10.0f, -10.0f, 0.0f).next();
				bufferBuilder.vertex(projectionMatrix, 10.0f, 10.0f, 0.0f).next();
				bufferBuilder.vertex(projectionMatrix, -10.0f, 10.0f, 0.0f).next();
				bufferBuilder.end();
				BufferRenderer.draw(bufferBuilder);
			}
			
			// Render the sunset effect on atmospheric planets.
			RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
			float skyAngle = (float) trueAzimuth;
			float[] fs = this.world.getDimensionEffects().getFogColorOverride((float) (skyAngle / (Math.PI * 2.0D)), tickDelta);
			float t;
			float p;
			float q;
			float r;
			
			if(fs != null && !starrySky)
			{
				RenderSystem.setShader(GameRenderer::getPositionColorShader);
				RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
				matrices.push();
				matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0f));
				s = MathHelper.sin(skyAngle) < 0.0f ? 180.0f : 0.0f;
				matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(s));
				matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(270.0f));
				float k = fs[0];
				t = fs[1];
				float m = fs[2];
				Matrix4f matrix4f2 = matrices.peek().getPositionMatrix();
				bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
				bufferBuilder.vertex(matrix4f2, 0.0f, 100.0f, 0.0f).color(k, t, m, fs[3]).next();

				for(int o = 0; o <= 16; o++)
				{
					p = (float) o * 6.2831855F / 16.0f;
					q = MathHelper.sin(p);
					r = MathHelper.cos(p);
					bufferBuilder.vertex(matrix4f2, q * 120.0f, r * 120.0f, -r * 40.0f * fs[3]).color(fs[0], fs[1], fs[2], 0.0f).next();
				}

				bufferBuilder.end();
				BufferRenderer.draw(bufferBuilder);
				matrices.pop();
			}
			
			// End of custom sky rendering.
			RenderSystem.enableTexture();
			RenderSystem.depthMask(true);
			info.cancel();
		}
	}
	
	@Inject(method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FDDD)V", at = @At("HEAD"), cancellable = true)
	public void renderCloudsInject(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double d, double e, double f, CallbackInfo info)
	{
		if(PlanetRenderList.getViewpointPlanet() != null && (PlanetRenderList.isViewpointInOrbit() || !PlanetRenderList.getViewpointPlanet().hasLowClouds()))
			info.cancel();
	}
}