package space.client.gui;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;
import space.StarflightMod;
import space.planet.Planet;
import space.planet.PlanetList;
import space.planet.PlanetRenderList;
import space.planet.PlanetRenderer;
import space.screen.PlanetariumScreenHandler;

@Environment(EnvType.CLIENT)
public class PlanetariumScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier SELECTION_TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/gui/planet_selection.png");
	private static final Identifier TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/gui/planetarium.png");
	private Planet selectedPlanet = null;
	private boolean mouseHold = false;
	private int buttonCooldown = 0;
	private double scaleFactor = 1.5e-10;

	public PlanetariumScreen(ScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
		backgroundHeight = 222;
		playerInventoryTitleY = backgroundHeight - 94;
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY)
	{
		if(selectedPlanet == null)
			selectedPlanet = PlanetList.getByName("sol");
		
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		boolean mousePressed = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		MinecraftClient client = MinecraftClient.getInstance();
		
		// Draw the background of the GUI.
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, TEXTURE);
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		
		if(PlanetList.getPlanets().size() != PlanetRenderList.getRenderers(false).size())
			return;
		
		@SuppressWarnings("unchecked")
		ArrayList<Planet> planetList = (ArrayList<Planet>) PlanetList.getPlanets().clone();
		
		PlanetRenderer selectedPlanetRenderer = PlanetRenderList.getRenderers(false).get(planetList.indexOf(selectedPlanet));
		double selectedPlanetX = selectedPlanetRenderer.getPosition().getX() * scaleFactor;
		double selectedPlanetY = selectedPlanetRenderer.getPosition().getZ() * scaleFactor;
		double focusX = selectedPlanetX;
		double focusY = selectedPlanetY;
		boolean nothingSelected = mousePressed;
		
		if(!mousePressed)
			mouseHold = false;
		
		// Render the displayed planets.
		
		for(Planet p : selectedPlanet.getSatellites())
		{
			// Draw the planet's orbit in the GUI.
			float rMin = (float) (p.getPeriapsis() * scaleFactor);
			float rMax = (float) (p.getApoapsis() * scaleFactor);
			float px = (float) (x + 99.0);
			float py = (float) (y + 71.0);
			drawOrbitEllipse(matrices.peek().getPositionMatrix(), px, py, rMin, rMax, (float) p.getArgumentOfPeriapsis(), 256);
		}
		
		for(int i = 0; i < planetList.size(); i++)
		{
			Planet planet = planetList.get(i);
			PlanetRenderer planetRenderer = PlanetRenderList.getRenderers(false).get(i);
			System.out.println(i + "   " + planet.getName() + " " + planetRenderer.getName());
			
			if(planet != selectedPlanet && planet.getParent() != selectedPlanet)
				continue;
			
			int renderType = planet.getName() == "sol" ? 1 : 0;
			float renderWidth = Math.max(5.0f, (float) (planet.getRadius() * scaleFactor * 16.0));
			float px = (float) ((planetRenderer.getPosition().getX() * scaleFactor) + x + 99.0 - focusX);
			float py = (float) ((planetRenderer.getPosition().getZ() * scaleFactor) + y + 71.0 - focusY);
			
			if(!inBounds(px, py) || (planet != selectedPlanet && Math.sqrt(Math.pow(px - (x + 99.0), 2.0) + Math.pow(py - (y + 71.0), 2.0)) < 4.0))
				continue;
			
			int selection = planetSelect(client, px, py, renderWidth, mouseX, mouseY, mousePressed);
			RenderSystem.setShaderTexture(0, renderType == 1 ? new Identifier(StarflightMod.MOD_ID, "textures/environment/sun_0.png") : PlanetRenderer.getTexture(planet.getName()));
			
			if(renderType == 1)
				drawTexturedQuad(matrices.peek().getPositionMatrix(), px, py, 0.0f, 0.0f, 1.0f, 1.0f, renderWidth);
			else
				drawTexturedQuad(matrices.peek().getPositionMatrix(), px, py, (8.0f / 16.0f) - (1.0f / 16.0f), 0.0f, (8.0f / 16.0f), 1.0f, renderWidth);
			
			if(mouseHold || planet == selectedPlanet)
				continue;
			
			if(selection > 0)
			{
				RenderSystem.setShaderTexture(0, SELECTION_TEXTURE);
				drawTexturedQuad(matrices.peek().getPositionMatrix(), px, py, 0.0f, 0.0f, 1.0f, 1.0f, renderWidth * 1.5f);
				
				if(selection > 1)
				{
					selectedPlanet = planet;
					mouseHold = true;
					nothingSelected = false;
					scaleFactor = selectedPlanet.getName() == "sol" ? 1.5e-10 : (1.0 / selectedPlanet.getRadius()) * 0.75;
				}
			}
		}
		
		// Draw the rest of the GUI.
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, TEXTURE);
		boolean canSelectPlanet = PlanetList.hasOrbit(selectedPlanet);
		int button1X = x + 7;
		int button1Y = y + 39;
		boolean button1Hover = mouseX >= button1X && mouseX < button1X + 18 && mouseY >= button1Y && mouseY < button1Y + 18;
		drawTexture(matrices, button1X, button1Y, canSelectPlanet ? (button1Hover ? 36 : 18) : 0, 222, 18, 18);
		int button2X = x + 7;
		int button2Y = y + 59;
		boolean button2Hover = mouseX >= button2X && mouseX < button2X + 18 && mouseY >= button2Y && mouseY < button2Y + 18;
		drawTexture(matrices, button2X, button2Y, button2Hover ? 72 : 54, 222, 18, 18);
		int button3X = x + 7;
		int button3Y = y + 79;
		boolean button3Hover = mouseX >= button3X && mouseX < button3X + 18 && mouseY >= button3Y && mouseY < button3Y + 18;
		drawTexture(matrices, button3X, button3Y, button3Hover ? 108 : 90, 222, 18, 18);
		MutableText text = Text.translatable("planet.space." + selectedPlanet.getName());
		drawTextWithShadow(matrices, textRenderer, text, (int) x + 32, (int) y + 20, 0x55FF55);
		
		// Select planets in the GUI.
		if(nothingSelected && !mouseHold && mouseX >= x + 30 && mouseX < x + 167 && mouseY >= y + 18 && mouseY < y + 123 && selectedPlanet.getParent() != null)
		{
			selectedPlanet = selectedPlanet.getParent();
			scaleFactor = selectedPlanet.getName() == "sol" ? 1.5e-10 : (1.0 / selectedPlanet.getRadius()) * 0.75;
			mouseHold = true;
		}
		
		// Write the selected planet to a navigation card if the green button is pressed while ensuring a time delay before the next press.
		if(buttonCooldown > 0)
			buttonCooldown--;
		
		if(mousePressed)
		{
			if(button1Hover && canSelectPlanet && buttonCooldown == 0)
			{
				int planetId = PlanetList.getPlanets().indexOf(selectedPlanet);
				client.interactionManager.clickButton(((PlanetariumScreenHandler)handler).syncId, planetId);
				client.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.5F, 1.0f);
				buttonCooldown = 20;
			}
			else if(button2Hover && selectedPlanet.getRadius() * scaleFactor < 8.0)
				scaleFactor *= 1.025;
			else if(button3Hover)
				scaleFactor *= 0.975;
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}

	@Override
	protected void init()
	{
		super.init();
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}
	
	private void drawTexturedQuad(Matrix4f matrix, float x, float y, float u0, float v0, float u1, float v1, float size)
	{
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        float halfSize = size / 2.0f;
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, x - halfSize, y + halfSize, 10.0f).texture(u0, v1).next();
        bufferBuilder.vertex(matrix, x + halfSize, y + halfSize, 10.0f).texture(u1, v1).next();
        bufferBuilder.vertex(matrix, x + halfSize, y - halfSize, 10.0f).texture(u1, v0).next();
        bufferBuilder.vertex(matrix, x - halfSize, y - halfSize, 10.0f).texture(u0, v0).next();
        BufferRenderer.drawWithShader(bufferBuilder.end());
    }
	
	private void drawOrbitEllipse(Matrix4f matrix, float centerX, float centerY, float rMin, float rMax, float aop, int divisions)
	{
		float a = (rMin + rMax) / 2.0f;
		float ecc = (rMax - rMin) / (rMax + rMin);
		float p = a * (1.0f - (ecc * ecc));
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        for(float theta = 0.0f; theta < Math.PI * 2.0f; theta += (Math.PI * 2.0f) / divisions)
        {
        	float r1 =  p / (1.0f + (ecc * (float) Math.cos((double) theta)));
        	float x1 = centerX + (r1 * (float) Math.cos((double) theta + aop));
        	float y1 = centerY + (r1 * (float) Math.sin((double) theta + aop));
        	float r2 =  p / (1.0f + (ecc * (float) Math.cos((double) theta + ((Math.PI * 2.0f) / divisions))));
        	float x2 = centerX + (r2 * (float) Math.cos((double) theta + aop + ((Math.PI * 2.0f) / divisions)));
        	float y2 = centerY + (r2 * (float) Math.sin((double) theta + aop + ((Math.PI * 2.0f) / divisions)));
        	
        	if(inBounds(x1, y1) && inBounds(x2, y2))
        	{
        		bufferBuilder.vertex(matrix, x1, y1, 10.0f).color(0.8f, 0.4f, 1.0f, 0.6f).next();
        		bufferBuilder.vertex(matrix, x2, y2, 10.0f).color(0.8f, 0.4f, 1.0f, 0.6f).next();
        	}
        }
        
        BufferRenderer.drawWithShader(bufferBuilder.end());
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
		int startX = (width - backgroundWidth) / 2;
		int startY = (height - backgroundHeight) / 2;
		float xMin = startX + 30;
		float yMin = startY + 18;
		float xMax = startX + 167;
		float yMax = startY + 123;
		return x > xMin && x < xMax && y > yMin && y < yMax;
	}
}