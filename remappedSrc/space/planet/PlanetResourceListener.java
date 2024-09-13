package space.planet;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.gson.stream.JsonReader;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import space.StarflightMod;

public class PlanetResourceListener implements SimpleResourceReloadListener<ArrayList<Planet>>
{
	@Override
	public Identifier getFabricId()
	{
		return new Identifier(StarflightMod.MOD_ID, "planets");
	}

	@Override
	public CompletableFuture<ArrayList<Planet>> load(ResourceManager manager, Profiler profiler, Executor executor)
	{
		return CompletableFuture.supplyAsync(() -> {
			ArrayList<Planet> planetList = new ArrayList<Planet>();
			StarflightMod.LOGGER.info("Loading data pack planets...");
			
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
								double gravity = isOrbit ? 0.0 : ((Planet.G * mass) / (radius * radius) / ((Planet.G * Planet.EARTH_MASS) / (Planet.EARTH_RADIUS * Planet.EARTH_RADIUS)));
								PlanetDimensionData dimensionData = new PlanetDimensionData(identifier, isOrbit, isSky, overridePhysics, overrideSky, isCloudy, hasLowClouds, hasWeather, hasOxygen, temperatureCategory, gravity, pressure);

								if(name1.equals("orbit"))
									orbit = dimensionData;
								else if(name1.equals("surface"))
									surface = dimensionData;
								else if(name1.equals("sky"))
									sky = dimensionData;
							}

							reader.endArray();
						} else
							reader.skipValue();

					}

					reader.endObject();
					reader.close();

					if(!name.equals("null"))
					{
						Planet planet = new Planet(name, parentName, mass, radius, parkingOrbitRadius);
						planet.setOrbitParameters(periapsis, apoapsis, argumentOfPeriapsis, trueAnomaly, ascendingNode, inclination);
						planet.setRotationParameters(isTidallyLocked, obliquity, rotationRate, 0.0);
						planet.setDecorativeParameters(simpleTexture, drawClouds, cloudRotationRate);
						String dimensionNames = "";
						
						if(orbit != null)
						{
							planet.setOrbit(orbit);
							dimensionNames = dimensionNames.concat(" " + orbit.getWorldKey().getValue().toString());
						}

						if(surface != null)
						{
							planet.setSurface(surface);
							dimensionNames = dimensionNames.concat(" " + surface.getWorldKey().getValue().toString());
						}

						if(sky != null)
						{
							planet.setSky(sky);
							dimensionNames = dimensionNames.concat(" " + sky.getWorldKey().getValue().toString());
						}

						planetList.add(planet);
						StarflightMod.LOGGER.info(planet.getName() + " [" + dimensionNames + " ]");
					}
				} catch(Exception e)
				{
					e.printStackTrace();
				}
			}
            
			for(Planet p : planetList)
				p.linkSatellites(planetList);
			
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
			
            return planetList;
        }, executor);
	}

	@Override
	public CompletableFuture<Void> apply(ArrayList<Planet> data, ResourceManager manager, Profiler profiler, Executor executor)
	{
		return CompletableFuture.runAsync(() -> {
			PlanetList.loadPlanets(data);
		}, executor);
	}
}