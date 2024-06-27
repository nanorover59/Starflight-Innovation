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
	public static final ScreenHandlerType<MetalFabricatorScreenHandler> METAL_FABRICATOR_SCREEN_HANDLER = register("metal_fabricator", MetalFabricatorScreenHandler::new);
	public static final ScreenHandlerType<ExtractorScreenHandler> EXTRACTOR_SCREEN_HANDLER = register("extractor", ExtractorScreenHandler::new);
	public static final ScreenHandlerType<BatteryScreenHandler> BATTERY_SCREEN_HANDLER = register("battery", BatteryScreenHandler::new);

	private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, Factory<T> factory)
	{
		return Registry.register(Registries.SCREEN_HANDLER, Identifier.of(StarflightMod.MOD_ID, id), new ScreenHandlerType<T>(factory, FeatureFlags.VANILLA_FEATURES));
	}
}