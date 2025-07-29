package space.item;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class EnergyStorageItem extends Item
{
	public EnergyStorageItem(int energyCapacity, Settings settings)
	{
		super(settings.maxCount(1).component(StarflightItems.ENERGY, 0).component(StarflightItems.MAX_ENERGY, energyCapacity));
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type)
	{
		int energy = stack.get(StarflightItems.ENERGY);
		int capacity = stack.get(StarflightItems.MAX_ENERGY);
		int percent = (int) (((double) energy / (double) capacity) * 100.0);
		tooltip.add(Text.translatable("block.space.energy", energy, capacity));
		tooltip.add(Text.literal(percent + "%"));
	}
	
	@Override
	public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack)
	{
		return false;
	}
	
	public static int addEnergy(ItemStack stack, int amount, boolean change)
	{
		int energy = stack.get(StarflightItems.ENERGY);
		int newEnergy = energy + amount;
		energy = MathHelper.clamp(newEnergy, 0, stack.get(StarflightItems.MAX_ENERGY));

		if(change)
			stack.set(StarflightItems.ENERGY, energy);

		return amount - (newEnergy - energy);
	}
	
	public static int removeEnergy(ItemStack stack, int amount, boolean change)
	{
		return -addEnergy(stack, -amount, change);
	}
}