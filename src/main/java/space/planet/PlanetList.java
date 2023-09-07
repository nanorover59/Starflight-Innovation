package space.planet;

import java.io.File;
import java.util.ArrayList;

import net.darkhax.ess.DataCompound;
import net.darkhax.ess.ESSHelper;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.StarflightMod;
import space.util.IWorldMixin;

public class PlanetList
{
	private static ArrayList<Planet> planetList = new ArrayList<Planet>();
	private static ArrayList<Planet> planetListBuffer = new ArrayList<Planet>();
	private static ArrayList<String> activeClients = new ArrayList<String>();
	private static int timeSteps = 1;
	
	// Intermediate between the ClientPlanetList class and the WorldMixin class.
	public static PlanetDimensionData viewpointDimensionData;
	public static boolean hasViewpoint;
	public static boolean inOrbit;
	public static double sunAngle;
	public static double sunAngleOrbit;
	
	public static void setTimeSteps(int i)
	{
		timeSteps = i;
	}
	
	/**
	 * Return the list of planets.
	 */
	public static ArrayList<Planet> getPlanets()
	{
		return planetList;
	}
	
	/**
	 * Get a planet by its string name.
	 */
	public static Planet getByName(String name)
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
	public static RegistryKey<World> getPlanetWorldKey(Planet p)
	{
		return p.getSurface().getWorldKey();
	}
	
	/**
	 * Get the world key for the given planet's parking orbit dimension. Returns null if the planet has no parking orbit dimension assigned.
	 */
	public static RegistryKey<World> getParkingOrbitWorldKey(Planet p)
	{
		return p.getOrbit().getWorldKey();
	}
	
	/**
	 * Get the Planet instance associated with the given world key.
	 */
	public static PlanetDimensionData getDimensionDataForWorld(World world)
	{
		return ((IWorldMixin) (Object) world).getPlanetDimensionData();
	}
	
	/**
	 * Return true if the given planet has a parking orbit dimension assigned.
	 */
	public static boolean hasOrbit(Planet planet)
	{
		return planet.getOrbit() != null;
	}
	
	/**
	 * Return true if the given planet has a surface dimension assigned.
	 */
	public static boolean hasSurface(Planet planet)
	{
		return planet.getSurface() != null;
	}
	
	public static void deactivateClient(ServerPlayerEntity player)
	{
		activeClients.remove(player.getUuidAsString());
	}
	
	public static void clear()
	{
		planetList.clear();
		planetListBuffer.clear();
		activeClients.clear();
	}
	
	/**
	 * Provide a new list of planet objects to load.
	 */
	public static void loadPlanets(ArrayList<Planet> newPlanets)
	{
		planetListBuffer = newPlanets;
	}
	
	public static void serverTick(MinecraftServer server)
	{
		checkReload(server);
		simulateMotion();
		sendToClients(server);
	}
	
