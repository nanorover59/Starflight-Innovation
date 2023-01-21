package space.client.gui;

import java.util.ArrayList;
import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.TooltipSupplier;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class GuideBookScreen extends Screen
{
	public static final Identifier BOOK_TEXTURE = new Identifier("space:textures/gui/guide/guide_book_screen.png");
	protected static final int WIDTH = 256;
    protected static final int HEIGHT = 180;
    private ArrayList<GuideBookSection> sectionList = new ArrayList<GuideBookSection>();
    private ArrayList<GuideBookButtonWidget> buttonList = new ArrayList<GuideBookButtonWidget>();
    private PageTurnWidget nextPageButton;
    private PageTurnWidget previousPageButton;
    private int sectionIndex;
	private int pageIndex;
	
	public GuideBookScreen()
	{
		super(NarratorManager.EMPTY);
		sectionList.add(new GuideBookSection("machines", 2).addText(0, 1).addText(27, 28));
		sectionIndex = -1;
		pageIndex = 0;
	}
	
	@Override
    protected void init()
	{
		int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;
        this.clearChildren();
		this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, y + 184, 200, 20, ScreenTexts.DONE, button -> this.client.setScreen(null)));
		nextPageButton = this.addDrawableChild(new PageTurnWidget(x + 200, y + 154, true, button -> this.goToNextPage(), true));
		previousPageButton = this.addDrawableChild(new PageTurnWidget(x + 24, y + 154, false, button -> this.goToPreviousPage(), true));
		
		for(int i = 0; i < 4; i++)
			buttonList.add(this.addDrawableChild(new GuideBookButtonWidget(x + 38, y + 14 + (12 * i), 200, 12, Text.translatable("guide_book.menu_" + i), this, i)));
		
		updatePageButtons();
	}

	@Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
        this.renderBackground(matrices);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BOOK_TEXTURE);
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;
        this.drawTexture(matrices, x, y, 0, 0, WIDTH, HEIGHT);
        
        if(sectionIndex >= 0 && sectionIndex < sectionList.size())
        {
	        GuideBookSection section = sectionList.get(sectionIndex);
	        
	        for(int i : section.textList.keySet())
			{
	        	if(i >= pageIndex * 28.0 && (i - pageIndex * 28.0) / 28 < pageIndex + 1)
	        		this.textRenderer.draw(matrices, section.textList.get(i), (i % 28) < 14 ? (x + 14) : (x + 138), y + 14 + (i % 14) * this.textRenderer.fontHeight, 0);
			}
	        
	        //DrawableHelper.drawTexture(matrices, x, y, this.zOffset, u, v, width, height, 256, 256);
        }
        else
        {
        	
        }
        
        super.render(matrices, mouseX, mouseY, delta);
	}

	protected void goToPreviousPage()
	{
		if(pageIndex > 0)
			pageIndex--;
		else
			sectionIndex = -1;
		
		updatePageButtons();
	}

	protected void goToNextPage()
	{
		if(pageIndex < sectionList.get(sectionIndex).pageCount)
			pageIndex++;
		
		updatePageButtons();
	}

	private void updatePageButtons()
	{
		nextPageButton.visible = sectionIndex >= 0 && pageIndex < sectionList.get(sectionIndex).pageCount;
		previousPageButton.visible = pageIndex >= 0;
		
		for(GuideBookButtonWidget button : buttonList)
			button.visible = sectionIndex == -1;
	}
	
	@Environment(EnvType.CLIENT)
	public static class GuideBookSection
	{
		public HashMap<Integer, Text> textList = new HashMap<Integer, Text>();
		public HashMap<Integer, Identifier> imageList = new HashMap<Integer, Identifier>();
		public String name;
		public int pageCount;
		
		public GuideBookSection(String name, int pageCount)
		{
			this.name = name;
			this.pageCount = pageCount;
		}
		
		public GuideBookSection addText(int start, int end)
		{
			for(int i = start; i <= end; i++)
				textList.put(i, Text.translatable("guide_book." + name + "_" + i));
					
			return this;
		}
		
		public GuideBookSection addImage(String image, int position)
		{
			imageList.put(position, new Identifier("space:textures/gui/guide/" + image + ".png"));
			return this;
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static class GuideBookButtonWidget extends PressableWidget
	{
		public static final TooltipSupplier EMPTY = (button, matrices, mouseX, mouseY) -> {};
		private final GuideBookScreen screen;
		private final int section;

		public GuideBookButtonWidget(int x, int y, int width, int height, Text message, GuideBookScreen screen, int section)
		{
			super(x, y, width, height, message);
			this.screen = screen;
			this.section = section;
			this.setMessage(message);
		}

		@Override
		public void onPress()
		{
			screen.sectionIndex = section;
			screen.updatePageButtons();
			this.changeFocus(true);
		}

		@Override
		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta)
		{
			MinecraftClient minecraftClient = MinecraftClient.getInstance();
			TextRenderer textRenderer = minecraftClient.textRenderer;
			int j = this.isHovered() ? 0x229954 : 0x196F3D;
			textRenderer.draw(matrices, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0f) << 24);
		}
		
		@Override
		public void playDownSound(SoundManager soundManager)
		{
			soundManager.play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0f));
		}

		@Override
		public void appendNarrations(NarrationMessageBuilder var1)
		{
		}
	}
}