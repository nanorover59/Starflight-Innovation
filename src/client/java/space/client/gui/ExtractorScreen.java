package space.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.screen.ExtractorScreenHandler;

@Environment(EnvType.CLIENT)
public class ExtractorScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/gui/extractor.png");

	public ExtractorScreen(ScreenHandler handler, PlayerInventory inventory, Text title)
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

	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}

	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		int i = this.x;
		int j = this.y;
		int l;
		context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
		l = ((ExtractorScreenHandler) this.handler).getCharge();
		context.drawTexture(TEXTURE, i + 35, j - l + 50, 176, 14 - l, 14, l);
		l = ((ExtractorScreenHandler) this.handler).getCookProgress();
		context.drawTexture(TEXTURE, i + 79, j + 34, 176, 14, l + 1, 16);
	}
}