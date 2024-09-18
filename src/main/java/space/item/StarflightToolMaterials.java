package space.item;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public enum StarflightToolMaterials implements ToolMaterial
{
	TITANIUM(BlockTags.INCORRECT_FOR_IRON_TOOL, 2000, 10.0F, 2.0F, 10, () -> Ingredient.ofItems(StarflightItems.TITANIUM_INGOT)),
	HEMATITE(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 500, 8.0F, 4.0F, 15, () -> Ingredient.ofItems(StarflightItems.HEMATITE));
	
	private final TagKey<Block> inverseTag;
	private final int itemDurability;
	private final float miningSpeed;
	private final float attackDamage;
	private final int enchantability;
	private final Supplier<Ingredient> repairIngredient;

	private StarflightToolMaterials(final TagKey<Block> inverseTag, final int itemDurability, final float miningSpeed, final float attackDamage, final int enchantability, final Supplier<Ingredient> repairIngredient)
	{
		this.inverseTag = inverseTag;
		this.itemDurability = itemDurability;
		this.miningSpeed = miningSpeed;
		this.attackDamage = attackDamage;
		this.enchantability = enchantability;
		this.repairIngredient = Suppliers.memoize(repairIngredient::get);
	}

	@Override
	public int getDurability()
	{
		return this.itemDurability;
	}

	@Override
	public float getMiningSpeedMultiplier()
	{
		return this.miningSpeed;
	}

	@Override
	public float getAttackDamage()
	{
		return this.attackDamage;
	}

	@Override
	public TagKey<Block> getInverseTag()
	{
		return this.inverseTag;
	}

	@Override
	public int getEnchantability()
	{
		return this.enchantability;
	}

	@Override
	public Ingredient getRepairIngredient()
	{
		return this.repairIngredient.get();
	}
}