package space.planet;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import space.StarflightMod;
import space.util.VectorUtil;

@Environment(value=EnvType.CLIENT)
public class ClientPlanet implements Comparable<ClientPlanet>
{
	public String name;
	public String parentName;
	public ClientPlanet parent;
	public ArrayList<ClientPlanet> satellites = new ArrayList<ClientPlanet>();
	public Vec3d position;
	public Vec3d surfaceViewpoint;
	public Vec3d parkingOrbitViewpoint;
	public Vec3d positionPrevious;
	public Vec3d surfaceViewpointPrevious;
	public Vec3d parkingOrbitViewpointPrevious;
	public double dVOrbit;
	public double dVSurface;
	public double dVTransfer;
	public double sunAngle;
	public double sunAngleOrbit;
	public double periapsis;
	public double apoapsis;
	public double argumentOfPeriapsis;
	public double trueAnomaly;
	public double ascendingNode;
	public double inclination;
	public double obliquity;
	public double precession;
	public double radius;
	public double surfaceGravity;
	public double surfacePressure;
	public boolean hasLowClouds;
	public boolean hasCloudCover;
	public boolean hasWeather;
	public boolean simpleTexture;
	public boolean drawClouds;
	public double cloudRotation;
	public int cloudLevel;
	public boolean hasOrbit;
	public boolean hasSurface;
	public boolean hasSky;
	public boolean unlocked;
	
	private static final Identifier PLANET_SHADING = new Identifier(StarflightMod.MOD_ID, "textures/environment/planet_shading.png");
	private static HashMap<String, Identifier> planetTextures = new HashMap<String, Identifier>();
	
	public ClientPlanet()
	{
	}
	
	@Override
    public int compareTo(ClientPlanet planetRenderer)
	{
		double r1 = position.subtract(ClientPlanetList.getViewpointPlanet().getPosition()).length();
		double r2 = planetRenderer.getPosition().subtract(ClientPlanetList.getViewpointPlanet().getPosition()).length();
        return (int) (r2 - r1);
    }

	public String getName()
	{
		return name;
	}
	
	public Vec3d getPosition()
	{
		return position;
	}

	public Vec3d getPosition(float partialTicks)
	{
		double x = MathHelper.lerp(partialTicks, positionPrevious.getX(), position.getX());
		double y = MathHelper.lerp(partialTicks, positionPrevious.getY(), position.getY());
		double z = MathHelper.lerp(partialTicks, positionPrevious.getZ(), position.getZ());
		return new Vec3d(x, y, z);
	}

	public void setPosition(Vec3d position)
	{
		this.position = position;
	}
	
	public Vec3d getSurfaceViewpoint()
	{
		return surfaceViewpoint;
	}

	public Vec3d getSurfaceViewpoint(float partialTicks)
	{
		double x = MathHelper.lerp(partialTicks, surfaceViewpointPrevious.getX(), surfaceViewpoint.getX());
		double y = MathHelper.lerp(partialTicks, surfaceViewpointPrevious.getY(), surfaceViewpoint.getY());
		double z = MathHelper.lerp(partialTicks, surfaceViewpointPrevious.getZ(), surfaceViewpoint.getZ());
		return new Vec3d(x, y, z);
	}

	public void setSurfaceViewpoint(Vec3d surfaceViewpoint)
	{
		this.surfaceViewpoint = surfaceViewpoint;
	}
	
	public Vec3d getParkingOrbitViewpoint()
	{
		return parkingOrbitViewpoint;
	}

	public Vec3d getParkingOrbitViewpoint(float partialTicks)
	{
		double x = MathHelper.lerp(partialTicks, parkingOrbitViewpointPrevious.getX(), parkingOrbitViewpoint.getX());
		double y = MathHelper.lerp(partialTicks, parkingOrbitViewpointPrevious.getY(), parkingOrbitViewpoint.getY());
		double z = MathHelper.lerp(partialTicks, parkingOrbitViewpointPrevious.getZ(), parkingOrbitViewpoint.getZ());
		return new Vec3d(x, y, z);
	}
	
	public void linkSatellites(ArrayList<ClientPlanet> planetList)
	{
		for(ClientPlanet p : planetList)
		{
			if(p.parentName.equals(name))
			{
				satellites.add(p);
				p.parent = this;
			}
		}
	}
	
