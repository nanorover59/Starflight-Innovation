package space.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import space.client.StarflightModClient;

public class NavigationCardItem extends Item
{
	public NavigationCardItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(stack.getNbt() != null && stack.getNbt().contains("planet"))
		{
			String planetName = stack.getNbt().getString("planet");
			MutableText text = Text.translatable("planet.space." + planetName).formatted(Formatting.ITALIC);
			tooltip.add(text);
		}
		else
			StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("item.space.navigation_card.description"));
	}
}