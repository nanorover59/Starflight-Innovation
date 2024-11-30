package space.client.render;

import java.util.HashMap;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import space.StarflightMod;
import space.planet.Planet;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

@Environment(EnvType.CLIENT)
public class PlanetRenderer
{
	private static final Identifier PLANET_SHADING = Identifier.of(StarflightMod.MOD_ID, "textures/environment/planet_shading.png");
	private static HashMap<String, Identifier> planetTextures = new HashMap<String, Identifier>();
	
	/**
	 * Return the size to render this planet object depending on its distance from the viewpoint.
	 * The size is calculated as a factor of the size of the Sun viewed from Earth.
	 */
	private static float getRenderSize(Planet planet, double distanceToViewpoint)
	{
		float t = (float) (8.0d * ((planet.getRadius() / 696.34e6) / (distanceToViewpoint / 1.4710e11)));
		
		if(t > 500.0f)
			t = 500.0f;
		
		return t;
	}
	
	public static Identifier getTexture(String name)
	{
		if(planetTextures.containsKey(name))
			return planetTextures.get(name);
		
		Identifier newTexture = Identifier.of(StarflightMod.MOD_ID, "textures/environment/" + name.toLowerCase() + ".png");
		planetTextures.put(name, newTexture);
		return newTexture;
	}
	
	public static void render(Planet planet, MatrixStack matrices, float partialTicks, float brightness, boolean weather)
	{
		// Angles to render this planet in the sky.
		Planet viewpointPlanet = PlanetList.getClient().getViewpointPlanet();
		PlanetDimensionData dimensionData = PlanetList.getClient().getViewpointDimensionData();
		Vec3d viewpoint = dimensionData.isOrbit() ? viewpointPlanet.getInterpolatedParkingOrbitViewpoint(partialTicks) : viewpointPlanet.getInterpolatedSurfaceViewpoint(partialTicks);
		Vec3d positionVector = planet.getInterpolatedPosition(partialTicks);
		double rPlanet = viewpoint.subtract(positionVector).length();
		Vec3d viewpointVector = viewpoint.subtract(viewpointPlanet.getInterpolatedPosition(partialTicks));
		Vec3d planetVector = positionVector.subtract(viewpointPlanet.getInterpolatedPosition(partialTicks));
		double phiViewpoint = Math.atan2(viewpointVector.getZ(), viewpointVector.getX());
		double thetaPlanet = Math.atan2(Math.sqrt((planetVector.getX() * planetVector.getX()) + (planetVector.getZ() * planetVector.getZ())), planetVector.getY()) - (Math.PI / 2.0d);
		double phiPlanet = Math.atan2(planetVector.getZ(), planetVector.getX());
		Tessellator tessellator = Tessellator.getInstance();
		
		if(planet.hasSimpleTexture())
		{
			// Render this planet object in the sky.
			float t = getRenderSize(planet, rPlanet);
			matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));

