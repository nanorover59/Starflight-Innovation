package space.item;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.StarflightBlocks;
import space.planet.Planet;
import space.planet.PlanetList;

public class PlanetariumCardItem extends Item
{
	public PlanetariumCardItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		if(stack.contains(StarflightItems.PLANET_NAME))
			tooltip.add(Text.translatable("planet.space." + stack.get(StarflightItems.PLANET_NAME)).withColor(stack.get(StarflightItems.PRIMARY_COLOR)));
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		ItemStack stack = context.getStack();
		World world = context.getWorld();
        BlockPos position = context.getBlockPos();
        BlockState blockState = world.getBlockState(position);
        
        if(!world.isClient() && stack.contains(StarflightItems.PLANET_NAME) && blockState.isOf(StarflightBlocks.PLANETARIUM))
        {
        	String planetName = stack.get(StarflightItems.PLANET_NAME);
        	Planet planet = PlanetList.get().getByName(planetName);
        	
        	/*if(planet != null)
        	{
        		StarflightPlayerState playerState = StarflightPlayerState.get(world.getServer());
        		
        		if(!playerState.isPlanetUnlocked((ServerPlayerEntity) context.getPlayer(), planetName))
        		{
	        		playerState.unlockPlanet((ServerPlayerEntity) context.getPlayer(), planetName);
	        		
	        		if(planet.getParent() != null)
	        			StarflightPlayerState.get(world.getServer()).unlockPlanet((ServerPlayerEntity) context.getPlayer(), planet.getParent().getName());
	        		
	        		for(Planet satellite : planet.getSatellites())
	        			StarflightPlayerState.get(world.getServer()).unlockPlanet((ServerPlayerEntity) context.getPlayer(), satellite.getName());
	        		
	        		SyncPlayerStateS2CPacket.sendUnlockPlanet(world, planetName, stack.get(StarflightItems.PRIMARY_COLOR));
        		}
        	}*/
        }
		
		return ActionResult.success(world.isClient);
	}
}