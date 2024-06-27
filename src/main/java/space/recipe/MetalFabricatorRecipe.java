package space.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import space.block.StarflightBlocks;

public class MetalFabricatorRecipe implements Recipe<SingleStackRecipeInput>
{
	protected final String group;
	CraftingRecipeCategory category;
	protected final Ingredient ingredient;
    protected final ItemStack result;
    protected final float experience;
    protected final int machiningTime;
	
	public MetalFabricatorRecipe(String group, CraftingRecipeCategory category, Ingredient ingredient, ItemStack result, float experience, int machiningTime)
	{
		this.group = group;
		this.category = category;
		this.ingredient = ingredient;
        this.result = result;
        this.experience = experience;
        this.machiningTime = machiningTime;
	}
	
	@Override
	public RecipeType<?> getType()
	{
		return StarflightRecipes.METAL_FABRICATOR;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return StarflightRecipes.METAL_FABRICATOR_SERIALIZER;
	}

	@Override
	public ItemStack createIcon()
	{
		return new ItemStack(StarflightBlocks.METAL_FABRICATOR);
	}
	
	@Override
	public String getGroup()
	{
		return group;
	}

	@Override
	public boolean matches(SingleStackRecipeInput input, World world)
	{
		return ingredient.test(input.item());
	}

	@Override
	public ItemStack craft(SingleStackRecipeInput input, RegistryWrapper.WrapperLookup wrapperLookup)
	{
		return result.copy();
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

	@Override
	public ItemStack getResult(RegistryWrapper.WrapperLookup wrapperLookup)
	{
		return result;
	}
	
	public int getMachiningTime()
	{
		return machiningTime;
	}
	
	public static interface RecipeFactory<T extends MetalFabricatorRecipe>
	{
        public T create(String group, CraftingRecipeCategory category, Ingredient ingredient, ItemStack result, float experience, int machiningTime);
    }
}