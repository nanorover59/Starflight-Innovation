package space.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.screen.StirlingEngineScreenHandler;

@Environment(EnvType.CLIENT)
public class StirlingEngineScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/gui/stirling_engine.png");

	public StirlingEngineScreen(ScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
	}

	public void init()
	{
		super.init();
		this.titleX = (this.backgroundWidth - this.textRenderer.getWidth((StringVisitable) this.title)) / 2;
	}

	public void handledScreenTick()
	{
		super.handledScreenTick();
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}

	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int i = this.x;
		int j = this.y;
		this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
		int l;

		if(((StirlingEngineScreenHandler) this.handler).isBurning())
		{
			l = ((StirlingEngineScreenHandler) this.handler).getFuelProgress();
			this.drawTexture(matrices, i + 80, j + 28 + 12 - l, 176, 12 - l, 14, l + 1);
		}
	}
}