package space.client.gui;

import java.text.DecimalFormat;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import space.StarflightMod;
import space.planet.ClientPlanet;
import space.planet.ClientPlanetList;

@Environment(EnvType.CLIENT)
public class RocketControllerScreen extends Screen
{
	private static final Identifier TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/gui/rocket_controller.png");
	private static final int MARGIN = 12;
    private static final int GREEN = 0xFF6ABE30;
	private static final int RED = 0xFFDC3222;
	public static String worldKey;
	public static BlockPos position;
	public static double mass;
	public static double thrust;
	public static double hydrogen;
	public static double hydrogenCapacity;
	public static double oxygen;
	public static double oxygenCapacity;
	public static double deltaV;
	public static double deltaVCapacity;
	public static double requiredDeltaV1;
	public static double requiredDeltaV2;
	private boolean mouseHold = false;
	private double scaleFactor = 3.0e-10;

	public RocketControllerScreen(String worldKeyName, BlockPos blockPos)
	{
		super(NarratorManager.EMPTY);
		worldKey = worldKeyName;
		position = blockPos;
	}
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
	
	@Override
    protected void init()
	{
        ScreenMouseEvents.afterMouseScroll(this).register((screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
            if(verticalAmount < 0)
		    	scaleFactor *= 0.9;
		    else if(verticalAmount > 0)
		    	scaleFactor *= 1.1;
        });
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		boolean mouseDown = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		
		if(!mouseDown)
			mouseHold = false;
		
		int x = MARGIN;
		int y = MARGIN;
		boolean inOrbit = ClientPlanetList.isViewpointInOrbit();
		ClientPlanet currentPlanet = ClientPlanetList.getViewpointPlanet();
		context.fill(0, 0, width, height, 0xFF000000);
		button(context, textRenderer, Text.translatable("block.space.rocket_controller.build"), 0, x, y, mouseX, mouseY, mouseDown);
		
		if(mass > 0.0)
		{
			DecimalFormat df = new DecimalFormat("#.#");
			double ttw = 0;
			MutableText ttwText = Text.translatable("block.space.ttw");
			
			if(!inOrbit && currentPlanet != null)
			{
				double weight = mass * currentPlanet.surfaceGravity * 9.81;
				ttw = thrust / weight;
				ttwText.append(df.format(ttw));
			}
			else
			{
				ttw = 100.0;
				ttwText.append("NA");
			}
			
			if(ttw > 1.0)
				button(context, textRenderer, Text.translatable("block.space.rocket_controller.launch"), 1, x, y += 14, mouseX, mouseY, mouseDown);
			
			context.drawText(textRenderer, Text.translatable("block.space.mass").append(df.format(mass / 1000.0)).append("t"), x, y += 14, GREEN, true);
			context.drawText(textRenderer, Text.translatable("block.space.hydrogen_container.level").append(df.format((hydrogen / hydrogenCapacity) * 100)).append("%"), x, y += 14, GREEN, true);
			context.drawText(textRenderer, Text.translatable("block.space.oxygen_container.level").append(df.format((oxygen / oxygenCapacity) * 100)).append("%"), x, y += 14, GREEN, true);
			context.drawText(textRenderer, ttwText, x, y += 14, ttw > 1.0 ? GREEN : RED, true);
			context.drawText(textRenderer, Text.translatable("block.space.deltav").append(df.format(deltaV)).append("m/s"), x, y += 14, GREEN, true);
			context.drawText(textRenderer, Text.translatable("block.space.deltav_capacity").append(df.format(deltaVCapacity)).append("m/s"), x, y += 14, GREEN, true);
			context.drawText(textRenderer, Text.translatable("block.space.deltav_to_orbit").append(df.format(requiredDeltaV1)).append("m/s"), x, y += 14, requiredDeltaV1 < deltaV ? GREEN : RED, true);
			context.drawText(textRenderer, Text.translatable("block.space.deltav_to_surface").append(df.format(requiredDeltaV2)).append("m/s"), x, y += 14, requiredDeltaV2 < deltaV ? GREEN : RED, true);
		}
	}
	
	private void button(DrawContext context, TextRenderer textRenderer, Text buttonText, int action, int x, int y, int mouseX, int mouseY, boolean mouseDown)
	{
		int textWidth = textRenderer.getWidth(buttonText);
        boolean hover = mouseX > x && mouseX < x + textWidth && mouseY > y && mouseY < y + 12;
        int color = hover ? 0xFF00FF00 : GREEN;
        context.drawText(textRenderer, buttonText, x + (hover ? 2 : 0), y, color, true);
        context.drawBorder(x + (hover ? 2 : 0) - 2, y - 2, textWidth + 3, 12, color);
        
        if(hover && mouseDown && !mouseHold)
        {
        	PacketByteBuf buffer = PacketByteBufs.create();
        	buffer.writeString(worldKey);
        	buffer.writeBlockPos(position);
    		buffer.writeInt(action);
    		ClientPlayNetworking.send(new Identifier(StarflightMod.MOD_ID, "rocket_controller_button"), buffer);
    		client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
    		mouseHold = true;
    		
    		if(action == 1)
    			close();
        }
	}
	
	public static void receiveDisplayDataUpdate(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
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