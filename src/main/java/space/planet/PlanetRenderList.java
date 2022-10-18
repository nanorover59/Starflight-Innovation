package space.planet;

import java.util.ArrayList;
import java.util.Collections;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class PlanetRenderList
{
	private static ArrayList<PlanetRenderer> planetList = new ArrayList<PlanetRenderer>();
	private static ArrayList<PlanetRenderer> planetListUnsorted = new ArrayList<PlanetRenderer>();
	private static ArrayList<PlanetRenderer> planetListTemporary = new ArrayList<PlanetRenderer>();
	private static PlanetRenderer viewpoint;
	private static PlanetRenderer viewpointTemporary;
	private static long lastUpdateTime;
	private static boolean inOrbit;
	private static boolean inOrbitTemporary;
	private static boolean updated;
	
	public static void receivePlanetListUpdate(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
		long serverTime = buffer.readLong();
		
		planetListTemporary.clear();
		viewpointTemporary = null;
		int count = buffer.readInt();
		int viewpointIndex = buffer.readInt();
		inOrbitTemporary = buffer.readBoolean();
		
		for(int i = 0; i < count; i++)
		{
			// Get dynamic values from the buffer.
			double positionX = buffer.readDouble();
			double positionY = buffer.readDouble();
			double positionZ = buffer.readDouble();
			double surfaceViewpointX = buffer.readDouble();
			double surfaceViewpointY = buffer.readDouble();
			double surfaceViewpointZ = buffer.readDouble();
			double parkingOrbitViewpointX = buffer.readDouble();
			double parkingOrbitViewpointY = buffer.readDouble();
			double parkingOrbitViewpointZ = buffer.readDouble();
			double precession = buffer.readDouble();
			double cloudRotation = buffer.readDouble();
			int cloudLevel = buffer.readInt();
			Vec3d position = new Vec3d(positionX, positionY, positionZ);
			Vec3d surfaceViewpoint = new Vec3d(surfaceViewpointX, surfaceViewpointY, surfaceViewpointZ);
			Vec3d parkingOrbitViewpoint = new Vec3d(parkingOrbitViewpointX, parkingOrbitViewpointY, parkingOrbitViewpointZ);
			
			// Get constant values from the PlanetList class.
			Planet planet = PlanetList.getPlanets().get(i);
			String name = planet.getName();
			double obliquity = planet.getObliquity();
			double radius = planet.getRadius();
			double surfacePressure = planet.getSurfacePressure();
			boolean hasLowClouds = planet.hasLowClouds();
			boolean hasCloudCover = planet.hasCloudCover();
			boolean hasWeather = planet.hasWeather();
			boolean simpleTexture = planet.hasSimpleTexture();
			boolean drawClouds = planet.drawClouds();
			
			// Create the updated PlanetRenderer instance.
			PlanetRenderer planetRenderer = new PlanetRenderer(name, position, surfaceViewpoint, parkingOrbitViewpoint, obliquity, precession, radius, surfacePressure, hasLowClouds, hasCloudCover, hasWeather, simpleTexture, drawClouds, cloudRotation, cloudLevel);
			planetRenderer.setPositionPrevious(position);
			planetRenderer.setSurfaceViewpointPrevious(surfaceViewpoint);
			planetRenderer.setParkingOrbitViewpointPrevious(parkingOrbitViewpoint);
			planetListTemporary.add(planetRenderer);
			
			if(i == viewpointIndex)
				viewpointTemporary = planetRenderer;
		}
	}
	
	public static void updateRenderers()
	{
		planetList.clear();
		
		for(int i = 0; i < planetListTemporary.size(); i++)
		{
			PlanetRenderer planetRenderer = planetListTemporary.get(i);
			PlanetRenderer planetRendererPrevious = planetListTemporary.size() == planetListUnsorted.size() ? planetListUnsorted.get(i) : null;
			
			if(planetRendererPrevious != null)
			{
				planetRenderer.setPositionPrevious(planetRendererPrevious.getPosition());
				planetRenderer.setSurfaceViewpointPrevious(planetRendererPrevious.getSurfaceViewpoint());
				planetRenderer.setParkingOrbitViewpointPrevious(planetRendererPrevious.getParkingOrbitViewpoint());
			}
			
			planetList.add(planetRenderer);
		}
		
		planetListUnsorted.clear();
		viewpoint = viewpointTemporary == null ? null : viewpointTemporary;
		inOrbit = inOrbitTemporary;
		planetListUnsorted.addAll(planetList);
		
		if(viewpoint != null)
			Collections.sort(planetList);
	}
	
	public static ArrayList<PlanetRenderer> getRenderers(boolean sorted)
	{
		return sorted ? planetList : planetListUnsorted;
	}
	
	public static PlanetRenderer getViewpointPlanet()
	{
		return viewpoint;
	}
	
	public static boolean isViewpointInOrbit()
	{
		return inOrbit;
	}
}