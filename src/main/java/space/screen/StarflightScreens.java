package space.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType.ExtendedFactory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.ScreenHandlerType.Factory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import space.StarflightMod;

public class StarflightScreens
{
	public static ScreenHandlerType<StirlingEngineScreenHandler> STIRLING_ENGINE_SCREEN_HANDLER;
	public static ScreenHandlerType<ElectricFurnaceScreenHandler> ELECTRIC_FURNACE_SCREEN_HANDLER;
	public static ScreenHandlerType<ElectricCrafterScreenHandler> ELECTRIC_CRAFTER_SCREEN_HANDLER;
	public static ScreenHandlerType<MetalFabricatorScreenHandler> METAL_FABRICATOR_SCREEN_HANDLER;
	public static ScreenHandlerType<ElectrolyzerScreenHandler> ELECTROLYZER_SCREEN_HANDLER;
	public static ScreenHandlerType<AdvancedFabricatorScreenHandler> FABRICATION_STATION_SCREEN_HANDLER;
	public static ScreenHandlerType<ExtractorScreenHandler> EXTRACTOR_SCREEN_HANDLER;
	public static ScreenHandlerType<BatteryScreenHandler> BATTERY_SCREEN_HANDLER;
	public static ScreenHandlerType<RocketControllerScreenHandler> ROCKET_CONTROLLER_SCREEN_HANDLER;
	
	public static void initializeScreens()
	{
		STIRLING_ENGINE_SCREEN_HANDLER = register("stirling_engine", StirlingEngineScreenHandler::new);
		ELECTRIC_FURNACE_SCREEN_HANDLER = register("electric_furnace", ElectricFurnaceScreenHandler::new);
		ELECTRIC_CRAFTER_SCREEN_HANDLER = register("electric_crafter", ElectricCrafterScreenHandler::new, BlockPos.PACKET_CODEC.cast());
		METAL_FABRICATOR_SCREEN_HANDLER = register("metal_fabricator", MetalFabricatorScreenHandler::new);
		ELECTROLYZER_SCREEN_HANDLER = register("electrolyzer", ElectrolyzerScreenHandler::new);
		FABRICATION_STATION_SCREEN_HANDLER = register("fabrication_station", AdvancedFabricatorScreenHandler::new);
		EXTRACTOR_SCREEN_HANDLER = register("extractor", ExtractorScreenHandler::new);
		BATTERY_SCREEN_HANDLER = register("battery", BatteryScreenHandler::new);
		ROCKET_CONTROLLER_SCREEN_HANDLER = register("rocket_controller", RocketControllerScreenHandler::new);
		
		Registry.register(Registries.SCREEN_HANDLER, Identifier.of(StarflightMod.MOD_ID, "electric_crafter"), ELECTRIC_CRAFTER_SCREEN_HANDLER);
	}
	
	private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, Factory<T> factory)
	{
		return Registry.register(Registries.SCREEN_HANDLER, Identifier.of(StarflightMod.MOD_ID, id), new ScreenHandlerType<T>(factory, FeatureFlags.VANILLA_FEATURES));
	}
	
	private static <T extends ScreenHandler, D> ExtendedScreenHandlerType<T, D> register(String id, ExtendedFactory<T, D> factory, PacketCodec<? super RegistryByteBuf, D> codec)
	{
		return Registry.register(Registries.SCREEN_HANDLER, Identifier.of(StarflightMod.MOD_ID, id), new ExtendedScreenHandlerType<T, D>(factory, codec));
	}
}