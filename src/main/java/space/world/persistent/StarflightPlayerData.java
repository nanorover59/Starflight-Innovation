package space.world.persistent;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NbtCompound;

public class StarflightPlayerData
{
	private Set<String> unlockedPlanets = new HashSet<String>();
	
	public void unlockPlanet(String name)
	{
		unlockedPlanets.add(name);
	}
	
	public Set<String> getUnlockedPlanetNames()
	{
		return unlockedPlanets;
	}
	
	public NbtCompound writeNbt(NbtCompound nbt)
	{
		String joined = String.join(",", unlockedPlanets);
		nbt.putString("unlockedPlanets", joined);
		return nbt;
	}
	
	public static StarflightPlayerData createFromNbt(NbtCompound nbt)
	{
		StarflightPlayerData data = new StarflightPlayerData();
		String[] names = nbt.getString("unlockedPlanets").split(",");
		
		for(String name : names)
			data.unlockedPlanets.add(name);
		
		return data;
	}
}