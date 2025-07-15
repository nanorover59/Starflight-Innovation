package space.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.collection.DefaultedList;

public class AdvancedFabricatorRecipeSerializer implements RecipeSerializer<AdvancedFabricatorRecipe>
{
	private static final MapCodec<AdvancedFabricatorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.STRING.optionalFieldOf("prerequisite", "").forGetter(recipe -> recipe.prerequisite), Codec.INT.fieldOf("x").forGetter(recipe -> recipe.x), Codec.INT.fieldOf("y").forGetter(recipe -> recipe.y), Codec.INT.fieldOf("science").forGetter(recipe -> recipe.science), ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.result), ItemStack.VALIDATED_CODEC.listOf().fieldOf("ingredients").flatXmap(ingredients -> {
		ItemStack[] ingredients2 = (ItemStack[]) ingredients.stream().toArray(ItemStack[]::new);
		
		if(ingredients2.length == 0)
			return DataResult.error(() -> "No ingredients for fabrication station recipe");
		else
			return ingredients2.length > 6 ? DataResult.error(() -> "Too many ingredients for fabrication station recipe") : DataResult.success(DefaultedList.copyOf(ItemStack.EMPTY, ingredients2));
		
	}, DataResult::success).forGetter(recipe -> recipe.ingredients)).apply(instance, AdvancedFabricatorRecipe::new));
	public static final PacketCodec<RegistryByteBuf, AdvancedFabricatorRecipe> PACKET_CODEC = PacketCodec.ofStatic(AdvancedFabricatorRecipeSerializer::write, AdvancedFabricatorRecipeSerializer::read);

	@Override
	public MapCodec<AdvancedFabricatorRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public PacketCodec<RegistryByteBuf, AdvancedFabricatorRecipe> packetCodec()
	{
		return PACKET_CODEC;
	}

	private static AdvancedFabricatorRecipe read(RegistryByteBuf buf)
	{
		String prerequisite = buf.readString();
		int x = buf.readInt();
		int y = buf.readInt();
		int science = buf.readInt();
		int i = buf.readVarInt();
		DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(i, ItemStack.EMPTY);
		defaultedList.replaceAll(empty -> ItemStack.PACKET_CODEC.decode(buf));
		ItemStack itemStack = ItemStack.PACKET_CODEC.decode(buf);
		return new AdvancedFabricatorRecipe(prerequisite, x, y, science, itemStack, defaultedList);
	}

	private static void write(RegistryByteBuf buf, AdvancedFabricatorRecipe recipe)
	{
		buf.writeString(recipe.prerequisite);
		buf.writeInt(recipe.x);
		buf.writeInt(recipe.y);
		buf.writeInt(recipe.science);
		buf.writeVarInt(recipe.ingredients.size());

		for(ItemStack ingredient : recipe.ingredients)
			ItemStack.PACKET_CODEC.encode(buf, ingredient);

		ItemStack.PACKET_CODEC.encode(buf, recipe.result);
	}
}