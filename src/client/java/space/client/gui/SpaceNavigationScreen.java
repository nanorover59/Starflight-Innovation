package space.client.gui;

import java.util.ArrayList;
import java.util.Comparator;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
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
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import space.StarflightMod;
import space.client.render.PlanetRenderer;
import space.network.c2s.RocketTravelButtonC2SPacket;
import space.network.s2c.OpenNavigationScreenS2CPacket;
import space.planet.Planet;
import space.planet.PlanetList;

@Environment(EnvType.CLIENT)
public class SpaceNavigationScreen extends Screen
{
	private static final Identifier GUI_ELEMENTS = Identifier.of(StarflightMod.MOD_ID, "textures/gui/planet_map.png");
    private static final Identifier SELECTION_TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/gui/planet_selection.png");
    private static final int MARGIN = 12;
	private static final int ORBIT_LINES = 0xFF614566;
	private static final int ORBIT_LINES_HIGHLIGHT = 0xFF9652A3;
	private static Framebuffer orbitLinesBuffer;
	private boolean mouseHold = false;
	private double scaleFactor = 4.0e-10;
	private static double transferDV;
	private static double surfaceToOrbitDV;
	private static double orbitToSurfaceDV;
	private static boolean fromOrbit;
	private static boolean toOrbit;
    private double deltaV;
    private ArrayList<Planet> planetsToRender = new ArrayList<Planet>();
	private Planet selectedPlanet = null;
	private Planet calculationPlanetFrom = null;
	private Planet calculationPlanetTo = null;
	
