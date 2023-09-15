package space.client.gui;

import java.text.DecimalFormat;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

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
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import space.StarflightMod;
import space.planet.ClientPlanet;
import space.planet.ClientPlanetList;

@Environment(EnvType.CLIENT)
public class SpaceNavigationScreen extends Screen
{
    private static final Identifier SELECTION_TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/gui/planet_selection.png");
    private static final int MARGIN = 12;
    private static final int GREEN = 0xFF6ABE30;
	private static final int RED = 0xFFDC3222;
	private boolean mouseHold = false;
	private double scaleFactor = 3.0e-10;
	private static double transferDeltaV;
    private double deltaV;
	private ClientPlanet selectedPlanet = null;
	private ClientPlanet calculationPlanetFrom = null;
	private ClientPlanet calculationPlanetTo = null;
	
	public SpaceNavigationScreen(double deltaV)
	{
		super(NarratorManager.EMPTY);
		this.deltaV = deltaV;
	}
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
	
	@Override
	public boolean shouldCloseOnEsc()
	{
		return deltaV <= 0.0;
	}
	
	@Override
    protected void init()
	{
		transferDeltaV = 0.0;
		
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
		if(selectedPlanet == null)
			selectedPlanet = ClientPlanetList.getByName("sol");
		
		int x = width / 2;
		int y = height / 2;
		boolean mouseDown = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlanet currentPlanet = ClientPlanetList.getViewpointPlanet();
		//ClientPlanet selectedPlanet = ClientPlanetList.getPlanets(false).get(ClientPlanetList.getPlanets(false).indexOf(selectedPlanet));
		double selectedPlanetX = selectedPlanet.getPosition().getX() * scaleFactor;
		double selectedPlanetY = selectedPlanet.getPosition().getZ() * scaleFactor;
		double focusX = selectedPlanetX;
		double focusY = selectedPlanetY;
		boolean nothingSelected = mouseDown;
		
		if(!mouseDown)
			mouseHold = false;
		
		// Render the displayed planets.
		context.fill(0, 0, width, height, 0xFF000000);
		
		for(int i = 0; i < ClientPlanetList.getPlanets(false).size(); i++)
		{
			// Planet planet = planetList.get(i);
			ClientPlanet planet = ClientPlanetList.getPlanets(false).get(i);

			if(planet != selectedPlanet && planet.parent != selectedPlanet)
				continue;

			int renderType = planet.getName().equals("sol") ? 1 : 0;
			float renderWidth = Math.max(4.0f, (float) (planet.radius * scaleFactor));
			float px = (float) ((planet.getPosition().getX() * scaleFactor) + x - focusX);
			float py = (float) ((planet.getPosition().getZ() * scaleFactor) + y - focusY);
			Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

			// drawGlow(matrix, px, py, 8.0f, 1.0f, 1.0f, 1.0f);

			if(!planet.unlocked || planet != selectedPlanet && Math.sqrt(Math.pow(px - x, 2.0) + Math.pow(py - y, 2.0)) < 4.0)
				continue;

			// Draw the planet's orbit in the GUI.
			drawOrbitEllipse(matrix, planet, (float) x, (float) y, 256);

			if(!inBounds(px, py))
				continue;

			// Draw the planet in the GUI.
			int selection = planetSelect(client, px, py, renderWidth, mouseX, mouseY, mouseDown);
			RenderSystem.setShaderTexture(0, renderType == 1 ? new Identifier(StarflightMod.MOD_ID, "textures/environment/sol.png") : ClientPlanet.getTexture(planet.getName()));

			if(renderType == 1)
			{
				drawGlow(matrix, px, py, 8.0f, 1.0f, 1.0f, 1.0f);
				drawTexturedQuad(matrix, px, py, 0.0f, 0.0f, 1.0f, 1.0f, renderWidth);
			}
			else if(planet.simpleTexture)
				drawTexturedQuad(matrix, px, py, 0.0f, 0.0f, 1.0f, 1.0f, renderWidth);
			else
				drawTexturedQuad(matrix, px, py, (8.0f / 16.0f) - (1.0f / 16.0f), 0.0f, (8.0f / 16.0f), 1.0f, renderWidth);

			if(mouseHold || planet == selectedPlanet)
				continue;

			if(selection > 0)
			{
				RenderSystem.setShaderTexture(0, SELECTION_TEXTURE);
				drawTexturedQuad(matrix, px, py, 0.0f, 0.0f, 1.0f, 1.0f, renderWidth * 1.5f);

				if(selection > 1)
				{
					selectedPlanet = planet;
					mouseHold = true;
					nothingSelected = false;
					scaleFactor = getInitialScaleFactor(selectedPlanet);
				}
			}
		}
		
		x = MARGIN;
		y = MARGIN;
        DecimalFormat df = new DecimalFormat("#.#");
        
        if(deltaV < 0.0)
        {
        	// Planetarium screen text and buttons.
        	if(calculationPlanetFrom != null)
        	{
        		MutableText toOrbit = Text.translatable("space.navigation.to_orbit").append(df.format(calculationPlanetFrom.dVOrbit)).append("m/s");
        		MutableText toSurface = Text.translatable("space.navigation.to_surface").append(df.format(calculationPlanetFrom.dVSurface)).append("m/s");
        		selectButton(context, textRenderer, Text.translatable("planet.space." + calculationPlanetFrom.getName()), true, x, y, mouseX, mouseY, mouseDown);
        		context.drawTextWithShadow(textRenderer, toOrbit, x, y + 14, GREEN);
        		context.drawTextWithShadow(textRenderer, toSurface, x, y + 28, GREEN);
        	}
        	else
        		selectButton(context, textRenderer, Text.translatable("space.navigation.select_from"), true, x, y, mouseX, mouseY, mouseDown);
        	
        	if(calculationPlanetTo != null)
        	{
        		MutableText toOrbit = Text.translatable("space.navigation.to_orbit").append(df.format(calculationPlanetTo.dVOrbit)).append("m/s");
        		MutableText toSurface = Text.translatable("space.navigation.to_surface").append(df.format(calculationPlanetTo.dVSurface)).append("m/s");
        		x = width - (Math.max(textRenderer.getWidth(toOrbit), textRenderer.getWidth(toSurface)) + MARGIN);
        		selectButton(context, textRenderer, Text.translatable("planet.space." + calculationPlanetTo.getName()), false, x, y, mouseX, mouseY, mouseDown);
        		context.drawTextWithShadow(textRenderer, toOrbit, x, y + 14, GREEN);
        		context.drawTextWithShadow(textRenderer, toSurface, x, y + 28, GREEN);
        	}
        	else
        		selectButton(context, textRenderer, Text.translatable("space.navigation.select_to"), false, width - (textRenderer.getWidth(Text.translatable("space.navigation.select_to")) + MARGIN), y, mouseX, mouseY, mouseDown);
        
        	if(calculationPlanetFrom != null && calculationPlanetTo != null)
        		context.drawCenteredTextWithShadow(textRenderer, Text.translatable("space.navigation.transfer").append(df.format(transferDeltaV)).append("m/s"), width / 2, y, GREEN);
        }
        else
        {
        	// Navigation screen text and buttons.
	        context.drawTextWithShadow(textRenderer, Text.translatable("planet.space." + currentPlanet.getName()), x, y, GREEN);
	        
	        if(currentPlanet.hasSurface)
	        {
	        	travelButton(context, textRenderer, Text.translatable("space.navigation.to_surface").append(df.format(currentPlanet.dVSurface)).append("m/s"), currentPlanet.getName(), currentPlanet.dVSurface, true, x, y + 14, mouseX, mouseY, mouseDown);
	        	travelButton(context, textRenderer, Text.translatable("space.navigation.stay"), currentPlanet.getName(), 0.0, false, x, y + 28, mouseX, mouseY, mouseDown);
	        }
	        else
	        	travelButton(context, textRenderer, Text.translatable("space.navigation.stay"), currentPlanet.getName(), 0.0, false, x, y + 14, mouseX, mouseY, mouseDown);
	        
	        if(selectedPlanet != currentPlanet)
	        {
		        if(selectedPlanet.hasOrbit)
		        {
		        	MutableText text = Text.translatable("space.navigation.transfer").append(df.format(selectedPlanet.dVTransfer)).append("m/s");
		        	x = width - (textRenderer.getWidth(text) + MARGIN);
		        	context.drawTextWithShadow(textRenderer, Text.translatable("planet.space." + selectedPlanet.getName()), x, y, selectedPlanet.dVTransfer < deltaV ? GREEN : RED);
		        	travelButton(context, textRenderer, text, selectedPlanet.getName(), selectedPlanet.dVTransfer, false, x, y + 14, mouseX, mouseY, mouseDown);
		        }
		        else
		        {
		        	MutableText text = Text.translatable("planet.space." + selectedPlanet.getName());
		        	x = width - (textRenderer.getWidth(text) + MARGIN);
		        	context.drawTextWithShadow(textRenderer, text, x, y, GREEN);
		        }
	        }
        }
        
		// Select planets in the GUI.
		if(nothingSelected && !mouseHold && selectedPlanet.parent != null)
		{
			selectedPlanet = selectedPlanet.parent;
			scaleFactor = getInitialScaleFactor(selectedPlanet);
			mouseHold = true;
		}
	}
	
