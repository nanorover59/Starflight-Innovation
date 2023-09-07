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
import space.screen.IceElectrolyzerScreenHandler;

@Environment(EnvType.CLIENT)
public class IceElectrolyzerScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/gui/ice_electrolyzer.png");

	public IceElectrolyzerScreen(ScreenHandler handler, PlayerInventory inventory, Text title)
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
		renderBackground(context);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}

	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		int i = this.x;
		int j = this.y;
		context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
		int l;

		if(((IceElectrolyzerScreenHandler) this.handler).isBurning())
			context.drawTexture(TEXTURE, i + 80, j + 52, 176, 0, 14, 14);

		l = (int) Math.floor(((IceElectrolyzerScreenHandler) this.handler).getProgress() * 29.0);
		context.drawTexture(TEXTURE, i + 63, j + 57 - l, 176, 42 - l, 12, l);
		context.drawTexture(TEXTURE, i + 99, j + 57 - l, 176, 42 - l, 12, l);
	}
}