	public Vec3d getRelativePositionAtTrueAnomaly(double ta)
	{
		double ecc = (apoapsis - periapsis) / (apoapsis + periapsis); // Eccentricity
		double sma = (periapsis + apoapsis) / 2.0; // Semi-Major Axis
		double r = (sma * (1.0 - (ecc * ecc))) / (1.0 + (ecc * Math.cos(ta)));
		Vec3d lanAxis = new Vec3d(1.0, 0.0, 0.0).rotateY((float) ascendingNode); // Longitude of Ascending Node Axis
		Vec3d newPosition = new Vec3d(lanAxis.getX(), lanAxis.getY(), lanAxis.getZ()).rotateY((float) (argumentOfPeriapsis + ta));
		newPosition = VectorUtil.rotateAboutAxis(newPosition, lanAxis, inclination).multiply(r);
		return newPosition;
	}
	
	/**
	 * Get the multiplier for solar panel power depending on distance to the sun and the presence of heavy cloud cover.
	 */
	public double getSolarMultiplier()
	{
		double d = getPosition().lengthSquared();
		
		if(d == 0.0)
			return 0.0;
		
		d /= 2.238016e22; // Convert the distance from meters to astronomical units.
		return (1.0 / d) * (hasCloudCover ? 0.5 : 1.0);
	}
	
	/**
	 * Return the size to render this planet object depending on its distance from the viewpoint.
	 * The size is calculated as a factor of the size of the Sun viewed from Earth.
	 */
	private float getRenderSize(double distanceToViewpoint)
	{
		float t = (float) (8.0d * ((radius / 696.34e6) / (distanceToViewpoint / 1.4710e11)));
		
		if(t > 500.0f)
			t = 500.0f;
		
		return t;
	}
	
	public static Identifier getTexture(String name)
	{
		if(planetTextures.containsKey(name))
			return planetTextures.get(name);
		
		Identifier newTexture = new Identifier(StarflightMod.MOD_ID, "textures/environment/" + name.toLowerCase() + ".png");
		planetTextures.put(name, newTexture);
		return newTexture;
	}
	
	public void doRender(BufferBuilder bufferBuilder, MatrixStack matrices, float partialTicks, float brightness, boolean weather)
	{
		// Angles to render this planet in the sky.
		ClientPlanet viewpointPlanet = ClientPlanetList.getViewpointPlanet();
		Vec3d viewpoint = ClientPlanetList.isViewpointInOrbit() ? viewpointPlanet.getParkingOrbitViewpoint(partialTicks) : viewpointPlanet.getSurfaceViewpoint(partialTicks);
		Vec3d positionVector = getPosition(partialTicks);
		double rPlanet = viewpoint.subtract(positionVector).length();
		Vec3d viewpointVector = viewpoint.subtract(viewpointPlanet.getPosition(partialTicks));
		Vec3d planetVector = positionVector.subtract(viewpointPlanet.getPosition(partialTicks));
		double phiViewpoint = Math.atan2(viewpointVector.getZ(), viewpointVector.getX());
		double thetaPlanet = Math.atan2(Math.sqrt((planetVector.getX() * planetVector.getX()) + (planetVector.getZ() * planetVector.getZ())), planetVector.getY()) - (Math.PI / 2.0d);
		double phiPlanet = Math.atan2(planetVector.getZ(), planetVector.getX());
		
		if(simpleTexture)
		{
			// Render this planet object in the sky.
			float t = getRenderSize(rPlanet);
			matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));

