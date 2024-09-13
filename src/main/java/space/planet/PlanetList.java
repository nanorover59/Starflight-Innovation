package space.planet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import net.darkhax.ess.DataCompound;
import net.darkhax.ess.ESSHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.network.s2c.PlanetDynamicDataS2CPacket;
import space.network.s2c.PlanetStaticDataS2CPacket;
import space.util.IWorldMixin;

public class PlanetList
{
	private static PlanetList instance = null;
	private static PlanetList clientInstance = null;
	
	private ArrayList<Planet> planetList = new ArrayList<Planet>();
	private ArrayList<Planet> planetListBuffer = new ArrayList<Planet>();
	private PlanetDynamicDataS2CPacket dynamicDataBuffer = null;
	private Planet viewpointPlanet = null;
	private PlanetDimensionData viewpointDimensionData = null;
	private int timeSteps = 1;
	
	public static PlanetList get()
	{
		if(instance == null)
			instance = new PlanetList();
		
		return instance;
	}
	
	public static PlanetList getClient()
	{
		if(clientInstance == null)
			clientInstance = new PlanetList();
		
		return clientInstance;
	}
	
	public static void reset()
	{
		instance = null;
		clientInstance = null;
	}
	
	public void setDynamicDataBuffer(PlanetDynamicDataS2CPacket packet)
	{
		dynamicDataBuffer = packet;
	}
	
	/**
	 * Get the Planet instance associated with the given world key.
	 */
	public static PlanetDimensionData getDimensionDataForWorld(World world)
	{
		return ((IWorldMixin) (Object) world).getPlanetDimensionData();
	}
	
	public void setTimeSteps(int i)
	{
		timeSteps = i;
	}
	
	/**
	 * Return the list of planets.
	 */
	public ArrayList<Planet> getPlanets()
	{
		return planetList;
	}
	
	/**
	 * Get a planet by its string name.
	 */
	public Planet getByName(String name)
	{
		if(name == null || name.isEmpty())
			return null;
		
		for(Planet p : planetList)
		{
			if(p.getName().contains(name))
				return p;
		}
		
		return null;
	}
	
	/**
	 * Get the world key for the given planet's surface dimension. Returns null if the planet has no surface dimension assigned.
	 */
	public RegistryKey<World> getPlanetWorldKey(Planet p)
	{
		return p.getSurface().getWorldKey();
	}
	
	/**
	 * Get the world key for the given planet's parking orbit dimension. Returns null if the planet has no parking orbit dimension assigned.
	 */
	public RegistryKey<World> getParkingOrbitWorldKey(Planet p)
	{
		return p.getOrbit().getWorldKey();
	}
	
	/**
	 * Get the viewpoint planet for the client player.
	 */
	public Planet getViewpointPlanet()
	{
		return viewpointPlanet;
	}
	
	/**
	 * Get the viewpoint planet dimension data for the client player.
	 */
	public PlanetDimensionData getViewpointDimensionData()
	{
		return viewpointDimensionData;
	}
	
	/**
	 * Provide a new list of planet objects to load.
	 */
	public void loadPlanets(ArrayList<Planet> newPlanets)
	{
		planetListBuffer = newPlanets;
	}
	
	public void serverTick(MinecraftServer server)
	{
		checkReload(server);
		simulateMotion();
		sendDynamicDataToClients(server);
	}
	
	public void clientTick(float tickDelta)
	{
		for(Planet planet : planetList)
			planet.movePreviousPositions();
		
		if(!planetListBuffer.isEmpty())
		{
			planetList = planetListBuffer;
			planetListBuffer = new ArrayList<Planet>();
		}
		
		if(dynamicDataBuffer != null)
		{
			HashMap<String, Planet.DynamicData> dynamicDataMap = dynamicDataBuffer.dynamicDataMap();
			RegistryKey<World> worldKey = dynamicDataBuffer.viewpoint();
			timeSteps = dynamicDataBuffer.timeSteps();
			viewpointPlanet = null;
			viewpointDimensionData = null;

			for(Planet planet : planetList)
			{
				Planet.DynamicData data = dynamicDataMap.get(planet.getName());

				if(data != null)
					planet.readDynamicData(data);

				if(planet.getOrbit() != null && planet.getOrbit().getWorldKey() == worldKey)
				{
					viewpointPlanet = planet;
					viewpointDimensionData = planet.getOrbit();
				}
				else if(planet.getSurface() != null && planet.getSurface().getWorldKey() == worldKey)
				{
					viewpointPlanet = planet;
					viewpointDimensionData = planet.getSurface();
				}
				else if(planet.getSky() != null && planet.getSky().getWorldKey() == worldKey)
				{
					viewpointPlanet = planet;
					viewpointDimensionData = planet.getSky();
				}
			}
			
			dynamicDataBuffer = null;
		}
		
		Collections.sort(planetList);
	}
	