	private void drawTexturedQuad(Matrix4f matrix, float x, float y, float u0, float v0, float u1, float v1, float size)
	{
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, x - size, y + size, 0.001f).texture(u0, v1).next();
        bufferBuilder.vertex(matrix, x + size, y + size, 0.001f).texture(u1, v1).next();
        bufferBuilder.vertex(matrix, x + size, y - size, 0.001f).texture(u1, v0).next();
        bufferBuilder.vertex(matrix, x - size, y - size, 0.001f).texture(u0, v0).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
	
	private void drawOrbitEllipse(Matrix4f matrix, ClientPlanet planet, float centerX, float centerY, int divisions)
	{
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        for(int i = 0; i <= divisions; i++)
        {
        	float theta = (float) i * (float) (Math.PI * 2.0) / divisions;
        	Vec3d r1 = planet.getRelativePositionAtTrueAnomaly(theta);
        	float x1 = (float) (r1.getX() * scaleFactor) + centerX;
        	float y1 = (float) (r1.getZ() * scaleFactor) + centerY;
        	Vec3d r2 = planet.getRelativePositionAtTrueAnomaly(theta + ((Math.PI * 2.0f) / divisions));
        	float x2 = (float) (r2.getX() * scaleFactor) + centerX;
        	float y2 = (float) (r2.getZ() * scaleFactor) + centerY;
        	
        	if(inBounds(x1, y1) && inBounds(x2, y2))
        	{
        		bufferBuilder.vertex(matrix, x1, y1, 0.0001f).color(0.8f, 0.4f, 1.0f, 0.8f).next();
        		bufferBuilder.vertex(matrix, x2, y2, 0.0001f).color(0.8f, 0.4f, 1.0f, 0.8f).next();
        	}
        }
        
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}
	
