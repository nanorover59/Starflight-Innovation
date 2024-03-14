package space.util;

public class FluidResourceType
{
	public static final FluidResourceType ANY = new FluidResourceType(0, 0.0, "any");
	public static final FluidResourceType WATER = new FluidResourceType(1, 1000.0, "water");
	public static final FluidResourceType OXYGEN = new FluidResourceType(2, 1140.0, "oxygen");
	public static final FluidResourceType HYDROGEN = new FluidResourceType(3, 190.0, "hydrogen");
	
	private final int id;
	private final double storageDensity;
	private final String name;
	
	public FluidResourceType(int id, double storageDensity, String name)
	{
		this.id = id;
		this.storageDensity = storageDensity;
		this.name = name;
	}
	
	public int getID()
	{
		return id;
	}
	
	public double getStorageDensity()
	{
		return storageDensity;
	}
	
	public String getName()
	{
		return name;
	}
}