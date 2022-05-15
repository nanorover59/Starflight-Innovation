package space.item;

import java.text.DecimalFormat;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import space.util.StarflightEffects;

public class OxygenTankItem extends Item
{
	public OxygenTankItem(Settings settings)
	{
		super(settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(stack.getNbt() != null)
		{
			DecimalFormat df = new DecimalFormat("#.##");
			tooltip.add(new TranslatableText("item.space.oxygen_tank_item.description").append(df.format(stack.getNbt().getDouble("oxygen")) + "kg"));
		}
		else
		{
			NbtCompound nbt = stack.getOrCreateNbt();
			nbt.putDouble("oxygen", 0);
		}
	}

	public double getMaxOxygen()
	{
		return 2.0;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
	{
		ItemStack itemStack = player.getStackInHand(hand);
		
		if(world.isClient || itemStack.getNbt() == null || itemStack.getNbt().getDouble("oxygen") <= 0)
			return TypedActionResult.pass(itemStack);
		
		double availableOxygen = itemStack.getNbt().getDouble("oxygen");
		
		for(ItemStack armorStack : player.getArmorItems())
		{
			if(armorStack.getItem() == StarflightItems.SPACE_SUIT_CHESTPLATE && armorStack.getNbt() != null)
			{
				double previousOxygen = armorStack.getNbt().getDouble("oxygen");
				double requiredOxygen = ((SpaceSuitItem) armorStack.getItem()).getMaxOxygen() - previousOxygen;
				
				if(requiredOxygen > 0.0)
				{
					StarflightEffects.sendFizz(world, player.getBlockPos());
					double oxygenToTransfer = Math.min(requiredOxygen, availableOxygen);
					itemStack.getNbt().putDouble("oxygen", availableOxygen - oxygenToTransfer);
					armorStack.getNbt().putDouble("oxygen", previousOxygen + oxygenToTransfer);
					TranslatableText text = new TranslatableText("item.space.space_suit.oxygen_supply");
					int percent = (int) Math.ceil((armorStack.getNbt().getDouble("oxygen") / ((SpaceSuitItem) armorStack.getItem()).getMaxOxygen()) * 100.0);
					text.append(percent + "%").formatted(Formatting.BOLD, Formatting.GREEN);
					player.sendMessage(text, false);
					return TypedActionResult.success(itemStack, true);
				}
			}
		}
		
		return TypedActionResult.pass(itemStack);
	}
}
