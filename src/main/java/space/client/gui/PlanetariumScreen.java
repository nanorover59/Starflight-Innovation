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
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;
import space.StarflightMod;
import space.planet.Planet;
import space.planet.PlanetList;
import space.planet.PlanetRenderer;
import space.screen.PlanetariumScreenHandler;

@Environment(EnvType.CLIENT)
public class PlanetariumScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/gui/planetarium.png");
	private Planet selectedPlanet = null;
	private boolean mouseHold = false;
	private int buttonCooldown = 0;

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
		
		boolean mousePressed = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		MinecraftClient client = MinecraftClient.getInstance();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		int buttonX = x + 7;
		int buttonY = y + 39;
		boolean buttonHover = mouseX >= buttonX && mouseX < buttonX + 18 && mouseY >= buttonY && mouseY < buttonY + 18;
		drawTexture(matrices, buttonX, buttonY, buttonHover ? 18 : 0, 222, 18, 18);
		MutableText text = new TranslatableText("planet.space." + selectedPlanet.getName());
		drawTextWithShadow(matrices, textRenderer, text, (int) x + 32, (int) y + 20, 0x55FF55);
		
		double scaleFactor = selectedPlanet.getName() == "sol" ? 1.5e-10 : 1.0 / selectedPlanet.getRadius();
		double focusX = selectedPlanet.getPosition().getX() * scaleFactor;
		double focusY = selectedPlanet.getPosition().getZ() * scaleFactor;
		boolean nothingSelected = mousePressed;
		
		if(!mousePressed)
			mouseHold = false;
		
		// Render the displayed planets.
		@SuppressWarnings("unchecked")
		ArrayList<Planet> planetList = (ArrayList<Planet>) PlanetList.getPlanets().clone();
		
		for(Planet p : planetList)
		{
			if(p != selectedPlanet && p.getParent() != selectedPlanet)
				continue;
			
			int renderType = p.getName() == "sol" ? 1 : 0;
			float renderWidth = Math.max(4.0F, (float) (p.getRadius() * scaleFactor * 16.0));
			float px = (float) ((p.getPosition().getX() * scaleFactor) + x + 99.0 - focusX);
			float py = (float) ((p.getPosition().getZ() * scaleFactor) + y + 71.0 - focusY);
			int selection = planetSelect(client, px, py, renderWidth, mouseX, mouseY, mousePressed);
			RenderSystem.setShaderTexture(0, renderType == 1 ? new Identifier(StarflightMod.MOD_ID, "textures/environment/sun_0.png") : PlanetRenderer.getTexture(p.getName()));
			drawTexturedQuad(matrices.peek().getPositionMatrix(), px - (renderWidth / 2.0F), py - (renderWidth / 2.0F), 0.0F, 0.0F, renderType == 1 ? 1.0F : (1.0F / 16.0F), 1.0F, renderWidth);
			
			if(mouseHold || p == selectedPlanet)
				continue;
			
			if(selection > 0)
			{
				RenderSystem.setShaderTexture(0, new Identifier(StarflightMod.MOD_ID, "textures/gui/planet_selection.png"));
				drawTexturedQuad(matrices.peek().getPositionMatrix(), px - (renderWidth / 2.0F), py - (renderWidth / 2.0F), 0.0F, 0.0F, 1.0F, 1.0F, renderWidth);
				
				if(selection > 1)
				{
					selectedPlanet = p;
					mouseHold = true;
					nothingSelected = false;
				}
			}
		}
		
		// Select planets in the GUI.
		if(nothingSelected && !mouseHold && mouseX >= x + 30 && mouseX < x + 167 && mouseY >= y + 18 && mouseY < y + 123 && selectedPlanet.getParent() != null)
		{
			selectedPlanet = selectedPlanet.getParent();
			mouseHold = true;
		}
		
		// Write the selected planet to a navigation card if the green button is pressed while ensuring a time delay before the next press.
		if(buttonCooldown > 0)
			buttonCooldown--;
		
		if(buttonHover && buttonCooldown == 0 && mousePressed)
		{
			int planetId = PlanetList.getPlanets().indexOf(selectedPlanet);
			client.interactionManager.clickButton(((PlanetariumScreenHandler)handler).syncId, planetId);
			client.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.5F, 1.0F);
			buttonCooldown = 20;
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
        bufferBuilder.vertex(matrix, x, y + size, 10.0F).texture(u0, v0).next();
        bufferBuilder.vertex(matrix, x + size, y + size, 10.0F).texture(u1, v0).next();
        bufferBuilder.vertex(matrix, x + size, y, 10.0F).texture(u1, v1).next();
        bufferBuilder.vertex(matrix, x, y, 10.0F).texture(u0, v1).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }
	
	private int planetSelect(MinecraftClient client, float centerX, float centerY, float radius, int mouseX, int mouseY, boolean mousePressed)
	{
		Vec2f mousePosition = new Vec2f(mouseX, mouseY);
		
		if(mousePosition.distanceSquared(new Vec2f(centerX, centerY)) < radius * radius)
			return mousePressed ? 2 : 1;
		
		return 0;
	}
}