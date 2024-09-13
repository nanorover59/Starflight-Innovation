package space.planet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class ClientPlanetList
{
	private static ArrayList<ClientPlanet> planetList = new ArrayList<ClientPlanet>();
	private static ArrayList<ClientPlanet> planetListUnsorted = new ArrayList<ClientPlanet>();
	private static ArrayList<StaticData> staticDataBuffer = new ArrayList<StaticData>();
	private static ArrayList<DynamicData> dynamicDataBuffer = new ArrayList<DynamicData>();
	private static PlanetDimensionData viewpointDimensionDataBuffer = null;
	private static ClientPlanet viewpoint;
	private static boolean inOrbit;
	
	/**
	 * Receive planet data from the server.
	 */
	public static void receivePlanetListUpdate(Executor executor, PacketByteBuf buffer)
	{
		ArrayList<StaticData> staticData = new ArrayList<StaticData>();
		ArrayList<DynamicData> dynamicData = new ArrayList<DynamicData>();
		boolean initialize = buffer.readBoolean();
		int count = buffer.readInt();
		int viewpointIndex = buffer.readInt();
		
		if(viewpointIndex > -1)
		{
			inOrbit = buffer.readBoolean();
			boolean inSky = buffer.readBoolean();
			boolean overridePhysics = buffer.readBoolean();
			boolean overrideSky = buffer.readBoolean();
			boolean isCloudy = buffer.readBoolean();
			boolean hasLowClouds = buffer.readBoolean();
			boolean hasWeather = buffer.readBoolean();
			boolean hasOxygen = buffer.readBoolean();
			int temperatureCategory = buffer.readInt();
			double gravity = buffer.readDouble();
			double pressure = buffer.readDouble();
			viewpointDimensionDataBuffer = new PlanetDimensionData(new Identifier("overworld"), inOrbit, inSky, overridePhysics, overrideSky, isCloudy, hasLowClouds, hasWeather, hasOxygen, temperatureCategory, gravity, pressure);
		}
		else
			inOrbit = false;
		
		for(int i = 0; i < count; i++)
		{
			// Get static values from the buffer if initializing.
			if(initialize)
			{
				String name = buffer.readString();
				String parentName = buffer.readString();
				double dVOrbit = buffer.readDouble();
				double dVSurface = buffer.readDouble();
				double periapsis = buffer.readDouble();
				double apoapsis = buffer.readDouble();
				double argumentOfPeriapsis = buffer.readDouble();
				double trueAnomaly = buffer.readDouble();
				double ascendingNode = buffer.readDouble();
				double inclination = buffer.readDouble();
				double obliquity = buffer.readDouble();
				double radius = buffer.readDouble();
				double surfaceGravity = buffer.readDouble();
				double surfacePressure = buffer.readDouble();
				boolean hasLowClouds = buffer.readBoolean();
				boolean hasCloudCover = buffer.readBoolean();
				boolean hasWeather = buffer.readBoolean();
				boolean simpleTexture = buffer.readBoolean();
				boolean drawClouds = buffer.readBoolean();
				boolean hasOrbit = buffer.readBoolean();
				boolean hasSurface = buffer.readBoolean();
				boolean hasSky = buffer.readBoolean();
				staticData.add(new StaticData(name, parentName, dVOrbit, dVSurface, periapsis, apoapsis, argumentOfPeriapsis, trueAnomaly, ascendingNode, inclination, obliquity, radius, surfaceGravity, surfacePressure, hasLowClouds, hasCloudCover, hasWeather, simpleTexture, drawClouds, hasOrbit, hasSurface, hasSky));
			}
			
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
			double dVTransfer = buffer.readDouble();
			double sunAngle = buffer.readDouble();
			double sunAngleOrbit = buffer.readDouble();
			double precession = buffer.readDouble();
			double cloudRotation = buffer.readDouble();
			int cloudLevel = buffer.readInt();
			boolean unlocked = buffer.readBoolean();
			Vec3d position = new Vec3d(positionX, positionY, positionZ);
			Vec3d surfaceViewpoint = new Vec3d(surfaceViewpointX, surfaceViewpointY, surfaceViewpointZ);
			Vec3d parkingOrbitViewpoint = new Vec3d(parkingOrbitViewpointX, parkingOrbitViewpointY, parkingOrbitViewpointZ);
			dynamicData.add(new DynamicData(position, surfaceViewpoint, parkingOrbitViewpoint, dVTransfer, precession, sunAngle, sunAngleOrbit, cloudRotation, cloudLevel, unlocked, i == viewpointIndex));
		}
		
		executor.execute(() -> {
			if(viewpointIndex == -1)
				viewpoint = null;
			else
			{
				if(staticDataBuffer.isEmpty())
					staticDataBuffer = staticData;
				
				if(dynamicDataBuffer.isEmpty())
					dynamicDataBuffer = dynamicData;
			}
		});
	}
	
	/**
	 * Apply planet data from the server. This should be called at the same point during a client tick.
	 */
	public static void updatePlanets()
	{
		if(dynamicDataBuffer.isEmpty())
			return;
		
		planetList.clear();
		viewpoint = null;
		
		if(staticDataBuffer.isEmpty())
		{
			if(planetListUnsorted.isEmpty())
				return;
			
			for(int i = 0; i < dynamicDataBuffer.size(); i++)
			{
				DynamicData dynamicData = dynamicDataBuffer.get(i);
				ClientPlanet clientPlanet = planetListUnsorted.get(i);
				
				clientPlanet.positionPrevious = clientPlanet.position;
				clientPlanet.surfaceViewpointPrevious = clientPlanet.surfaceViewpoint;
				clientPlanet.parkingOrbitViewpointPrevious = clientPlanet.parkingOrbitViewpoint;
				clientPlanet.position = dynamicData.position();
				clientPlanet.surfaceViewpoint = dynamicData.surfaceViewpoint();
				clientPlanet.parkingOrbitViewpoint = dynamicData.parkingOrbitViewpoint();
				clientPlanet.sunAngle = dynamicData.sunAngle();
				clientPlanet.sunAngleOrbit = dynamicData.sunAngleOrbit();
				clientPlanet.cloudRotation = dynamicData.cloudRotation();
				clientPlanet.cloudLevel = dynamicData.cloudLevel();
				clientPlanet.unlocked = dynamicData.unlocked();
				
				if(dynamicData.isViewpoint)
					viewpoint = clientPlanet;
			}
		}
		else
		{
			planetListUnsorted.clear();
			
			for(int i = 0; i < dynamicDataBuffer.size(); i++)
			{
				ClientPlanet clientPlanet = new ClientPlanet();
				StaticData staticData = staticDataBuffer.get(i);
				clientPlanet.name = staticData.name();
				clientPlanet.parentName = staticData.parentName();
				clientPlanet.dVOrbit = staticData.dVOrbit();
				clientPlanet.dVSurface = staticData.dVSurface();
				clientPlanet.periapsis = staticData.periapsis();
				clientPlanet.apoapsis = staticData.apoapsis();
				clientPlanet.argumentOfPeriapsis = staticData.argumentOfPeriapsis();
				clientPlanet.trueAnomaly = staticData.trueAnomaly();
				clientPlanet.ascendingNode = staticData.ascendingNode();
				clientPlanet.inclination = staticData.inclination();
				clientPlanet.obliquity = staticData.obliquity();
				clientPlanet.radius = staticData.radius();
				clientPlanet.surfaceGravity = staticData.surfaceGravity();
				clientPlanet.surfacePressure = staticData.surfacePressure();
				clientPlanet.hasLowClouds = staticData.hasLowClouds();
				clientPlanet.hasCloudCover = staticData.hasCloudCover();
				clientPlanet.hasWeather = staticData.hasWeather();
				clientPlanet.simpleTexture = staticData.simpleTexture();
				clientPlanet.drawClouds = staticData.drawClouds();
				clientPlanet.hasOrbit = staticData.hasOrbit();
				clientPlanet.hasSurface = staticData.hasSurface();
				clientPlanet.hasSky = staticData.hasSky();
				DynamicData dynamicData = dynamicDataBuffer.get(i);
				clientPlanet.positionPrevious = dynamicData.position();
				clientPlanet.surfaceViewpointPrevious = dynamicData.surfaceViewpoint();
				clientPlanet.parkingOrbitViewpointPrevious = dynamicData.parkingOrbitViewpoint();
				clientPlanet.position = dynamicData.position();
				clientPlanet.surfaceViewpoint = dynamicData.surfaceViewpoint();
				clientPlanet.parkingOrbitViewpoint = dynamicData.parkingOrbitViewpoint();
				clientPlanet.dVTransfer = dynamicData.dVTransfer();
				clientPlanet.sunAngle = dynamicData.sunAngle();
				clientPlanet.sunAngleOrbit = dynamicData.sunAngleOrbit();
				clientPlanet.cloudRotation = dynamicData.cloudRotation();
				clientPlanet.cloudLevel = dynamicData.cloudLevel();
				clientPlanet.unlocked = dynamicData.unlocked();
				planetListUnsorted.add(clientPlanet);
				
				if(dynamicData.isViewpoint)
					viewpoint = clientPlanet;
			}
			
			for(ClientPlanet p : planetListUnsorted)
				p.linkSatellites(planetListUnsorted);
		}
		
		planetList.addAll(planetListUnsorted);
		staticDataBuffer.clear();
		dynamicDataBuffer.clear();
		PlanetList.hasViewpoint = viewpoint != null;
		PlanetList.viewpointDimensionData = viewpointDimensionDataBuffer;
		
		if(viewpoint != null)
		{
			Collections.sort(planetList);
			PlanetList.inOrbit = inOrbit;
			PlanetList.sunAngle = viewpoint.sunAngle;
			PlanetList.sunAngleOrbit = viewpoint.sunAngleOrbit;
		}
		else
			PlanetList.viewpointDimensionData = null;
	}
	
	public static ArrayList<ClientPlanet> getPlanets(boolean sorted)
	{
		return sorted ? planetList : planetListUnsorted;
	}
	
	public static ClientPlanet getViewpointPlanet()
	{
		return viewpoint;
	}
	
	public static PlanetDimensionData getViewpointDimensionData()
	{
		return viewpointDimensionDataBuffer;
	}
	
	public static boolean isViewpointInOrbit()
	{
		return inOrbit;
	}
	
	/**
	 * Get a planet by its string name.
	 */
	public static ClientPlanet getByName(String name)
	{
		if(name == null || name.isEmpty())
			return null;
		
		for(ClientPlanet p : planetList)
		{
			if(p.getName().contains(name))
				return p;
		}
		
		return null;
	}
	
	private record StaticData(String name, String parentName, double dVOrbit, double dVSurface, double periapsis, double apoapsis, double argumentOfPeriapsis, double trueAnomaly, double ascendingNode, double inclination, double obliquity, double radius, double surfaceGravity, double surfacePressure, boolean hasLowClouds, boolean hasCloudCover, boolean hasWeather, boolean simpleTexture, boolean drawClouds, boolean hasOrbit, boolean hasSurface, boolean hasSky) {}
	private record DynamicData(Vec3d position, Vec3d surfaceViewpoint, Vec3d parkingOrbitViewpoint, double dVTransfer, double precession, double sunAngle, double sunAngleOrbit, double cloudRotation, int cloudLevel, boolean unlocked, boolean isViewpoint) {}
}