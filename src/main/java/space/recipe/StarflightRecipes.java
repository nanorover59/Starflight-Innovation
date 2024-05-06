package space.recipe;

import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public class StarflightRecipes
{
	public static RecipeType<VacuumFurnaceRecipe> VACUUM_FURNACE;
	public static RecipeSerializer<VacuumFurnaceRecipe> VACUUM_FURNACE_SERIALIZER;

	public static void initializeRecipes()
	{
		VACUUM_FURNACE = Registry.register(Registries.RECIPE_TYPE, new Identifier(StarflightMod.MOD_ID, "vacuum_furnace"), new RecipeType<VacuumFurnaceRecipe>() {
	        @Override
	        public String toString()
	        {
	        	return "vacuum_furnace";
	        }
	    });
		
		VACUUM_FURNACE_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(StarflightMod.MOD_ID, "vacuum_furnace"), new CookingRecipeSerializer<>(VacuumFurnaceRecipe::new, 200));
	}
}