			if(name != ClientPlanetList.getViewpointPlanet().getName())
			{
				matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) viewpointPlanet.precession));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (viewpointPlanet.obliquity)));
				matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) (phiViewpoint - phiPlanet)));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (thetaPlanet)));
			}
			else
				matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) Math.PI));

			Matrix4f matrix4f3 = matrices.peek().getPositionMatrix();

			if(name.contains("sol"))
			{
				if(!weather)
					brightness = 1.0f;
				
				RenderSystem.setShaderColor(brightness, brightness, brightness, 0.95f);
				RenderSystem.setShader(GameRenderer::getPositionTexProgram);
				RenderSystem.setShaderTexture(0, getTexture(name + "_haze"));
				bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
				bufferBuilder.vertex(matrix4f3, -t, 100.0f, -t).texture(0.0f, 0.0f).next();
				bufferBuilder.vertex(matrix4f3, t, 100.0f, -t).texture(1.0f, 0.0f).next();
				bufferBuilder.vertex(matrix4f3, t, 100.0f, t).texture(1.0f, 1.0f).next();
				bufferBuilder.vertex(matrix4f3, -t, 100.0f, t).texture(0.0f, 1.0f).next();
				BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
				RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);
				RenderSystem.setShader(GameRenderer::getPositionTexProgram);
				RenderSystem.setShaderTexture(0, getTexture(name));
				bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
				bufferBuilder.vertex(matrix4f3, -t, 100.0f, -t).texture(0.0f, 0.0f).next();
				bufferBuilder.vertex(matrix4f3, t, 100.0f, -t).texture(1.0f, 0.0f).next();
				bufferBuilder.vertex(matrix4f3, t, 100.0f, t).texture(1.0f, 1.0f).next();
				bufferBuilder.vertex(matrix4f3, -t, 100.0f, t).texture(0.0f, 1.0f).next();
				BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
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
				RenderSystem.setShaderTexture(0, getTexture(name));
				bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
				bufferBuilder.vertex(matrix4f3, -t, 100.0f, -t).texture(0.0f, 0.0f).next();
				bufferBuilder.vertex(matrix4f3, t, 100.0f, -t).texture(1.0f, 0.0f).next();
				bufferBuilder.vertex(matrix4f3, t, 100.0f, t).texture(1.0f, 1.0f).next();
				bufferBuilder.vertex(matrix4f3, -t, 100.0f, t).texture(0.0f, 1.0f).next();
				BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
			}
			
			matrices.pop();
		}
		else
		{
			// Rotation frame to view this planet.
			double absoluteRotation = Math.atan2(getSurfaceViewpoint(partialTicks).getZ() - positionVector.getZ(), getSurfaceViewpoint(partialTicks).getX() - positionVector.getX());
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
			float t = getRenderSize(rPlanet);
			
			// Render this planet object in the sky.
			matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
			
			if(name != ClientPlanetList.getViewpointPlanet().getName())
			{
				matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) viewpointPlanet.precession));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (viewpointPlanet.obliquity)));
				matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) (phiViewpoint - phiPlanet)));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (thetaPlanet)));
			}
			else
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));
			
			Matrix4f matrix4f3 = matrices.peek().getPositionMatrix();
			RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);
			RenderSystem.setShader(GameRenderer::getPositionTexProgram);
			RenderSystem.setShaderTexture(0, getTexture(name));
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bufferBuilder.vertex(matrix4f3, -t, 100.0f, t).texture(endFrame, 0.0f).next();
			bufferBuilder.vertex(matrix4f3, -t, 100.0f, -t).texture(startFrame, 0.0f).next();
			bufferBuilder.vertex(matrix4f3, t, 100.0f, -t).texture(startFrame, 1.0f).next();
			bufferBuilder.vertex(matrix4f3, t, 100.0f, t).texture(endFrame, 1.0f).next();
			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
			
			if(drawClouds)
			{
				angleToLineOfSight = cloudRotation - angleToViewpoint + Math.PI;
				
				if(angleToLineOfSight < 0.0d)
					angleToLineOfSight += Math.PI * 2.0d;
				else if(angleToLineOfSight > Math.PI * 2.0d)
					angleToLineOfSight -= Math.PI * 2.0d;
				
				double cloudIndex = Math.round(16.0d * (angleToLineOfSight / (Math.PI * 2.0d)));
				float startCloudFrameU = (float) (cloudIndex * interval);
				float endCloudFrameU = (float) (startCloudFrameU + interval);
				float startCloudFrameV = (float) (cloudLevel * 0.25f);
				float endCloudFrameV = (float) (startCloudFrameV + 0.25f);
				
				RenderSystem.setShaderTexture(0, getTexture(name + "_clouds"));
				bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
				bufferBuilder.vertex(matrix4f3, -t, 100.0f, t).texture(endCloudFrameU, startCloudFrameV).next();
				bufferBuilder.vertex(matrix4f3, -t, 100.0f, -t).texture(startCloudFrameU, startCloudFrameV).next();
				bufferBuilder.vertex(matrix4f3, t, 100.0f, -t).texture(startCloudFrameU, endCloudFrameV).next();
				bufferBuilder.vertex(matrix4f3, t, 100.0f, t).texture(endCloudFrameU, endCloudFrameV).next();
				BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
			}
			
			RenderSystem.setShaderTexture(0, PLANET_SHADING);
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bufferBuilder.vertex(matrix4f3, -t,	100.0f, t).texture(endShadingFrame, 0.0f).next();
			bufferBuilder.vertex(matrix4f3, -t, 100.0f, -t).texture(startShadingFrame, 0.0f).next();
			bufferBuilder.vertex(matrix4f3, t, 100.0f, -t).texture(startShadingFrame, 1.0f).next();
			bufferBuilder.vertex(matrix4f3, t, 100.0f, t).texture(endShadingFrame, 1.0f).next();
			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
			
			matrices.pop();
		}
	}
}