	private void drawGlow(Matrix4f matrix, float x, float y, float size, float r, float g, float b)
	{
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, x, y, 0.00001f).color(r, g, b, 0.8f).next();

		for(int i = 0; i <= 16; i++)
		{
			float theta = (float) i * (float) (Math.PI * 2.0) / 16.0f;
			float sinTheta = MathHelper.sin(theta);
			float cosTheta = MathHelper.cos(theta);
			float radius = size;
			
			if(i % 4 == 0)
				radius *= 1.15f;
			
			bufferBuilder.vertex(matrix, x + (radius * cosTheta), y - (radius * sinTheta), 0.00001f).color(r, g, b, 0.0f).next();
		}

		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		RenderSystem.disableBlend();
	}
	
	private int planetSelect(MinecraftClient client, float centerX, float centerY, float radius, int mouseX, int mouseY, boolean mousePressed)
	{
		Vec2f mousePosition = new Vec2f(mouseX, mouseY);
		
		if(mousePosition.distanceSquared(new Vec2f(centerX, centerY)) < radius * radius)
			return mousePressed ? 2 : 1;
		
		return 0;
	}
	
	private boolean inBounds(float x, float y)
	{
		float xMin = 0;
		float yMin = 0;
		float xMax = width;
		float yMax = height;
		return x > xMin && x < xMax && y > yMin && y < yMax;
	}
	
	private double getInitialScaleFactor(ClientPlanet planet)
	{
		if(selectedPlanet.parent == null)
			return 3.0e-10;
		else
		{
			double maxDistance = selectedPlanet.radius;
			
			for(ClientPlanet satellite : selectedPlanet.satellites)
			{
				if(satellite.apoapsis > maxDistance)
					maxDistance = satellite.apoapsis;
			}
			
			return (1.0 / maxDistance) * 32.0;
		}
	}
	
	private void selectButton(DrawContext context, TextRenderer textRenderer, Text buttonText, boolean from, int x, int y, int mouseX, int mouseY, boolean mouseDown)
	{
		int textWidth = textRenderer.getWidth(buttonText);
        boolean hover = mouseX > x && mouseX < x + textWidth && mouseY > y && mouseY < y + 12;
        context.drawText(textRenderer, buttonText, x + (hover ? 2 : 0), y, GREEN, true);
        context.drawBorder(x + (hover ? 2 : 0) - 2, y - 2, textWidth + 3, 12, GREEN);
        
        if(hover && mouseDown && !mouseHold)
        {
        	if(from)
        		calculationPlanetFrom = selectedPlanet;
        	else
        		calculationPlanetTo = selectedPlanet;
        	
        	if(calculationPlanetFrom != null && calculationPlanetTo != null)
        	{
        		PacketByteBuf buffer = PacketByteBufs.create();
        		buffer.writeString(calculationPlanetFrom.getName());
        		buffer.writeString(calculationPlanetTo.getName());
        		ClientPlayNetworking.send(new Identifier(StarflightMod.MOD_ID, "planetarium_transfer"), buffer);
        		client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
        	}
        	
        	mouseHold = true;
        }
	}
	
	private void travelButton(DrawContext context, TextRenderer textRenderer, Text buttonText, String planetName, double requiredDeltaV, boolean landing, int x, int y, int mouseX, int mouseY, boolean mouseDown)
	{
		int textWidth = textRenderer.getWidth(buttonText);
        boolean hover = requiredDeltaV < deltaV && mouseX > x && mouseX < x + textWidth && mouseY > y && mouseY < y + 12;
        int color = requiredDeltaV < deltaV ? (hover ? 0xFF00FF00 : GREEN) : RED;
        context.drawText(textRenderer, buttonText, x + (hover ? 2 : 0), y, color, true);
        context.drawBorder(x + (hover ? 2 : 0) - 2, y - 2, textWidth + 3, 12, color);
        
        if(hover && mouseDown && !mouseHold)
        {
        	PacketByteBuf buffer = PacketByteBufs.create();
    		buffer.writeString(planetName);
    		buffer.writeDouble(requiredDeltaV);
    		buffer.writeBoolean(landing);
    		ClientPlayNetworking.send(new Identifier(StarflightMod.MOD_ID, "rocket_travel_button"), buffer);
    		client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
    		mouseHold = true;
    		close();
        }
	}
	
	public static void receiveTransferCalculation(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
		double deltaV = buffer.readDouble();
		client.execute(() -> transferDeltaV = deltaV);
	}
}