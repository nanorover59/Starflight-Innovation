package space.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
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
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import space.StarflightMod;
import space.client.render.PlanetRenderer;
import space.client.render.StarflightRenderEffects;
import space.planet.Planet;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

@Environment(value=EnvType.CLIENT)
@Mixin(value = WorldRenderer.class)	
public abstract class WorldRendererMixin
{
	@Shadow @Final private MinecraftClient client;
	@Shadow private VertexBuffer starsBuffer;
	@Shadow abstract void renderEndSky(MatrixStack matrixStack);
	@Shadow private ClientWorld world;
	@Shadow private VertexBuffer lightSkyBuffer;
	@Shadow private VertexBuffer darkSkyBuffer;
	@Shadow private boolean cloudsDirty = true;
	@Shadow @Nullable private VertexBuffer cloudsBuffer;
	@Shadow private int lastCloudsBlockX = Integer.MIN_VALUE;
	@Shadow private int lastCloudsBlockY = Integer.MIN_VALUE;
	@Shadow private int lastCloudsBlockZ = Integer.MIN_VALUE;
	@Shadow private Vec3d lastCloudsColor = Vec3d.ZERO;
	@Shadow private int ticks;
	@Shadow @Final private static Identifier SUN;
	@Shadow @Final private static Identifier MOON_PHASES;
	@Shadow @Final private static Identifier CLOUDS;
	
