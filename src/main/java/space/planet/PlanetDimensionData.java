package space.planet;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class PlanetDimensionData
{
	private Planet planet;
	private RegistryKey<World> worldKey;
	private boolean isOrbit;
	private boolean isSurface2;
	private boolean overridePhysics;
	private boolean overrideSky;
	private boolean isCloudy;
	private boolean hasLowClouds;
	private boolean hasWeather;
	private boolean hasOxygen;
	private int temperatureCategory;
	private double pressure;
	
	public PlanetDimensionData(Identifier dimensionID, boolean isOrbit, boolean isSurface2, boolean overridePhysics, boolean overrideSky, boolean isCloudy, boolean hasLowClouds, boolean hasWeather, boolean hasOxygen, int temperatureCategory, double pressure)
	{
		this.worldKey = RegistryKey.of(Registry.WORLD_KEY, dimensionID);
		this.isOrbit = isOrbit;
		this.isSurface2 = isSurface2;
		this.overridePhysics = overridePhysics;
		this.overrideSky = overrideSky;
		this.isCloudy = isCloudy;
		this.hasLowClouds = hasLowClouds;
		this.hasOxygen = hasOxygen;
		this.temperatureCategory = temperatureCategory;
		this.pressure = pressure;
	}
	
	public PlanetDimensionData forPlanet(Planet planet)
	{
		this.planet = planet;
		return this;
	}

	public Planet getPlanet()
	{
		return planet;
	}

	public RegistryKey<World> getWorldKey()
	{
		return worldKey;
	}

	public boolean isOrbit()
	{
		return isOrbit;
	}
	
	public boolean isSurface2()
	{
		return isSurface2;
	}

	public boolean overridePhysics()
	{
		return overridePhysics;
	}

	public boolean overrideSky()
	{
		return overrideSky;
	}
	
	public boolean isCloudy()
	{
		return isCloudy;
	}
	
	public boolean hasLowClouds()
	{
		return hasLowClouds;
	}
	
	public boolean hasWeather()
	{
		return hasWeather;
	}

	public boolean hasOxygen()
	{
		return hasOxygen;
	}

	public int getTemperatureCategory()
	{
		return temperatureCategory;
	}

	public double getPressure()
	{
		return pressure;
	}
}