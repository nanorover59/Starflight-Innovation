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
			DecimalFormat df = new DecimalFormat("#.##");
			tooltip.add(Text.translatable("item.space.battery_cell.description").append(df.format(stack.get(StarflightItems.ENERGY)) + "kJ"));
			//stack.setDamage((int) (getMaxDamage() * (MAX_CHARGE / stack.getNbt().getDouble("charge"))));
		}
	}
}