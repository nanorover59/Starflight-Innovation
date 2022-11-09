package space.planet;

import java.util.HashMap;

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
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import space.StarflightMod;

@Environment(value=EnvType.CLIENT)
public class PlanetRenderer implements Comparable<PlanetRenderer>
{
	private String name;
	private Vec3d position;
	private Vec3d surfaceViewpoint;
	private Vec3d parkingOrbitViewpoint;
	private Vec3d positionPrevious;
	private Vec3d surfaceViewpointPrevious;
	private Vec3d parkingOrbitViewpointPrevious;
	private double obliquity;
	private double precession;
	private double radius;
	private double surfacePressure;
	private boolean hasLowClouds;
	private boolean hasCloudCover;
	private boolean hasWeather;
	private boolean simpleTexture;
	private boolean drawClouds;
	private double cloudRotation;
	private int cloudLevel;
	
	private static final Identifier PLANET_SHADING = new Identifier(StarflightMod.MOD_ID, "textures/environment/planet_shading.png");
	private static final Identifier SUN_HAZE_0 = new Identifier(StarflightMod.MOD_ID, "textures/environment/sun_haze_0.png");
	private static HashMap<String, Identifier> planetTextures = new HashMap<String, Identifier>();
	
	public PlanetRenderer(String name_, double obliquity_, double radius_, double surfacePressure_, boolean hasLowClouds_, boolean hasCloudCover_, boolean hasWeather_, boolean simpleTexture_, boolean drawClouds_)
	{
		name = name_;
		setObliquity(obliquity_);
		setRadius(radius_);
		setSurfacePressure(surfacePressure_);
		setLowClouds(hasLowClouds_);
		setCloudCover(hasCloudCover_);
		setWeather(hasWeather_);
		setSimpleTexture(simpleTexture_);
		setDrawClouds(drawClouds_);
	}
	
	public PlanetRenderer(PlanetRenderer other)
	{
		name = other.name;
		setPosition(other.position);
		setSurfaceViewpoint(other.surfaceViewpoint);
		setParkingOrbitViewpoint(other.parkingOrbitViewpoint);
		setPositionPrevious(other.positionPrevious);
		setSurfaceViewpointPrevious(other.surfaceViewpointPrevious);
		setParkingOrbitViewpointPrevious(other.parkingOrbitViewpointPrevious);
		setObliquity(other.obliquity);
		setPrecession(other.precession);
		setRadius(other.radius);
		setSurfacePressure(other.surfacePressure);
		setLowClouds(other.hasLowClouds);
		setCloudCover(other.hasCloudCover);
		setWeather(other.hasWeather);
		setSimpleTexture(other.simpleTexture);
		setDrawClouds(other.drawClouds);
		setCloudRotation(other.cloudRotation);
		setCloudLevel(other.cloudLevel);
	}
	
	@Override
    public int compareTo(PlanetRenderer planetRenderer)
	{
		double r1 = position.subtract(PlanetRenderList.getViewpointPlanet().getPosition()).length();
		double r2 = planetRenderer.getPosition().subtract(PlanetRenderList.getViewpointPlanet().getPosition()).length();
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

	public void setParkingOrbitViewpoint(Vec3d parkingOrbitViewpoint)
	{
		this.parkingOrbitViewpoint = parkingOrbitViewpoint;
	}
	
	public Vec3d getPositionPrevious()
	{
		return positionPrevious;
	}

	public void setPositionPrevious(Vec3d position)
	{
		this.positionPrevious = position;
	}
	
	public Vec3d getSurfaceViewpointPrevious()
	{
		return surfaceViewpointPrevious;
	}

	public void setSurfaceViewpointPrevious(Vec3d surfaceViewpoint)
	{
		this.surfaceViewpointPrevious = surfaceViewpoint;
	}
	
	public Vec3d getParkingOrbitViewpointPrevious()
	{
		return parkingOrbitViewpointPrevious;
	}

	public void setParkingOrbitViewpointPrevious(Vec3d parkingOrbitViewpoint)
	{
		this.parkingOrbitViewpointPrevious = parkingOrbitViewpoint;
	}
	
	public double getObliquity()
	{
		return obliquity;
	}

	public void setObliquity(double obliquity)
	{
		this.obliquity = obliquity;
	}
	
	public double getPrecession()
	{
		return precession;
	}

	public void setPrecession(double precession)
	{
		this.precession = precession;
	}
	
	public double getRadius()
	{
		return radius;
	}

	public void setRadius(double radius)
	{
		this.radius = radius;
	}
	
	public double getSurfacePressure()
	{
		return surfacePressure;
	}
	
	public void setSurfacePressure(double surfacePressure)
	{
		this.surfacePressure = surfacePressure;
	}
	
	public boolean hasLowClouds()
	{
		return hasLowClouds;
	}
	
	public void setLowClouds(boolean hasLowClouds)
	{
		this.hasLowClouds = hasLowClouds;
	}
	
	public boolean hasCloudCover()
	{
		return hasCloudCover;
	}
	
	public void setCloudCover(boolean hasCloudCover)
	{
		this.hasCloudCover = hasCloudCover;
	}
	
	public boolean hasWeather()
	{
		return hasWeather;
	}
	
	public void setWeather(boolean hasWeather)
	{
		this.hasWeather = hasWeather;
	}
	
	public boolean hasSimpleTexture()
	{
		return simpleTexture;
	}
	
	public void setSimpleTexture(boolean simpleTexture)
	{
		this.simpleTexture = simpleTexture;
	}
	
	public boolean drawClouds()
	{
		return drawClouds;
	}
	
	public void setDrawClouds(boolean drawClouds)
	{
		this.drawClouds = drawClouds;
	}
	
	public double getCloudRotation()
	{
		return cloudRotation;
	}

	public void setCloudRotation(double cloudRotation)
	{
		this.cloudRotation = cloudRotation;
	}
	
	public int getCloudLevel()
	{
		return cloudLevel;
	}

	public void setCloudLevel(int cloudLevel)
	{
		this.cloudLevel = cloudLevel;
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
		PlanetRenderer viewpointPlanet = PlanetRenderList.getViewpointPlanet();
		Vec3d viewpoint = PlanetRenderList.isViewpointInOrbit() ? viewpointPlanet.getParkingOrbitViewpoint(partialTicks) : viewpointPlanet.getSurfaceViewpoint(partialTicks);
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
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90.0f));

