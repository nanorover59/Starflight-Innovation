package space.item;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public class BatteryCellItem extends Item
{
	public BatteryCellItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type)
	{
		if(stack.contains(StarflightItems.ENERGY))
		{
			DecimalFormat df = new DecimalFormat("#.#");
			float energy = stack.get(StarflightItems.ENERGY);
			float maxEnergy = stack.get(StarflightItems.MAX_ENERGY);
			tooltip.add(Text.literal(df.format(energy) + " / " + df.format(maxEnergy) + " kJ"));
			tooltip.add(Text.literal(df.format((energy / maxEnergy) * 100.0f) + "%"));
		}
	}
}