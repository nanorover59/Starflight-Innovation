package space.item;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class TargetingCardItem extends Item
{
	public TargetingCardItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		if(stack.contains(StarflightItems.PLANET_NAME))
		{
			if(stack.get(StarflightItems.PLANET_NAME).isEmpty())
				tooltip.add(Text.translatable("item.space.targeting_card.unset"));
			else
			{
				if(stack.get(StarflightItems.PLANET_NAME).endsWith("_orbit"))
					tooltip.add(Text.translatable("planet.space." + stack.get(StarflightItems.PLANET_NAME).split("_orbit")[0]).append(Text.translatable("planet.space.orbit")));
				else
					tooltip.add(Text.translatable("planet.space." + stack.get(StarflightItems.PLANET_NAME)));
					
				tooltip.add(Text.literal(stack.get(StarflightItems.POSITION).toShortString()));
				tooltip.add(Text.literal(stack.get(StarflightItems.DIRECTION).toString()));
			}
		}
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		ItemStack stack = context.getStack();
		World world = context.getWorld();
        BlockPos position = context.getBlockPos().offset(context.getSide());
        Direction direction = context.getHorizontalPlayerFacing();
        
        if(context.getPlayer().isSneaking())
        {
        	if(world.isClient())
        	{
        		for(int i = 0; i < 6; i++)
            	{
            		double dx = (world.getRandom().nextDouble() - world.getRandom().nextDouble()) * 0.5 + 0.5;
            		double dy = (world.getRandom().nextDouble() - world.getRandom().nextDouble()) * 0.5 + 0.5;
            		double dz = (world.getRandom().nextDouble() - world.getRandom().nextDouble()) * 0.5 + 0.5;
            		world.addParticle(ParticleTypes.ELECTRIC_SPARK, (double) position.getX() + dx, (double) position.getY() + dy, (double) position.getZ() + dz, 0.0, 0.0, 0.0);
            	}
        		
            	context.getPlayer().sendMessage(Text.translatable("item.space.targeting_card.set"), false);
        	}
        	else
        	{
        		stack.set(StarflightItems.PLANET_NAME, world.getRegistryKey().getValue().getPath());
            	stack.set(StarflightItems.POSITION, position);
            	stack.set(StarflightItems.DIRECTION, direction);
        	}
        	
        	return ActionResult.success(world.isClient);
        }
        
		return ActionResult.PASS;
	}
}