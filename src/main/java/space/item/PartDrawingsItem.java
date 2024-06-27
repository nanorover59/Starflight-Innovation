package space.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PartDrawingsItem extends Item
{
	public PartDrawingsItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		ArrayList<Text> textList = new ArrayList<Text>();

		if(stack.contains(StarflightItems.PART_DRAWING_GROUPS))
		{
			String[] names = stack.get(StarflightItems.PART_DRAWING_GROUPS).split(",");

			for(String name : names)
				textList.add(Text.translatable(name).formatted(Formatting.ITALIC));

			tooltip.addAll(textList);
		}
	}
}