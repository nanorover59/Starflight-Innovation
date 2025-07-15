package space.client.gui;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.screen.BatteryScreenHandler;

@Environment(EnvType.CLIENT)
public class BatteryScreen extends HandledScreen<BatteryScreenHandler>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/gui/battery.png");

	public BatteryScreen(BatteryScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
		//this.passEvents = false;
		//this.backgroundHeight = 133;
		//this.playerInventoryTitleY = this.backgroundHeight - 94;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		this.renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
		
		long energy = ((BatteryScreenHandler) this.handler).getCharge();
		long capacity = ((EnergyBlock) StarflightBlocks.BATTERY).getEnergyCapacity();
		int barX = 64;
		int barY = 38;
		int width = (int) Math.ceil(((double) energy / (double) capacity) * 48.0);
		context.drawTexture(TEXTURE, x + barX, y + barY, 176, 0, width, 16);
		boolean hover = mouseX - x > barX - 1 && mouseX - x < barX + 10 && mouseY - y > barY - 1 && mouseY - y < barY + 48;
		
		if(hover)
		{
			int percent = (int) (((double) energy / (double) capacity) * 100.0);
			List<Text> text = List.of(Text.translatable("block.space.energy", energy), Text.translatable("block.space.capacity", capacity), Text.literal(percent + "%"));
			context.drawTexture(TEXTURE, x + barX - 1, y + barY - 1, 176, 16, 66, 18);
			context.drawTooltip(textRenderer, text, mouseX, mouseY);
		}
	}
}