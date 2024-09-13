package space.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;

public class ExtractorRecipeSerializer<T extends ExtractorRecipe> implements RecipeSerializer<T>
{
	private final ExtractorRecipe.RecipeFactory<T> recipeFactory;
	private final MapCodec<T> codec;
	private final PacketCodec<RegistryByteBuf, T> packetCodec;

	public ExtractorRecipeSerializer(ExtractorRecipe.RecipeFactory<T> recipeFactory, int cookingTime)
	{
		this.recipeFactory = recipeFactory;
		this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group), Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient), ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.result), Codec.FLOAT.fieldOf("water").orElse(0.0F).forGetter(recipe -> recipe.water), Codec.FLOAT.fieldOf("oxygen").orElse(0.0F).forGetter(recipe -> recipe.oxygen), Codec.FLOAT.fieldOf("hydrogen").orElse(0.0F).forGetter(recipe -> recipe.experience), Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(recipe -> recipe.experience), Codec.INT.fieldOf("cookingtime").orElse(cookingTime).forGetter(recipe -> recipe.cookingTime)).apply(instance, recipeFactory::create));
		this.packetCodec = PacketCodec.ofStatic(this::write, this::read);
	}

	@Override
	public MapCodec<T> codec()
	{
		return this.codec;
	}

	@Override
	public PacketCodec<RegistryByteBuf, T> packetCodec()
	{
		return this.packetCodec;
	}

	private T read(RegistryByteBuf buf)
	{
		String string = buf.readString();
		Ingredient ingredient = Ingredient.PACKET_CODEC.decode(buf);
		ItemStack itemStack = ItemStack.PACKET_CODEC.decode(buf);
		float water = buf.readFloat();
		float oxygen = buf.readFloat();
		float hydrogen = buf.readFloat();
		float experience = buf.readFloat();
		int i = buf.readVarInt();
		return this.recipeFactory.create(string, ingredient, itemStack, water, oxygen, hydrogen, experience, i);
	}

	private void write(RegistryByteBuf buf, T recipe)
	{
		buf.writeString(recipe.group);
		Ingredient.PACKET_CODEC.encode(buf, recipe.ingredient);
		ItemStack.PACKET_CODEC.encode(buf, recipe.result);
		buf.writeFloat(recipe.water);
		buf.writeFloat(recipe.oxygen);
		buf.writeFloat(recipe.hydrogen);
		buf.writeFloat(recipe.experience);
		buf.writeVarInt(recipe.cookingTime);
	}

	public ExtractorRecipe create(String group, Ingredient ingredient, ItemStack result, float water, float oxygen, float hydrogen, float experience, int cookingTime)
	{
		return this.recipeFactory.create(group, ingredient, result, water, oxygen, hydrogen, experience, cookingTime);
	}
}