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
import space.screen.ElectricFurnaceScreenHandler;

@Environment(EnvType.CLIENT)
public class ElectricFurnaceScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/gui/electric_furnace.png");

	public ElectricFurnaceScreen(ScreenHandler handler, PlayerInventory inventory, Text title)
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
		context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
		MachineScreenIcons.renderEnergy(context, textRenderer, 57, 35, this.x, this.y, mouseX, mouseY, ((ElectricFurnaceScreenHandler) this.handler).getCharge(), ((EnergyBlock) StarflightBlocks.ELECTRIC_FURNACE).getEnergyCapacity());
		MachineScreenIcons.renderProgress(context, textRenderer, 79, 34, this.x, this.y, mouseX, mouseY, ((ElectricFurnaceScreenHandler) this.handler).getCookProgress());
	}
}