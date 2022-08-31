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
			boolean drawClouds = planet.drawClouds();
			
			PlanetRenderer planetRenderer = new PlanetRenderer(name, position, surfaceViewpoint, parkingOrbitViewpoint, obliquity, precession, radius, surfacePressure, hasLowClouds, hasCloudCover, hasWeather, drawClouds, cloudRotation, cloudLevel);
			planetListTemporary.add(planetRenderer);
			
			if(i == viewpointIndex)
				viewpointTemporary = planetRenderer;
		}
		
		updated = true;
	}
	
	public static ArrayList<PlanetRenderer> getRenderers(boolean sorted)
	{
		if(updated)
		{
			planetList.clear();
			planetListUnsorted.clear();
			
			for(PlanetRenderer planetRenderer : planetListTemporary)	
				planetList.add(new PlanetRenderer(planetRenderer));
			
			viewpoint = viewpointTemporary == null ? null : new PlanetRenderer(viewpointTemporary);
			inOrbit = inOrbitTemporary;
			planetListUnsorted.addAll(planetList);
			
			if(viewpoint != null)
				Collections.sort(planetList);
			
			updated = false;
		}
		
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