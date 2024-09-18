package space.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.ColoredFallingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ColorCode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.entity.AlienMobEntity;
import space.item.StarflightItems;

public class HotRegolith extends ColoredFallingBlock
{
	public HotRegolith(ColorCode color, Settings settings)
	{
		super(color, settings);
	}
	
	@Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity)
	{
		if(entity instanceof LivingEntity)
		{
			boolean isTemperatureSafe = entity instanceof AlienMobEntity && ((AlienMobEntity) entity).isTemperatureSafe(3);
			boolean hasBoots = false;
			
			for(ItemStack stack : ((LivingEntity) entity).getArmorItems())
			{
				if(stack.getItem() == StarflightItems.THERMAL_BOOTS)
				{
					hasBoots = true;
					break;
				}
			}
			
			if(!isTemperatureSafe && !hasBoots)
	            entity.damage(world.getDamageSources().hotFloor(), 0.5f);
		}
		
        super.onSteppedOn(world, pos, state, entity);
    }
}