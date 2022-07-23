package space.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.client.StarflightModClient;

public class ArrivalCardItem extends Item
{
	public ArrivalCardItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(stack.getNbt() != null)
		{
			int x = stack.getNbt().getInt("x");
			int z = stack.getNbt().getInt("z");
			int d = stack.getNbt().getInt("d");
			tooltip.add(Text.translatable("").append("X: " + x + "    Z: " + z).formatted(Formatting.ITALIC));
			tooltip.add(Text.translatable("").append("Facing: " + Direction.fromHorizontal(d).asString().toUpperCase()).formatted(Formatting.ITALIC));
		}
		else
			StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("item.space.arrival_card.description"));
	}
	
	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
	{
		ItemStack itemStack = player.getStackInHand(hand);
		NbtCompound nbt = itemStack.getOrCreateNbt();
		nbt.putInt("x", player.getBlockX());
		nbt.putInt("z", player.getBlockZ());
		nbt.putInt("d", Direction.fromRotation(player.headYaw).getHorizontal());
		MutableText text = Text.translatable("item.space.arrival_card.use");
		player.sendMessage(text, true);
		return TypedActionResult.pass(itemStack);
	}
}