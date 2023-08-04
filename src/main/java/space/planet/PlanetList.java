package space.planet;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import com.google.gson.stream.JsonReader;

import net.darkhax.ess.DataCompound;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import space.StarflightMod;
import space.util.IWorldMixin;

public class PlanetList
{
	private static HashMap<String, ArrayList<String>> unlocked = new HashMap<String, ArrayList<String>>();
	private static ArrayList<Planet> planetList = new ArrayList<Planet>();
	private static int timeSteps = 1;
	
	/**
	 * Register all planets.
	 */
	public static void initialize(MinecraftServer server)
	{
		planetList.clear();
		
		for(World world : server.getWorlds())
			((IWorldMixin) (Object) world).clearPlanetDimensionData();
		
		ResourceManager manager = server.getResourceManager();
		
		for(Identifier id : manager.findResources("planets", path -> path.getPath().endsWith(".json")).keySet())
		{
			try(InputStream stream = manager.getResource(id).get().getInputStream())
			{
				JsonReader reader = new JsonReader(new InputStreamReader(stream));
				String name = "null";
				String parentName = "null";
				double mass = 0.0;
				double radius = 0.0;
				double parkingOrbitRadius = 0.0;
				double surfacePressure = 0.0;
				double periapsis = 0.0;
				double apoapsis = 0.0;
				double argumentOfPeriapsis = 0.0;
				double trueAnomaly = 0.0;
				double ascendingNode = 0.0;
				double inclination = 0.0;
				boolean isTidallyLocked = false;
				double obliquity = 0.0;
				double rotationRate = 0.0;
				boolean simpleTexture = false;
				boolean drawClouds = false;
				double cloudRotationRate = 0.0;
				PlanetDimensionData orbit = null;
				PlanetDimensionData surface = null;
				PlanetDimensionData sky = null;
				reader.beginObject();

				while(reader.hasNext())
				{
					String tagName = reader.nextName();

					if(tagName.equals("name"))
						name = reader.nextString();
					else if(tagName.equals("parentName"))
						parentName = reader.nextString();
					else if(tagName.equals("mass"))
						mass = reader.nextDouble();
					else if(tagName.equals("radius"))
						radius = reader.nextDouble();
					else if(tagName.equals("parkingOrbitRadius"))
						parkingOrbitRadius = reader.nextDouble();
					else if(tagName.equals("periapsis"))
						periapsis = reader.nextDouble();
					else if(tagName.equals("apoapsis"))
						apoapsis = reader.nextDouble();
					else if(tagName.equals("argumentOfPeriapsis"))
						argumentOfPeriapsis = reader.nextDouble();
					else if(tagName.equals("trueAnomaly"))
						trueAnomaly = reader.nextDouble();
					else if(tagName.equals("ascendingNode"))
						ascendingNode = reader.nextDouble();
					else if(tagName.equals("inclination"))
						inclination = reader.nextDouble();
					else if(tagName.equals("isTidallyLocked"))
						isTidallyLocked = reader.nextBoolean();
					else if(tagName.equals("obliquity"))
						obliquity = reader.nextDouble();
					else if(tagName.equals("rotationRate"))
						rotationRate = reader.nextDouble();
					else if(tagName.equals("simpleTexture"))
						simpleTexture = reader.nextBoolean();
					else if(tagName.equals("drawClouds"))
						drawClouds = reader.nextBoolean();
					else if(tagName.equals("cloudRotationRate"))
						cloudRotationRate = reader.nextDouble();
					else if(tagName.equals("dimensionData"))
					{
						reader.beginArray();

						while(reader.hasNext())
						{
							String name1 = "null";
							String dimensionID = "null";
							boolean overridePhysics = false;
							boolean overrideSky = false;
							boolean isCloudy = false;
							boolean hasLowClouds = false;
							boolean hasWeather = false;
							boolean hasOxygen = false;
							int temperatureCategory = 2;
							double pressure = 0.0;
							reader.beginObject();

							while(reader.hasNext())
							{
								String tagName1 = reader.nextName();

								if(tagName1.equals("name"))
									name1 = reader.nextString();
								else if(tagName1.equals("dimensionID"))
									dimensionID = reader.nextString();
								else if(tagName1.equals("overridePhysics"))
									overridePhysics = reader.nextBoolean();
								else if(tagName1.equals("overrideSky"))
									overrideSky = reader.nextBoolean();
								else if(tagName1.equals("isCloudy"))
									isCloudy = reader.nextBoolean();
								else if(tagName1.equals("hasLowClouds"))
									hasLowClouds = reader.nextBoolean();
								else if(tagName1.equals("hasWeather"))
									hasWeather = reader.nextBoolean();
								else if(tagName1.equals("hasOxygen"))
									hasOxygen = reader.nextBoolean();
								else if(tagName1.equals("temperatureCategory"))
									temperatureCategory = reader.nextInt();
								else if(tagName1.equals("pressure"))
									pressure = reader.nextDouble();
								else
									reader.skipValue();
							}

							reader.endObject();
							Identifier identifier = new Identifier(dimensionID);
							boolean isOrbit = name1.equals("orbit");
							boolean isSky = name1.equals("sky");
							PlanetDimensionData dimensionData = new PlanetDimensionData(identifier, isOrbit, isSky, overridePhysics, overrideSky, isCloudy, hasLowClouds, hasWeather, hasOxygen, temperatureCategory, pressure);
							
							if(name1.equals("orbit"))
								orbit = dimensionData;
							else if(name1.equals("surface"))
							{
								surface = dimensionData;
								surfacePressure = pressure;
							}
							else if(name1.equals("sky"))
								sky = dimensionData;
						}

						reader.endArray();
					}
					else
						reader.skipValue();

				}

				reader.endObject();
				reader.close();

				if(!name.equals("null"))
				{
					Planet planet = new Planet(name, parentName, mass, radius, parkingOrbitRadius, surfacePressure);
					planet.setOrbitParameters(periapsis, apoapsis, argumentOfPeriapsis, trueAnomaly, ascendingNode, inclination);
					planet.setRotationParameters(isTidallyLocked, obliquity, rotationRate, 0.0);
					planet.setDecorativeParameters(simpleTexture, drawClouds, cloudRotationRate);

					if(orbit != null)
						planet.setOrbit(orbit);

					if(surface != null)
						planet.setSurface(surface);

					if(sky != null)
						planet.setSky(sky);

					planetList.add(planet);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		linkSatellites();
		timeSteps = 1;
	}
	
	/**
	 * Find and set the parent body of each planet from the string name provided then assign satellite level indicators.
	 */
	private static void linkSatellites()
	{
		for(Planet p : planetList)
			p.linkSatellites();
		
		for(Planet p1 : planetList)
		{
			int level = 0;
			Planet p2 = p1.getParent();
			
			while(p2 != null)
			{
				level++;
				p2 = p2.getParent();
			}
			
			p1.setSatelliteLevel(level);
		}
	}
	
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
	
	/**
	 * Unlock a planet in the planetarium for a player.
	 */
	public static void unlock(UUID playerUUID, String planetName)
	{
		String uuid = playerUUID.toString();
		ArrayList<String> list = new ArrayList<String>();
		
		if(unlocked.containsKey(uuid))
			list = unlocked.get(uuid);
		
		list.add(planetName);
		unlocked.put(uuid, list);
	}
	
	/**
	 * Save all world specific planet data.
	 */
	public static DataCompound saveData()
	{
		DataCompound data = new DataCompound();
		
		for(Planet p : planetList)
			data = p.saveData(data);
		
		data.setValue("planetCount", planetList.size());
		data.setValue("timeSteps", timeSteps);
		DataCompound unlockedData = new DataCompound();
		
		for(String uuid : unlocked.keySet())
			unlockedData.setValue(uuid, unlocked.get(uuid).toArray());
		
		data.setValue("unlocked", unlockedData);
		return data;
	}
	
	/**
	 * Load all world specific planet data.
	 */
	public static void loadData(DataCompound data)
	{
		Planet centerPlanet = getByName("sol");
		ArrayList<String> checkList = new ArrayList<String>();
		unlocked.clear();
		
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
		
		if(data != null && data.hasName("unlocked"))
		{
			DataCompound unlockedData = data.getDataCompound("unlocked");
			
			if(unlockedData != null)
			{
				for(String uuid : unlockedData.getNames())
					unlocked.put(uuid, new ArrayList<String>(Arrays.asList(unlockedData.getStringArray(uuid))));
			}
		}
	}
	
	/**
	 * Send planet rendering information to clients.
	 */
	public static void sendToClients(MinecraftServer server)
	{
		for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
		{
			PacketByteBuf buffer = PacketByteBufs.create();
			PlanetDimensionData data = getDimensionDataForWorld(player.world);
			ArrayList<String> unlockedPlanets = unlocked.get(player.getUuidAsString());
			
			if(data == null)
			{
				buffer.writeInt(0);
				buffer.writeInt(-1);
				buffer.writeBoolean(false);
				ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "planet_data"), buffer);
				continue;
			}
			
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
			
			for(int i = 0; i < planetList.size(); i++)
			{
				Planet p = planetList.get(i);
				buffer.writeDouble(p.getPosition().getX());
				buffer.writeDouble(p.getPosition().getY());
				buffer.writeDouble(p.getPosition().getZ());
				buffer.writeDouble(p.getSurfaceViewpoint().getX());
				buffer.writeDouble(p.getSurfaceViewpoint().getY());
				buffer.writeDouble(p.getSurfaceViewpoint().getZ());
				buffer.writeDouble(p.getParkingOrbitViewpoint().getX());
				buffer.writeDouble(p.getParkingOrbitViewpoint().getY());
				buffer.writeDouble(p.getParkingOrbitViewpoint().getZ());
				buffer.writeDouble(p.getPrecession());
				buffer.writeDouble(p.getCloudRotation());
				buffer.writeInt(p.getCloudLevel());
				buffer.writeBoolean(unlockedPlanets.contains(p.getName()));
			}
			
			ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "planet_data"), buffer);
		}
	}
	
	/**
	 * Simulate the orbital motion and rotation of each planet. I am aware of how excessive this seems for a Minecraft mod. :l
	 */
	public static void simulateMotion()
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
	public static void skipToMorning(Planet planet)
	{
		double angle = 0.0;
		
		while(angle < 1.75)
		{
			Vec3d position = planet.getPosition();
			Vec3d viewpoint = PlanetRenderList.isViewpointInOrbit() ? planet.getParkingOrbitViewpoint().subtract(position) : planet.getSurfaceViewpoint().subtract(position);
			Vec3d starPosition = new Vec3d(0.0, 0.0, 0.0).subtract(position);
			angle = Math.acos(position.dotProduct(viewpoint) / (viewpoint.length() * starPosition.length()));
			simulateMotion();
		}
	}
}