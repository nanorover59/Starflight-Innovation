package space.client.gui;

import java.text.DecimalFormat;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import space.StarflightMod;
import space.planet.ClientPlanet;
import space.planet.ClientPlanetList;
import space.screen.PlanetariumScreenHandler;

@Environment(EnvType.CLIENT)
public class PlanetariumScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier SELECTION_TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/gui/planet_selection.png");
	private static final Identifier TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/gui/planetarium.png");
	private ClientPlanet selectedPlanet = null;
	private boolean mouseHold = false;
	private boolean viewData = false;
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
			selectedPlanet = ClientPlanetList.getByName("sol");
		
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		boolean mousePressed = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		MinecraftClient client = MinecraftClient.getInstance();
		
		// Draw the background of the GUI.
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, TEXTURE);
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		
		ClientPlanet selectedPlanetRenderer = ClientPlanetList.getPlanets(false).get(ClientPlanetList.getPlanets(false).indexOf(selectedPlanet));
		double selectedPlanetX = selectedPlanetRenderer.getPosition().getX() * scaleFactor;
		double selectedPlanetY = selectedPlanetRenderer.getPosition().getZ() * scaleFactor;
		double focusX = selectedPlanetX;
		double focusY = selectedPlanetY;
		boolean nothingSelected = mousePressed;
		
		if(!mousePressed)
			mouseHold = false;
		
		// Render the displayed planets.
		
		if(!viewData)
		{
			for(int i = 0; i < ClientPlanetList.getPlanets(false).size(); i++)
			{
				//Planet planet = planetList.get(i);
				ClientPlanet planet = ClientPlanetList.getPlanets(false).get(i);
				
				if(planet != selectedPlanet && planet.parent != selectedPlanet)
					continue;
				
				int renderType = planet.getName().equals("sol") ? 1 : 0;
				float renderWidth = Math.max(2.5f, (float) (planet.radius * scaleFactor));
				float px = (float) ((planet.getPosition().getX() * scaleFactor) + x + 99.0 - focusX);
				float py = (float) ((planet.getPosition().getZ() * scaleFactor) + y + 71.0 - focusY);
				Matrix4f matrix = matrices.peek().getPositionMatrix();
				
				//drawGlow(matrix, px, py, 8.0f, 1.0f, 1.0f, 1.0f);
				
				if(!planet.unlocked || planet != selectedPlanet && Math.sqrt(Math.pow(px - (x + 99.0), 2.0) + Math.pow(py - (y + 71.0), 2.0)) < 4.0)
					continue;
				
				// Draw the planet's orbit in the GUI.
				drawOrbitEllipse(matrix, planet, (float) (x + 99.0), (float) (y + 71.0), 256);
				
				if(!inBounds(px, py))
					continue;
					
				// Draw the planet in the GUI.
				int selection = planetSelect(client, px, py, renderWidth, mouseX, mouseY, mousePressed);
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
		}
		
		// Draw the rest of the GUI.
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, TEXTURE);
		boolean canSelectPlanet = selectedPlanet.hasOrbit;
		int button1X = x + 7;
		int button1Y = y + 39;
		boolean button1Hover = mouseX >= button1X && mouseX < button1X + 18 && mouseY >= button1Y && mouseY < button1Y + 18;
		drawTexture(matrices, button1X, button1Y, canSelectPlanet ? (button1Hover ? 36 : 18) : 0, 222, 18, 18);
		int button2X = x + 7;
		int button2Y = y + 59;
		boolean button2Hover = mouseX >= button2X && mouseX < button2X + 18 && mouseY >= button2Y && mouseY < button2Y + 18;
		drawTexture(matrices, button2X, button2Y, canSelectPlanet ? (button2Hover ? 90 : 72) : 54, 222, 18, 18);
		int button3X = x + 7;
		int button3Y = y + 79;
		boolean button3Hover = mouseX >= button3X && mouseX < button3X + 18 && mouseY >= button3Y && mouseY < button3Y + 18;
		drawTexture(matrices, button3X, button3Y, button3Hover ? 126 : 108, 222, 18, 18);
		int button4X = x + 7;
		int button4Y = y + 99;
		boolean button4Hover = mouseX >= button4X && mouseX < button4X + 18 && mouseY >= button4Y && mouseY < button4Y + 18;
		drawTexture(matrices, button4X, button4Y, button4Hover ? 162 : 144, 222, 18, 18);
		MutableText text = Text.translatable("planet.space." + selectedPlanet.getName());
		drawTextWithShadow(matrices, textRenderer, text, (int) x + 32, (int) y + 20, 0x55FF55);
		//PlanetDimensionData currentPlanetData = PlanetList.getDimensionDataForWorld(client.world);
		
		if(viewData)
		{
			DecimalFormat df = new DecimalFormat("#.#");
			text = Text.translatable("block.space.rocket_controller.transfer").append(": " + df.format(selectedPlanet.dVTransfer) + "m/s");
			drawTextWithShadow(matrices, textRenderer, text, (int) x + 32, (int) y + 33, 0x55FF55);
			text = Text.translatable("block.space.deltav_to_orbit").append(df.format(selectedPlanet.dVOrbit) + "m/s");
			drawTextWithShadow(matrices, textRenderer, text, (int) x + 32, (int) y + 46, 0x55FF55);
			text = Text.translatable("block.space.deltav_to_surface").append(df.format(selectedPlanet.dVSurface) + "m/s");
			drawTextWithShadow(matrices, textRenderer, text, (int) x + 32, (int) y + 59, 0x55FF55);
		}
		
		// Select planets in the GUI.
		if(nothingSelected && !viewData && !mouseHold && mouseX >= x + 30 && mouseX < x + 167 && mouseY >= y + 18 && mouseY < y + 123 && selectedPlanet.parent != null)
		{
			selectedPlanet = selectedPlanet.parent;
			scaleFactor = getInitialScaleFactor(selectedPlanet);
			mouseHold = true;
		}
		
		if(mousePressed)
		{
			if(button1Hover && canSelectPlanet && !mouseHold)
			{
				int planetId = ClientPlanetList.getPlanets(false).indexOf(selectedPlanet);
				client.interactionManager.clickButton(((PlanetariumScreenHandler)handler).syncId, planetId);
				client.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.5F, 1.0f);
				mouseHold = true;
			}
			else if(button2Hover && canSelectPlanet && !mouseHold)
			{
				viewData = !viewData;
				mouseHold = true;
			}
			else if(button3Hover && selectedPlanet.radius * scaleFactor < 32.0)
				scaleFactor *= 1.025;
			else if(button4Hover)
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
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, x - size, y + size, 10.0f).texture(u0, v1).next();
        bufferBuilder.vertex(matrix, x + size, y + size, 10.0f).texture(u1, v1).next();
        bufferBuilder.vertex(matrix, x + size, y - size, 10.0f).texture(u1, v0).next();
        bufferBuilder.vertex(matrix, x - size, y - size, 10.0f).texture(u0, v0).next();
        BufferRenderer.drawWithShader(bufferBuilder.end());
    }
	
	private void drawOrbitEllipse(Matrix4f matrix, ClientPlanet planet, float centerX, float centerY, int divisions)
	{
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
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
        		bufferBuilder.vertex(matrix, x1, y1, 10.0f).color(0.8f, 0.4f, 1.0f, 0.8f).next();
        		bufferBuilder.vertex(matrix, x2, y2, 10.0f).color(0.8f, 0.4f, 1.0f, 0.8f).next();
        	}
        }
        
        BufferRenderer.drawWithShader(bufferBuilder.end());
	}
	
	private void drawGlow(Matrix4f matrix, float x, float y, float size, float r, float g, float b)
	{
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, x, y, 10.0f).color(r, g, b, 0.8f).next();

		for(int i = 0; i <= 16; i++)
		{
			float theta = (float) i * (float) (Math.PI * 2.0) / 16.0f;
			float sinTheta = MathHelper.sin(theta);
			float cosTheta = MathHelper.cos(theta);
			float radius = size;
			
			if(i % 4 == 0)
				radius *= 1.15f;
			
			bufferBuilder.vertex(matrix, x + (radius * cosTheta), y - (radius * sinTheta), 10.0f).color(r, g, b, 0.0f).next();
		}

		BufferRenderer.drawWithShader(bufferBuilder.end());
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
		int startX = (width - backgroundWidth) / 2;
		int startY = (height - backgroundHeight) / 2;
		float xMin = startX + 30;
		float yMin = startY + 18;
		float xMax = startX + 167;
		float yMax = startY + 123;
		return x > xMin && x < xMax && y > yMin && y < yMax;
	}
	
	private double getInitialScaleFactor(ClientPlanet planet)
	{
		if(selectedPlanet.parent == null)
			return 1.5e-10;
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
}