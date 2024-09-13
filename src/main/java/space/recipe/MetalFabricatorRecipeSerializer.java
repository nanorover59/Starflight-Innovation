package space.recipe;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;

public class MetalFabricatorRecipeSerializer<T extends MetalFabricatorRecipe> implements RecipeSerializer<T>
{
	private final MetalFabricatorRecipe.RecipeFactory<T> recipeFactory;
    private final MapCodec<T> codec;
    private final PacketCodec<RegistryByteBuf, T> packetCodec;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MetalFabricatorRecipeSerializer(MetalFabricatorRecipe.RecipeFactory<T> recipeFactory, int machiningTime)
    {
        this.recipeFactory = recipeFactory;
		this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.STRING.optionalFieldOf("group", "default").forGetter(recipe -> recipe.group), ((MapCodec) CraftingRecipeCategory.CODEC.fieldOf("category")).orElse(CraftingRecipeCategory.MISC).forGetter(recipe -> ((MetalFabricatorRecipe) recipe).category), ((MapCodec) Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient")).forGetter(recipe -> ((MetalFabricatorRecipe) recipe).ingredient), ((MapCodec) ItemStack.VALIDATED_CODEC.fieldOf("result")).forGetter(recipe -> ((MetalFabricatorRecipe) recipe).result), ((MapCodec) Codec.FLOAT.fieldOf("experience")).orElse(Float.valueOf(0.0f)).forGetter(recipe -> Float.valueOf(((MetalFabricatorRecipe) recipe).experience)), ((MapCodec) Codec.INT.fieldOf("machiningTime")).orElse(machiningTime).forGetter(recipe -> ((MetalFabricatorRecipe) recipe).machiningTime)).apply((Applicative) instance, (group, category, ingredient, result, f, i) -> recipeFactory.create((String) group, (CraftingRecipeCategory) category, (Ingredient) ingredient, (ItemStack) result, (float) f, (int) i)));
		this.packetCodec = PacketCodec.ofStatic(this::write, this::read);
    }

	@Override
	public MapCodec<T> codec()
	{
		return codec;
	}
	
	@Override
	public PacketCodec<RegistryByteBuf, T> packetCodec()
	{
		return packetCodec;
	}

	public T read(RegistryByteBuf buffer)
	{
		String group = buffer.readString();
		CraftingRecipeCategory category = buffer.readEnumConstant(CraftingRecipeCategory.class);
		Ingredient ingredient = (Ingredient) Ingredient.PACKET_CODEC.decode(buffer);
        ItemStack itemStack = (ItemStack) ItemStack.PACKET_CODEC.decode(buffer);
		float f = buffer.readFloat();
		int i = buffer.readVarInt();
		return this.recipeFactory.create(group, category, ingredient, itemStack, f, i);
	}

	public void write(RegistryByteBuf buffer, MetalFabricatorRecipe recipe)
	{
		buffer.writeString(recipe.group);
		buffer.writeEnumConstant(recipe.category);
		Ingredient.PACKET_CODEC.encode(buffer, ((MetalFabricatorRecipe) recipe).ingredient);
        ItemStack.PACKET_CODEC.encode(buffer, ((MetalFabricatorRecipe) recipe).result);
        buffer.writeFloat(recipe.experience);
        buffer.writeVarInt(recipe.machiningTime);
	}
	
	public MetalFabricatorRecipe create(String group, CraftingRecipeCategory category, Ingredient ingredient, ItemStack result, float experience, int machiningTime)
	{
        return this.recipeFactory.create(group, category, ingredient, result, experience, machiningTime);
    }
}