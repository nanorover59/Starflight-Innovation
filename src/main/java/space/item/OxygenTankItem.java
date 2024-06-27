package space.item;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
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
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		if(stack.contains(StarflightItems.OXYGEN))
		{
			DecimalFormat df = new DecimalFormat("#.##");
			tooltip.add(Text.translatable("item.space.oxygen_tank_item.description").append(df.format(stack.get(StarflightItems.OXYGEN)) + "kg"));
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
	{
		ItemStack stack = player.getStackInHand(hand);
		
		if(world.isClient || !stack.contains(StarflightItems.OXYGEN) || stack.get(StarflightItems.OXYGEN) <= 0.0f)
			return TypedActionResult.pass(stack);
		
		float availableOxygen = stack.get(StarflightItems.OXYGEN);
		
		for(ItemStack armorStack : player.getArmorItems())
		{
			if(armorStack.getItem() == StarflightItems.SPACE_SUIT_CHESTPLATE && armorStack.contains(StarflightItems.OXYGEN) && armorStack.contains(StarflightItems.MAX_OXYGEN))
			{
				float previousOxygen = armorStack.get(StarflightItems.OXYGEN);
				float requiredOxygen = armorStack.get(StarflightItems.MAX_OXYGEN) - previousOxygen;
				
				if(requiredOxygen > 0.0f)
				{
					StarflightEffects.sendFizz(world, player.getBlockPos());
					float oxygenToTransfer = Math.min(requiredOxygen, availableOxygen);
					stack.set(StarflightItems.OXYGEN, availableOxygen - oxygenToTransfer);
					armorStack.set(StarflightItems.OXYGEN, availableOxygen + oxygenToTransfer);
					MutableText text = Text.translatable("item.space.space_suit.oxygen_supply");
					int percent = (int) Math.ceil((armorStack.get(StarflightItems.OXYGEN) / armorStack.get(StarflightItems.MAX_OXYGEN)) * 100.0f);
					text.append(percent + "%").formatted(Formatting.BOLD, Formatting.GREEN);
					player.sendMessage(text, false);
					return TypedActionResult.success(stack, true);
				}
			}
		}
		
		return TypedActionResult.pass(stack);
	}
}