			if(planet.getName() != viewpointPlanet.getName())
			{
				matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) viewpointPlanet.getPrecession()));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (viewpointPlanet.getObliquity())));
				matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) (phiViewpoint - phiPlanet)));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (thetaPlanet)));
			}
			else
				matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) Math.PI));

			Matrix4f matrix4f3 = matrices.peek().getPositionMatrix();

			if(planet.getName().contains("sol"))
			{
				if(!weather)
					brightness = 1.0f;
				
				RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);
				RenderSystem.setShader(GameRenderer::getPositionTexProgram);
				RenderSystem.setShaderTexture(0, getTexture(planet.getName() + "_haze"));
				BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
				buffer.vertex(matrix4f3, -t, 100.0f, -t).texture(0.0f, 0.0f);
				buffer.vertex(matrix4f3, t, 100.0f, -t).texture(1.0f, 0.0f);
				buffer.vertex(matrix4f3, t, 100.0f, t).texture(1.0f, 1.0f);
				buffer.vertex(matrix4f3, -t, 100.0f, t).texture(0.0f, 1.0f);
				BufferRenderer.drawWithGlobalProgram(buffer.end());
				RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);
				RenderSystem.setShader(GameRenderer::getPositionTexProgram);
				RenderSystem.setShaderTexture(0, getTexture(planet.getName()));
				buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
				buffer.vertex(matrix4f3, -t, 100.0f, -t).texture(0.0f, 0.0f);
				buffer.vertex(matrix4f3, t, 100.0f, -t).texture(1.0f, 0.0f);
				buffer.vertex(matrix4f3, t, 100.0f, t).texture(1.0f, 1.0f);
				buffer.vertex(matrix4f3, -t, 100.0f, t).texture(0.0f, 1.0f);
				BufferRenderer.drawWithGlobalProgram(buffer.end());
			}
			else
			{
				// Phase angle for lighting factor.
				Vec3d starPosition = new Vec3d(0.0d, 0.0d, 0.0d);
				double angleToStar = Math.atan2(starPosition.getZ() - positionVector.getZ(), starPosition.getX() - positionVector.getX());
				double angleToViewpoint = Math.atan2(viewpoint.getZ() - positionVector.getZ(), viewpoint.getX() - positionVector.getX());
				double phaseAngle = angleToStar - angleToViewpoint;
				
				if(phaseAngle < 0.0d)
					phaseAngle += Math.PI * 2.0d;
				else if(phaseAngle > Math.PI * 2.0d)
					phaseAngle -= Math.PI * 2.0d;
				
				float lightingFactor = brightness * (float) (Math.abs(phaseAngle - Math.PI) / Math.PI);
				RenderSystem.setShaderColor(lightingFactor, lightingFactor, lightingFactor, 1.0f);
				RenderSystem.setShader(GameRenderer::getPositionTexProgram);
				RenderSystem.setShaderTexture(0, getTexture(planet.getName()));
				BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
				buffer.vertex(matrix4f3, -t, 100.0f, -t).texture(0.0f, 0.0f);
				buffer.vertex(matrix4f3, t, 100.0f, -t).texture(1.0f, 0.0f);
				buffer.vertex(matrix4f3, t, 100.0f, t).texture(1.0f, 1.0f);
				buffer.vertex(matrix4f3, -t, 100.0f, t).texture(0.0f, 1.0f);
				BufferRenderer.drawWithGlobalProgram(buffer.end());
			}
			
			matrices.pop();
		}
		else
		{
			// Rotation frame to view this planet.
			double absoluteRotation = Math.atan2(planet.getInterpolatedSurfaceViewpoint(partialTicks).getZ() - positionVector.getZ(), planet.getInterpolatedSurfaceViewpoint(partialTicks).getX() - positionVector.getX());
			double angleToViewpoint = Math.atan2(viewpoint.getZ() - positionVector.getZ(), viewpoint.getX() - positionVector.getX());
			double angleToLineOfSight = absoluteRotation - angleToViewpoint + Math.PI;
			
			if(angleToLineOfSight < 0.0d)
				angleToLineOfSight += Math.PI * 2.0d;
			else if(angleToLineOfSight > Math.PI * 2.0d)
				angleToLineOfSight -= Math.PI * 2.0d;
			
			double interval = 1.0f / 16.0f;
			double index = Math.round(16.0d * (angleToLineOfSight / (Math.PI * 2.0d)));
			float startFrame = (float) (index * interval);
			float endFrame = (float) (startFrame + interval);
			
			// Phase angle for lighting frame.
			Vec3d starPosition = new Vec3d(0.0d, 0.0d, 0.0d);
			double angleToStar = Math.atan2(starPosition.getZ() - positionVector.getZ(), starPosition.getX() - positionVector.getX());
			double phaseAngle = angleToStar - angleToViewpoint;
			
			if(phaseAngle < 0.0d)
				phaseAngle += Math.PI * 2.0d;
			else if(phaseAngle > Math.PI * 2.0d)
				phaseAngle -= Math.PI * 2.0d;
			
			// Final calculations for rendering parameters.
			double shadingIndex = Math.round(16.0d * (phaseAngle / (Math.PI * 2.0d)));
			float startShadingFrame = (float) (shadingIndex * interval);
			float endShadingFrame = (float) (startShadingFrame + interval);
			float t = getRenderSize(planet, rPlanet);
			
			// Render this planet object in the sky.
			matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
			
			if(planet.getName() != PlanetList.getClient().getViewpointPlanet().getName())
			{
				matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) viewpointPlanet.getPrecession()));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (viewpointPlanet.getObliquity())));
				matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) (phiViewpoint - phiPlanet)));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (thetaPlanet)));
			}
			else
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));
			
			Matrix4f matrix4f3 = matrices.peek().getPositionMatrix();
			RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);
			RenderSystem.setShader(GameRenderer::getPositionTexProgram);
			RenderSystem.setShaderTexture(0, getTexture(planet.getName()));
			BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			buffer.vertex(matrix4f3, -t, 100.0f, t).texture(endFrame, 0.0f);
			buffer.vertex(matrix4f3, -t, 100.0f, -t).texture(startFrame, 0.0f);
			buffer.vertex(matrix4f3, t, 100.0f, -t).texture(startFrame, 1.0f);
			buffer.vertex(matrix4f3, t, 100.0f, t).texture(endFrame, 1.0f);
			BufferRenderer.drawWithGlobalProgram(buffer.end());
			
			if(planet.drawClouds())
			{
				angleToLineOfSight = planet.getCloudRotation() - angleToViewpoint + Math.PI;
				
				if(angleToLineOfSight < 0.0d)
					angleToLineOfSight += Math.PI * 2.0d;
				else if(angleToLineOfSight > Math.PI * 2.0d)
					angleToLineOfSight -= Math.PI * 2.0d;
				
				double cloudIndex = Math.round(16.0d * (angleToLineOfSight / (Math.PI * 2.0d)));
				float startCloudFrameU = (float) (cloudIndex * interval);
				float endCloudFrameU = (float) (startCloudFrameU + interval);
				float startCloudFrameV = (float) (planet.getCloudLevel() * 0.25f);
				float endCloudFrameV = (float) (startCloudFrameV + 0.25f);
				
				RenderSystem.setShaderTexture(0, getTexture(planet.getName() + "_clouds"));
				buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
				buffer.vertex(matrix4f3, -t, 100.0f, t).texture(endCloudFrameU, startCloudFrameV);
				buffer.vertex(matrix4f3, -t, 100.0f, -t).texture(startCloudFrameU, startCloudFrameV);
				buffer.vertex(matrix4f3, t, 100.0f, -t).texture(startCloudFrameU, endCloudFrameV);
				buffer.vertex(matrix4f3, t, 100.0f, t).texture(endCloudFrameU, endCloudFrameV);
				BufferRenderer.drawWithGlobalProgram(buffer.end());
			}
			
			RenderSystem.setShaderTexture(0, PLANET_SHADING);
			buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			buffer.vertex(matrix4f3, -t,	100.0f, t).texture(endShadingFrame, 0.0f);
			buffer.vertex(matrix4f3, -t, 100.0f, -t).texture(startShadingFrame, 0.0f);
			buffer.vertex(matrix4f3, t, 100.0f, -t).texture(startShadingFrame, 1.0f);
			buffer.vertex(matrix4f3, t, 100.0f, t).texture(endShadingFrame, 1.0f);
			BufferRenderer.drawWithGlobalProgram(buffer.end());
			
			matrices.pop();
		}
	}
}