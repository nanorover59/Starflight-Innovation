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
	private double obliquity;
	private double precession;
	private double radius;
	private double surfacePressure;
	private boolean hasLowClouds;
	private boolean hasCloudCover;
	private boolean hasWeather;
	private boolean drawClouds;
	private double cloudRotation;
	private int cloudLevel;
	
	private static final Identifier PLANET_SHADING = new Identifier(StarflightMod.MOD_ID, "textures/environment/planet_shading.png");
	private static final Identifier SUN_HAZE_0 = new Identifier(StarflightMod.MOD_ID, "textures/environment/sun_haze_0.png");
	private static final Identifier SUN_0 = new Identifier(StarflightMod.MOD_ID, "textures/environment/sun_0.png");
	private static HashMap<String, Identifier> planetTextures = new HashMap<String, Identifier>();
	
	public PlanetRenderer(String name_, Vec3d position_, Vec3d surfaceViewpoint_, Vec3d parkingOrbitViewpoint_, double obliquity_, double precession_, double radius_, double surfacePressure_, boolean hasLowClouds_, boolean hasCloudCover_, boolean hasWeather_, boolean drawClouds_, double cloudRotation_, int cloudIndex_)
	{
		name = name_;
		setPosition(position_);
		setSurfaceViewpoint(surfaceViewpoint_);
		setParkingOrbitViewpoint(parkingOrbitViewpoint_);
		setObliquity(obliquity_);
		setPrecession(precession_);
		setRadius(radius_);
		setSurfacePressure(surfacePressure_);
		setLowClouds(hasLowClouds_);
		setCloudCover(hasCloudCover_);
		setWeather(hasWeather_);
		setDrawClouds(drawClouds_);
		setCloudRotation(cloudRotation_);
		setCloudLevel(cloudIndex_);
	}
	
	public PlanetRenderer(PlanetRenderer other)
	{
		name = other.name;
		setPosition(other.position);
		setSurfaceViewpoint(other.surfaceViewpoint);
		setParkingOrbitViewpoint(other.parkingOrbitViewpoint);
		setObliquity(other.obliquity);
		setPrecession(other.precession);
		setRadius(other.radius);
		setSurfacePressure(other.surfacePressure);
		setLowClouds(other.hasLowClouds);
		setCloudCover(other.hasCloudCover);
		setWeather(other.hasWeather);
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

	public void setPosition(Vec3d position)
	{
		this.position = position;
	}

	public Vec3d getSurfaceViewpoint()
	{
		return surfaceViewpoint;
	}

	public void setSurfaceViewpoint(Vec3d surfaceViewpoint)
	{
		this.surfaceViewpoint = surfaceViewpoint;
	}

	public Vec3d getParkingOrbitViewpoint()
	{
		return parkingOrbitViewpoint;
	}

	public void setParkingOrbitViewpoint(Vec3d parkingOrbitViewpoint)
	{
		this.parkingOrbitViewpoint = parkingOrbitViewpoint;
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
		
		if(t < 0.25F)
			t = 0.25F;
		else if(t > 500.0f)
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
	
	public void doRender(BufferBuilder bufferBuilder, MatrixStack matrices, float brightness, boolean weather)
	{
		// Angles to render this planet in the sky.
		PlanetRenderer viewpointPlanet = PlanetRenderList.getViewpointPlanet();
		Vec3d viewpoint = PlanetRenderList.isViewpointInOrbit() ? viewpointPlanet.getParkingOrbitViewpoint() : viewpointPlanet.getSurfaceViewpoint();
		double rPlanet = viewpoint.subtract(position).length();
		Vec3d viewpointVector = viewpoint.subtract(viewpointPlanet.getPosition());
		Vec3d planetVector = position.subtract(viewpointPlanet.getPosition());
		double phiViewpoint = Math.atan2(viewpointVector.getZ(), viewpointVector.getX());
		double thetaPlanet = Math.atan2(Math.sqrt((planetVector.getX() * planetVector.getX()) + (planetVector.getZ() * planetVector.getZ())), planetVector.getY()) - (Math.PI / 2.0d);
		double phiPlanet = Math.atan2(planetVector.getZ(), planetVector.getX());
		
		if(name.contains("sol"))
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
			
			if(weather)
				RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);
			else
				RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, SUN_HAZE_0);
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bufferBuilder.vertex(matrix4f3, -t, 100.0f, -t).texture(0.0f, 0.0f).next();
			bufferBuilder.vertex(matrix4f3, t, 100.0f, -t).texture(1.0f, 0.0f).next();
			bufferBuilder.vertex(matrix4f3, t, 100.0f, t).texture(1.0f, 1.0f).next();
			bufferBuilder.vertex(matrix4f3, -t, 100.0f, t).texture(0.0f, 1.0f).next();
			BufferRenderer.drawWithShader(bufferBuilder.end());
			
			RenderSystem.setShaderTexture(0, SUN_0);
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
			double absoluteRotation = Math.atan2(surfaceViewpoint.getZ() - position.getZ(), surfaceViewpoint.getX() - position.getX());
			double angleToViewpoint = Math.atan2(viewpoint.getZ() - position.getZ(), viewpoint.getX() - position.getX());
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
			double angleToStar = Math.atan2(starPosition.getZ() - position.getZ(), starPosition.getX() - position.getX());
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