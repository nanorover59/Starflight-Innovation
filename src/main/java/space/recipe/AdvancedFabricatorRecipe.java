package space.recipe;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class AdvancedFabricatorRecipe implements Recipe<CraftingRecipeInput>
{
	final String prerequisite;
	final int x;
	final int y;
	final int science;
	final ItemStack result;
	final DefaultedList<ItemStack> ingredients;
	private AdvancedFabricatorRecipe prerequisiteRecipe;

	public AdvancedFabricatorRecipe(String prerequisite, int x, int y, int science, ItemStack result, DefaultedList<ItemStack> ingredients)
	{
		this.prerequisite = prerequisite;
		this.x = x;
		this.y = y;
		this.science = science;
		this.result = result;
		this.ingredients = ingredients;
		this.prerequisiteRecipe = null;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return StarflightRecipes.FABRICATION_STATION_SERIALIZER;
	}
	
	@Override
	public RecipeType<?> getType()
	{
		return StarflightRecipes.ADVANCED_FABRICATOR;
	}
	
	public AdvancedFabricatorRecipe getPrerequisiteRecipe(World world)
	{
		if(prerequisiteRecipe == null)
		{
			RecipeManager recipeManager = world.getRecipeManager();
			List<RecipeEntry<AdvancedFabricatorRecipe>> recipeEntries = recipeManager.listAllOfType(StarflightRecipes.ADVANCED_FABRICATOR);
			
			for(RecipeEntry<AdvancedFabricatorRecipe> recipeEntry : recipeEntries)
			{
				if(recipeEntry.id().toString().equals(prerequisite))
				{
					prerequisiteRecipe = (AdvancedFabricatorRecipe) recipeEntry.value();
					break;
				}
			}
		}
		
		return prerequisiteRecipe;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public int getScience()
	{
		return science;
	}

	@Override
	public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup)
	{
		return this.result;
	}

	@Override
	public DefaultedList<Ingredient> getIngredients()
	{
		DefaultedList<Ingredient> ingredientsList = DefaultedList.of();
		ingredients.stream().forEach(stack -> ingredientsList.add(Ingredient.ofStacks(stack)));
		return ingredientsList;
	}

	@Override
	public boolean matches(CraftingRecipeInput craftingRecipeInput, World world)
	{
		if(craftingRecipeInput.getStackCount() != ingredients.size())
			return false;
		
		int remaining = ingredients.size();
		
		for(ItemStack ingredient : ingredients)
		{
			for(ItemStack stack : craftingRecipeInput.getStacks())
			{
				if(stack.isOf(ingredient.getItem()) && stack.getCount() >= ingredient.getCount())
					remaining--;
				
				if(remaining == 0)
					return true;
			}
		}
		
		return false;
	}

	@Override
	public ItemStack craft(CraftingRecipeInput craftingRecipeInput, WrapperLookup lookup)
	{
		return this.result.copy();
	}

	@Override
	public boolean fits(int width, int height)
	{
		return width * height >= this.ingredients.size();
	}
}