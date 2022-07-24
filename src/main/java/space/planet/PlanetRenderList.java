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
	private static ArrayList<PlanetRenderer> planetListTemporary = new ArrayList<PlanetRenderer>();
	private static PlanetRenderer viewpoint;
	private static PlanetRenderer viewpointTemporary;
	private static boolean inOrbit;
	private static boolean inOrbitTemporary;
	private static boolean updated;
	
	public static void receivePlanetListUpdate(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
		if(updated)
			return;
		
		planetListTemporary.clear();
		int count = buffer.readInt();
		String viewpointName = buffer.readString();
		inOrbitTemporary = viewpointName.contains("_orbit");	
		
		for(int i = 0; i < count; i++)
		{
			String name = buffer.readString();
			double positionX = buffer.readDouble();
			double positionY = buffer.readDouble();
			double positionZ = buffer.readDouble();
			double surfaceViewpointX = buffer.readDouble();
			double surfaceViewpointY = buffer.readDouble();
			double surfaceViewpointZ = buffer.readDouble();
			double parkingOrbitViewpointX = buffer.readDouble();
			double parkingOrbitViewpointY = buffer.readDouble();
			double parkingOrbitViewpointZ = buffer.readDouble();
			double obliquity = buffer.readDouble();
			double precession = buffer.readDouble();
			double radius = buffer.readDouble();
			double surfacePressure = buffer.readDouble();
			boolean hasLowClouds = buffer.readBoolean();
			boolean hasCloudCover = buffer.readBoolean();
			boolean hasWeather = buffer.readBoolean();
			boolean drawClouds = buffer.readBoolean();
			double cloudRotation = buffer.readDouble();
			int cloudLevel = buffer.readInt();
			Vec3d position = new Vec3d(positionX, positionY, positionZ);
			Vec3d surfaceViewpoint = new Vec3d(surfaceViewpointX, surfaceViewpointY, surfaceViewpointZ);
			Vec3d parkingOrbitViewpoint = new Vec3d(parkingOrbitViewpointX, parkingOrbitViewpointY, parkingOrbitViewpointZ);
			PlanetRenderer planetRenderer = new PlanetRenderer(name, position, surfaceViewpoint, parkingOrbitViewpoint, obliquity, precession, radius, surfacePressure, hasLowClouds, hasCloudCover, hasWeather, drawClouds, cloudRotation, cloudLevel);
			planetListTemporary.add(planetRenderer);
			
			if(viewpointName.contains(name))
				viewpointTemporary = planetRenderer;
		}
		
		updated = true;
	}
	
	public static ArrayList<PlanetRenderer> getRenderers()
	{
		if(updated)
		{
			planetList.clear();
			
			for(PlanetRenderer planetRenderer : planetListTemporary)
				planetList.add(new PlanetRenderer(planetRenderer));
			
			viewpoint = new PlanetRenderer(viewpointTemporary);
			inOrbit = inOrbitTemporary;
			Collections.sort(planetList);
			updated = false;
		}
		
		return planetList;
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
