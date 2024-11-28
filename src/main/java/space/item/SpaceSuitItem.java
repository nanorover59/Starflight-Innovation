package space.item;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public class SpaceSuitItem extends ArmorItem
{
	public SpaceSuitItem(RegistryEntry<ArmorMaterial> armorMaterial, ArmorItem.Type equipmentSlot, Item.Settings settings)
	{
		super(armorMaterial, equipmentSlot, settings.maxCount(1).component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(-1, false)));
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		if(this.getSlotType() == EquipmentSlot.CHEST && stack.contains(StarflightItems.OXYGEN))
		{
			DecimalFormat df = new DecimalFormat("#.#");
			float oxygen = stack.get(StarflightItems.OXYGEN);
			float maxOxygen = stack.get(StarflightItems.MAX_OXYGEN);
			tooltip.add(Text.literal(df.format(oxygen) + " / " + df.format(maxOxygen) + " kg"));
			tooltip.add(Text.literal(df.format((oxygen / maxOxygen) * 100.0f) + "%"));
		}
	}
}