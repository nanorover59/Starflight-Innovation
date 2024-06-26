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
	public static RecipeType<MetalFabricatorRecipe> METAL_FABRICATOR;
	public static RecipeSerializer<VacuumFurnaceRecipe> VACUUM_FURNACE_SERIALIZER;
	public static RecipeSerializer<MetalFabricatorRecipe> METAL_FABRICATOR_SERIALIZER;

	public static void initializeRecipes()
	{
		VACUUM_FURNACE = Registry.register(Registries.RECIPE_TYPE, Identifier.of(StarflightMod.MOD_ID, "vacuum_furnace"), new RecipeType<VacuumFurnaceRecipe>() {
	        @Override
	        public String toString()
	        {
	        	return "vacuum_furnace";
	        }
	    });
		
		METAL_FABRICATOR = Registry.register(Registries.RECIPE_TYPE, Identifier.of(StarflightMod.MOD_ID, "metal_fabricator"), new RecipeType<MetalFabricatorRecipe>() {
	        @Override
	        public String toString()
	        {
	        	return "metal_fabricator";
	        }
	    });
		
		VACUUM_FURNACE_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(StarflightMod.MOD_ID, "vacuum_furnace"), new CookingRecipeSerializer<>(VacuumFurnaceRecipe::new, 200));
		METAL_FABRICATOR_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(StarflightMod.MOD_ID, "metal_fabricator"), new MetalFabricatorRecipeSerializer<>(MetalFabricatorRecipe::new, 200));
	}
}