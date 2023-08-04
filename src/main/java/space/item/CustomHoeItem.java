package space.item;

import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;

public class CustomHoeItem extends HoeItem
{
	protected CustomHoeItem(ToolMaterial material, int attackDamage, float attackSpeed, Item.Settings settings)
	{
		super(material, attackDamage, attackSpeed, settings);
	}
}