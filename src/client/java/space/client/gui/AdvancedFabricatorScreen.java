package space.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import space.StarflightMod;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.block.entity.AdvancedFabricatorBlockEntity;
import space.network.c2s.AdvancedFabricatorButtonC2SPacket;
import space.network.c2s.AdvancedFabricatorUnlockC2SPacket;
import space.recipe.AdvancedFabricatorRecipe;
import space.screen.AdvancedFabricatorScreenHandler;
import space.world.persistent.StarflightPlayerData;

@Environment(EnvType.CLIENT)
public class AdvancedFabricatorScreen extends HandledScreen<ScreenHandler>
{
	private static final Identifier BACKGROUND = Identifier.of(StarflightMod.MOD_ID, "textures/gui/advanced_fabricator.png");
	private static final Identifier WIDGETS = Identifier.of(StarflightMod.MOD_ID, "textures/gui/advanced_fabricator_widgets.png");
	private static final Identifier TILE = Identifier.of(StarflightMod.MOD_ID, "textures/block/structural_titanium.png");
	private List<RecipeWidget> recipes;
	private DynamicRegistryManager registryManager;
	private double originX;
	private double originY;
	private int minPanX = Integer.MAX_VALUE;
	private int minPanY = Integer.MAX_VALUE;
	private int maxPanX = Integer.MIN_VALUE;
	private int maxPanY = Integer.MIN_VALUE;
	private boolean initialized;
	private boolean movingWindow;
	private boolean mouseHold;

	public AdvancedFabricatorScreen(ScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
		this.backgroundWidth = 252;
		this.backgroundHeight = 231;
		
		World world = inventory.player.getWorld();
		registryManager = world.getRegistryManager();
		reloadRecipes(world);
	}

	public void init()
	{
		super.init();
		this.titleX = (this.backgroundWidth - this.textRenderer.getWidth((StringVisitable) this.title)) / 2;
	}
	
	public void reloadRecipes(World world)
	{
		recipes = new ArrayList<RecipeWidget>();
		List<RecipeEntry<AdvancedFabricatorRecipe>> recipeEntries = AdvancedFabricatorBlockEntity.listAllRecipes(world.getRecipeManager());
		
		for(RecipeEntry<AdvancedFabricatorRecipe> entry : recipeEntries)
		{
			StarflightPlayerData playerData = StarflightPlayerData.clientPlayerData;
			recipes.add(new RecipeWidget(world.getRegistryManager(), entry, playerData.unlockedRecipes.contains(entry.id().toString())));
			int i = entry.value().getX();
			int j = i + 28;
			int k = entry.value().getY();
			int l = k + 27;
			this.minPanX = Math.min(this.minPanX, i);
			this.maxPanX = Math.max(this.maxPanX, j);
			this.minPanY = Math.min(this.minPanY, k);
			this.maxPanY = Math.max(this.maxPanY, l);
		}
		
		for(RecipeWidget recipe : recipes)
			recipe.setParent(recipes, world);
	}

	public void handledScreenTick()
	{
		super.handledScreenTick();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		drawMenu(context, x + 9, y + 6, mouseX, mouseY);
		RenderSystem.enableBlend();
		context.drawTexture(BACKGROUND, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
		MachineScreenIcons.renderEnergy(context, textRenderer, 18, 125, this.x, this.y, mouseX, mouseY, ((AdvancedFabricatorScreenHandler) this.handler).getCharge(), ((EnergyBlock) StarflightBlocks.ADVANCED_FABRICATOR).getEnergyCapacity());
		MachineScreenIcons.renderProgress(context, textRenderer, 159, 125, this.x, this.y, mouseX, mouseY, ((AdvancedFabricatorScreenHandler) this.handler).getMachiningProgress());
	}
	
	private void drawMenu(DrawContext context, int x, int y, int mouseX, int mouseY)
	{
		if(!this.initialized)
		{
			this.originX = 0; //(double) (0 - (this.maxPanX + this.minPanX) / 2);
			this.originY = 0; //(double) (0 - (this.maxPanY + this.minPanY) / 2);
			this.initialized = true;
		}
		
		boolean mouseDown = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		
		if(!mouseDown)
			mouseHold = false;

		context.enableScissor(x, y, x + 234, y + 113);
		context.getMatrices().push();
		context.getMatrices().translate((float) x, (float) y, 0.0F);
		int i = MathHelper.floor(this.originX);
		int j = MathHelper.floor(this.originY);
		int k = i % 16;
		int l = j % 16;

		for (int m = -1; m <= 15; m++)
		{
			for (int n = -1; n <= 8; n++)
				context.drawTexture(TILE, k + 16 * m, l + 16 * n, 0.0F, 0.0F, 16, 16, 16, 16);
		}
		
		if(!drawSelected(context, mouseX, mouseY, mouseDown))
		{
			for(RecipeWidget recipeWidget : recipes)
				recipeWidget.renderLines(context, i, j, true);
			
			for(RecipeWidget recipeWidget : recipes)
				recipeWidget.renderLines(context, i, j, false);
			
			for(int index = 0; index < recipes.size(); index++)
			{
				RecipeWidget recipeWidget = recipes.get(index);
				recipeWidget.renderWidget(context, textRenderer, this.x, this.y, i, j, mouseX, mouseY);
				
				if(mouseDown && !mouseHold && (recipeWidget.unlocked || recipeWidget.parent == null || (recipeWidget.parent != null && recipeWidget.parent.unlocked)) && recipeWidget.hover(this.x, this.y, i, j, mouseX, mouseY))
				{
					ClientPlayNetworking.send(new AdvancedFabricatorButtonC2SPacket(index));
					mouseHold = true;
					client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
				}
			}
		}
		
		context.getMatrices().pop();
		context.disableScissor();
	}
	
	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
	}
	
