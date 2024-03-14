package space.item;

import java.text.DecimalFormat;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class BatteryCellItem extends Item
{
	private double maxCharge;
	
	public BatteryCellItem(Settings settings, double maxCharge)
	{
		super(settings);
		this.maxCharge = maxCharge;
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(stack.getNbt() != null)
		{
			DecimalFormat df = new DecimalFormat("#.##");
			tooltip.add(Text.translatable("item.space.battery_cell.description").append(df.format(stack.getNbt().getDouble("charge")) + "kJ"));
			//stack.setDamage((int) (getMaxDamage() * (MAX_CHARGE / stack.getNbt().getDouble("charge"))));
		}
		else
		{
			NbtCompound nbt = stack.getOrCreateNbt();
			nbt.putDouble("charge", 0);
		}
	}
	
	public double getMaxCharge()
	{
		return maxCharge;
	}
}