	public SpaceNavigationScreen(double deltaV)
	{
		super(NarratorManager.EMPTY);
		this.deltaV = deltaV;
		transferDV = 0;
		surfaceToOrbitDV = 0;
		orbitToSurfaceDV = 0;
		fromOrbit = false;
		toOrbit = false;
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
		if(orbitLinesBuffer != null)
			orbitLinesBuffer.delete();
		
		int frameWidth = client.getWindow().getFramebufferWidth();
		int frameHeight = client.getWindow().getFramebufferHeight();
		int bufferWidth = 512;
		int bufferHeight = (int) (bufferWidth * ((float) frameHeight / (float) frameWidth));
		orbitLinesBuffer = new SimpleFramebuffer(bufferWidth, bufferHeight, false, MinecraftClient.IS_SYSTEM_MAC);
		
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
			selectPlanet(planetList, planetList.getByName("sol"));
		
		if(selectedPlanet == null)
			return;
		
		boolean mouseDown = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		MinecraftClient client = MinecraftClient.getInstance();
		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		int hoverPlanet = -1;
		
		if(!mouseDown)
			mouseHold = false;
		
		// Render the displayed planets.
		int x = (orbitLinesBuffer.textureWidth / 2) - 42;
		int y = (orbitLinesBuffer.textureHeight / 2) - 20;
		int sideButtonX = 4;
		int sideButtonY = 4;
		context.fill(0, 0, width, height, 0, 0xFF000000);
		orbitLinesBuffer.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		orbitLinesBuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
		orbitLinesBuffer.beginWrite(false);
		
		for(int i = 0; i < planetsToRender.size(); i++)
		{
			Planet planet = planetsToRender.get(i);
			int renderType = planet.getName().equals("sol") ? 1 : 0;
			float renderWidth = Math.max(planet == selectedPlanet ? 8.0f : 4.0f, (float) (planet.getRadius() * scaleFactor));
			float px = (float) (((planet.getPosition().getX() - selectedPlanet.getPosition().getX()) * scaleFactor) + x);
			float py = (float) (((planet.getPosition().getZ() - selectedPlanet.getPosition().getZ()) * scaleFactor) + y);
			
			// Skip planets too close to the center at scale.
			if(planet != selectedPlanet && Math.sqrt(Math.pow(px - x, 2.0) + Math.pow(py - y, 2.0)) < 8.0)
				continue;

			// Check if the mouse cursor is hovering over any planet or its side button.
			int selection = planetSelect(client, px, py, renderWidth, mouseX, mouseY, mouseDown);
			
			if(!planet.equals(selectedPlanet))
			{
				sideButtonY += 16;
				
				if(selection == 0 && mouseX > sideButtonX && mouseX < sideButtonX + 78 && mouseY > sideButtonY && mouseY < sideButtonY + 14)
					selection = mouseDown ? 2 : 1;
			}
			
			// Draw the planet's orbit in the GUI.
			drawOrbitEllipse(matrix, planet, (float) x, (float) y, 256, scaleFactor, selection > 0 ? ORBIT_LINES_HIGHLIGHT : ORBIT_LINES);
			
			// Draw the planet in the GUI.
			RenderSystem.setShaderTexture(0, renderType == 1 ? Identifier.of(StarflightMod.MOD_ID, "textures/environment/sol.png") : PlanetRenderer.getTexture(planet.getName()));

			if(renderType == 1)
			{
				drawGlow(matrix, px, py, 32.0f, 1.0f, 1.0f, 1.0f);
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
				hoverPlanet = i;

				if(selection > 1)
					selectPlanet(planetList, planet);
			}
		}
		
		orbitLinesBuffer.endWrite();
	    client.getFramebuffer().beginWrite(false);
	    RenderSystem.enableBlend();
	    RenderSystem.defaultBlendFunc();
	    orbitLinesBuffer.draw(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight(), false);
	    RenderSystem.disableBlend();
		x = MARGIN;
		y = MARGIN;
        
        // Planet Selection Buttons
        sideButtonY = 4;
        Text selectedPlanetName = Text.translatable("planet.space." + selectedPlanet.getName());
        context.drawTexture(GUI_ELEMENTS, (width / 2) - 45, 4, 0, 48, 90, 16);
        context.drawText(textRenderer, selectedPlanetName, (width / 2) - textRenderer.getWidth(selectedPlanetName) / 2, 8, Colors.WHITE, false);
        
        for(int i = 0; i < planetsToRender.size(); i++)
		{
			Planet planet = planetsToRender.get(i);
			
			if(planet.equals(selectedPlanet))
				continue;
			
			sideButtonY += 16;
			context.drawTexture(GUI_ELEMENTS, sideButtonX, sideButtonY, 0, hoverPlanet == i ? 14 : 0, 78, 14);
			context.drawText(textRenderer, Text.translatable("planet.space." + planet.getName()), sideButtonX + 4, sideButtonY + 3, Colors.WHITE, false);
        }
        
        boolean backHover = mouseX > sideButtonX && mouseX < sideButtonX + 78 && mouseY > 4 && mouseY < 18;
        context.drawTexture(GUI_ELEMENTS, sideButtonX, 4, 0, selectedPlanet.getParent() == null ? 28 : (backHover ? 14 : 0), 78, 14);
		context.drawText(textRenderer, Text.literal("..."), sideButtonX + 4, 7, Colors.WHITE, false);
        
		if(backHover && mouseDown && !mouseHold && selectedPlanet.getParent() != null)
			selectPlanet(planetList, selectedPlanet.getParent());
		
		int rightSideX = width - 82;
		
        if(deltaV < 0.0)
        {
        	boolean departureHover = mouseX > rightSideX && mouseX < rightSideX + 78 && mouseY > 4 && mouseY < 36;
        	boolean arrivalHover = mouseX > rightSideX && mouseX < rightSideX + 78 && mouseY > 38 && mouseY < 70;
        	context.drawTexture(GUI_ELEMENTS, rightSideX, 4, 96, departureHover ? 32 : 0, 78, 32);
        	context.drawTexture(GUI_ELEMENTS, rightSideX, 38, 96, arrivalHover ? 32 : 0, 78, 32);
        	context.drawTexture(GUI_ELEMENTS, rightSideX, 78, 96, 64, 78, 32);
        	context.drawText(textRenderer, Text.translatable("space.navigation.departure"), rightSideX + 4, 8, Colors.WHITE, false);
        	context.drawText(textRenderer, Text.translatable("space.navigation.arrival"), rightSideX + 4, 42, Colors.WHITE, false);
        	context.drawText(textRenderer, Text.translatable("space.navigation.deltav"), rightSideX + 4, 82, Colors.WHITE, false);
        	
        	if(calculationPlanetFrom != null)
        	{
        		context.drawText(textRenderer, Text.translatable("planet.space." + calculationPlanetFrom.getName()), rightSideX + 4, 17, Colors.WHITE, false);
        		context.drawText(textRenderer, Text.translatable("space.navigation." + (fromOrbit ? "orbit" : "surface")), rightSideX + 4, 26, Colors.LIGHT_GRAY, false);
        	}
        	
        	if(calculationPlanetTo != null)
        	{
        		context.drawText(textRenderer, Text.translatable("planet.space." + calculationPlanetTo.getName()), rightSideX + 4, 51, Colors.WHITE, false);
        		context.drawText(textRenderer, Text.translatable("space.navigation." + (toOrbit ? "orbit" : "surface")), rightSideX + 4, 60, Colors.LIGHT_GRAY, false);
        	}
        	
        	double totalDV = (fromOrbit ? 0 : surfaceToOrbitDV) + (toOrbit ? 0 : orbitToSurfaceDV) + transferDV;
        	
        	if(calculationPlanetFrom != null && calculationPlanetTo != null)
        		context.drawText(textRenderer, Text.literal(Math.round(totalDV) + "m/s"), rightSideX + 4, 92, Colors.WHITE, false);
        	
        	if(departureHover && mouseDown && !mouseHold)
            {
        		if(calculationPlanetFrom == selectedPlanet)
        			fromOrbit = !fromOrbit;
        		else
        			fromOrbit = false;
        		
            	calculationPlanetFrom = selectedPlanet;
            	surfaceToOrbitDV = calculationPlanetFrom.dVSurfaceToOrbit();
            	
            	if(calculationPlanetFrom != null && calculationPlanetTo != null)
            	{
            		transferDV = calculationPlanetFrom.dVToPlanet(calculationPlanetTo);
            		client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
            	}
            	
            	mouseHold = true;
            }
        	
        	if(arrivalHover && mouseDown && !mouseHold)
            {
        		if(calculationPlanetTo == selectedPlanet)
        			toOrbit = !toOrbit;
        		else
        			toOrbit = false;
        		
            	calculationPlanetTo = selectedPlanet;
            	orbitToSurfaceDV = calculationPlanetTo.dVOrbitToSurface();
            	
            	if(calculationPlanetFrom != null && calculationPlanetTo != null)
            	{
            		transferDV = calculationPlanetFrom.dVToPlanet(calculationPlanetTo);
            		client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
            	}
            	
            	mouseHold = true;
            }
        }
        else
        {
			Planet viewpointPlanet = planetList.getViewpointPlanet();
			boolean fromOrbit = planetList.getViewpointDimensionData().isOrbit();
			boolean originPlanet = selectedPlanet.equals(viewpointPlanet);
			int travelButtonY = 4;
			double dVSurfaceToOrbit = planetList.getViewpointDimensionData().isSky() ? viewpointPlanet.dVSkyToOrbit() : viewpointPlanet.dVSurfaceToOrbit();
        	
        	if(selectedPlanet.getSurface() != null)
        	{
        		boolean surfaceHover = mouseX > rightSideX && mouseX < rightSideX + 78 && mouseY > travelButtonY && mouseY < (travelButtonY + 32);
            	double actionDV = viewpointPlanet.dVToPlanet(selectedPlanet) + selectedPlanet.dVOrbitToSurface();
            	
            	if(!fromOrbit)
            		actionDV += dVSurfaceToOrbit;
            	
            	if(!fromOrbit && originPlanet)
            		actionDV = 0;
            	
            	context.drawTexture(GUI_ELEMENTS, rightSideX, travelButtonY, 96, actionDV > deltaV ? 64 : (surfaceHover ? 32 : 0), 78, 32);
            	context.drawText(textRenderer, Text.translatable("space.navigation.surface"), rightSideX + 4, travelButtonY + 4, deltaV < actionDV ? Colors.RED : Colors.WHITE, false);
        		context.drawText(textRenderer, Text.literal(Math.round(actionDV) + "m/s"), rightSideX + 4, travelButtonY + 13, deltaV < actionDV ? Colors.RED : Colors.WHITE, false);
        		
        		if(deltaV >= actionDV && surfaceHover && mouseDown && !mouseHold)
                {
            		ClientPlayNetworking.send(new RocketTravelButtonC2SPacket(selectedPlanet.getName(), actionDV, true));
            		client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
            		mouseHold = true;
            		close();
                }
        	}
        	
        	if(selectedPlanet.getOrbit() != null)
        	{
        		travelButtonY += 40;
        		boolean orbitHover = mouseX > rightSideX && mouseX < rightSideX + 78 && mouseY > travelButtonY && mouseY < (travelButtonY + 32);
        		double actionDV = viewpointPlanet.dVToPlanet(selectedPlanet);
        		
        		if(!fromOrbit)
            		actionDV += dVSurfaceToOrbit;
        		
        		if(fromOrbit && originPlanet)
            		actionDV = 0;
            	
            	context.drawTexture(GUI_ELEMENTS, rightSideX, travelButtonY, 96, actionDV > deltaV ? 64 : (orbitHover ? 32 : 0), 78, 32);
        		context.drawText(textRenderer, Text.translatable("space.navigation.orbit"), rightSideX + 4, travelButtonY + 4, deltaV < actionDV ? Colors.RED : Colors.WHITE, false);
        		context.drawText(textRenderer, Text.literal(Math.round(actionDV) + "m/s"), rightSideX + 4, travelButtonY + 13, deltaV < actionDV ? Colors.RED : Colors.WHITE, false);
        		
        		if(deltaV >= actionDV && orbitHover && mouseDown && !mouseHold)
                {
            		ClientPlayNetworking.send(new RocketTravelButtonC2SPacket(selectedPlanet.getName(), actionDV, false));
            		client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
            		mouseHold = true;
            		close();
                }
        	}
        }
	}
	
