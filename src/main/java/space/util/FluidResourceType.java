package space.util;

public class FluidResourceType
{
	public static final FluidResourceType WATER = new FluidResourceType("water", 1000);
	public static final FluidResourceType OXYGEN = new FluidResourceType("oxygen", 1200);
	public static final FluidResourceType HYDROGEN = new FluidResourceType("hydrogen", 150);
	public static final FluidResourceType[] ALL = {WATER, OXYGEN, HYDROGEN};
	private final String name;
	private final long storageDensity;
	
	public FluidResourceType(String name, long storageDensity)
	{
		this.name = name;
		this.storageDensity = storageDensity;
	}
	
	public long getStorageDensity()
	{
		return storageDensity;
	}
	
	public String getName()
	{
		return name;
	}
	
	public static FluidResourceType getForName(String name)
	{
		for(FluidResourceType fluidType : ALL)
		{
			if(fluidType.getName().equals(name))
				return fluidType;
		}
		
		return null;
	}
}