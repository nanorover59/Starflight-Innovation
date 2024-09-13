package space.materials;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import space.item.StarflightItems;

public class SpaceSuitArmorMaterial implements ArmorMaterial
{
	private static final int[] BASE_DURABILITY = new int[] {13, 15, 16, 11};
	private static final int[] PROTECTION_VALUES = new int[] {1, 2, 3, 1};

	@Override
	public int getDurability(ArmorItem.Type slot)
	{
		return BASE_DURABILITY[slot.ordinal()] * 6;
	}

	@Override
	public int getProtection(ArmorItem.Type slot)
	{
		return PROTECTION_VALUES[slot.ordinal()];
	}

	@Override
	public int getEnchantability()
	{
		return 15;
	}

	@Override
	public SoundEvent getEquipSound()
	{
		return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
	}

	@Override
	public Ingredient getRepairIngredient()
	{
		return Ingredient.ofItems(StarflightItems.ALUMINUM_INGOT);
	}

	@Override
	public String getName()
	{
		return "space_suit";
	}

	@Override
	public float getToughness()
	{
		return 0.0F;
	}

	@Override
	public float getKnockbackResistance()
	{
		return 0.0F;
	}
}