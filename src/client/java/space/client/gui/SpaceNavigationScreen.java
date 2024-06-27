package space.client.gui;

import java.text.DecimalFormat;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import space.StarflightMod;
import space.client.render.PlanetRenderer;
import space.client.render.StarflightClientEffects;
import space.network.c2s.RocketTravelButtonC2SPacket;
import space.planet.Planet;
import space.planet.PlanetList;

@Environment(EnvType.CLIENT)
public class SpaceNavigationScreen extends Screen
{
    private static final Identifier SELECTION_TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/gui/planet_selection.png");
    private static final int MARGIN = 12;
    private static final int GREEN = 0xFF6ABE30;
	private static final int RED = 0xFFDC3222;
	private boolean mouseHold = false;
	private double scaleFactor = 3.0e-10;
	private static double transferDeltaV;
    private double deltaV;
	private Planet selectedPlanet = null;
	private Planet calculationPlanetFrom = null;
	private Planet calculationPlanetTo = null;
	
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
		PlanetList planetList = PlanetList.getClient();
		
		if(selectedPlanet == null)
			selectedPlanet = planetList.getByName("sol");
		
		if(selectedPlanet == null)
			return;
		
		int x = width / 2;
		int y = height / 2;
		boolean mouseDown = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		MinecraftClient client = MinecraftClient.getInstance();
		Planet currentPlanet = planetList.getViewpointPlanet();
		//ClientPlanet selectedPlanet = ClientPlanetList.getPlanets(false).get(ClientPlanetList.getPlanets(false).indexOf(selectedPlanet));
		double selectedPlanetX = selectedPlanet.getPosition().getX() * scaleFactor;
		double selectedPlanetY = selectedPlanet.getPosition().getZ() * scaleFactor;
		double focusX = selectedPlanetX;
		double focusY = selectedPlanetY;
		boolean nothingSelected = mouseDown;
		
		if(!mouseDown)
			mouseHold = false;
		
		// Render the displayed planets.
		context.fill(0, 0, width, height, -100, 0xFF000000);
		StarflightClientEffects.renderScreenGUIOverlay(client, context, width, height, delta);
		
		for(int i = 0; i < planetList.getPlanets().size(); i++)
		{
			// Planet planet = planetList.get(i);
			Planet planet = planetList.getPlanets().get(i);

			if(planet != selectedPlanet && planet.getParent() != selectedPlanet)
				continue;

			int renderType = planet.getName().equals("sol") ? 1 : 0;
			float renderWidth = Math.max(4.0f, (float) (planet.getRadius() * scaleFactor));
			float px = (float) ((planet.getPosition().getX() * scaleFactor) + x - focusX);
			float py = (float) ((planet.getPosition().getZ() * scaleFactor) + y - focusY);
			Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

			// drawGlow(matrix, px, py, 8.0f, 1.0f, 1.0f, 1.0f);
			// !planet.unlocked || 
			if(planet != selectedPlanet && Math.sqrt(Math.pow(px - x, 2.0) + Math.pow(py - y, 2.0)) < 4.0)
				continue;

			// Draw the planet's orbit in the GUI.
			drawOrbitEllipse(matrix, planet, (float) x, (float) y, 256);

			if(!inBounds(px, py))
				continue;

			// Draw the planet in the GUI.
			int selection = planetSelect(client, px, py, renderWidth, mouseX, mouseY, mouseDown);
			RenderSystem.setShaderTexture(0, renderType == 1 ? Identifier.of(StarflightMod.MOD_ID, "textures/environment/sol.png") : PlanetRenderer.getTexture(planet.getName()));

			if(renderType == 1)
			{
				drawGlow(matrix, px, py, 8.0f, 1.0f, 1.0f, 1.0f);
				drawTexturedQuad(matrix, px, py, 0.0f, 0.0f, 1.0f, 1.0f, renderWidth);
			}
			else if(planet.hasSimpleTexture())
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
        		MutableText toOrbit = Text.translatable("space.navigation.to_orbit").append(df.format(calculationPlanetFrom.dVSurfaceToOrbit())).append("m/s");
        		MutableText toSurface = Text.translatable("space.navigation.to_surface").append(df.format(calculationPlanetFrom.dVOrbitToSurface())).append("m/s");
        		selectButton(context, textRenderer, Text.translatable("planet.space." + calculationPlanetFrom.getName()), true, x, y, mouseX, mouseY, mouseDown);
        		context.drawTextWithShadow(textRenderer, toOrbit, x, y + 14, GREEN);
        		context.drawTextWithShadow(textRenderer, toSurface, x, y + 28, GREEN);
        	}
        	else
        		selectButton(context, textRenderer, Text.translatable("space.navigation.select_from"), true, x, y, mouseX, mouseY, mouseDown);
        	