	private boolean drawSelected(DrawContext context, int mouseX, int mouseY, boolean mouseDown)
	{
		int recipeIndex = ((AdvancedFabricatorScreenHandler) this.handler).getRecipeIndex();
		
		if(recipeIndex < 0)
			return false;
		
		RecipeWidget recipeWidget = recipes.get(recipeIndex);
		this.originX = 0;
		this.originY = 0;
		context.drawTexture(WIDGETS, 45, 2, 0, 24, 144, 20);
		context.drawTexture(WIDGETS, 85, 24, 144, 0, 64, 64);
		Text text = Text.translatable(recipeWidget.icon.getTranslationKey());
		context.drawCenteredTextWithShadow(textRenderer, text, 117, 8, Colors.WHITE);
		context.getMatrices().push();
		context.getMatrices().scale(3.0f, 3.0f, 3.0f);
		context.drawItemWithoutEntity(recipeWidget.recipe.getResult(registryManager), 31, 12);
		context.getMatrices().pop();
		
		if(recipeWidget.unlocked)
		{
			int ingredientX = 117 - (recipeWidget.recipe.getIngredients().size() * 10); //(mouseX > originX + 126 ? 2 : 28);
			int ingredientY = 92;
			context.drawTexture(WIDGETS, 45, 90, 0, 24, 144, 20);
			
			for(Ingredient ingredient : recipeWidget.recipe.getIngredients())
			{
				ItemStack stack = ingredient.getMatchingStacks()[0];
				context.drawItemWithoutEntity(stack, ingredientX, ingredientY);
				context.drawItemInSlot(textRenderer, stack, ingredientX, ingredientY, null);
				
				if(mouseX > ingredientX + this.x + 8 && mouseX < ingredientX + this.x + 25 && mouseY > ingredientY + this.y + 5 && mouseY < ingredientY + this.y + 21)
					context.drawItemTooltip(textRenderer, stack, mouseX - this.x - 9, mouseY - this.y - 6);
				
				ingredientX += 20;
			}
		}
		else
		{
			context.drawTexture(WIDGETS, 45, 90, 0, 44, 144, 20);
			Text scienceText = Text.translatable("block.space.advanced_fabricator.unlock", recipeWidget.recipe.getScience());
			context.drawCenteredTextWithShadow(textRenderer, scienceText, 117, 96, Colors.WHITE);
			
			if(mouseX > this.x + 53 && mouseX < this.x + 198 && mouseY > this.y + 95 && mouseY < this.y + 116)
			{
				context.drawTexture(WIDGETS, 45, 90, 0, 64, 144, 20);
				
				if(mouseDown && !mouseHold)
				{
					ClientPlayNetworking.send(new AdvancedFabricatorUnlockC2SPacket(recipeWidget.recipeEntry.id().toString(), recipeWidget.recipe.getScience()));
					recipeWidget.unlocked = true;
					mouseHold = true;
					client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
				}
			}
		}
		
		int backX = 4;
		int backY = 4;
		boolean backHover = mouseX > backX + this.x + 8 && mouseX < backX + this.x + 20 && mouseY > backY + this.y + 5 && mouseY < backY + this.y + 23;
		context.drawTexture(WIDGETS, 4, 4, backHover ? 219 : 208, 0, 11, 17);
		
		if(mouseDown && !mouseHold && backHover)
		{
			ClientPlayNetworking.send(new AdvancedFabricatorButtonC2SPacket(-1));
			mouseHold = true;
			client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
		}
		
		return true;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
	{
		if(button != 0)
		{
			this.movingWindow = false;
			return false;
		}
		else
		{
			if(!this.movingWindow)
				this.movingWindow = true;
			else if(isMouseInsideMenu(mouseX, mouseY))
				moveWindow(deltaX, deltaY);

			return true;
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
	{
		if(isMouseInsideMenu(mouseX, mouseY))
			moveWindow(horizontalAmount * 16.0, verticalAmount * 16.0);
		
		return true;
	}
	
	public void moveWindow(double offsetX, double offsetY)
	{
		if(this.maxPanX - this.minPanX > 234)
			this.originX = MathHelper.clamp(this.originX + offsetX, (double) -(this.maxPanX - 234), 0.0);

		if(this.maxPanY - this.minPanY > 113)
			this.originY = MathHelper.clamp(this.originY + offsetY, (double) -(this.maxPanY - 113), 0.0);
	}
	
	private boolean isMouseInsideMenu(double mouseX, double mouseY)
	{
		return mouseX > x + 8 && mouseX < x + 243 && mouseY > y + 5 && mouseY < y + 119;
	}
	
	private static class RecipeWidget
	{
		private RecipeEntry<AdvancedFabricatorRecipe> recipeEntry;
		private AdvancedFabricatorRecipe recipe;
		private ItemStack icon;
		private RecipeWidget parent;
		private boolean unlocked;
		
		public RecipeWidget(DynamicRegistryManager registryManager, RecipeEntry<AdvancedFabricatorRecipe> recipeEntry, boolean unlocked)
		{
			this.recipeEntry = recipeEntry;
			this.recipe = recipeEntry.value();
			this.icon = recipe.getResult(registryManager);
			this.unlocked = unlocked;
		}
		
		public void setParent(List<RecipeWidget> recipes, World world)
		{
			for(RecipeWidget recipeWidget : recipes)
			{
				if(recipeWidget.recipe.equals(recipe.getPrerequisiteRecipe(world)))
				{
					parent = recipeWidget;
					break;
				}
			}
		}
		
		public void renderLines(DrawContext context, int originX, int originY, boolean border)
		{
			if(this.parent == null)
				return;
			
			int widgetX = recipe.getX();
			int widgetY = recipe.getY();
			int parentX = parent.recipe.getX();
			int parentY = parent.recipe.getY();
			int i = originX + parentX + 13;
			int j = originX + parentX + (widgetX - parentX) / 2 + 13;
			int k = originY + parentY + 12;
			int l = originX + widgetX + 13;
			int m = originY + widgetY + 12;
			int n = border ? Colors.BLACK : (parent.unlocked ? Colors.WHITE : Colors.GRAY);
			
			if(border)
			{
				context.drawHorizontalLine(j, i, k - 1, n);
				context.drawHorizontalLine(j + 1, i, k, n);
				context.drawHorizontalLine(j, i, k + 1, n);
				context.drawHorizontalLine(l, j - 1, m - 1, n);
				context.drawHorizontalLine(l, j - 1, m, n);
				context.drawHorizontalLine(l, j - 1, m + 1, n);
				context.drawVerticalLine(j - 1, m, k, n);
				context.drawVerticalLine(j + 1, m, k, n);
			}
			else
			{
				context.drawHorizontalLine(j, i, k, n);
				context.drawHorizontalLine(l, j, m, n);
				context.drawVerticalLine(j, m, k, n);
			}
		}
		
		public void renderWidget(DrawContext context, TextRenderer textRenderer, int originX, int originY, int panX, int panY, int mouseX, int mouseY)
		{
			int widgetX = panX + recipe.getX();
			int widgetY = panY + recipe.getY();
			int u = 0;
			int v = 0;
			boolean insideMenu = mouseX > originX + 8 && mouseX < originX + 243 && mouseY > originY + 5 && mouseY < originY + 119;
			boolean hover = mouseX - originX > widgetX + 8 && mouseX - originX < widgetX + 33 && mouseY - originY > widgetY + 5 && mouseY - originY < widgetY + 30;
			boolean canUnlock = !(parent != null && !parent.unlocked);
			
			if(unlocked)
				u = 0;
			else if(canUnlock)
				u = 24;
			else
				u = 48;
			
			if(insideMenu && hover)
				context.drawItemTooltip(textRenderer, icon, mouseX - originX - 9, mouseY - originY - 6);
			
			context.drawTexture(WIDGETS, widgetX, widgetY, u, v, 24, 24);
			context.drawItemWithoutEntity(icon, widgetX + 4, widgetY + 4);
			
			if(insideMenu && hover && (unlocked || canUnlock))
				context.drawTexture(WIDGETS, widgetX, widgetY, 72, 0, 24, 24);
		}
		
		public boolean hover(int originX, int originY, int panX, int panY, int mouseX, int mouseY)
		{
			int widgetX = panX + recipe.getX();
			int widgetY = panY + recipe.getY();
			return mouseX - originX > widgetX + 8 && mouseX - originX < widgetX + 33 && mouseY - originY > widgetY + 5 && mouseY - originY < widgetY + 30;
		}
	}
}