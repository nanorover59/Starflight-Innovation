package space.client;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import space.StarflightMod;
import space.block.StarflightBlocks;
import space.client.gui.BatteryScreen;
import space.client.gui.ElectricFurnaceScreen;
import space.client.gui.IceElectrolyzerScreen;
import space.client.gui.PlanetariumScreen;
import space.client.gui.RocketControllerScreen;
import space.client.gui.StirlingEngineScreen;
import space.client.particle.StarflightParticleManager;
import space.client.render.entity.AncientHumanoidEntityRenderer;
import space.client.render.entity.CeruleanEntityRenderer;
import space.client.render.entity.DustEntityRenderer;
import space.client.render.entity.MovingCraftEntityRenderer;
import space.client.render.entity.SolarSpectreEntityRenderer;
import space.client.render.entity.model.AncientHumanoidEntityModel;
import space.client.render.entity.model.CeruleanEntityModel;
import space.client.render.entity.model.DustEntityModel;
import space.client.render.entity.model.SolarSpectreEntityModel;
import space.entity.MovingCraftEntity;
import space.entity.RocketEntity;
import space.entity.StarflightEntities;
import space.item.StarflightItems;
import space.mixin.client.KeyBindingMixin;
import space.particle.StarflightParticleTypes;
import space.planet.ClientPlanetList;
import space.screen.StarflightScreens;
import space.util.StarflightEffects;
import space.vessel.MovingCraftRenderList;

@Environment(EnvType.CLIENT)
public class StarflightModClient implements ClientModInitializer
{
	/*private static KeyBinding throttleUp = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.throttle_up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "key.category.space"));
	private static KeyBinding throttleDown = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.throttle_down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.category.space"));
	private static KeyBinding throttleMax = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.throttle_max", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.category.space"));
	private static KeyBinding throttleMin = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.throttle_min", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.category.space"));
	
	private static KeyBinding rollCCW = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.roll_ccw", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.category.space"));
	private static KeyBinding rollCW = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.roll_cw", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.category.space"));
	
	private static KeyBinding pitchUp = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.pitch_up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, "key.category.space"));
	private static KeyBinding pitchDown = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.pitch_down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.category.space"));
	private static KeyBinding yawLeft = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.yaw_left", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.category.space"));
	private static KeyBinding yawRight = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.yaw_right", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "key.category.space"));*/
	
	public static final EntityModelLayer MODEL_DUST_LAYER = new EntityModelLayer(new Identifier(StarflightMod.MOD_ID, "dust"), "main");
	public static final EntityModelLayer MODEL_CERULEAN_LAYER = new EntityModelLayer(new Identifier(StarflightMod.MOD_ID, "cerulean"), "main");
	public static final EntityModelLayer MODEL_ANCIENT_HUMANOID_LAYER = new EntityModelLayer(new Identifier(StarflightMod.MOD_ID, "ancient_humanoid"), "main");
	public static final EntityModelLayer MODEL_ANCIENT_HUMANOID_INNER_ARMOR_LAYER = new EntityModelLayer(new Identifier(StarflightMod.MOD_ID, "ancient_humanoid"), "inner_armor");
	public static final EntityModelLayer MODEL_ANCIENT_HUMANOID_OUTER_ARMOR_LAYER = new EntityModelLayer(new Identifier(StarflightMod.MOD_ID, "ancient_humanoid"), "outer_armor");
	public static final EntityModelLayer MODEL_SOLAR_SPECTRE_LAYER = new EntityModelLayer(new Identifier(StarflightMod.MOD_ID, "solar_spectre"), "main");
	
	private static HashMap<Identifier, DimensionEffects> dimensionEffects = new HashMap<Identifier, DimensionEffects>();
	
