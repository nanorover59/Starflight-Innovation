package space.client;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.block.StarflightBlocks;
import space.client.gui.BatteryScreen;
import space.client.gui.ElectricFurnaceScreen;
import space.client.gui.PlanetariumScreen;
import space.client.gui.RocketControllerScreen;
import space.client.gui.StirlingEngineScreen;
import space.client.render.entity.MovingCraftEntityRenderer;
import space.entity.MovingCraftEntity;
import space.entity.StarflightEntities;
import space.item.StarflightItems;
import space.planet.PlanetRenderList;
import space.screen.BatteryScreenHandler;
import space.screen.ElectricFurnaceScreenHandler;
import space.screen.PlanetariumScreenHandler;
import space.screen.RocketControllerScreenHandler;
import space.screen.StirlingEngineScreenHandler;
import space.util.StarflightEffects;
import space.vessel.MovingCraftRenderList;

public class StarflightModClient implements ClientModInitializer
{
	public static final KeyBinding TOOLTIP_KEY = KeyBindingHelper.registerKeyBinding(new StickyKeyBinding("key.space.tooltip", GLFW.GLFW_KEY_LEFT_CONTROL, "key.category.space", () -> true));
	public static final ScreenHandlerType<PlanetariumScreenHandler> PLANETARIUM_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(StarflightMod.MOD_ID, "planetarium"), PlanetariumScreenHandler::new);
	public static final ScreenHandlerType<StirlingEngineScreenHandler> STIRLING_ENGINE_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(StarflightMod.MOD_ID, "stirling_engine"), StirlingEngineScreenHandler::new);
	public static final ScreenHandlerType<ElectricFurnaceScreenHandler> ELECTRIC_FURNACE_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(StarflightMod.MOD_ID, "electric_furnace"), ElectricFurnaceScreenHandler::new);
	public static final ScreenHandlerType<BatteryScreenHandler> BATTERY_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(StarflightMod.MOD_ID, "battery"), BatteryScreenHandler::new);
	public static final ScreenHandlerType<RocketControllerScreenHandler> ROCKET_CONTROLLER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(StarflightMod.MOD_ID, "rocket_controller"), RocketControllerScreenHandler::new);
	
	@Override
	public void onInitializeClient()
	{
		ColorProviderRegistry.ITEM.register(
				(stack, tintIndex) -> tintIndex == 0 ? getColor(stack) : 0xFFFFFFF,
				StarflightItems.SPACE_SUIT_HELMET,
				StarflightItems.SPACE_SUIT_CHESTPLATE,
				StarflightItems.SPACE_SUIT_LEGGINGS,
				StarflightItems.SPACE_SUIT_BOOTS
		);
		
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.RUBBER_SAPLING, RenderLayer.getCutout());
		
		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "planet_data"), (client1, handler1, buf, sender1) -> PlanetRenderList.receivePlanetListUpdate(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "moving_craft_render_data"), (client1, handler1, buf, sender1) -> MovingCraftRenderList.receiveCraftListUpdate(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "moving_craft_entity_offsets"), (client1, handler1, buf, sender1) -> MovingCraftEntity.receiveEntityOffsets(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "rocket_controller_data"), (client1, handler1, buf, sender1) -> RocketControllerScreen.receiveDisplayDataUpdate(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "fizz"), (client1, handler1, buf, sender1) -> StarflightEffects.receiveFizz(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "outgas"), (client1, handler1, buf, sender1) -> StarflightEffects.receiveOutgas(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "jet"), (client1, handler1, buf, sender1) -> StarflightEffects.receiveJet(handler1, sender1, client1, buf));
		});
		
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.AIRLOCK_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.AIRLOCK_TRAPDOOR, RenderLayer.getCutout());
		
		ScreenRegistry.register(PLANETARIUM_SCREEN_HANDLER, PlanetariumScreen::new);
		ScreenRegistry.register(STIRLING_ENGINE_SCREEN_HANDLER, StirlingEngineScreen::new);
		ScreenRegistry.register(ELECTRIC_FURNACE_SCREEN_HANDLER, ElectricFurnaceScreen::new);
		ScreenRegistry.register(BATTERY_SCREEN_HANDLER, BatteryScreen::new);
		ScreenRegistry.register(ROCKET_CONTROLLER_SCREEN_HANDLER, RocketControllerScreen::new);
		
		EntityRendererRegistry.register(StarflightEntities.MOVING_CRAFT, (context) -> new MovingCraftEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.ROCKET, (context) -> new MovingCraftEntityRenderer(context));
	}
	
	int getColor(ItemStack stack)
	{
	      NbtCompound nbtCompound = stack.getSubNbt("display");
	      return nbtCompound != null && nbtCompound.contains("color", 99) ? nbtCompound.getInt("color") : 0xFFFFFFF;
	}
	
	public static void hiddenItemTooltip(List<Text> tooltip, Text ... texts)
	{
		if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL))
		{
			for(Text text : texts)
				tooltip.add(text);
		}
		else
			tooltip.add(Text.translatable("item.space.press_for_more").formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
	}
	
	public static void hiddenItemTooltip(List<Text> tooltip, List<Text> texts)
	{
		if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL))
		{
			for(Text text : texts)
				tooltip.add(text);
		}
		else
			tooltip.add(Text.translatable("item.space.press_for_more").formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
	}
}
