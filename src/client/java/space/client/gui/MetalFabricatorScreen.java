package space.client.gui;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import space.StarflightMod;
import space.recipe.MetalFabricatorRecipe;
import space.screen.MetalFabricatorScreenHandler;

@Environment(EnvType.CLIENT)
public class MetalFabricatorScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/gui/metal_fabricator.png");
	private static final int GREEN = 0xFF6ABE30;
	private float scrollAmount;
	private boolean mouseClicked;
	private int scrollOffset;
	private boolean canCraft;

	public MetalFabricatorScreen(ScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
	}

	public void init()
	{
		super.init();
		this.titleX = (this.backgroundWidth - this.textRenderer.getWidth((StringVisitable) this.title)) / 2;
		this.titleY = 5;
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
		int l;
		context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
		l = ((MetalFabricatorScreenHandler) this.handler).getCharge();
		context.drawTexture(TEXTURE, x + 19, y - l + 71, 176, 29 - l, 14, l);
		l = ((MetalFabricatorScreenHandler) this.handler).getMachiningProgress();
		context.drawTexture(TEXTURE, x + 143, y - l + 44, 176, 41 - l, 16, l);
		int scrollY = (int) (41.0f * this.scrollAmount);
		context.drawTexture(TEXTURE, x + 119, y + 15 + scrollY, this.shouldScroll() ? 176 : 188, 0, 12, 15);
		this.canCraft = ((MetalFabricatorScreenHandler) this.handler).getMachiningProgress() == 0 && !((MetalFabricatorScreenHandler) this.handler).getSlot(3).hasStack();
		
		if(this.canCraft)
		{
			int recipesX = x + 52;
			int recipesY = y + 14;
			this.renderRecipeBackground(context, mouseX, mouseY, recipesX, recipesY, scrollOffset);
	        this.renderRecipeIcons(context, recipesX, recipesY, scrollOffset);
		}
		else
		{
			DecimalFormat df = new DecimalFormat("#");
			df.setRoundingMode(RoundingMode.DOWN);
			context.drawText(textRenderer, Text.literal(df.format(((MetalFabricatorScreenHandler) this.handler).getMachiningProgressPercent())).append("%"), x + 54, y + 16, GREEN, true);
		}
	}

	@Override
	protected void drawMouseoverTooltip(DrawContext context, int x, int y)
	{
		super.drawMouseoverTooltip(context, x, y);

		if(this.canCraft)
		{
			int i = this.x + 52;
			int j = this.y + 14;
			int k = this.scrollOffset + 12;
			List<RecipeEntry<MetalFabricatorRecipe>> list = ((MetalFabricatorScreenHandler) this.handler).getAvailableRecipes();

			for(int l = this.scrollOffset; l < k && l < ((MetalFabricatorScreenHandler) this.handler).getAvailableRecipeCount(); ++l)
			{
				int m = l - this.scrollOffset;
				int n = i + m % 4 * 16;
				int o = j + m / 4 * 18 + 2;
				
				if(x < n || x >= n + 16 || y < o || y >= o + 18)
					continue;
				
				context.drawItemTooltip(this.textRenderer, ((MetalFabricatorRecipe) ((RecipeEntry<?>) list.get(l)).value()).getResult(this.client.world.getRegistryManager()), x, y);
			}
		}
	}

	private void renderRecipeBackground(DrawContext context, int mouseX, int mouseY, int x, int y, int scrollOffset)
	{
		for(int i = this.scrollOffset; i < scrollOffset + 12 && i < ((MetalFabricatorScreenHandler) this.handler).getAvailableRecipeCount(); ++i)
		{
			int j = i - this.scrollOffset;
			int k = x + j % 4 * 16;
			int l = j / 4;
			int m = y + l * 18 + 2;
			int v = 166;
			
			if(i == ((MetalFabricatorScreenHandler) this.handler).getSelectedRecipe())
				v = 184;
			else if(mouseX >= k && mouseY >= m && mouseX < k + 16 && mouseY < m + 18)
				v = 202;
			
			context.drawTexture(TEXTURE, k, m - 1, 0, v, 16, 18);
		}
	}

	private void renderRecipeIcons(DrawContext context, int x, int y, int scrollOffset)
	{
		List<RecipeEntry<MetalFabricatorRecipe>> list = ((MetalFabricatorScreenHandler) this.handler).getAvailableRecipes();
		
		for(int i = this.scrollOffset; i < scrollOffset + 12 && i < ((MetalFabricatorScreenHandler) this.handler).getAvailableRecipeCount(); i++)
		{
			int j = i - this.scrollOffset;
			int k = x + j % 4 * 16;
			int l = j / 4;
			int m = y + l * 18 + 2;
			context.drawItem(((RecipeEntry<?>) list.get(i)).value().getResult(this.client.world.getRegistryManager()), k, m);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		this.mouseClicked = false;
		
		if(this.canCraft)
		{
			int i = this.x + 52;
			int j = this.y + 14;
			int k = this.scrollOffset + 12;

			for(int l = this.scrollOffset; l < k; l++)
			{
				int m = l - this.scrollOffset;
				double d = mouseX - (double) (i + m % 4 * 16);
				double e = mouseY - (double) (j + m / 4 * 18);

				if(!(d >= 0.0) || !(e >= 0.0) || !(d < 16.0) || !(e < 18.0) || !((MetalFabricatorScreenHandler) this.handler).onButtonClick((PlayerEntity) this.client.player, l))
					continue;

				MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0f));
				this.client.interactionManager.clickButton(((MetalFabricatorScreenHandler) this.handler).syncId, l);
				return true;
			}

			i = this.x + 119;
			j = this.y + 9;

			if(mouseX >= (double) i && mouseX < (double) (i + 12) && mouseY >= (double) j && mouseY < (double) (j + 54))
				this.mouseClicked = true;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
	{
		if(this.mouseClicked && this.shouldScroll())
		{
			int i = this.y + 14;
			int j = i + 54;
			this.scrollAmount = ((float) mouseY - (float) i - 7.5f) / ((float) (j - i) - 15.0f);
			this.scrollAmount = MathHelper.clamp((float) this.scrollAmount, (float) 0.0f, (float) 1.0f);
			this.scrollOffset = (int) ((double) (this.scrollAmount * (float) this.getMaxScroll()) + 0.5) * 4;
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
	{
		if(this.shouldScroll())
		{
			int i = this.getMaxScroll();
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp((float) (this.scrollAmount - f), (float) 0.0f, (float) 1.0f);
			this.scrollOffset = (int) ((double) (this.scrollAmount * (float) i) + 0.5) * 4;
		}

		return true;
	}

	private boolean shouldScroll()
	{
		return this.canCraft && ((MetalFabricatorScreenHandler) this.handler).getAvailableRecipeCount() > 12;
	}

	protected int getMaxScroll()
	{
		return (((MetalFabricatorScreenHandler) this.handler).getAvailableRecipeCount() + 4 - 1) / 4 - 3;
	}
}