	private void selectPlanet(PlanetList planetList, Planet planet)
	{
		if(planet == null)
			return;
		
		selectedPlanet = planet;
		scaleFactor = getInitialScaleFactor(selectedPlanet);
		mouseHold = true;
		planetsToRender.clear();
		planetsToRender.add(selectedPlanet);
		planetsToRender.addAll(selectedPlanet.getSatellites());
		planetsToRender.sort(Comparator.comparing(Planet::getPeriapsis));
		client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
		
		if(deltaV >= 0.0)
			transferDV = planetList.getViewpointPlanet().dVToPlanet(selectedPlanet);
	}
	
	private void drawTexturedQuad(Matrix4f matrix, float x, float y, float u0, float v0, float u1, float v1, float size)
	{
		Tessellator tessellator = Tessellator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, x - size, y + size, 0.0f).texture(u0, v1);
        bufferBuilder.vertex(matrix, x + size, y + size, 0.0f).texture(u1, v1);
        bufferBuilder.vertex(matrix, x + size, y - size, 0.0f).texture(u1, v0);
        bufferBuilder.vertex(matrix, x - size, y - size, 0.0f).texture(u0, v0);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end()); 
    }
	
	private void drawOrbitEllipse(Matrix4f matrix, Planet planet, float centerX, float centerY, int divisions, double scale, int colorRGB)
	{
		Tessellator tessellator = Tessellator.getInstance();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        int count = 0;
        
        for(int i = 0; i <= divisions; i++)
        {
        	float theta = (float) i * (float) (Math.PI * 2.0) / divisions;
        	Vec3d r1 = planet.getRelativePositionAtTrueAnomaly(theta);
        	float x1 = (float) (r1.getX() * scale) + centerX;
        	float y1 = (float) (r1.getZ() * scale) + centerY;
        	Vec3d r2 = planet.getRelativePositionAtTrueAnomaly(theta + ((Math.PI * 2.0f) / divisions));
        	float x2 = (float) (r2.getX() * scale) + centerX;
        	float y2 = (float) (r2.getZ() * scale) + centerY;
        	
        	if(inBounds(x1, y1) && inBounds(x2, y2))
        	{
        		bufferBuilder.vertex(matrix, x1, y1, -100.0f).color(colorRGB);
        		bufferBuilder.vertex(matrix, x2, y2, -100.0f).color(colorRGB);
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
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, x, y, 1.0f).color(r, g, b, 10.0f);

		for(int i = 0; i <= 16; i++)
		{
			float theta = (float) i * (float) (Math.PI * 2.0) / 16.0f;
			float sinTheta = MathHelper.sin(theta);
			float cosTheta = MathHelper.cos(theta);
			float radius = size;
			
			if(i % 4 == 0)
				radius *= 1.15f; 
			
			bufferBuilder.vertex(matrix, x + (radius * cosTheta), y - (radius * sinTheta), 10.0f).color(r, g, b, 0.0f);
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
			return 4.0e-10;
		else
		{
			double maxDistance = selectedPlanet.getRadius();
			
			for(Planet satellite : selectedPlanet.getSatellites())
			{
				if(satellite.getApoapsis() > maxDistance)
					maxDistance = satellite.getApoapsis();
			}
			
			return (1.0 / maxDistance) * 80.0;
		}
	}
	
	public static void receiveOpenNavigationScreen(OpenNavigationScreenS2CPacket payload, ClientPlayNetworking.Context context)
	{
		double deltaV = payload.deltaV();
		context.client().execute(() -> context.client().setScreen(new SpaceNavigationScreen(deltaV)));
	}
}