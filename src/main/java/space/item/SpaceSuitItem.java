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
			DecimalFormat df = new DecimalFormat("#.##");
			tooltip.add(Text.translatable("item.space.oxygen_tank_item.description").append(df.format(stack.get(StarflightItems.OXYGEN)) + "kg"));
		}
	}
}