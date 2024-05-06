package space.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.StarflightBlocks;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.StarflightEffects;
import space.world.persistent.StarflightPlayerState;

public class PlanetariumCardItem extends Item
{
	public PlanetariumCardItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(stack.hasNbt())
			tooltip.add(Text.translatable("planet.space." + stack.getNbt().getString("name")).withColor(stack.getNbt().getInt("primaryColor")));
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		ItemStack stack = context.getStack();
		World world = context.getWorld();
        BlockPos position = context.getBlockPos();
        BlockState blockState = world.getBlockState(position);
        
        if(stack.hasNbt() && stack.getNbt().contains("name") && blockState.isOf(StarflightBlocks.PLANETARIUM))
        {
        	String planetName = stack.getNbt().getString("name");
        	Planet planet = PlanetList.getByName(planetName);
        	
        	if(!world.isClient() && planet != null)
        	{
        		StarflightPlayerState.get(world.getServer()).unlockPlanet((ServerPlayerEntity) context.getPlayer(), planetName);
        		
        		if(planet.getParent() != null)
        			StarflightPlayerState.get(world.getServer()).unlockPlanet((ServerPlayerEntity) context.getPlayer(), planet.getParent().getName());
        		
        		for(Planet satellite : planet.getSatellites())
        			StarflightPlayerState.get(world.getServer()).unlockPlanet((ServerPlayerEntity) context.getPlayer(), satellite.getName());
        	}
        	
        	world.playSoundAtBlockCenter(position, StarflightEffects.WRENCH_SOUND_EVENT, SoundCategory.BLOCKS, 1.0f, 1.0f, true);
        	return ActionResult.success(world.isClient);
        }
		
		return ActionResult.FAIL;
	}
}