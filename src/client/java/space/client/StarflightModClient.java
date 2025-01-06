package space.client;

import java.util.HashMap;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import space.StarflightMod;
import space.block.RocketControllerBlock;
import space.block.StarflightBlocks;
import space.client.gui.BatteryScreen;
import space.client.gui.ElectricFurnaceScreen;
import space.client.gui.ExtractorScreen;
import space.client.gui.MetalFabricatorScreen;
import space.client.gui.RocketControllerScreen;
import space.client.gui.StirlingEngineScreen;
import space.client.particle.StarflightParticleManager;
import space.client.render.MarsDimensionEffect;
import space.client.render.StarflightRenderEffects;
import space.client.render.block.entity.WaterTankBlockEntityRenderer;
import space.client.render.entity.AncientHumanoidEntityRenderer;
import space.client.render.entity.BlockShellEntityRenderer;
import space.client.render.entity.CaveLampreyEntityRenderer;
import space.client.render.entity.CeruleanEntityRenderer;
import space.client.render.entity.CloudSharkEntityRenderer;
import space.client.render.entity.DustEntityRenderer;
import space.client.render.entity.MovingCraftEntityRenderer;
import space.client.render.entity.PlasmaBallEntityRenderer;
import space.client.render.entity.SolarEyesEntityRenderer;
import space.client.render.entity.SolarSpectreEntityRenderer;
import space.client.render.entity.StratofishEntityRenderer;
import space.client.render.entity.model.AncientHumanoidEntityModel;
import space.client.render.entity.model.BlockShellEntityModel;
import space.client.render.entity.model.CaveLampreyEntityModel;
import space.client.render.entity.model.CeruleanEntityModel;
import space.client.render.entity.model.CloudSharkEntityModel;
import space.client.render.entity.model.DustEntityModel;
import space.client.render.entity.model.SolarSpectreEntityModel;
import space.client.render.entity.model.StratofishEntityModel;
import space.entity.AlienMobEntity;
import space.entity.StarflightEntities;
import space.item.StarflightItems;
import space.particle.StarflightParticleTypes;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.screen.StarflightScreens;

@Environment(EnvType.CLIENT)
public class StarflightModClient implements ClientModInitializer
{
	public static final EntityModelLayer MODEL_DUST_LAYER = new EntityModelLayer(Identifier.of(StarflightMod.MOD_ID, "dust"), "main");
	public static final EntityModelLayer MODEL_CERULEAN_LAYER = new EntityModelLayer(Identifier.of(StarflightMod.MOD_ID, "cerulean"), "main");
	public static final EntityModelLayer MODEL_ANCIENT_HUMANOID_LAYER = new EntityModelLayer(Identifier.of(StarflightMod.MOD_ID, "ancient_humanoid"), "main");
	public static final EntityModelLayer MODEL_ANCIENT_HUMANOID_INNER_ARMOR_LAYER = new EntityModelLayer(Identifier.of(StarflightMod.MOD_ID, "ancient_humanoid"), "inner_armor");
	public static final EntityModelLayer MODEL_ANCIENT_HUMANOID_OUTER_ARMOR_LAYER = new EntityModelLayer(Identifier.of(StarflightMod.MOD_ID, "ancient_humanoid"), "outer_armor");
	public static final EntityModelLayer MODEL_SOLAR_SPECTRE_LAYER = new EntityModelLayer(Identifier.of(StarflightMod.MOD_ID, "solar_spectre"), "main");
	public static final EntityModelLayer MODEL_PLASMA_BALL_LAYER = new EntityModelLayer(Identifier.of(StarflightMod.MOD_ID, "plasma_ball"), "main");
	public static final EntityModelLayer MODEL_STRATOFISH_LAYER = new EntityModelLayer(Identifier.of(StarflightMod.MOD_ID, "stratofish"), "main");
	public static final EntityModelLayer MODEL_CLOUD_SHARK_LAYER = new EntityModelLayer(Identifier.of(StarflightMod.MOD_ID, "cloud_shark"), "main");
	public static final EntityModelLayer MODEL_CAVE_LAMPREY_LAYER = new EntityModelLayer(Identifier.of(StarflightMod.MOD_ID, "cave_lamprey"), "main");
	public static final EntityModelLayer MODEL_BLOCK_SHELL_LAYER = new EntityModelLayer(Identifier.of(StarflightMod.MOD_ID, "block_shell"), "main");
	
	private static HashMap<Identifier, DimensionEffects> dimensionEffects = new HashMap<Identifier, DimensionEffects>();
	
