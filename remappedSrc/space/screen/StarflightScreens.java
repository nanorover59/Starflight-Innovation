package space.screen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.ScreenHandlerType.Factory;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public class StarflightScreens
{
	public static final ScreenHandlerType<StirlingEngineScreenHandler> STIRLING_ENGINE_SCREEN_HANDLER = register("stirling_engine", StirlingEngineScreenHandler::new);
	public static final ScreenHandlerType<ElectricFurnaceScreenHandler> ELECTRIC_FURNACE_SCREEN_HANDLER = register("electric_furnace", ElectricFurnaceScreenHandler::new);
	public static final ScreenHandlerType<IceElectrolyzerScreenHandler> ICE_ELECTROLYZER_SCREEN_HANDLER = register("ice_electrolyzer", IceElectrolyzerScreenHandler::new);
	public static final ScreenHandlerType<BatteryScreenHandler> BATTERY_SCREEN_HANDLER = register("battery", BatteryScreenHandler::new);

	private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, Factory<T> factory)
	{
		return Registry.register(Registries.SCREEN_HANDLER, new Identifier(StarflightMod.MOD_ID, id), new ScreenHandlerType<T>(factory, FeatureFlags.VANILLA_FEATURES));
	}
}