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
import space.screen.ElectricCrafterScreenHandler;

@Environment(EnvType.CLIENT)
public class ElectricCrafterScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/gui/electric_crafter.png");

	public ElectricCrafterScreen(ScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
		this.backgroundHeight = 192;
		this.playerInventoryTitleY = 98;
	}

	@Override
	protected void init()
	{
		super.init();
		this.titleX = (this.backgroundWidth - this.textRenderer.getWidth((StringVisitable) this.title)) / 2;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		int i = this.x;
		int j = (this.height - this.backgroundHeight) / 2;
		context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
		MachineScreenIcons.renderEnergy(context, textRenderer, 9, 35, this.x, this.y, mouseX, mouseY, ((ElectricCrafterScreenHandler) this.handler).getCharge(), ((EnergyBlock) StarflightBlocks.ELECTRIC_CRAFTER).getEnergyCapacity());
		MachineScreenIcons.renderProgress(context, textRenderer, 90, 34, this.x, this.y, mouseX, mouseY, ((ElectricCrafterScreenHandler) this.handler).getCookProgress());
		context.drawItem(((ElectricCrafterScreenHandler) this.handler).getPreviewStack(), x + 93, y + 53);
		context.drawItemInSlot(textRenderer, ((ElectricCrafterScreenHandler) this.handler).getPreviewStack(), x + 93, y + 53, null);
	}
}