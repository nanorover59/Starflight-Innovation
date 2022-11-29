package space.planet;

import java.util.ArrayList;
import java.util.Collections;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class PlanetRenderList
{
	private static ArrayList<PlanetRenderer> planetList = new ArrayList<PlanetRenderer>();
	private static ArrayList<PlanetRenderer> planetListUnsorted = new ArrayList<PlanetRenderer>();
	private static ArrayList<DynamicData> dataBuffer = new ArrayList<DynamicData>();
	private static PlanetRenderer viewpoint;
	private static boolean inOrbit;
	
	public static void receivePlanetListUpdate(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
		ArrayList<DynamicData> receivedData = new ArrayList<DynamicData>();
		int count = buffer.readInt();
		int viewpointIndex = buffer.readInt();
		inOrbit = buffer.readBoolean();
		
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
			receivedData.add(new DynamicData(position, surfaceViewpoint, parkingOrbitViewpoint, precession, cloudRotation, cloudLevel, i == viewpointIndex));
		}
		
		client.execute(() -> {
			dataBuffer.clear();
			dataBuffer.addAll(receivedData);
		});
	}
	
	public static void updateRenderers()
	{
		if(planetListUnsorted.size() != dataBuffer.size())
		{
			planetList.clear();
			planetListUnsorted.clear();
			
			for(int i = 0; i < dataBuffer.size(); i++)
			{
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
				
				// Create the PlanetRenderer instance.
				DynamicData data = dataBuffer.get(i);
				PlanetRenderer planetRenderer = new PlanetRenderer(name, obliquity, radius, surfacePressure, hasLowClouds, hasCloudCover, hasWeather, simpleTexture, drawClouds);
				planetRenderer.setPosition(data.position);
				planetRenderer.setSurfaceViewpoint(data.surfaceViewpoint);
				planetRenderer.setParkingOrbitViewpoint(data.parkingOrbitViewpoint);
				planetRenderer.setPositionPrevious(data.position);
				planetRenderer.setSurfaceViewpointPrevious(data.surfaceViewpoint);
				planetRenderer.setParkingOrbitViewpointPrevious(data.parkingOrbitViewpoint);
				planetRenderer.setCloudRotation(data.cloudRotation);
				planetRenderer.setCloudLevel(data.cloudLevel);
				planetList.add(planetRenderer);
				
				if(data.isViewpoint)
					viewpoint = planetRenderer;
			}
			
			planetListUnsorted.addAll(planetList);
			
			if(viewpoint != null)
				Collections.sort(planetList);
		}
		else
		{
			for(int i = 0; i < dataBuffer.size(); i++)
			{
				DynamicData data = dataBuffer.get(i);
				PlanetRenderer planetRenderer = planetListUnsorted.get(i);
				planetRenderer.setPositionPrevious(planetRenderer.getPosition());
				planetRenderer.setSurfaceViewpointPrevious(planetRenderer.getSurfaceViewpoint());
				planetRenderer.setParkingOrbitViewpointPrevious(planetRenderer.getParkingOrbitViewpoint());
				planetRenderer.setPosition(data.position);
				planetRenderer.setSurfaceViewpoint(data.surfaceViewpoint);
				planetRenderer.setParkingOrbitViewpoint(data.parkingOrbitViewpoint);
				planetRenderer.setCloudRotation(data.cloudRotation);
				planetRenderer.setCloudLevel(data.cloudLevel);
				
				if(data.isViewpoint)
				{
					viewpoint = planetRenderer;
					Collections.sort(planetList);
				}
			}
		}
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
	
	private record DynamicData(Vec3d position, Vec3d surfaceViewpoint, Vec3d parkingOrbitViewpoint, double precession, double cloudRotation, int cloudLevel, boolean isViewpoint) {}
}