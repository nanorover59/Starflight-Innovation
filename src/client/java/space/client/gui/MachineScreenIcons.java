package space.client.gui;

import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import space.StarflightMod;

@Environment(EnvType.CLIENT)
public class MachineScreenIcons
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/gui/machine_widgets.png");
	private final static Sprite WATER_SPRITE = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.WATER.getDefaultState()).getParticleSprite();
	
	public static void renderEnergy(DrawContext context, TextRenderer textRenderer, int x, int y, int originX, int originY, int mouseX, int mouseY, long energy, long capacity)
	{
		int height = (int) Math.ceil(((double) energy / (double) capacity) * 14.0);
		context.drawTexture(TEXTURE, originX + x, originY + y - height + 15, 0, 31 - height, 14, height);
		boolean hover = mouseX - originX > x - 1 && mouseX - originX < x + 14 && mouseY - originY > y - 1 && mouseY - originY < y + 16;
		
		if(hover)
		{
			int percent = (int) (((double) energy / (double) capacity) * 100.0);
			List<Text> text = List.of(Text.translatable("block.space.energy", energy), Text.translatable("block.space.capacity", capacity), Text.literal(percent + "%"));
			context.drawTexture(TEXTURE, originX + x, originY + y, 0, 32, 16, 16);
			context.drawTooltip(textRenderer, text, mouseX, mouseY);
		}
	}
	
	public static void renderEnergyBar(DrawContext context, TextRenderer textRenderer, int x, int y, int originX, int originY, int mouseX, int mouseY, long energy, long capacity)
	{
		int height = (int) Math.ceil(((double) energy / (double) capacity) * 48.0);
		context.drawTexture(TEXTURE, originX + x, originY + y - height + 48, 32, 96 - height, 10, height);
		boolean hover = mouseX - originX > x - 1 && mouseX - originX < x + 10 && mouseY - originY > y - 1 && mouseY - originY < y + 48;
		
		if(hover)
		{
			int percent = (int) (((double) energy / (double) capacity) * 100.0);
			List<Text> text = List.of(Text.translatable("block.space.energy", energy), Text.translatable("block.space.capacity", capacity), Text.literal(percent + "%"));
			context.drawTexture(TEXTURE, originX + x - 1, originY + y - 1, 48, 48, 10, 50);
			context.drawTooltip(textRenderer, text, mouseX, mouseY);
		}
	}
	
	public static void renderFluid(DrawContext context, TextRenderer textRenderer, String name, int x, int y, int originX, int originY, int mouseX, int mouseY, long amount, long capacity, int color)
	{
		RenderSystem.setShaderTexture(0, WATER_SPRITE.getAtlasId());
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
		
		double fillRatio = MathHelper.clamp((double) amount / (double) capacity, 0.0, 1.0);
		int height = (int) (48 * fillRatio);
	    
		for(int i = 0; i < height; i += 16)
		{
			int drawHeight = Math.min(16, height - i);
			float vSpan = WATER_SPRITE.getMaxV() - WATER_SPRITE.getMinV();
            float vStart = WATER_SPRITE.getMaxV() - vSpan * ((float) drawHeight / 16.0f);
    		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
    		bufferBuilder.vertex(matrix4f, originX + x, originY + y + 48 - i - drawHeight, 0.0f).texture(WATER_SPRITE.getMinU(), vStart).color(color);
    		bufferBuilder.vertex(matrix4f, originX + x, originY + y + 48 - i, 0.0f).texture(WATER_SPRITE.getMinU(), WATER_SPRITE.getMaxV()).color(color);
    		bufferBuilder.vertex(matrix4f, originX + x + 16, originY + y + 48 - i, 0.0f).texture(WATER_SPRITE.getMaxU(), WATER_SPRITE.getMaxV()).color(color);
    		bufferBuilder.vertex(matrix4f, originX + x + 16, originY + y + 48 - i - drawHeight, 0.0f).texture(WATER_SPRITE.getMaxU(), vStart).color(color);
    		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		}
		
		boolean hover = mouseX - originX > x - 1 && mouseX - originX < x + 18 && mouseY - originY > y - 1 && mouseY - originY < y + 50;
		
		if(hover)
		{
			int percent = (int) (((double) amount / (double) capacity) * 100.0);
			List<Text> text = List.of(Text.translatable("block.space." + name, amount), Text.translatable("block.space.capacity", capacity), Text.literal(percent + "%"));
			context.drawTexture(TEXTURE, originX + x - 1, originY + y - 1, 0, 48, 18, 50);
			context.drawTooltip(textRenderer, text, mouseX, mouseY);
		}
	}
	
	public static void renderProgress(DrawContext context, TextRenderer textRenderer, int x, int y, int originX, int originY, int mouseX, int mouseY, int progress)
	{
		context.drawTexture(TEXTURE, originX + x, originY + y, 0, 0, progress + 1, 16);
	}
}