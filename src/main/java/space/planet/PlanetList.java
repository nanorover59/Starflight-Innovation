package space.planet;

import java.util.ArrayList;
import java.util.HashMap;

import net.darkhax.ess.DataCompound;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import space.StarflightMod;

public class PlanetList
{
	private static ArrayList<Planet> planetList = new ArrayList<Planet>();
	private static HashMap<Planet, RegistryKey<World>> planetWorldKeys = new HashMap<Planet, RegistryKey<World>>();
	private static HashMap<Planet, RegistryKey<World>> parkingOrbitWorldKeys = new HashMap<Planet, RegistryKey<World>>();
	
	/**
	 * Register all planets.
	 */
	public static void initialize()
	{
		planetList.clear();
		planetWorldKeys.clear();
		parkingOrbitWorldKeys.clear();
		
		Planet sol = new Planet("sol", "null", 0, 1.98847e30, 696.34e6, 1000e6);
		sol.setOrbitParameters(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		sol.setRotationParameters(false, -0.1265364d, 2.9089e-6, 0);
		sol.setAtmosphereParameters(Planet.EXTRA_HOT, 0.0, false, false, false, false);
		sol.setDecorativeParameters(false, 0);
		planetList.add(sol);
		 
		Planet earth = new Planet("earth", "sol", 1, Planet.EARTH_MASS, Planet.EARTH_RADIUS, 500e3);
		earth.setOrbitParameters(1.4710e11, 1.5210e11, 5.0282936, 0.0, 3.0525809, 0.0);
		earth.setRotationParameters(false, -0.40910518, 7.2921150e-5, 7.663e-12);
		earth.setAtmosphereParameters(Planet.TEMPERATE, 1.0, true, true, false, true);
		earth.setDecorativeParameters(true, 7.4e-5);
		planetList.add(earth);
		planetWorldKeys.put(earth, World.OVERWORLD);
		parkingOrbitWorldKeys.put(earth, RegistryKey.of(Registry.WORLD_KEY, new Identifier(StarflightMod.MOD_ID, "earth_orbit")));
		 
		Planet moon = new Planet("moon", "earth", 2, 7.34767309e22, 1.7381e6, 200e3);
		moon.setOrbitParameters(3.633e8, 4.055e8, 5.55276502, 2.36090688, 2.18305783, 0.09005899);
		moon.setRotationParameters(true, -0.1164135, 0.0, 0.0);
		moon.setAtmosphereParameters(Planet.TEMPERATE, 0.0, false, false, false, false);
		moon.setDecorativeParameters(false, 0);
		planetList.add(moon);
		planetWorldKeys.put(moon, RegistryKey.of(Registry.WORLD_KEY, new Identifier(StarflightMod.MOD_ID, "moon")));
		parkingOrbitWorldKeys.put(moon, RegistryKey.of(Registry.WORLD_KEY, new Identifier(StarflightMod.MOD_ID, "moon_orbit")));
		
		Planet mars = new Planet("mars", "sol", 1, 0.64171e24, 3.3962e6, 500e3);
		mars.setOrbitParameters(2.06617e11, 2.49229e11, 5.86501907915, 0.0, 0.86530876133, 0.03229923767);
		mars.setRotationParameters(false, -0.43964844, 7.088218e-5, 1.1385e-12);
		mars.setAtmosphereParameters(Planet.COLD, 0.00602, false, false, false, true);
		mars.setDecorativeParameters(true, 7.2e-5);
		planetList.add(mars);
		planetWorldKeys.put(mars, RegistryKey.of(Registry.WORLD_KEY, new Identifier(StarflightMod.MOD_ID, "mars")));
		parkingOrbitWorldKeys.put(mars, RegistryKey.of(Registry.WORLD_KEY, new Identifier(StarflightMod.MOD_ID, "mars_orbit")));
		
		linkSatellites();
	}
	
	/**
	 * Find and set the parent body of each planet from the string name provided.
	 */
	private static void linkSatellites()
	{
		for(Planet p : planetList)
			p.linkSatellites();
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
		if(planetWorldKeys.containsKey(p))
			return planetWorldKeys.get(p);
		
		return null;
	}
	
	/**
	 * Get the world key for the given planet's parking orbit dimension. Returns null if the planet has no parking orbit dimension assigned.
	 */
	public static RegistryKey<World> getParkingOrbitWorldKey(Planet p)
	{
		if(parkingOrbitWorldKeys.containsKey(p))
			return parkingOrbitWorldKeys.get(p);
		
		return null;
	}
	
	/**
	 * Get the Planet instance associated with the given world key.
	 */
	public static Planet getPlanetForWorld(RegistryKey<World> world)
	{
		if(planetWorldKeys.containsValue(world))
		{
			for(Planet p : planetWorldKeys.keySet())
			{
				if(planetWorldKeys.get(p) == world)
					return p;
			}
		}
		else if(parkingOrbitWorldKeys.containsValue(world))
		{
			for(Planet p : parkingOrbitWorldKeys.keySet())
			{
				if(parkingOrbitWorldKeys.get(p) == world)
					return p;
			}
		}
		
		return null;
	}
	
	/**
	 * Return true if the given world key is for a planet's parking orbit dimension.
	 */
	public static boolean isOrbit(RegistryKey<World> world)
	{
		return parkingOrbitWorldKeys.containsValue(world);
	}
	
	/**
	 * Return true if the given planet has a parking orbit dimension assigned.
	 */
	public static boolean hasOrbit(Planet planet)
	{
		return parkingOrbitWorldKeys.containsKey(planet);
	}
	
	/**
	 * Return true if the given planet has a surface dimension assigned.
	 */
	public static boolean hasSurface(Planet planet)
	{
		return planetWorldKeys.containsKey(planet);
	}
	
	/**
	 * Save all world specific planet data.
	 */
	public static DataCompound saveData()
	{
		DataCompound data = new DataCompound();
		
		for(Planet p : planetList)
			data = p.saveData(data);
		
		return data;
	}
	
	/**
	 * Load all world specific planet data.
	 */
	public static void loadData(DataCompound data)
	{
		initialize();
		Planet centerPlanet = planetList.get(0);
		ArrayList<String> checkList = new ArrayList<String>();
		
		if(data != null)
			centerPlanet.loadData(data, checkList);
		else
			centerPlanet.setInitialPositionAndVelocity(checkList);
	}
	
	/**
	 * Send planet rendering information to clients.
	 */
	public static void sendToClients(MinecraftServer server)
	{
		for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
		{
			if(getPlanetForWorld(player.getWorld().getRegistryKey()) == null)
				continue;
			
			PacketByteBuf buffer = PacketByteBufs.create();
			buffer.writeInt(planetList.size());
			RegistryKey<World> worldKey = player.world.getRegistryKey();
			Planet planet = getPlanetForWorld(worldKey);
			buffer.writeString(isOrbit(worldKey) ? (planet.getName() + "_orbit") : planet.getName());
			
			for(Planet p : planetList)
			{
				buffer.writeString(p.getName());
				buffer.writeDouble(p.getPosition().getX());
				buffer.writeDouble(p.getPosition().getY());
				buffer.writeDouble(p.getPosition().getZ());
				buffer.writeDouble(p.getSurfaceViewpoint().getX());
				buffer.writeDouble(p.getSurfaceViewpoint().getY());
				buffer.writeDouble(p.getSurfaceViewpoint().getZ());
				buffer.writeDouble(p.getParkingOrbitViewpoint().getX());
				buffer.writeDouble(p.getParkingOrbitViewpoint().getY());
				buffer.writeDouble(p.getParkingOrbitViewpoint().getZ());	
				buffer.writeDouble(p.getObliquity());
				buffer.writeDouble(p.getPrecession());
				buffer.writeDouble(p.getRadius());
				buffer.writeDouble(p.getSurfacePressure());
				buffer.writeBoolean(p.hasLowClouds());
				buffer.writeBoolean(p.hasCloudCover());
				buffer.writeBoolean(p.hasWeather());
				buffer.writeBoolean(p.drawClouds());
				buffer.writeDouble(p.getCloudRotation());
				buffer.writeInt(p.getCloudLevel());
			}
			
			ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "planet_data"), buffer);
		}
	}
	
	/**
	 * Simulate the orbital motion and rotation of each planet. I am aware of how excessive this seems for a Minecraft mod. :l
	 */
	public static void simulateMotion()
	{
		double timeStep = 72.0d * 0.05d;
		
		for(Planet p : planetList)
		{
			p.simulateGravityAcceleration();
			p.simulateVelocityChange(timeStep);
		}
		
		for(Planet p : planetList)
		{
			p.simulatePositionAndRotationChange(timeStep);
		}
	}
}
