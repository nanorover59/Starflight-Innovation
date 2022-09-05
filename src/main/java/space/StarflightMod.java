package space;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import space.block.StarflightBlocks;
import space.block.entity.RocketControllerBlockEntity;
import space.command.StarflightCommands;
import space.entity.RocketEntity;
import space.entity.StarflightEntities;
import space.event.StarflightEvents;
import space.item.StarflightItems;
import space.planet.PlanetList;
import space.util.StarflightEffects;
import space.world.StarflightBiomes;
import space.world.StarflightWorldGeneration;

public class StarflightMod implements ModInitializer
{
	public static final String MOD_ID = "space";
	public static ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(new Identifier(StarflightMod.MOD_ID, "general"), () -> new ItemStack(StarflightBlocks.PLANETARIUM));
	
	@Override
	public void onInitialize()
	{
		StarflightBlocks.initializeBlocks();
		StarflightItems.initializeItems();
		StarflightEntities.initializeEntities();
		StarflightBiomes.initializeBiomes();
		StarflightWorldGeneration.initializeWorldGeneration();
		StarflightEvents.registerServerEvents();
		StarflightEffects.initializeSounds();
		StarflightCommands.initializeCommands();
		PlanetList.initialize();
		
		ServerPlayNetworking.registerGlobalReceiver(new Identifier(StarflightMod.MOD_ID, "rocket_controller_button"), (server1, player, handler1, buf, sender) -> RocketControllerBlockEntity.receiveButtonPress(server1, player, handler1, buf, sender));
		ServerPlayNetworking.registerGlobalReceiver(new Identifier(StarflightMod.MOD_ID, "rocket_input"), (server1, player, handler1, buf, sender) -> RocketEntity.receiveInput(server1, player, handler1, buf, sender));
	}
}