			if(name != PlanetRenderList.getViewpointPlanet().getName())
			{
				matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion((float) viewpointPlanet.getPrecession()));
				matrices.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion((float) (viewpointPlanet.getObliquity())));
				matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion((float) (phiViewpoint - phiPlanet)));
				matrices.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion((float) (thetaPlanet)));
			}
			else
				matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion((float) Math.PI));

			Matrix4f matrix4f3 = matrices.peek().getPositionMatrix();

			if(name.contains("sol"))
			{
				if(weather)
					RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);
				else
				{
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					RenderSystem.setShaderTexture(0, SUN_HAZE_0);
					bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
					bufferBuilder.vertex(matrix4f3, -t, 100.0f, -t).texture(0.0f, 0.0f).next();
					bufferBuilder.vertex(matrix4f3, t, 100.0f, -t).texture(1.0f, 0.0f).next();
					bufferBuilder.vertex(matrix4f3, t, 100.0f, t).texture(1.0f, 1.0f).next();
					bufferBuilder.vertex(matrix4f3, -t, 100.0f, t).texture(0.0f, 1.0f).next();
					BufferRenderer.drawWithShader(bufferBuilder.end());
				}
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
			}

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, getTexture(name));
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bufferBuilder.vertex(matrix4f3, -t, 100.0f, -t).texture(0.0f, 0.0f).next();
			bufferBuilder.vertex(matrix4f3, t, 100.0f, -t).texture(1.0f, 0.0f).next();
			bufferBuilder.vertex(matrix4f3, t, 100.0f, t).texture(1.0f, 1.0f).next();
			bufferBuilder.vertex(matrix4f3, -t, 100.0f, t).texture(0.0f, 1.0f).next();
			BufferRenderer.drawWithShader(bufferBuilder.end());
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
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90.0f));
			
			if(name != PlanetRenderList.getViewpointPlanet().getName())
			{
				matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion((float) viewpointPlanet.getPrecession()));
				matrices.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion((float) (viewpointPlanet.getObliquity())));
				matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion((float) (phiViewpoint - phiPlanet)));
				matrices.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion((float) (thetaPlanet)));
			}
			else
				matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180.0f));
			
			Matrix4f matrix4f3 = matrices.peek().getPositionMatrix();
			RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, getTexture(name));
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bufferBuilder.vertex(matrix4f3, -t, 100.0f, t).texture(endFrame, 0.0f).next();
			bufferBuilder.vertex(matrix4f3, -t, 100.0f, -t).texture(startFrame, 0.0f).next();
			bufferBuilder.vertex(matrix4f3, t, 100.0f, -t).texture(startFrame, 1.0f).next();
			bufferBuilder.vertex(matrix4f3, t, 100.0f, t).texture(endFrame, 1.0f).next();
			BufferRenderer.drawWithShader(bufferBuilder.end());
			
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
				BufferRenderer.drawWithShader(bufferBuilder.end());
			}
			
			RenderSystem.setShaderTexture(0, PLANET_SHADING);
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bufferBuilder.vertex(matrix4f3, -t,	100.0f, t).texture(endShadingFrame, 0.0f).next();
			bufferBuilder.vertex(matrix4f3, -t, 100.0f, -t).texture(startShadingFrame, 0.0f).next();
			bufferBuilder.vertex(matrix4f3, t, 100.0f, -t).texture(startShadingFrame, 1.0f).next();
			bufferBuilder.vertex(matrix4f3, t, 100.0f, t).texture(endShadingFrame, 1.0f).next();
			BufferRenderer.drawWithShader(bufferBuilder.end());
			
			matrices.pop();
		}
	}
}