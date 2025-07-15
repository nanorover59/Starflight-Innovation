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
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.screen.StirlingEngineScreenHandler;

@Environment(EnvType.CLIENT)
public class StirlingEngineScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/gui/stirling_engine.png");

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
		context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
		int l;

		if(((StirlingEngineScreenHandler) this.handler).isBurning())
		{
			l = ((StirlingEngineScreenHandler) this.handler).getFuelProgress();
			context.drawTexture(TEXTURE, i + 80, j + 48 - l, 176, 12 - l, 14, l + 1);
		}
		
		MachineScreenIcons.renderEnergyBar(context, textRenderer, 104, 19, this.x, this.y, mouseX, mouseY, ((StirlingEngineScreenHandler) this.handler).getCharge(), ((EnergyBlock) StarflightBlocks.STIRLING_ENGINE).getEnergyCapacity());
	}
}