package space.item;

import java.util.ArrayList;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.entity.MovingCraftEntity;

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
        
        BlockPos position = player.getBlockPos().down();
        ArrayList<BlockPos> positionList = new ArrayList<BlockPos>();
        MovingCraftEntity.searchForBlocks(world, position, positionList, 4096);
        
        if(positionList.size() >= 4096)
        {
        	System.out.println("Too many blocks detected.");
        	return super.use(world, player, hand);
        }
        
        //RocketEntity entity = new RocketEntity(world, Direction.fromRotation(player.bodyYaw), positionList, PlanetList.getPlanetWorldKey(PlanetList.getByName("moon")), 10.0);
        
        //if(!entity.isRemoved())
        //	world.spawnEntity(entity);
        
        return TypedActionResult.success(player.getStackInHand(hand));
    }
}
