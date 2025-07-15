package space.world.persistent;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NbtCompound;

public class StarflightPlayerData
{
	public static StarflightPlayerData clientPlayerData;
	public Set<String> unlockedRecipes = new HashSet<String>();
	public int science = 0;
	
	
	public NbtCompound writeNbt(NbtCompound nbt)
	{
		nbt.putInt("science", science);
		
		if(unlockedRecipes != null && !unlockedRecipes.isEmpty())
		{
			String recipes = String.join(",", unlockedRecipes);
			nbt.putString("unlockedRecipes", recipes);
		}
	
		return nbt;
	}
	
	public static StarflightPlayerData createFromNbt(NbtCompound nbt)
	{
		StarflightPlayerData data = new StarflightPlayerData();
		data.science = nbt.getInt("science");
		
		if(nbt.contains("unlockedRecipes"))
		{
			String[] recipes = nbt.getString("unlockedRecipes").split(",");
			
			for(String recipe : recipes)
				data.unlockedRecipes.add(recipe);
		}
		
		return data;
	}
}