package space.client;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilder.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import space.StarflightMod;
import space.block.StarflightBlocks;
import space.client.gui.BatteryScreen;
import space.client.gui.ElectricFurnaceScreen;
import space.client.gui.IceElectrolyzerScreen;
import space.client.gui.PlanetariumScreen;
import space.client.gui.RocketControllerScreen;
import space.client.gui.StirlingEngineScreen;
import space.client.render.entity.CeruleanEntityRenderer;
import space.client.render.entity.DustEntityRenderer;
import space.client.render.entity.MovingCraftEntityRenderer;
import space.client.render.entity.model.CeruleanEntityModel;
import space.client.render.entity.model.DustEntityModel;
import space.entity.MovingCraftEntity;
import space.entity.RocketEntity;
import space.entity.StarflightEntities;
import space.item.StarflightItems;
import space.planet.PlanetRenderList;
import space.screen.BatteryScreenHandler;
import space.screen.ElectricFurnaceScreenHandler;
import space.screen.IceElectrolyzerScreenHandler;
import space.screen.PlanetariumScreenHandler;
import space.screen.RocketControllerScreenHandler;
import space.screen.StirlingEngineScreenHandler;
import space.util.StarflightEffects;
import space.vessel.MovingCraftRenderList;

public class StarflightModClient implements ClientModInitializer
{
	private static KeyBinding throttleUp = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.throttle_up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "key.category.space"));
	private static KeyBinding throttleDown = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.throttle_down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.category.space"));
	private static KeyBinding throttleMax = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.throttle_max", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.category.space"));
	private static KeyBinding throttleMin = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.throttle_min", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.category.space"));
	private static KeyBinding pitchUp = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.pitch_up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UP, "key.category.space"));
	private static KeyBinding pitchDown = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.pitch_down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, "key.category.space"));
	private static KeyBinding yawLeft = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.yaw_left", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, "key.category.space"));
	private static KeyBinding yawRight = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.space.yaw_right", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, "key.category.space"));
	
	//public static final KeyBinding TOOLTIP_KEY = KeyBindingHelper.registerKeyBinding(new StickyKeyBinding("key.space.tooltip", GLFW.GLFW_KEY_LEFT_CONTROL, "key.category.space", () -> true));
	public static final ScreenHandlerType<PlanetariumScreenHandler> PLANETARIUM_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(StarflightMod.MOD_ID, "planetarium"), PlanetariumScreenHandler::new);
	public static final ScreenHandlerType<StirlingEngineScreenHandler> STIRLING_ENGINE_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(StarflightMod.MOD_ID, "stirling_engine"), StirlingEngineScreenHandler::new);
	public static final ScreenHandlerType<ElectricFurnaceScreenHandler> ELECTRIC_FURNACE_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(StarflightMod.MOD_ID, "electric_furnace"), ElectricFurnaceScreenHandler::new);
	public static final ScreenHandlerType<IceElectrolyzerScreenHandler> ICE_ELECTROLYZER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(StarflightMod.MOD_ID, "ice_electrolyzer"), IceElectrolyzerScreenHandler::new);
	public static final ScreenHandlerType<BatteryScreenHandler> BATTERY_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(StarflightMod.MOD_ID, "battery"), BatteryScreenHandler::new);
	public static final ScreenHandlerType<RocketControllerScreenHandler> ROCKET_CONTROLLER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(StarflightMod.MOD_ID, "rocket_controller"), RocketControllerScreenHandler::new);
	
	public static final EntityModelLayer MODEL_DUST_LAYER = new EntityModelLayer(new Identifier(StarflightMod.MOD_ID, "dust"), "main");
	public static final EntityModelLayer MODEL_CERULEAN_LAYER = new EntityModelLayer(new Identifier(StarflightMod.MOD_ID, "cerulean"), "main");
	
	public static VertexBuffer stars;
	public static VertexBuffer milkyWay;
	
	@Override
	public void onInitializeClient()
	{
		// Client side networking.
		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "planet_data"), (client1, handler1, buf, sender1) -> PlanetRenderList.receivePlanetListUpdate(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "moving_craft_render_data"), (client1, handler1, buf, sender1) -> MovingCraftRenderList.receiveCraftListUpdate(handler1, sender1, client1, buf));
			ClientPlayNetworking.registerReceiver(new Identifier(StarflightMod.MOD_ID, "moving_craft_entity_offsets"), (client1, handler1, buf, sender1) -> MovingCraftEntity.receiveEntityOffsets(handler1, sender1, client1, buf));
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
		ScreenRegistry.register(PLANETARIUM_SCREEN_HANDLER, PlanetariumScreen::new);
		ScreenRegistry.register(STIRLING_ENGINE_SCREEN_HANDLER, StirlingEngineScreen::new);
		ScreenRegistry.register(ELECTRIC_FURNACE_SCREEN_HANDLER, ElectricFurnaceScreen::new);
		ScreenRegistry.register(ICE_ELECTROLYZER_SCREEN_HANDLER, IceElectrolyzerScreen::new);
		ScreenRegistry.register(BATTERY_SCREEN_HANDLER, BatteryScreen::new);
		ScreenRegistry.register(ROCKET_CONTROLLER_SCREEN_HANDLER, RocketControllerScreen::new);
		
		// Entity Rendering
		EntityRendererRegistry.register(StarflightEntities.MOVING_CRAFT, (context) -> new MovingCraftEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.ROCKET, (context) -> new MovingCraftEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.DUST, (context) -> new DustEntityRenderer(context));
		EntityRendererRegistry.register(StarflightEntities.CERULEAN, (context) -> new CeruleanEntityRenderer(context));
		
		EntityModelLayerRegistry.registerModelLayer(MODEL_DUST_LAYER, DustEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MODEL_CERULEAN_LAYER, CeruleanEntityModel::getTexturedModelData);
		
		// Client Tick Event
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			PlanetRenderList.updateRenderers();
			
			if(client.player != null && client.player.hasVehicle() && client.player.getVehicle() instanceof RocketEntity)
			{
				int throttleState = 0;
				int pitchState = 0;
				int yawState = 0;
				
				if(throttleUp.isPressed())
					throttleState++;

				if(throttleDown.isPressed())
					throttleState--;
				
				if(throttleMax.isPressed())
					throttleState = 2;
				
				if(throttleMin.isPressed())
					throttleState = -2;

				if(pitchUp.isPressed())
					pitchState++;

				if(pitchDown.isPressed())
					pitchState--;

				if(yawLeft.isPressed())
					yawState++;

				if(yawRight.isPressed())
					yawState--;
				
				PacketByteBuf buffer = PacketByteBufs.create();
				buffer.writeInt(throttleState);
				buffer.writeInt(pitchState);
				buffer.writeInt(yawState);
				ClientPlayNetworking.send(new Identifier(StarflightMod.MOD_ID, "rocket_input"), buffer);
			}
		});
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
	
	public static void initializeBuffers()
	{
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		stars = buildStars(stars, bufferBuilder);
		milkyWay = buildMilkyWay(milkyWay, bufferBuilder);
	}
	
	private static VertexBuffer buildMilkyWay(VertexBuffer vertexBuffer, BufferBuilder bufferBuilder)
	{
		if(vertexBuffer != null)
			vertexBuffer.close();
		
		vertexBuffer = new VertexBuffer();
		BuiltBuffer builtBuffer = wrapAroundSky(bufferBuilder, 64, 100.0f, 0.125f);
		vertexBuffer.bind();
		vertexBuffer.upload(builtBuffer);
		return vertexBuffer;
	}
	
	/**
	 * Used to properly render the milky way wrapped around the sky.
	 */
	private static BuiltBuffer wrapAroundSky(BufferBuilder bufferBuilder, int segments, float radius, float textureRatio)
	{
		float height = radius * (float) Math.tan(Math.PI / segments) * (float) segments * textureRatio;
		
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		
		for(int i = 0; i < segments; i++)
		{
			double theta1 = (i * Math.PI * 2.0) / segments;
			double theta2 = ((i + 1) * Math.PI * 2.0) / segments;
			float y1 = radius * (float) Math.cos(theta1);
			float z1 = radius * (float) Math.sin(theta1);
			float y2 = radius * (float) Math.cos(theta2);
			float z2 = radius * (float) Math.sin(theta2);
			float u1 = (float) i / segments;
			float u2 = (float) (i + 1) / segments;
			
			bufferBuilder.vertex(-height, y1, z1).texture(u1, 0.0f).next();
			bufferBuilder.vertex(height, y1, z1).texture(u1, 1.0f).next();
			bufferBuilder.vertex(height, y2, z2).texture(u2, 1.0f).next();
			bufferBuilder.vertex(-height, y2, z2).texture(u2, 0.0f).next();
		}
		
		return bufferBuilder.end();
	}
	
	private static VertexBuffer buildStars(VertexBuffer vertexBuffer, BufferBuilder bufferBuilder)
	{
		if(vertexBuffer != null)
			vertexBuffer.close();
		
		vertexBuffer = new VertexBuffer();
		BuiltBuffer builtBuffer = renderStars(bufferBuilder);
		vertexBuffer.bind();
		vertexBuffer.upload(builtBuffer);
		return vertexBuffer;
	}
	
	/**
	 * Render textured stars to a vertex buffer. Used by the sky render inject.
	 */
	private static BuiltBuffer renderStars(BufferBuilder buffer)
	{
		Random random = Random.create(20844L);
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

		for(int i = 0; i < 4000; i++)
		{
			int frame = 0;

			while(random.nextFloat() < 0.4 && frame < 3)
				frame++;
			
			double d = random.nextFloat() * 2.0f - 1.0f;
			double e = random.nextFloat() * 2.0f - 1.0f;
			double f = random.nextFloat() * 2.0f - 1.0f;
			double g = 0.8f - (frame * 0.1f) + random.nextFloat() * 0.1f; // Star size.
			double h = d * d + e * e + f * f;

			if(!(h < 1.0) || !(h > 0.01))
				continue;

			h = 1.0 / Math.sqrt(h);
			double j = (d *= h) * 100.0;
			double k = (e *= h) * 100.0;
			double l = (f *= h) * 100.0;
			double m = Math.atan2(d, f);
			double n = Math.sin(m);
			double o = Math.cos(m);
			double p = Math.atan2(Math.sqrt(d * d + f * f), e);
			double q = Math.sin(p);
			double r = Math.cos(p);
			double s = random.nextDouble() * Math.PI * 2.0;
			double t = Math.sin(s);
			double u = Math.cos(s);
			
			double interval = 1.0f / 4.0f;
			float startFrame = (float) (frame * interval);
			float endFrame = (float) (startFrame + interval);

			for(int v = 0; v < 4; v++)
			{
				double x = (double) ((v & 2) - 1) * g;
				double y = (double) ((v + 1 & 2) - 1) * g;
				double aa = x * u - y * t;
				double ac = y * u + x * t;
				double ad = aa * q + 0.0 * r;
				double ae = 0.0 * q - aa * r;
				double af = ae * n - ac * o;
				double ah = ac * n + ae * o;
				buffer.vertex(j + af, k + ad, l + ah).texture(v == 0 || v == 3 ? endFrame : startFrame, v < 2 ? 0.0f : 1.0f).next();
			}
		}
		
		return buffer.end();
	}
}