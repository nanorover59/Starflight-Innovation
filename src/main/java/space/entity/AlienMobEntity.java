package space.entity;

public interface AlienMobEntity
{
	boolean isPressureSafe(double pressure);
	
	boolean isTemperatureSafe(int temperature);
	
	boolean requiresOxygen();
	
	default boolean oxygenCompatible()
	{
		return true;
	}
	
	default int getRadiationRange()
	{
		return 0;
	}
	
	default float getRadiationStrength()
	{
		return 0.0f;
	}
}