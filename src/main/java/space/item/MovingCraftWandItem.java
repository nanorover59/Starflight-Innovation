package space.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class MovingCraftWandItem extends Item
{
	public MovingCraftWandItem(Settings settings)
	{
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
	{
        if(world.isClient())
        	return super.use(world, player, hand);
        
        /*BlockPos position = player.getBlockPos().down();
        ArrayList<BlockPos> positionList = new ArrayList<BlockPos>();
        MovingCraftEntity.searchForBlocks(world, position, positionList, 4096);
        
        if(positionList.size() >= 4096)
        {
        	System.out.println("Too many blocks detected.");
        	return super.use(world, player, hand);
        }*/
        
        return TypedActionResult.success(player.getStackInHand(hand));
    }
}