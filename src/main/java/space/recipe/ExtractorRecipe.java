package space.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class ExtractorRecipe implements Recipe<SingleStackRecipeInput>
{
	protected final String group;
	protected final Ingredient ingredient;
	protected final ItemStack result;
	protected final float water;
	protected final float oxygen;
	protected final float hydrogen;
	protected final float experience;
	protected final int cookingTime;

	public ExtractorRecipe(String group, Ingredient ingredient, ItemStack result, float water, float oxygen, float hydrogen, float experience, int cookingTime)
	{
		this.group = group;
		this.ingredient = ingredient;
		this.result = result;
		this.water = water;
		this.oxygen = oxygen;
		this.hydrogen = hydrogen;
		this.experience = experience;
		this.cookingTime = cookingTime;
	}
	
	@Override
	public RecipeType<?> getType()
	{
		return StarflightRecipes.EXTRACTOR;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return StarflightRecipes.EXTRACTOR_SERIALIZER;
	}

	public boolean matches(SingleStackRecipeInput singleStackRecipeInput, World world)
	{
		return this.ingredient.test(singleStackRecipeInput.item());
	}

	public ItemStack craft(SingleStackRecipeInput singleStackRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup)
	{
		return this.result.copy();
	}

	@Override
	public boolean fits(int width, int height)
	{
		return true;
	}

	@Override
	public DefaultedList<Ingredient> getIngredients()
	{
		DefaultedList<Ingredient> defaultedList = DefaultedList.of();
		defaultedList.add(this.ingredient);
		return defaultedList;
	}

	public float getExperience()
	{
		return this.experience;
	}

	@Override
	public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup)
	{
		return this.result;
	}

	@Override
	public String getGroup()
	{
		return this.group;
	}

	public int getCookingTime()
	{
		return this.cookingTime;
	}
	
	public float getWater()
	{
		return this.water;
	}
	
	public float getOxygen()
	{
		return this.oxygen;
	}
	
	public float getHydrogen()
	{
		return this.hydrogen;
	}

	public interface RecipeFactory<T extends ExtractorRecipe>
	{
		T create(String group, Ingredient ingredient, ItemStack result, float water, float oxygen, float hydrogen, float experience, int cookingTime);
	}
}