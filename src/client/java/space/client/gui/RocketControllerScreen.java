package space.client.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import space.StarflightMod;
import space.client.render.entity.MovingCraftEntityRenderer;
import space.entity.MovingCraftEntity;
import space.network.c2s.RocketControllerButtonC2SPacket;
import space.network.s2c.RocketControllerDataS2CPacket;
import space.planet.Planet;
import space.planet.PlanetList;

@Environment(EnvType.CLIENT)
public class RocketControllerScreen extends Screen
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/gui/rocket_controller.png");
	private static final int MARGIN = 12;
    private static final int GREEN = 0xFF6ABE30;
	private static final int RED = 0xFFDC3222;
	private static ArrayList<MovingCraftEntity.BlockData> blocks = new ArrayList<MovingCraftEntity.BlockData>();
	public static String worldKey;
	public static BlockPos position;
	public static double mass;
	public static double volume;
	public static double thrust;
	public static double buoyancy;
	public static double hydrogen;
	public static double hydrogenCapacity;
	public static double oxygen;
	public static double oxygenCapacity;
	public static double deltaV;
	public static double deltaVCapacity;
	public static double requiredDeltaV1;
	public static double requiredDeltaV2;
	private static float scaleFactor = 1.0f;
	private boolean mouseHold = false;

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
		boolean inOrbit = PlanetList.getClient().getViewpointDimensionData().isOrbit();
		Planet currentPlanet = PlanetList.getClient().getViewpointPlanet();
		context.fill(0, 0, width, height, -100, 0xFF000000);
		float rotation = (client.world.getTime()) * 0.05f; // Adjust rotation speed as needed.
		context.getMatrices().push();
		context.getMatrices().translate(width / 2, height / 2, -10.0f);
		context.getMatrices().multiplyPositionMatrix(new Matrix4f().scaling(scaleFactor, -scaleFactor, scaleFactor));
		context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotation(rotation));
		
		for(MovingCraftEntity.BlockData blockData : blocks)
			MovingCraftEntityRenderer.renderBlock(client.world, context.getMatrices(), context.getVertexConsumers(), client.world.random, blockData, new BlockPos(0, 0, 0), new BlockPos(0, 0, 0), 0xF000F0, 0);
		
		context.getMatrices().pop();
		
		button(context, textRenderer, Text.translatable("block.space.rocket_controller.build"), 0, x, y, mouseX, mouseY, mouseDown);
		
		if(mass > 0.0)
		{
			DecimalFormat df = new DecimalFormat("#.#");
			double ttw = 0;
			MutableText ttwText = Text.translatable("block.space.ttw");
			double weight = mass * currentPlanet.getSurfaceGravity() * 9.81;
			
			if(!inOrbit && currentPlanet != null)
			{
				ttw = thrust / weight;
				ttwText.append(df.format(ttw));
			}
			else
			{
				ttw = 100.0;
				ttwText.append("NA");
			}
			
			int y1 = y;
			
			if(ttw > 1.0)
				button(context, textRenderer, Text.translatable("block.space.rocket_controller.launch"), 1, x, y1 += 14, mouseX, mouseY, mouseDown);
			
			context.drawText(textRenderer, Text.translatable("block.space.mass").append(df.format(mass / 1000.0)).append("t"), x, y1 += 14, GREEN, true);
			context.drawText(textRenderer, Text.translatable("block.space.hydrogen_container.level").append(df.format((hydrogen / hydrogenCapacity) * 100)).append("%"), x, y1 += 14, GREEN, true);
			context.drawText(textRenderer, Text.translatable("block.space.oxygen_container.level").append(df.format((oxygen / oxygenCapacity) * 100)).append("%"), x, y1 += 14, GREEN, true);
			context.drawText(textRenderer, Text.translatable("block.space.weight").append(df.format(weight / 1000.0)).append("kN"), x, y1 += 14, ttw > 1.0 ? GREEN : RED, true);
			context.drawText(textRenderer, Text.translatable("block.space.thrust").append(df.format(thrust / 1000.0)).append("kN"), x, y1 += 14, ttw > 1.0 ? GREEN : RED, true);
			context.drawText(textRenderer, Text.translatable("block.space.deltav").append(df.format(deltaV)).append("m/s"), x, y1 += 14, GREEN, true);
			context.drawText(textRenderer, Text.translatable("block.space.deltav_capacity").append(df.format(deltaVCapacity)).append("m/s"), x, y1 += 14, GREEN, true);
			context.drawText(textRenderer, Text.translatable("block.space.deltav_to_orbit").append(df.format(requiredDeltaV1)).append("m/s"), x, y1 += 14, requiredDeltaV1 < deltaV ? GREEN : RED, true);
			context.drawText(textRenderer, Text.translatable("block.space.deltav_to_surface").append(df.format(requiredDeltaV2)).append("m/s"), x, y1 += 14, requiredDeltaV2 < deltaV ? GREEN : RED, true);
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
    		ClientPlayNetworking.send(new RocketControllerButtonC2SPacket(worldKey, position, action));
    		client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
    		mouseHold = true;
    		
    		if(action == 1)
    			close();
        }
	}
	
	public static void receiveDisplayDataUpdate(RocketControllerDataS2CPacket payload, ClientPlayNetworking.Context context)
	{
		double[] stats = payload.stats();
		mass = stats[0];
		volume = stats[1];
		thrust = stats[2];
		buoyancy = stats[3];
		hydrogen = stats[4];
		hydrogenCapacity = stats[5];
		oxygen = stats[6];
		oxygenCapacity = stats[7];
		deltaV = stats[8];
		deltaVCapacity = stats[9];
		requiredDeltaV1 = stats[10];
		requiredDeltaV2 = stats[11];
		
		int minY = 0;
		int maxY = 0;
		ArrayList<MovingCraftEntity.BlockData> bufferedBlocks = new ArrayList<MovingCraftEntity.BlockData>();
		
		for(MovingCraftEntity.BlockData blockData : payload.blockDataList())
		{
			BlockState blockState = blockData.getBlockState();
			BlockPos blockPos = blockData.getPosition();
			NbtCompound blockEntityData = blockData.getBlockEntityData();
			
			boolean[] sidesShowing = blockData.getSidesShowing();
			boolean placeFirst = blockData.placeFirst();
			boolean redstone = blockData.redstonePower();
			double storedFluid = blockData.getStoredFluid();
			bufferedBlocks.add(new MovingCraftEntity.BlockData(blockState, blockPos, blockEntityData, sidesShowing, placeFirst, redstone, storedFluid));
			
			if(blockPos.getY() < minY)
				minY = blockPos.getY();
			
			if(blockPos.getY() > maxY)
				maxY = blockPos.getY();
		}
		
		float scale = 100.0f / (maxY - minY);
		
		context.client().execute(() -> {
			blocks.clear();
			blocks.addAll(bufferedBlocks);
			scaleFactor = scale;
		});
	}
}