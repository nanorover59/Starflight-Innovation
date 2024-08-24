package space.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CookingRecipeCategory;
import space.block.StarflightBlocks;

public class ElectricFurnaceRecipe extends AbstractCookingRecipe
{
	public ElectricFurnaceRecipe(String group, CookingRecipeCategory category, Ingredient input, ItemStack output, float experience, int cookTime)
	{
		super(StarflightRecipes.ELECTRIC_FURNACE, group, category, input, output, experience, cookTime);
	}

	@Override
	public ItemStack createIcon()
	{
		return new ItemStack(StarflightBlocks.ELECTRIC_FURNACE);
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return StarflightRecipes.ELECTRIC_FURNACE_SERIALIZER;
	}
}