	/**
	 * Perform server actions following a reload of planets if necessary.
	 */
	private void checkReload(MinecraftServer server)
	{
		if(planetListBuffer.isEmpty())
			return;
		
		DataCompound previousDynamicData = null;
		
		if(!planetList.isEmpty())
			previousDynamicData = saveDynamicData();
		
		planetList = planetListBuffer;
		
		if(previousDynamicData == null)
		{
			File planetFile = new File(server.getSavePath(WorldSavePath.ROOT).toString() + "/space/planets.dat");
	    	DataCompound planetData = null;
	    	
	    	if(planetFile.exists())
	    		planetData = ESSHelper.readCompound(planetFile);
	    	
	    	loadDynamicData(planetData);
		}
		else
			loadDynamicData(previousDynamicData);
		
    	// Reset world specific planet dimension data.
    	for(World world : server.getWorlds())
			((IWorldMixin) (Object) world).clearPlanetDimensionData();
    	
    	// Clear Array Lists
    	planetListBuffer = new ArrayList<Planet>();
    	
    	// Update Clients
    	sendStaticDataToClients(server);
	}
	
	/**
	 * Simulate the orbital motion and rotation of each planet. I am aware of how excessive this seems for a Minecraft mod. :l
	 */
	private void simulateMotion()
	{
		double timeStep = 72.0 * 0.05;
		
		for(int i = 0; i < timeSteps; i++)
		{
			for(Planet p : planetList)
			{
				p.simulateGravityAcceleration();
				p.simulateVelocityChange(timeStep);
			}
			
			for(Planet p : planetList)
				p.simulatePositionAndRotationChange(timeStep);
		}
	}
	
	/**
	 * Fast forward the simulation to sunrise on the given planet.
	 */
	public void skipToMorning(Planet planet)
	{
		double angle = 0.0;
		
		while(angle < 1.75)
		{
			Vec3d position = planet.getPosition();
			Vec3d viewpoint = planet.getSurfaceViewpoint().subtract(position);
			Vec3d starPosition = new Vec3d(0.0, 0.0, 0.0).subtract(position);
			angle = Math.acos(position.dotProduct(viewpoint) / (viewpoint.length() * starPosition.length()));
			simulateMotion();
		}
	}
	
	/**
	 * Save all world specific dynamic data.
	 */
	public DataCompound saveDynamicData()
	{
		DataCompound data = new DataCompound();
		
		for(Planet p : planetList)
			data = p.saveData(data);
		
		data.setValue("planetCount", planetList.size());
		data.setValue("timeSteps", timeSteps);
		return data;
	}
	
	/**
	 * Load all world specific dynamic data.
	 */
	public void loadDynamicData(DataCompound data)
	{
		Planet centerPlanet = getByName("sol");
		ArrayList<String> checkList = new ArrayList<String>();
		
		if(data != null && data.hasName("planetCount"))
		{
			if(data.getInt("planetCount") != planetList.size())
				centerPlanet.setInitialPositionAndVelocity(checkList);
			else
			{
				timeSteps = data.getInt("timeSteps");
				centerPlanet.loadData(data, checkList);
			}
		}
		else
			centerPlanet.setInitialPositionAndVelocity(checkList);
	}
	
	public void sendDynamicDataToClients(MinecraftServer server)
	{
		HashMap<String, Planet.DynamicData> dynamicDataMap = new HashMap<String, Planet.DynamicData>();
		
		for(Planet planet : planetList)
			dynamicDataMap.put(planet.getName(), planet.getDynamicData());
		
		for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
			ServerPlayNetworking.send(player, new PlanetDynamicDataS2CPacket(dynamicDataMap, player.getWorld().getRegistryKey(), timeSteps));
	}
	
	public void sendStaticDataToClients(MinecraftServer server)
	{
		HashMap<String, Planet.StaticData> staticDataMap = new HashMap<String, Planet.StaticData>();
		
		for(Planet planet : planetList)
			staticDataMap.put(planet.getName(), planet.getStaticData());
		
		for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
			ServerPlayNetworking.send(player, new PlanetStaticDataS2CPacket(staticDataMap));
	}
	
	public void sendStaticDataToClient(ServerPlayerEntity player)
	{
		HashMap<String, Planet.StaticData> staticDataMap = new HashMap<String, Planet.StaticData>();
		
		for(Planet planet : planetList)
			staticDataMap.put(planet.getName(), planet.getStaticData());
		
		ServerPlayNetworking.send(player, new PlanetStaticDataS2CPacket(staticDataMap));
	}
}