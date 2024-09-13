package space.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class GuideBookItem extends Item
{
	public GuideBookItem(Settings settings)
	{
		super(settings);
	}

	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
        ItemStack itemStack = user.getStackInHand(hand);
        
        /*if(world.isClient)
        {
        	MinecraftClient minecraft = MinecraftClient.getInstance();
        	minecraft.setScreen(new GuideBookScreen());
        }*/
        
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.success(itemStack, world.isClient());
    }
}