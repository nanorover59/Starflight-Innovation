package space.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import com.ibm.icu.text.DecimalFormat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import space.StarflightMod;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.client.render.entity.MovingCraftEntityRenderer;
import space.craft.MovingCraftBlock;
import space.network.c2s.RocketControllerButtonC2SPacket;
import space.network.s2c.RocketControllerDataS2CPacket;
import space.planet.Planet;
import space.planet.PlanetList;
import space.screen.RocketControllerScreenHandler;

@Environment(EnvType.CLIENT)
public class RocketControllerScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier BACKGROUND = Identifier.of(StarflightMod.MOD_ID, "textures/gui/rocket_controller.png");
	private static final Identifier WIDGETS = Identifier.of(StarflightMod.MOD_ID, "textures/gui/rocket_controller_widgets.png");
	private static final Identifier TILE = Identifier.of(StarflightMod.MOD_ID, "textures/block/structural_titanium.png");
	private static final int MARGIN = 12;
    private static final int GREEN = 0xFF6ABE30;
	private static final int RED = 0xFFDC3222;
	private static ArrayList<MovingCraftBlock> blocks = new ArrayList<MovingCraftBlock>();
	public static double mass;
	public static double volume;
	public static double thrust;
	public static double weight;
	public static double buoyancy;
	public static double hydrogen;
	public static double hydrogenCapacity;
	public static double oxygen;
	public static double oxygenCapacity;
	public static double deltaV;
	public static double deltaVCapacity;
	public static double requiredDeltaV1;
	public static double requiredDeltaV2;
	private static float previewScale = 1.0f;
	private static int previewY = 0;
	private boolean mouseHold = false;

	public RocketControllerScreen(ScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
		this.backgroundWidth = 252;
		this.backgroundHeight = 231;
	}
	
	@Override
    protected void init()
	{
		super.init();
        /*ScreenMouseEvents.afterMouseScroll(this).register((screen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
            if(verticalAmount < 0)
		    	scaleFactor *= 0.9;
		    else if(verticalAmount > 0)
		    	scaleFactor *= 1.1;
        });*/
	}
	
	@Override
	public void close()
	{
		super.close();
		blocks.clear();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}
	
	@Override
	public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		context.drawTexture(BACKGROUND, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
		MachineScreenIcons.renderEnergy(context, textRenderer, 18, 125, this.x, this.y, mouseX, mouseY, ((RocketControllerScreenHandler) this.handler).getCharge(), ((EnergyBlock) StarflightBlocks.ELECTROLYZER).getEnergyCapacity());
		
		if(((RocketControllerScreenHandler) this.handler).getScanProgress() > 0)
		{
			drawScanProgress(context, x + 126, y + 50);
			return;
		}
		
		drawButtons(context, x + 15, y + 12, mouseX, mouseY);
		
		if(!blocks.isEmpty())
		{
			drawPreview(context, x + 180, y + previewY);
			drawStats(context, x + 15, y + 56, 96, 11, mouseX, mouseY);
			drawPropellantStats(context, x + 15, y + 30, mouseX, mouseY);
		}
		
	}
	
	private void drawPreview(DrawContext context, int x, int y)
	{
		float rotation = (client.world.getTime()) * 0.05f; // Adjust rotation speed as needed.
		context.getMatrices().push();
		context.getMatrices().translate(x, y, 100.0f);
		context.getMatrices().multiplyPositionMatrix(new Matrix4f().scaling(previewScale, -previewScale, previewScale));
		context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotation(rotation));
		
		for(MovingCraftBlock blockData : blocks)
			MovingCraftEntityRenderer.renderBlock(client.world, context.getMatrices(), context.getVertexConsumers(), client.world.random, blockData, new BlockPos(0, 0, 0), new BlockPos(0, 0, 0), 0xF000F0, 0);
		
		context.getMatrices().pop();
	}
	
	private void drawButtons(DrawContext context, int x, int y, int mouseX, int mouseY)
	{
		int scanX = x;
		int scanY = y;
		int launchX  = x + 50;
		int launchY = y;
		boolean scanHover = mouseX > scanX && mouseX < scanX + 48 && mouseY > scanY && mouseY < scanY + 16;
		boolean launchHover = mouseX > launchX && mouseX < launchX + 48 && mouseY > launchY && mouseY < launchY + 16;
		boolean canLaunch = !blocks.isEmpty() && thrust > weight;
		int scanV = scanHover ? 16 : 0;
		int launchV = canLaunch ? (launchHover ? 16 : 0) : 32;
		context.drawTexture(WIDGETS, scanX, scanY, 0, scanV, 48, 16);
		context.drawTexture(WIDGETS, launchX, launchY, 0, launchV, 48, 16);
		context.drawCenteredTextWithShadow(textRenderer, Text.translatable("block.space.rocket_controller.scan"), scanX + 24, scanY + 4, Colors.WHITE);
		context.drawCenteredTextWithShadow(textRenderer, Text.translatable("block.space.rocket_controller.launch"), launchX + 24, launchY + 4, Colors.WHITE);
		boolean mouseDown = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		
		if(!mouseDown)
			mouseHold = false;
		
		if(scanHover)
			buttonPress(mouseDown, 0);
		else if(launchHover && canLaunch)
			buttonPress(mouseDown, 1);
	}
	
	private void buttonPress(boolean mouseDown, int action)
	{
		if(mouseDown && !mouseHold)
		{
			ClientPlayNetworking.send(new RocketControllerButtonC2SPacket(action));
			client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
			mouseHold = true;
			
			if(action == 1)
				close();	
		}
	}
	
	private void drawStats(DrawContext context, int x, int y, int width, int lineSpacing, int mouseX, int mouseY)
	{
		//boolean inOrbit = PlanetList.getClient().getViewpointDimensionData().isOrbit();
		Planet currentPlanet = PlanetList.getClient().getViewpointPlanet();
		double weight = mass * currentPlanet.getSurfaceGravity() * 9.81;
		DecimalFormat df = new DecimalFormat("#.#");
		
		Text thrustValueText = Text.literal(df.format(thrust / 1000.0) + "kN");
		context.drawText(textRenderer, Text.translatable("block.space.thrust"), x, y, Colors.WHITE, true);
		context.drawText(textRenderer, thrustValueText, x + width - textRenderer.getWidth(thrustValueText), y, Colors.WHITE, true);
		
		Text weightValueText = Text.literal(df.format(weight / 1000.0) + "kN");
		context.drawText(textRenderer, Text.translatable("block.space.weight"), x, y += lineSpacing, Colors.WHITE, true);
		context.drawText(textRenderer, weightValueText, x + width - textRenderer.getWidth(weightValueText), y, Colors.WHITE, true);
		
		Text deltaVValueText = Text.literal((long) deltaV + "m/s");
		context.drawText(textRenderer, Text.translatable("block.space.deltav"), x, y += lineSpacing, Colors.WHITE, true);
		context.drawText(textRenderer, deltaVValueText, x + width - textRenderer.getWidth(deltaVValueText), y, Colors.WHITE, true);
		
		Text deltaVCapacityValueText = Text.literal((long) deltaVCapacity + "m/s");
		context.drawText(textRenderer, Text.translatable("block.space.max_deltav"), x, y += lineSpacing, Colors.WHITE, true);
		context.drawText(textRenderer, deltaVValueText, x + width - textRenderer.getWidth(deltaVCapacityValueText), y, Colors.WHITE, true);
		
		//context.drawText(textRenderer, Text.translatable("block.space.thrust", df.format(thrust / 1000.0)), x, y, Colors.WHITE, true);
		//context.drawText(textRenderer, Text.translatable("block.space.weight", df.format(weight / 1000.0)), x, y + 9, Colors.WHITE, true);
		//context.drawText(textRenderer, Text.translatable("block.space.deltav", (long) deltaV), x, y + 18, Colors.WHITE, true);
	}
	
	private void drawPropellantStats(DrawContext context, int x, int y, int mouseX, int mouseY)
	{
		context.drawText(textRenderer, Text.literal("O2"), x, y + 1, 0x9191E8, true);
		context.drawText(textRenderer, Text.literal("H2"), x, y + 13, 0xF29047, true);
		drawPropellantGauge(context, x + 14, y, mouseX, mouseY, (long) oxygen, (long) oxygenCapacity, "block.space.oxygen", "block.space.capacity", 84);
		drawPropellantGauge(context, x + 14, y + 12, mouseX, mouseY, (long) hydrogen, (long) hydrogenCapacity, "block.space.hydrogen", "block.space.capacity", 92);
		//drawPropellantGauge(context, x + 36, y + 16, mouseX, mouseY, (long) deltaV, (long) deltaVCapacity, "block.space.deltav", "block.space.max_deltav", 108);
	}
	
	private void drawPropellantGauge(DrawContext context, int x, int y, int mouseX, int mouseY, long level, long capacity, String levelKey, String capacityKey, int v)
	{
		int drawWidth = (int) Math.ceil(((double) level / (double) capacity) * 64.0);
		context.drawTexture(WIDGETS, x, y, 0, 64, 66, 10);
		context.drawTexture(WIDGETS, x + 1, y + 1, 0, v, drawWidth, 8);
		
		if(mouseX > x && mouseX < x + 66 && mouseY > y && mouseY < y + 12)
		{
			int percent = (int) (((double) level / (double) capacity) * 100.0);
			List<Text> text = List.of(Text.translatable(levelKey, level), Text.translatable(capacityKey, capacity), Text.literal(percent + "%"));
			context.drawTexture(WIDGETS, x, y, 0, 74, 66, 10);
			context.drawTooltip(textRenderer, text, mouseX, mouseY);
		}
	}
	
	private void drawScanProgress(DrawContext context, int x, int y)
	{
		int progress = (int) MathHelper.map(40 - ((RocketControllerScreenHandler) this.handler).getScanProgress(), 0, 39, 0, 64);
		context.drawCenteredTextWithShadow(textRenderer, Text.translatable("block.space.rocket_controller.scanning"), x, y, Colors.WHITE);
		context.drawTexture(WIDGETS, x - 32, y + 12, 0, 64, 66, 10);
		context.drawTexture(WIDGETS, x - 31, y + 13, 0, 128, progress, 8);
	}
	
	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
	}
	
	public static void receiveDisplayDataUpdate(RocketControllerDataS2CPacket payload, ClientPlayNetworking.Context context)
	{
		double[] stats = payload.stats();
		int minY = 0;
		int maxY = 0;
		ArrayList<MovingCraftBlock> bufferedBlocks = new ArrayList<MovingCraftBlock>();
		
		for(MovingCraftBlock blockData : payload.blockDataList())
		{
			BlockState blockState = blockData.getBlockState();
			BlockPos blockPos = blockData.getPosition();
			NbtCompound blockEntityData = blockData.getBlockEntityData();
			
			boolean[] sidesShowing = blockData.getSidesShowing();
			boolean placeFirst = blockData.placeFirst();
			boolean redstone = blockData.redstonePower();
			long storedFluid = blockData.getStoredFluid();
			bufferedBlocks.add(new MovingCraftBlock(blockState, blockPos, blockEntityData, sidesShowing, placeFirst, redstone, storedFluid));
			
			if(blockPos.getY() < minY)
				minY = blockPos.getY();
			
			if(blockPos.getY() > maxY)
				maxY = blockPos.getY();
		}
		
		float scale = 100.0f / (maxY - minY);
		int yCentered =  60 + (int) ((minY + (maxY - minY) / 2.0f) * scale);
		
		context.client().execute(() -> {
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
			blocks.clear();
			blocks.addAll(bufferedBlocks);
			previewScale = scale;
			previewY = yCentered;
			weight = 0;
			boolean inOrbit = PlanetList.getClient().getViewpointDimensionData().isOrbit();
			Planet currentPlanet = PlanetList.getClient().getViewpointPlanet();
			
			if(currentPlanet != null && !inOrbit)
				weight = mass * currentPlanet.getSurfaceGravity() * 9.81;
		});
	}
}