	/**
	 * Perform server actions following a reload of planets if necessary.
	 */
	private static void checkReload(MinecraftServer server)
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
    	activeClients.clear();
	}
	
	/**
	 * Simulate the orbital motion and rotation of each planet. I am aware of how excessive this seems for a Minecraft mod. :l
	 */
	private static void simulateMotion()
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
	 * Send planet rendering information to clients.
	 */
	private static void sendToClients(MinecraftServer server)
	{
		for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
		{
			PacketByteBuf buffer = PacketByteBufs.create();
			PlanetDimensionData data = getDimensionDataForWorld(player.getWorld());
			boolean initialize = !activeClients.contains(player.getUuidAsString());
			
			if(data == null)
			{
				buffer.writeBoolean(false);
				buffer.writeInt(0);
				buffer.writeInt(-1);
				ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "planet_data"), buffer);
				continue;
			}
			
			buffer.writeBoolean(initialize);
			buffer.writeInt(planetList.size());
			int viewpointIndex = -1; // Defaults to -1 for undefined worlds.
			
			if(data.overrideSky())
			{
				Planet planet = data.getPlanet();
				
				for(int i = 0; i < planetList.size(); i++)
				{
					if(planetList.get(i).equals(planet))
					{
						viewpointIndex = i;
						break;
					}
				}
			}
			
			buffer.writeInt(viewpointIndex);
			buffer.writeBoolean(data.isOrbit());
			buffer.writeBoolean(data.isSky());
			buffer.writeBoolean(data.overridePhysics());
			buffer.writeBoolean(data.overrideSky());
			buffer.writeBoolean(data.isCloudy());
			buffer.writeBoolean(data.hasLowClouds());
			buffer.writeBoolean(data.hasWeather());
			buffer.writeBoolean(data.hasOxygen());
			buffer.writeInt(data.getTemperatureCategory());
			buffer.writeDouble(data.getGravity());
			buffer.writeDouble(data.getPressure());
			
			for(int i = 0; i < planetList.size(); i++)
			{
				Planet p = planetList.get(i);
				
				if(initialize)
				{
					buffer.writeString(p.getName());
					buffer.writeString(p.getParentName());
					buffer.writeDouble(p.dVSurfaceToOrbit());
					buffer.writeDouble(p.dVOrbitToSurface());
					buffer.writeDouble(p.getPeriapsis());
					buffer.writeDouble(p.getApoapsis());
					buffer.writeDouble(p.getArgumentOfPeriapsis());
					buffer.writeDouble(p.getTrueAnomaly());
					buffer.writeDouble(p.getAscendingNode());
					buffer.writeDouble(p.getInclination());
					buffer.writeDouble(p.getObliquity());
					buffer.writeDouble(p.getRadius());
					buffer.writeDouble(p.getSurfaceGravity());
					buffer.writeDouble(p.getSurfacePressure());
					buffer.writeBoolean(p.hasLowClouds());
					buffer.writeBoolean(p.hasCloudCover());
					buffer.writeBoolean(p.hasWeather());
					buffer.writeBoolean(p.hasSimpleTexture());
					buffer.writeBoolean(p.drawClouds());
					buffer.writeBoolean(p.getOrbit() != null);
					buffer.writeBoolean(p.getSurface() != null);
					buffer.writeBoolean(p.getSky() != null);
				}
				
				buffer.writeDouble(p.getPosition().getX());
				buffer.writeDouble(p.getPosition().getY());
				buffer.writeDouble(p.getPosition().getZ());
				buffer.writeDouble(p.getSurfaceViewpoint().getX());
				buffer.writeDouble(p.getSurfaceViewpoint().getY());
				buffer.writeDouble(p.getSurfaceViewpoint().getZ());
				buffer.writeDouble(p.getParkingOrbitViewpoint().getX());
				buffer.writeDouble(p.getParkingOrbitViewpoint().getY());
				buffer.writeDouble(p.getParkingOrbitViewpoint().getZ());
				buffer.writeDouble(p.dVToPlanet(data.getPlanet()));
				buffer.writeDouble(p.sunAngle);
				buffer.writeDouble(p.sunAngleOrbit);
				buffer.writeDouble(p.getPrecession());
				buffer.writeDouble(p.getCloudRotation());
				buffer.writeInt(p.getCloudLevel());
				buffer.writeBoolean(true);
			}
			
			if(initialize)
				activeClients.add(player.getUuidAsString());
			
			ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "planet_data"), buffer);
		}
	}
	
	/**
	 * Fast forward the simulation to sunrise on the given planet.
	 */
	public static void skipToMorning(Planet planet)
	{
		double angle = 0.0;
		
		while(angle < 1.75)
		{
			Vec3d position = planet.getPosition();
			Vec3d viewpoint = ClientPlanetList.isViewpointInOrbit() ? planet.getParkingOrbitViewpoint().subtract(position) : planet.getSurfaceViewpoint().subtract(position);
			Vec3d starPosition = new Vec3d(0.0, 0.0, 0.0).subtract(position);
			angle = Math.acos(position.dotProduct(viewpoint) / (viewpoint.length() * starPosition.length()));
			simulateMotion();
		}
	}
	
	/**
	 * Save all world specific dynamic data.
	 */
	public static DataCompound saveDynamicData()
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
	public static void loadDynamicData(DataCompound data)
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
}