        	if(calculationPlanetTo != null)
        	{
        		MutableText toOrbit = Text.translatable("space.navigation.to_orbit").append(df.format(calculationPlanetTo.dVSurfaceToOrbit())).append("m/s");
        		MutableText toSurface = Text.translatable("space.navigation.to_surface").append(df.format(calculationPlanetTo.dVOrbitToSurface())).append("m/s");
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
	        
	        if(currentPlanet.getSurface() != null)
	        {
	        	travelButton(context, textRenderer, Text.translatable("space.navigation.to_surface").append(df.format(currentPlanet.dVOrbitToSurface())).append("m/s"), currentPlanet.getName(), currentPlanet.dVOrbitToSurface(), true, x, y + 14, mouseX, mouseY, mouseDown);
	        	travelButton(context, textRenderer, Text.translatable("space.navigation.stay"), currentPlanet.getName(), 0.0, false, x, y + 28, mouseX, mouseY, mouseDown);
	        }
	        else
	        	travelButton(context, textRenderer, Text.translatable("space.navigation.stay"), currentPlanet.getName(), 0.0, false, x, y + 14, mouseX, mouseY, mouseDown);
	        
	        if(selectedPlanet != currentPlanet)
	        {
		        if(selectedPlanet.getOrbit() != null)
		        {
		        	MutableText text = Text.translatable("space.navigation.transfer").append(df.format(transferDeltaV)).append("m/s");
		        	x = width - (textRenderer.getWidth(text) + MARGIN);
		        	context.drawTextWithShadow(textRenderer, Text.translatable("planet.space." + selectedPlanet.getName()), x, y, transferDeltaV < deltaV ? GREEN : RED);
		        	travelButton(context, textRenderer, text, selectedPlanet.getName(), transferDeltaV, false, x, y + 14, mouseX, mouseY, mouseDown);
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
		if(nothingSelected && !mouseHold && selectedPlanet.getParent() != null)
		{
			selectedPlanet = selectedPlanet.getParent();
			scaleFactor = getInitialScaleFactor(selectedPlanet);
			mouseHold = true;
			
			if(deltaV >= 0.0)
				transferDeltaV = currentPlanet.dVToPlanet(selectedPlanet);
		}
	}
	
	private void drawTexturedQuad(Matrix4f matrix, float x, float y, float u0, float v0, float u1, float v1, float size)
	{
		Tessellator tessellator = Tessellator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, x - size, y + size, 0.001f).texture(u0, v1);
        bufferBuilder.vertex(matrix, x + size, y + size, 0.001f).texture(u1, v1);
        bufferBuilder.vertex(matrix, x + size, y - size, 0.001f).texture(u1, v0);
        bufferBuilder.vertex(matrix, x - size, y - size, 0.001f).texture(u0, v0);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
	
	private void drawOrbitEllipse(Matrix4f matrix, Planet planet, float centerX, float centerY, int divisions)
	{
		Tessellator tessellator = Tessellator.getInstance();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        int count = 0;
        
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
        		bufferBuilder.vertex(matrix, x1, y1, 0.0001f).color(0.8f, 0.4f, 1.0f, 0.8f);
        		bufferBuilder.vertex(matrix, x2, y2, 0.0001f).color(0.8f, 0.4f, 1.0f, 0.8f);
        		count++;
        	}
        }
        
        if(count > 0)
        	BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}
	
	private void drawGlow(Matrix4f matrix, float x, float y, float size, float r, float g, float b)
	{
		Tessellator tessellator = Tessellator.getInstance();
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, x, y, 0.00001f).color(r, g, b, 0.8f);

		for(int i = 0; i <= 16; i++)
		{
			float theta = (float) i * (float) (Math.PI * 2.0) / 16.0f;
			float sinTheta = MathHelper.sin(theta);
			float cosTheta = MathHelper.cos(theta);
			float radius = size;
			
			if(i % 4 == 0)
				radius *= 1.15f;
			
			bufferBuilder.vertex(matrix, x + (radius * cosTheta), y - (radius * sinTheta), 0.00001f).color(r, g, b, 0.0f);
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
	
	private double getInitialScaleFactor(Planet planet)
	{
		if(selectedPlanet.getParent() == null)
			return 3.0e-10;
		else
		{
			double maxDistance = selectedPlanet.getRadius();
			
			for(Planet satellite : selectedPlanet.getSatellites())
			{
				if(satellite.getApoapsis() > maxDistance)
					maxDistance = satellite.getApoapsis();
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
        		transferDeltaV = calculationPlanetFrom.dVToPlanet(calculationPlanetTo);
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
    		ClientPlayNetworking.send(new RocketTravelButtonC2SPacket(planetName, requiredDeltaV, landing));
    		client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
    		mouseHold = true;
    		close();
        }
	}
}