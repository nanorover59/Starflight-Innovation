package space.client.gui;

import java.text.DecimalFormat;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import space.StarflightMod;
import space.planet.ClientPlanet;
import space.planet.ClientPlanetList;

@Environment(EnvType.CLIENT)
public class RocketControllerScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/gui/rocket_controller.png");
	private static final int GREEN = 0x6abe30;
	private static final int RED = 0xdc3222;
	public static String worldKey;
	public static BlockPos position = new BlockPos(-256, -256, -256);
	public static String targetName;
	public static double mass = 0.0;
	public static double thrust = 0.0;
	public static double hydrogen = 0.0;
	public static double hydrogenCapacity = 0.0;
	public static double oxygen = 0.0;
	public static double oxygenCapacity = 0.0;
	public static double deltaV = 0.0;
	public static double deltaVCapacity = 0.0;
	public static double requiredDeltaV1 = 0.0;
	public static double requiredDeltaV2 = 0.0;
	
	private int buttonCooldown = 0;

	public RocketControllerScreen(ScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
		backgroundHeight = 222;
		playerInventoryTitleY = backgroundHeight - 94;
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		boolean mousePressed = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		//PlanetDimensionData data = PlanetList.getDimensionDataForWorld(client.world);
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
		boolean inOrbit = ClientPlanetList.isViewpointInOrbit();
		double ttw = 0;
		ClientPlanet currentPlanet = ClientPlanetList.getViewpointPlanet();
		ClientPlanet targetPlanet = ClientPlanetList.getByName(targetName);
		DecimalFormat df = new DecimalFormat("#.#");
		MutableText massText = Text.translatable("block.space.mass").append(df.format(mass / 1000.0)).append("t");
		MutableText ttwText = Text.translatable("block.space.ttw");
		
		if(!inOrbit && currentPlanet != null)
		{
			double weight = mass * currentPlanet.surfaceGravity * 9.81;
			ttw = thrust / weight;
			ttwText.append(df.format(ttw));
		}
		else
			ttwText.append("NA");
			
		MutableText hydrogenText = Text.translatable("block.space.hydrogen_container.level").append(df.format((hydrogen / hydrogenCapacity) * 100)).append("%");
		MutableText oxygenText = Text.translatable("block.space.oxygen_container.level").append(df.format((oxygen / oxygenCapacity) * 100)).append("%");
		MutableText deltaVText = Text.translatable("block.space.deltav").append(df.format(deltaV)).append("m/s");
		MutableText deltaVCapacityText = Text.translatable("block.space.deltav_capacity").append(df.format(deltaVCapacity)).append("m/s");
		MutableText deltaVToOrbitText = Text.translatable("block.space.deltav_to_orbit").append(df.format(requiredDeltaV1)).append("m/s");
		MutableText deltaVToSurfaceText = Text.translatable("block.space.deltav_to_surface").append(df.format(requiredDeltaV1)).append("m/s");
		MutableText deltaVToTargetText = null;
		
		if(targetPlanet != null)
			deltaVToTargetText = Text.translatable("planet.space." + targetPlanet.getName()).append(": ").append(df.format(requiredDeltaV2)).append("m/s");
		
		MutableText button1Text = Text.translatable("block.space.rocket_controller.build");
		MutableText button2Text = Text.translatable(inOrbit ? "block.space.rocket_controller.surface" : "block.space.rocket_controller.orbit");
		MutableText button3Text = Text.translatable("block.space.rocket_controller.transfer");
		int button1X = x + 118;
		int button1Y = y + 20;
		int button2X = x + 118;
		int button2Y = y + 33;
		int button3X = x + 118;
		int button3Y = y + 46;
		boolean button2Enabled = currentPlanet != null && currentPlanet.hasSurface && requiredDeltaV1 > 0 && ((!inOrbit && ttw > 1 && requiredDeltaV1 < deltaV) || (inOrbit && requiredDeltaV1 < deltaV));
		boolean button3Enabled = inOrbit && requiredDeltaV2 > 0 && requiredDeltaV2 < deltaV;
		boolean button1Hover = mouseX >= button1X && mouseX < button1X + 48 && mouseY >= button1Y && mouseY < button1Y + 11;
		boolean button2Hover = button2Enabled && mouseX >= button2X && mouseX < button2X + 48 && mouseY >= button2Y && mouseY < button2Y + 11;
		boolean button3Hover = button3Enabled && mouseX >= button3X && mouseX < button3X + 48 && mouseY >= button3Y && mouseY < button3Y + 11;
		context.drawTexture(TEXTURE, button1X, button1Y, 176, button1Hover ? 11 : 0, 48, 11);
		
		if(button2Enabled)
			context.drawTexture(TEXTURE, button2X, button2Y, 176, button2Hover ? 11 : 0, 48, 11);
		
		if(button3Enabled)
			context.drawTexture(TEXTURE, button3X, button3Y, 176, button3Hover ? 11 : 0, 48, 11);
		
		context.drawText(textRenderer, button1Text, button1X + 2, button1Y + 2, 0x000000, false);
		
		if(button2Enabled)
			context.drawText(textRenderer, button2Text, button2X + 2, button2Y + 2, 0x000000, false);
		
		if(button3Enabled)
			context.drawText(textRenderer, button3Text, button3X + 2, button3Y + 2, 0x000000, false);
		
		context.drawText(textRenderer, massText, x + 32, y + 20, GREEN, true);
		context.drawText(textRenderer, ttwText, x + 32, y + 33, !inOrbit && ttw <= 1 ? RED : GREEN, true);
		context.drawText(textRenderer, hydrogenText, x + 32, y + 46, GREEN, true);
		context.drawText(textRenderer, oxygenText, x + 32, y + 59, GREEN, true);
		context.drawText(textRenderer, deltaVText, x + 32, y + 72, (deltaV < requiredDeltaV1 || deltaV < requiredDeltaV2) ? RED : GREEN, true);
		context.drawText(textRenderer, deltaVCapacityText, x + 32, y + 85, (deltaVCapacity < requiredDeltaV1 || deltaVCapacity < requiredDeltaV2) ? RED : GREEN, true);
		
		if(!inOrbit)
			context.drawText(textRenderer, deltaVToOrbitText, x + 32, y + 98, requiredDeltaV1 < deltaV ? GREEN : RED, true);
		else
		{
			context.drawText(textRenderer, deltaVToSurfaceText, x + 32, y + 98, requiredDeltaV1 < deltaV ? GREEN : RED, true);
			
			if(deltaVToTargetText != null)
				context.drawText(textRenderer, deltaVToTargetText, x + 32, y + 111, requiredDeltaV2 < deltaV ? GREEN : RED, true);
		}
		
		if(buttonCooldown > 0)
			buttonCooldown--;
		
		if(buttonCooldown == 0 && mousePressed)
		{
			if(button1Hover)
				sendButtonPress(0);
			else if(button2Hover)
				sendButtonPress(1);
			else if(button3Hover)
				sendButtonPress(2);
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void init()
	{
		super.init();
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}
	
	private void sendButtonPress(int buttonID)
	{
		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeString(worldKey);
		buffer.writeBlockPos(position);
		buffer.writeInt(buttonID);
		ClientPlayNetworking.send(new Identifier(StarflightMod.MOD_ID, "rocket_controller_button"), buffer);
		client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
		buttonCooldown = 20;
		
		if(buttonID > 0)
			close();
	}
	
	public static void receiveDisplayDataUpdate(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
		worldKey = buffer.readString();
		position = buffer.readBlockPos();
		targetName = buffer.readString();
		mass = buffer.readDouble();
		thrust = buffer.readDouble();
		hydrogen = buffer.readDouble();
		hydrogenCapacity = buffer.readDouble();
		oxygen = buffer.readDouble();
		oxygenCapacity = buffer.readDouble();
		deltaV = buffer.readDouble();
		deltaVCapacity = buffer.readDouble();
		requiredDeltaV1 = buffer.readDouble();
		requiredDeltaV2 = buffer.readDouble();
	}
}