	@Override
	public void onInitializeClient()
	{		
		// Client side networking.
		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "planet_data"), (client1, handler1, buf, sender1) -> ClientPlanetList.receivePlanetListUpdate(client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "moving_craft_render_data"), (client1, handler1, buf, sender1) -> MovingCraftRenderList.receiveCraftListUpdate(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "moving_craft_entity_offsets"), (client1, handler1, buf, sender1) -> MovingCraftEntity.receiveEntityOffsets(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "rocket_open_travel_screen"), (client1, handler1, buf, sender1) -> RocketEntity.receiveOpenTravelScreen(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "rocket_controller_data"), (client1, handler1, buf, sender1) -> RocketControllerScreen.receiveDisplayDataUpdate(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "fizz"), (client1, handler1, buf, sender1) -> StarflightEffects.receiveFizz(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "outgas"), (client1, handler1, buf, sender1) -> StarflightEffects.receiveOutgas(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "jet"), (client1, handler1, buf, sender1) -> StarflightEffects.receiveJet(handler1, sender1, client1, buf));
		});
		
		// Client side block properties.
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.ALUMINUM_FRAME, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.WALKWAY, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.AIRLOCK_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.AIRLOCK_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.RUBBER_SAPLING, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.LYCOPHYTE_TOP, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StarflightBlocks.LYCOPHYTE_STEM, RenderLayer.getCutout());
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> 0x437346, StarflightBlocks.RUBBER_LEAVES);
		ColorProviderRegistry.ITEM.register((itemstack, i) -> 0x437346, StarflightBlocks.RUBBER_LEAVES.asItem());
		
		// Client side item properties.
		ColorProviderRegistry.ITEM.register(
			(stack, tintIndex) -> tintIndex == 0 ? getColor(stack) : 0xFFFFFFF,
			StarflightItems.SPACE_SUIT_HELMET,
			StarflightItems.SPACE_SUIT_CHESTPLATE,
			StarflightItems.SPACE_SUIT_LEGGINGS,
			StarflightItems.SPACE_SUIT_BOOTS
		);
		
		// GUIs
		HandledScreens.register(StarflightScreens.PLANETARIUM_SCREEN_HANDLER, PlanetariumScreen::new);
		HandledScreens.register(StarflightScreens.STIRLING_ENGINE_SCREEN_HANDLER, StirlingEngineScreen::new);
		HandledScreens.register(StarflightScreens.ELECTRIC_FURNACE_SCREEN_HANDLER, ElectricFurnaceScreen::new);
		HandledScreens.register(StarflightScreens.ICE_ELECTROLYZER_SCREEN_HANDLER, IceElectrolyzerScreen::new);
		HandledScreens.register(StarflightScreens.BATTERY_SCREEN_HANDLER, BatteryScreen::new);
		HandledScreens.register(StarflightScreens.ROCKET_CONTROLLER_SCREEN_HANDLER, RocketControllerScreen::new);
		
		// Entity Rendering
		EntityRendererRegistry.register(StarflightEntities.MOVING_CRAFT, (context) -> new MovingCraftEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.ROCKET, (context) -> new MovingCraftEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.DUST, (context) -> new DustEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.CERULEAN, (context) -> new CeruleanEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.ANCIENT_HUMANOID, (context) -> new AncientHumanoidEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.SOLAR_SPECTRE, (context) -> new SolarSpectreEntityRenderer(context));
		
		EntityModelLayerRegistry.registerModelLayer(MODEL_DUST_LAYER, DustEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_CERULEAN_LAYER, CeruleanEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_ANCIENT_HUMANOID_LAYER, AncientHumanoidEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_ANCIENT_HUMANOID_INNER_ARMOR_LAYER, StarflightModClient::getInnerArmorModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_ANCIENT_HUMANOID_OUTER_ARMOR_LAYER, StarflightModClient::getOuterArmorModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_SOLAR_SPECTRE_LAYER, SolarSpectreEntityModel::getTexturedModelData);
		
		// Particles
		StarflightParticleManager.initializeParticles();
		
		// Dimension Effects
		registerDimensionEffect(new Identifier(StarflightMod.MOD_ID, "mars"), new Mars());
		
		// Client Tick Event
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			
			// Update the planet render list.
			ClientPlanetList.updatePlanets();
			
			// Rocket user input.
			if(client.player != null && client.player.hasVehicle() && client.player.getVehicle() instanceof RocketEntity)
			{
				int throttleState = 0;
				int rollState = 0;
				int pitchState = 0;
				int yawState = 0;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT))
					throttleState++;

				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL))
					throttleState--;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_Z))
					throttleState = 2;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_X))
					throttleState = -2;

				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_A))
					rollState++;

				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_D))
					rollState--;
				
				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_W))
					pitchState++;

				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_S))
					pitchState--;

				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_Q))
					yawState++;

				if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_E))
					yawState--;
				
				GameOptions o = client.options;
				unpressKeys(o.dropKey, o.inventoryKey, o.sneakKey, o.sprintKey, o.forwardKey, o.backKey, o.leftKey, o.rightKey);
				
				PacketByteBuf buffer = PacketByteBufs.create();
				buffer.writeInt(throttleState);
				buffer.writeInt(rollState);
				buffer.writeInt(pitchState);
				buffer.writeInt(yawState);
				ClientPlayNetworking.send(new Identifier(StarflightMod.MOD_ID, "rocket_input"), buffer);
			}
			
			// Weather particle effects.
			if(!client.isPaused() && client.world != null && client.player != null && client.world.getRainGradient(1.0f) > 0.0f)
			{
				if(!ClientPlanetList.isViewpointInOrbit() && ClientPlanetList.getViewpointPlanet() != null && ClientPlanetList.getViewpointPlanet().getName().equals("mars"))
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
		});
	}
	
	int getColor(ItemStack stack)
	{
	      NbtCompound nbtCompound = stack.getSubNbt("display");
	      return nbtCompound != null && nbtCompound.contains("color", 99) ? nbtCompound.getInt("color") : 0xFFFFFFF;
	}
	
	private static TexturedModelData getInnerArmorModelData()
	{
		return TexturedModelData.of(BipedEntityModel.getModelData(new Dilation(0.5f), 0.0f), 64, 32);
	}
	
	private static TexturedModelData getOuterArmorModelData()
	{
		return TexturedModelData.of(BipedEntityModel.getModelData(new Dilation(1.0f), 0.0f), 64, 32);
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
	
	public static void unpressKeys(KeyBinding ... keyBindings)
	{
		for(KeyBinding key : keyBindings)
			((KeyBindingMixin) key).callReset();
	}
	
	public static void registerDimensionEffect(Identifier dimensionType, DimensionEffects dimensionEffect)
	{
		dimensionEffects.put(dimensionType, dimensionEffect);
	}
	
	public static DimensionEffects getDimensionEffect(Identifier dimensionType)
	{
		return dimensionEffects.get(dimensionType);
	}
	
	@Environment(value = EnvType.CLIENT)
	public static class Mars extends DimensionEffects
	{
		public Mars()
		{
			super(192.0f, true, SkyType.NORMAL, false, false);
		}

		@Override
		public float[] getFogColorOverride(float skyAngle, float tickDelta)
		{
			float g = MathHelper.cos(skyAngle * (float) (Math.PI * 2.0));
			
			if(g >= -0.4f && g <= 0.4f)
			{
				float i = (g - -0.0f) / 0.4f * 0.5f + 0.5f;
				float j = 1.0f - (1.0f - MathHelper.sin(i * (float) Math.PI)) * 0.99f;
				float[] rgba = new float[4];
				rgba[0] = 0.3f;
				rgba[1] = i * i * 0.7f + 0.3f;
				rgba[2] = i * 0.3f + 0.7f;
				rgba[3] = j * j;
				return rgba;
			}
			
			return null;
		}
		
		@Override
		public Vec3d adjustFogColor(Vec3d color, float sunHeight)
		{
			return color.multiply(sunHeight * 0.94f + 0.06f, sunHeight * 0.94f + 0.06f, sunHeight * 0.91f + 0.09f);
		}

		@Override
		public boolean useThickFog(int camX, int camY)
		{
			return false;
		}
	}
}