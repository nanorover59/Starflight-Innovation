package space.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CookingRecipeCategory;

public class VacuumFurnaceRecipe extends AbstractCookingRecipe
{
	public VacuumFurnaceRecipe(String group, CookingRecipeCategory category, Ingredient input, ItemStack output, float experience, int cookTime)
	{
		super(StarflightRecipes.VACUUM_FURNACE, group, category, input, output, experience, cookTime);
	}

	@Override
	public ItemStack createIcon()
	{
		return new ItemStack(Items.BLACKSTONE);
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return StarflightRecipes.VACUUM_FURNACE_SERIALIZER;
	}
}