	@Override
	public void onInitializeClient()
	{
		// Client side networking.
		StarflightNetworkingS2C.initializePackets();
		
		// Client side block properties.
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.ALUMINUM_FRAME, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.WALKWAY, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.IRON_FRAME, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.TITANIUM_FRAME, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.TITANIUM_GLASS, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.REDSTONE_GLASS, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.WATER_TANK, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.AIRWAY, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.AIRLOCK_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.AIRLOCK_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.TITANIUM_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.ICICLE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.RUBBER_SAPLING, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.LYCOPHYTE_TOP, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.LYCOPHYTE_STEM, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.REDSTONE_BLOSSOM, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.MARS_ROOTS, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.MARS_POTATOES, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.REDSTONE_CLUSTER, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.AEROPLANKTON_VINES, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.AEROPLANKTON_VINES_BOTTOM, RenderLayer.getCutout());
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> 0x437346, StarflightBlocks.RUBBER_LEAVES);
		ColorProviderRegistry.ITEM.register((itemstack, i) -> 0x437346, StarflightBlocks.RUBBER_LEAVES.asItem());
		
		// Client side item properties.
		ColorProviderRegistry.ITEM.register(
			(stack, tintIndex) -> tintIndex == 0 ? getColor(stack) : -1,
			StarflightItems.SPACE_SUIT_HELMET,
			StarflightItems.SPACE_SUIT_CHESTPLATE,
			StarflightItems.SPACE_SUIT_LEGGINGS,
			StarflightItems.SPACE_SUIT_BOOTS
		);
		
		ColorProviderRegistry.ITEM.register(
			(stack, tintIndex) -> {
				if(!stack.contains(StarflightItems.PRIMARY_COLOR) || !stack.contains(StarflightItems.SECONDARY_COLOR))
					return -1;
				
				if(tintIndex == 0)
					return stack.get(StarflightItems.PRIMARY_COLOR) | 0xFF000000;
				else if(tintIndex == 1)
					return stack.get(StarflightItems.SECONDARY_COLOR) | 0xFF000000;
				else
					return -1;
			},
			StarflightItems.PLANETARIUM_CARD
		);
		
		// Block Entity Rendering
		BlockEntityRendererFactories.register(StarflightBlocks.WATER_TANK_BLOCK_ENTITY, WaterTankBlockEntityRenderer::new);
		
		// GUIs
		HandledScreens.register(StarflightScreens.STIRLING_ENGINE_SCREEN_HANDLER, StirlingEngineScreen::new);
		HandledScreens.register(StarflightScreens.ELECTRIC_FURNACE_SCREEN_HANDLER, ElectricFurnaceScreen::new);
		HandledScreens.register(StarflightScreens.METAL_FABRICATOR_SCREEN_HANDLER, MetalFabricatorScreen::new);
		HandledScreens.register(StarflightScreens.EXTRACTOR_SCREEN_HANDLER, ExtractorScreen::new);
		HandledScreens.register(StarflightScreens.BATTERY_SCREEN_HANDLER, BatteryScreen::new);
		
		// Entity Rendering
		EntityRendererRegistry.register(StarflightEntities.MOVING_CRAFT, (context) -> new MovingCraftEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.ROCKET, (context) -> new MovingCraftEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.LINEAR_PLATFORM, (context) -> new MovingCraftEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.AIRSHIP, (context) -> new MovingCraftEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.PLASMA_BALL, (context) -> new PlasmaBallEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.DUST, (context) -> new DustEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.CERULEAN, (context) -> new CeruleanEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.ANCIENT_HUMANOID, (context) -> new AncientHumanoidEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.SOLAR_SPECTRE, (context) -> new SolarSpectreEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.SOLAR_EYES, (context) -> new SolarEyesEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.STRATOFISH, (context) -> new StratofishEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.CLOUD_SHARK, (context) -> new CloudSharkEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.CAVE_LAMPREY, (context) -> new CaveLampreyEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.BLOCK_SHELL, (context) -> new BlockShellEntityRenderer(context));
		
		EntityModelLayerRegistry.registerModelLayer(MODEL_DUST_LAYER, DustEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_CERULEAN_LAYER, CeruleanEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_ANCIENT_HUMANOID_LAYER, AncientHumanoidEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_ANCIENT_HUMANOID_INNER_ARMOR_LAYER, StarflightModClient::getInnerArmorModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_ANCIENT_HUMANOID_OUTER_ARMOR_LAYER, StarflightModClient::getOuterArmorModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_SOLAR_SPECTRE_LAYER, SolarSpectreEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_PLASMA_BALL_LAYER, PlasmaBallEntityRenderer::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_STRATOFISH_LAYER, StratofishEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_CLOUD_SHARK_LAYER, CloudSharkEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_CAVE_LAMPREY_LAYER, CaveLampreyEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_BLOCK_SHELL_LAYER, BlockShellEntityModel::getTexturedModelData);
		
		// Particles
		StarflightParticleManager.initializeParticles();
		
		// Dimension Effects
		registerDimensionEffect(Identifier.of(StarflightMod.MOD_ID, "mars"), new MarsDimensionEffect());
		
		// Start Client Tick Event
		ClientTickEvents.START_CLIENT_TICK.register(client ->
		{
			// Client planet list tick.
			PlanetList.getClient().clientTick(client.getRenderTickCounter().getTickDelta(false));
			
			// Item Tooltip Key
			long windowHandle = client.getWindow().getHandle();
			StarflightItems.tooltipKey = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_RIGHT_CONTROL);
			
			// Rocket user input.
			StarflightControls.vehicleControls(client);
			
			// Open screens that do not have a handler.
			if(RocketControllerBlock.openScreen != null)
			{
				client.setScreen(new RocketControllerScreen(client.world.getRegistryKey().getValue().toString(), RocketControllerBlock.openScreen));
				RocketControllerBlock.openScreen = null;
			}
			
			// Weather particle effects.
			if(!client.isPaused() && client.world != null && client.player != null && client.world.getRainGradient(1.0f) > 0.0f)
			{
				PlanetDimensionData viewpointDimensionData = PlanetList.getClient().getViewpointDimensionData();
				
				if(viewpointDimensionData != null && !viewpointDimensionData.isOrbit() && viewpointDimensionData.getPlanet().getName().equals("mars"))
				{
					Random random = client.world.random;
					int count = 64 + random.nextInt(64);
					int maxDistance = 16;
					
					for(int i = 0; i < count; i++)
					{
						double x = client.player.getX() - maxDistance + random.nextInt(maxDistance * 2);
						double y = client.player.getY() - maxDistance + random.nextInt(maxDistance * 2);
						double z = client.player.getZ() - maxDistance + random.nextInt(maxDistance * 2);
						
						if(client.world.isSkyVisible(new BlockPos((int) x, (int) y, (int) z)))
							client.world.addParticle(StarflightParticleTypes.MARS_DUST, x, y, z, -1.0 + random.nextDouble() * 2.0, -1.0 + random.nextDouble() * 2.0, -1.0 + random.nextDouble() * 2.0);
					}
				}
			}
			
			// Radiation Shader Effect
			if(client.world != null && client.player != null && !client.player.isCreative() && !client.player.isSpectator())
			{
				float maxRadiation = 0.0f;
				
				for(Entity entity : client.world.getEntities())
				{
					if(entity instanceof AlienMobEntity)
					{
						float distance = entity.distanceTo(client.player);
						
						if(distance < ((AlienMobEntity) entity).getRadiationRange())
						{
							float df = distance / ((AlienMobEntity) entity).getRadiationRange();
							float radiation = (1.0f - df) * ((AlienMobEntity) entity).getRadiationStrength();
							
							if(radiation > maxRadiation)
								maxRadiation = radiation;
						}
					}
				}
				
				StarflightRenderEffects.radiation = maxRadiation;
			}
			else
				StarflightRenderEffects.radiation = 0.0f;
		});
		
		// Client Disconnect Event
		ClientLoginConnectionEvents.DISCONNECT.register((handler, client) ->
		{
			PlanetList.reset();
		});
	}
	
	int getColor(ItemStack stack)
	{
	      DyedColorComponent color = stack.get(DataComponentTypes.DYED_COLOR);
	      return color != null ? color.rgb() | 0xFF000000 : -1;
	}
	
	private static TexturedModelData getInnerArmorModelData()
	{
		return TexturedModelData.of(BipedEntityModel.getModelData(new Dilation(0.5f), 0.0f), 64, 32);
	}
	
	private static TexturedModelData getOuterArmorModelData()
	{
		return TexturedModelData.of(BipedEntityModel.getModelData(new Dilation(1.0f), 0.0f), 64, 32);
	}
	
	public static void registerDimensionEffect(Identifier dimensionType, DimensionEffects dimensionEffect)
	{
		dimensionEffects.put(dimensionType, dimensionEffect);
	}
	
	public static DimensionEffects getDimensionEffect(Identifier dimensionType)
	{
		return dimensionEffects.get(dimensionType);
	}
}