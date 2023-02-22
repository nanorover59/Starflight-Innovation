package space.item;

import java.text.DecimalFormat;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class SpaceSuitItem extends DyeableArmorItem
{
	public static final double MAX_OXYGEN = 4.0;
	
	public SpaceSuitItem(ArmorMaterial armorMaterial, EquipmentSlot equipmentSlot, Item.Settings settings)
	{
		super(armorMaterial, equipmentSlot, settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(this.getSlotType() == EquipmentSlot.CHEST)
		{
			if(stack.getNbt() != null && stack.getNbt().contains("oxygen"))
			{
				DecimalFormat df = new DecimalFormat("#.##");
				tooltip.add(Text.translatable("item.space.oxygen_tank_item.description").append(df.format(stack.getNbt().getDouble("oxygen")) + "kg"));
			}
			else
			{
				NbtCompound nbt = stack.getOrCreateNbt();
				nbt.putDouble("oxygen", 0);
			}
		}
	}
	
	@Override
	public int getColor(ItemStack stack)
	{
	      NbtCompound nbtCompound = stack.getSubNbt("display");
	      return nbtCompound != null && nbtCompound.contains("color", 99) ? nbtCompound.getInt("color") : 0xFFFFFFF;
	}
}