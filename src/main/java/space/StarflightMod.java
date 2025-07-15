package space;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import space.block.StarflightBlocks;
import space.command.StarflightCommands;
import space.entity.StarflightEntities;
import space.event.StarflightEvents;
import space.item.StarflightItems;
import space.network.c2s.StarflightNetworkingC2S;
import space.particle.StarflightParticleTypes;
import space.recipe.StarflightRecipes;
import space.screen.StarflightScreens;
import space.util.StarflightSoundEvents;
import space.world.StarflightWorldGeneration;

public class StarflightMod implements ModInitializer
{
	public static final String MOD_ID = "space";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	@Override
	public void onInitialize()
	{
		StarflightBlocks.initializeBlocks();
		StarflightItems.initializeItems();
		StarflightEntities.initializeEntities();
		StarflightParticleTypes.initializeParticles();
		StarflightWorldGeneration.initializeWorldGeneration();
		StarflightEvents.registerEvents();
		StarflightSoundEvents.initializeSounds();
		StarflightCommands.initializeCommands();
		StarflightRecipes.initializeRecipes();
		StarflightScreens.initializeScreens();
		StarflightNetworkingC2S.initializePackets();
	}
}