package space.planet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PlanetDimensionData
{
	private Planet planet;
	private RegistryKey<World> worldKey;
	private boolean isOrbit;
	private boolean isSky;
	private boolean overridePhysics;
	private boolean overrideSky;
	private boolean isCloudy;
	private boolean hasLowClouds;
	private boolean hasWeather;
	private boolean hasOxygen;
	private int temperatureCategory;
	private double gravity;
	private double pressure;
	
	public PlanetDimensionData(Identifier dimensionID, boolean isOrbit, boolean isSky, boolean overridePhysics, boolean overrideSky, boolean isCloudy, boolean hasLowClouds, boolean hasWeather, boolean hasOxygen, int temperatureCategory, double gravity, double pressure)
	{
		this.worldKey = RegistryKey.of(RegistryKeys.WORLD, dimensionID);
		this.isOrbit = isOrbit;
		this.isSky = isSky;
		this.overridePhysics = overridePhysics;
		this.overrideSky = overrideSky;
		this.isCloudy = isCloudy;
		this.hasLowClouds = hasLowClouds;
		this.hasWeather = hasWeather;
		this.hasOxygen = hasOxygen;
		this.temperatureCategory = temperatureCategory;
		this.gravity = gravity;
		this.pressure = pressure;
	}
	
	public PlanetDimensionData(PacketByteBuf buffer)
	{
		this.worldKey = buffer.readRegistryKey(RegistryKeys.WORLD);
		this.isOrbit = buffer.readBoolean();
		this.isSky = buffer.readBoolean();
		this.overridePhysics = buffer.readBoolean();
		this.overrideSky = buffer.readBoolean();
		this.isCloudy = buffer.readBoolean();
		this.hasLowClouds = buffer.readBoolean();
		this.hasWeather = buffer.readBoolean();
		this.hasOxygen = buffer.readBoolean();
		this.temperatureCategory = buffer.readInt();
		this.gravity = buffer.readDouble();
		this.pressure = buffer.readDouble();
	}
	
	public static void write(PacketByteBuf buffer, PlanetDimensionData data)
	{
		buffer.writeRegistryKey(data.worldKey);
		buffer.writeBoolean(data.isOrbit);
		buffer.writeBoolean(data.isSky);
		buffer.writeBoolean(data.overridePhysics);
		buffer.writeBoolean(data.overrideSky);
		buffer.writeBoolean(data.isCloudy);
		buffer.writeBoolean(data.hasLowClouds);
		buffer.writeBoolean(data.hasWeather);
		buffer.writeBoolean(data.hasOxygen);
		buffer.writeInt(data.temperatureCategory);
		buffer.writeDouble(data.gravity);
		buffer.writeDouble(data.pressure);
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
	
	public boolean isSky()
	{
		return isSky;
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

	public int getTemperatureCategory(float skyAngle)
	{
		if(temperatureCategory >= Planet.TEMPERATE && (isOrbit || pressure < 0.01))
			return skyAngle < 0.25f || skyAngle > 0.75f ? Planet.HOT : Planet.COLD;
			
		return temperatureCategory;
	}

	public double getGravity()
	{
		return gravity;
	}
	
	public double getPressure()
	{
		return pressure;
	}
}