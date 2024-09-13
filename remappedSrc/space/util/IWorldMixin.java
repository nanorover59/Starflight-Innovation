package space.util;

import space.planet.PlanetDimensionData;

public interface IWorldMixin
{
	public PlanetDimensionData getPlanetDimensionData();
	public void clearPlanetDimensionData();
}