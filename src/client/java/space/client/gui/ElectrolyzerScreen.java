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
import space.screen.ElectrolyzerScreenHandler;
import space.util.FluidResourceType;

@Environment(EnvType.CLIENT)
public class ElectrolyzerScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/gui/electrolyzer.png");
	
	public ElectrolyzerScreen(ScreenHandler handler, PlayerInventory inventory, Text title)
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
		MachineScreenIcons.renderEnergy(context, textRenderer, 27, 36, this.x, this.y, mouseX, mouseY, ((ElectrolyzerScreenHandler) this.handler).getCharge(), ((EnergyBlock) StarflightBlocks.ELECTROLYZER).getEnergyCapacity());
		MachineScreenIcons.renderFluid(context, textRenderer, "water", 54, 20, this.x, this.y, mouseX, mouseY, ((ElectrolyzerScreenHandler) this.handler).getWater(), FluidResourceType.WATER.getStorageDensity(), 0xFF3F76E4);
		MachineScreenIcons.renderFluid(context, textRenderer, "oxygen", 110, 20, this.x, this.y, mouseX, mouseY, ((ElectrolyzerScreenHandler) this.handler).getOxygen(), ((ElectrolyzerScreenHandler) this.handler).getOxygenCapacity(), 0xFFC0FFEE);
		MachineScreenIcons.renderFluid(context, textRenderer, "hydrogen", 134, 20, this.x, this.y, mouseX, mouseY, ((ElectrolyzerScreenHandler) this.handler).getHydrogen(), ((ElectrolyzerScreenHandler) this.handler).getHydrogenCapacity(), 0xFFC0FFEE);
	}
}