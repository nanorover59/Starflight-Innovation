package space.screen;

import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import space.StarflightMod;

public class StarflightScreens
{
	public static final ScreenHandlerType<PlanetariumScreenHandler> PLANETARIUM_SCREEN_HANDLER = new ScreenHandlerType<>(PlanetariumScreenHandler::new);
	public static final ScreenHandlerType<StirlingEngineScreenHandler> STIRLING_ENGINE_SCREEN_HANDLER = new ScreenHandlerType<>(StirlingEngineScreenHandler::new);
	public static final ScreenHandlerType<ElectricFurnaceScreenHandler> ELECTRIC_FURNACE_SCREEN_HANDLER = new ScreenHandlerType<>(ElectricFurnaceScreenHandler::new);
	public static final ScreenHandlerType<IceElectrolyzerScreenHandler> ICE_ELECTROLYZER_SCREEN_HANDLER = new ScreenHandlerType<>(IceElectrolyzerScreenHandler::new);
	public static final ScreenHandlerType<BatteryScreenHandler> BATTERY_SCREEN_HANDLER = new ScreenHandlerType<>(BatteryScreenHandler::new);
	public static final ScreenHandlerType<RocketControllerScreenHandler> ROCKET_CONTROLLER_SCREEN_HANDLER = new ScreenHandlerType<>(RocketControllerScreenHandler::new);
	
	public static void initializeScreens()
	{
		Registry.register(Registry.SCREEN_HANDLER, new Identifier(StarflightMod.MOD_ID, "planetarium"), PLANETARIUM_SCREEN_HANDLER);
		Registry.register(Registry.SCREEN_HANDLER, new Identifier(StarflightMod.MOD_ID, "stirling_engine"), STIRLING_ENGINE_SCREEN_HANDLER);
		Registry.register(Registry.SCREEN_HANDLER, new Identifier(StarflightMod.MOD_ID, "electric_furnace"), ELECTRIC_FURNACE_SCREEN_HANDLER);
		Registry.register(Registry.SCREEN_HANDLER, new Identifier(StarflightMod.MOD_ID, "ice_electrolyzer"), ICE_ELECTROLYZER_SCREEN_HANDLER);
		Registry.register(Registry.SCREEN_HANDLER, new Identifier(StarflightMod.MOD_ID, "battery"), BATTERY_SCREEN_HANDLER);
		Registry.register(Registry.SCREEN_HANDLER, new Identifier(StarflightMod.MOD_ID, "rocket_controller"), ROCKET_CONTROLLER_SCREEN_HANDLER);
	}
}