	//@Inject(method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("RETURN"))
	@Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
	public void renderSkyInject(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo info)
	{
		Planet viewpointPlanet = PlanetList.getClient().getViewpointPlanet();
		PlanetDimensionData dimensionData = PlanetList.getClient().getViewpointDimensionData();
		
		if(viewpointPlanet != null && dimensionData != null)
		{
			Vec3d viewpointPlanetPosition = viewpointPlanet.getInterpolatedPosition(tickDelta);
			
			// Find the position of the sun and background stars in angular coordinates.
			Vec3d starPosition = new Vec3d(0.0d, 0.0d, 0.0d);
			Vec3d viewpoint = dimensionData.isOrbit() ? viewpointPlanet.getInterpolatedParkingOrbitViewpoint(tickDelta) : viewpointPlanet.getInterpolatedSurfaceViewpoint(tickDelta);
			double azimuthOfViewpoint = Math.atan2(viewpointPlanetPosition.getZ() - viewpoint.getZ(), viewpointPlanetPosition.getX() - viewpoint.getX());
			double azimuthOfStar = Math.atan2(starPosition.getZ() - viewpoint.getZ(), starPosition.getX() - viewpoint.getX());
			double trueAzimuth = azimuthOfViewpoint - azimuthOfStar;
			
			// Setup for sky rendering.
			Tessellator tessellator = Tessellator.getInstance();
			MatrixStack matrixStack = new MatrixStack();
	        matrixStack.multiplyPositionMatrix(matrix4f);
	        
	        RenderSystem.depthMask(false);
	        RenderSystem.enableBlend();
	        RenderSystem.defaultBlendFunc();
			
			// Ensure a black background.
			RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
			RenderSystem.setShaderTexture(0, Identifier.of(StarflightMod.MOD_ID, "textures/environment/background.png"));
			BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferBuilder.vertex(projectionMatrix, -1.0f, -1.0f, 0.0f).texture(0.0f, 0.0f).color(0.0f, 0.0f, 0.0f, 1.0f);
			bufferBuilder.vertex(projectionMatrix,  1.0f, -1.0f, 0.0f).texture(1.0f, 0.0f).color(0.0f, 0.0f, 0.0f, 1.0f);
			bufferBuilder.vertex(projectionMatrix,  1.0f,  1.0f, 0.0f).texture(1.0f, 1.0f).color(0.0f, 0.0f, 0.0f, 1.0f);
			bufferBuilder.vertex(projectionMatrix, -1.0f,  1.0f, 0.0f).texture(0.0f, 1.0f).color(0.0f, 0.0f, 0.0f, 1.0f);
			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
			
			// Render the stars and milky way.
			Vec3d viewpointVector = viewpoint.subtract(viewpointPlanetPosition);
			double phiViewpoint = Math.atan2(viewpointVector.getZ(), viewpointVector.getX());	
			matrixStack.push();
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float) viewpointPlanet.getPrecession()));
			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation((float) viewpointPlanet.getObliquity()));
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float) phiViewpoint));
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60.0f));
			
			float altFactor = MathHelper.clamp(1.0f - (float) (camera.getPos().getY() - 320.0f) / 320.0f, 0.0f, 1.0f);
			float skyOpacity = altFactor;
			float rainGradient = this.world.getRainGradient(tickDelta) * altFactor;
			
			if(dimensionData.isOrbit() || dimensionData.getPressure() < 0.001d)
			{
				skyOpacity = 0.0f;
				rainGradient = 0.0f;
			}
			else if(dimensionData.isCloudy())
				rainGradient = 1.0f;
			
			float starFactor = Math.max(this.world.getStarBrightness(tickDelta) * (1.0f - rainGradient) * 2.0f, 1.0f - skyOpacity);
			starFactor = (float) Math.pow(starFactor, 3.0);
			
			if(starFactor > 0.0f)
			{
				Matrix4f matrix4f3 = matrixStack.peek().getPositionMatrix();
				float milkyWayFactor = 0.6f;
				RenderSystem.setShader(GameRenderer::getPositionProgram);
				RenderSystem.setShaderColor(starFactor * milkyWayFactor, starFactor * milkyWayFactor, starFactor * milkyWayFactor, starFactor * milkyWayFactor);
				RenderSystem.setShaderTexture(0, Identifier.of(StarflightMod.MOD_ID, "textures/environment/milky_way.png"));
				StarflightRenderEffects.milkyWay.bind();
				StarflightRenderEffects.milkyWay.draw(matrix4f3, projectionMatrix, GameRenderer.getPositionTexProgram());
				RenderSystem.setShaderColor(starFactor, starFactor, starFactor, starFactor);
				RenderSystem.setShaderTexture(0, Identifier.of(StarflightMod.MOD_ID, "textures/environment/stars.png"));
				StarflightRenderEffects.stars.bind();
				StarflightRenderEffects.stars.draw(matrix4f3, projectionMatrix, GameRenderer.getPositionTexProgram());
	            VertexBuffer.unbind();
			}
			
			matrixStack.pop();
			
			// Render all celestial objects.
			float celestialFactor = Math.min(starFactor + 0.5f, 1.0f);
			celestialFactor = Math.max(celestialFactor - rainGradient * 0.5f, 0.0f);
			
			for(Planet planet : PlanetList.getClient().getPlanets())
				PlanetRenderer.render(planet, matrixStack, tickDelta, celestialFactor, rainGradient > 0.0f);
			
			// Apply the bloom shader effect for players with fabulous graphics.
			/*if(MinecraftClient.isFabulousGraphicsOrBetter() && StarflightRenderEffects.bloomShader != null && starFactor > 0.5f)
			{
				StarflightRenderEffects.bloomShader.render(tickDelta);
				client.getFramebuffer().beginWrite(false);
			}*/
			
			if(!dimensionData.isOrbit())
			{
				Vec3d skyRGB = world.getSkyColor(client.gameRenderer.getCamera().getPos(), tickDelta);
				float skyR = (float) skyRGB.getX();
				float skyG = (float) skyRGB.getY();
				float skyB = (float) skyRGB.getZ();
				float v = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(tickDelta) * ((float) Math.PI * 2)) * 2.0f + 0.5f, 0.0f, 1.0f);
				Vec3d vec3d = client.gameRenderer.getCamera().getPos().subtract(2.0, 2.0, 2.0).multiply(0.25);
				Vec3d fogRGB = CubicSampler.sampleColor(vec3d, (x, y, z) -> world.getDimensionEffects().adjustFogColor(Vec3d.unpackRgb(client.world.getBiomeForNoiseGen(x, y, z).value().getFogColor()), v));
				float fogR = (float) fogRGB.getX();
				float fogG = (float) fogRGB.getY();
				float fogB = (float) fogRGB.getZ();
				float rain = world.getRainGradient(tickDelta);
				float thunder = world.getThunderGradient(tickDelta);
				float range = Math.min(128.0f, client.gameRenderer.getViewDistance());
				float fogYOffset = -8.0f;
	
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
				
				if(dimensionData.isSky() && viewpointPlanet.hasCloudCover())
				{
					float fogFactor = MathHelper.clamp(1.0f - (float) (camera.getPos().getY()) / 256.0f, 0.0f, 1.0f);
					skyR = MathHelper.lerp(fogFactor, skyR, fogR);
					skyG = MathHelper.lerp(fogFactor, skyG, fogG);
					skyB = MathHelper.lerp(fogFactor, skyB, fogB);
				}
				
				// Horizon Mask
				float gap = 4.0f;
				RenderSystem.setShader(GameRenderer::getPositionColorProgram);
				RenderSystem.setShaderColor(altFactor, altFactor, altFactor, altFactor);
				bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
				
				for(int i = 0; i < 16; i++)
				{
					float theta1 = (float) i * (float) (Math.PI * 2.0) / 16.0f;
					float theta2 = (float) (i + 1) * (float) (Math.PI * 2.0) / 16.0f;
					float x1 = range * (float) Math.cos(theta1);
					float z1 = range * (float) Math.sin(theta1);
					float x2 = range * (float) Math.cos(theta2);
					float z2 = range * (float) Math.sin(theta2);
					bufferBuilder.vertex(matrix4f, x1, fogYOffset, z1).color(0.0f, 0.0f, 0.0f, 0.0f);
					bufferBuilder.vertex(matrix4f, x1, fogYOffset - gap, z1).color(0.0f, 0.0f, 0.0f, 1.0f);
					bufferBuilder.vertex(matrix4f, x2, fogYOffset - gap, z2).color(0.0f, 0.0f, 0.0f, 1.0f);
					bufferBuilder.vertex(matrix4f, x2, fogYOffset, z2).color(0.0f, 0.0f, 0.0f, 0.0f);
				}
				
				BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
				
				bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
				bufferBuilder.vertex(matrix4f, 0.0f, -16.0f, 0.0f).color(0.0f, 0.0f, 0.0f, 1.0f);
	
				for(int i = 0; i <= 16; i++)
				{
					float theta = (float) i * (float) (Math.PI * 2.0) / 16.0f;
					float sinTheta = MathHelper.sin(theta);
					float cosTheta = MathHelper.cos(theta);
					bufferBuilder.vertex(matrix4f, range * cosTheta, fogYOffset - gap, -range * sinTheta).color(0.0f, 0.0f, 0.0f, 1.0f);
				}
	
				BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
				
				// Sky Color
				RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
				//RenderSystem.setShaderColor(skyOpacity, skyOpacity, skyOpacity, skyOpacity);
				bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
				bufferBuilder.vertex(matrix4f, 0.0f, fogYOffset + 16.0f, 0.0f).color(skyR, skyG, skyB, 1.0f);
	
				for(int i = 0; i <= 16; i++)
				{
					float theta = (float) i * (float) (Math.PI * 2.0) / 16.0f;
					float sinTheta = MathHelper.sin(theta);
					float cosTheta = MathHelper.cos(theta);
					bufferBuilder.vertex(matrix4f, range * cosTheta, fogYOffset, range * sinTheta).color(fogR, fogG, fogB, 1.0f);
				}
	
				BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
				bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
				bufferBuilder.vertex(matrix4f, 0.0f, -16.0f, 0.0f).color(fogR, fogG, fogB, 1.0f);
	
				for(int i = 0; i <= 16; i++)
				{
					float theta = (float) i * (float) (Math.PI * 2.0) / 16.0f;
					float sinTheta = MathHelper.sin(theta);
					float cosTheta = MathHelper.cos(theta);
					bufferBuilder.vertex(matrix4f, range * cosTheta, fogYOffset, -range * sinTheta).color(fogR, fogG, fogB, 1.0f);
				}
	
				BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	
				// Render the sunset effect.
				float skyAngle = (float) trueAzimuth;
				float[] fs = this.world.getDimensionEffects().getFogColorOverride((float) (skyAngle / (Math.PI * 2.0d)), tickDelta);
	
				if(fs != null)
				{
					RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
					matrixStack.push();
					matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
					matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(this.world.getSkyAngleRadians(tickDelta)) < 0.0f ? 180.0f : 0.0f));
					matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
					matrix4f = matrixStack.peek().getPositionMatrix();
					bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
					bufferBuilder.vertex(matrix4f, 0.0f, 0.0f, 16.0f).color(fs[0], fs[1], fs[2], fs[3]);
	
					for(int i = 0; i <= 16; i++)
					{
						float theta = (float) i * (float) (Math.PI * 2.0) / 16.0f;
						float sinTheta = MathHelper.sin(theta);
						float cosTheta = MathHelper.cos(theta);
						bufferBuilder.vertex(matrix4f, range * sinTheta, range * cosTheta, -cosTheta * 64.0f * fs[3]).color(0.0f, 0.0f, 0.0f, 0.0f);
					}
	
					BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
					matrixStack.pop();
				}
			}
			
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			RenderSystem.depthMask(true);
			info.cancel();
		}
	}
	
	@Inject(method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FDDD)V", at = @At("HEAD"), cancellable = true)
	public void renderCloudsInject(MatrixStack matrices, Matrix4f matrix4f, Matrix4f matrix4f2, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo info)
	{
		if(PlanetList.getClient().getViewpointPlanet() != null && (PlanetList.getClient().getViewpointDimensionData().isOrbit() || !PlanetList.getClient().getViewpointDimensionData().hasLowClouds()))
			info.cancel();
	}
	
	@Inject(method = "renderStars()V", at = @At("HEAD"))
	public void renderStarsInject(CallbackInfo info)
	{
		StarflightRenderEffects.initializeBuffers();
	}
	
	@Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("TAIL"))
	public void reloadInject(ResourceManager manager, CallbackInfo info)
	{
		StarflightRenderEffects.bloomShader = StarflightRenderEffects.loadShader(client, "bloom");
		StarflightRenderEffects.radiationShader = StarflightRenderEffects.loadShader(client, "radiation");
	}
	
	@Inject(method = "onResized(II)V", at = @At("TAIL"))
	public void onResizedInject(int width, int height, CallbackInfo info)
	{
		if(StarflightRenderEffects.bloomShader != null)
			StarflightRenderEffects.bloomShader.setupDimensions(width, height);
		
		if(StarflightRenderEffects.radiationShader != null)
			StarflightRenderEffects.radiationShader.setupDimensions(width, height);
	}
}