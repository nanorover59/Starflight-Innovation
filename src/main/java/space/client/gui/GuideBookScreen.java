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
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class GuideBookScreen extends Screen
{
	public static final Identifier BOOK_TEXTURE = new Identifier("space:textures/gui/guide/guide_book_screen.png");
	protected static final int WIDTH = 256;
    protected static final int HEIGHT = 256;
    protected static final int PAGE_LENGTH = 16;
    private ArrayList<GuideBookSection> sectionList = new ArrayList<GuideBookSection>();
    private ArrayList<GuideBookButtonWidget> buttonList = new ArrayList<GuideBookButtonWidget>();
    public HashMap<Integer, Text> textList = new HashMap<Integer, Text>();
    private PageTurnWidget nextPageButton;
    private PageTurnWidget previousPageButton;
    private int sectionIndex;
	private int pageIndex;
	
	public GuideBookScreen()
	{
		super(NarratorManager.EMPTY);
		sectionList.add(new GuideBookSection("machines", 2).addImage("machines", 0).addImage("solar", 16));
		sectionList.add(new GuideBookSection("electrolysis", 2).addImage("electrolysis", 0).addImage("fluid_tanks", 16));
		sectionList.add(new GuideBookSection("r_construction", 2).addImage("rocket", 0).addImage("propulsion", 16));
		sectionList.add(new GuideBookSection("r_flight", 2));
		sectionIndex = -1;
		pageIndex = 0;
	}
	
	@Override
    protected void init()
	{
		int x = (this.width - WIDTH) / 2 - 28;
        int y = (this.height - HEIGHT) / 2 + 16;
        this.clearChildren();
		this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, y + 184, 200, 20, ScreenTexts.DONE, button -> this.client.setScreen(null)));
		nextPageButton = this.addDrawableChild(new PageTurnWidget(x + 262, y + 158, true, button -> this.goToNextPage(), true));
		previousPageButton = this.addDrawableChild(new PageTurnWidget(x + 24, y + 158, false, button -> this.goToPreviousPage(), true));
		
		for(int i = 0; i < 4; i++)
			buttonList.add(this.addDrawableChild(new GuideBookButtonWidget(x + 172, y + 14 + (12 * i), Text.translatable("guide_book.menu_" + i).formatted(Formatting.BOLD), this, i)));
		
		updatePage();
	}

	@Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
        this.renderBackground(matrices);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BOOK_TEXTURE);
        int x = (this.width - WIDTH) / 2 - 28;
        int y = (this.height - HEIGHT) / 2 + 16;
        DrawableHelper.drawTexture(matrices, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
        DrawableHelper.drawTexture(matrices, x + 54, y, 0, 0, WIDTH, HEIGHT, -WIDTH, HEIGHT);
        
        if(sectionIndex >= 0 && sectionIndex < sectionList.size())
        {
	        GuideBookSection section = sectionList.get(sectionIndex);
	        
	        for(int i : textList.keySet())
	        	this.textRenderer.draw(matrices, textList.get(i), (i % (PAGE_LENGTH * 2)) < PAGE_LENGTH ? (x + PAGE_LENGTH) : (x + 172), y + PAGE_LENGTH + (i % PAGE_LENGTH) * this.textRenderer.fontHeight, 0);
	        
	        RenderSystem.setShader(GameRenderer::getPositionTexShader);
	        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	        
	        for(int i : section.imageList.keySet())
			{
	        	if(i >= pageIndex * PAGE_LENGTH * 2 && (i - pageIndex * PAGE_LENGTH * 2) / (PAGE_LENGTH * 2) < pageIndex + 1)
	        	{
	        		RenderSystem.setShaderTexture(0, section.imageList.get(i));
	        		DrawableHelper.drawTexture(matrices, ((i % (PAGE_LENGTH * 2)) < PAGE_LENGTH ? (x + PAGE_LENGTH) : (x + 172)) + 14, y + PAGE_LENGTH + (i % PAGE_LENGTH) * this.textRenderer.fontHeight, 0, 0.0f, 0.0f, 96, 48, 96, 48);
	        	}
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
		
		updatePage();
	}

	protected void goToNextPage()
	{
		if(pageIndex < sectionList.get(sectionIndex).pageCount)
			pageIndex++;
		
		updatePage();
	}

	private void updatePage()
	{
		nextPageButton.visible = sectionIndex >= 0 && pageIndex < sectionList.get(sectionIndex).pageCount - 1;
		previousPageButton.visible = sectionIndex >= 0;
		
		for(GuideBookButtonWidget button : buttonList)
			button.visible = sectionIndex == -1;
		
		if(sectionIndex >= 0)
		{
			GuideBookSection section = sectionList.get(sectionIndex);
			textList.clear();
			
			for(int i = pageIndex * PAGE_LENGTH * 2; i < (pageIndex + 1) * PAGE_LENGTH * 2; i++)
			{
				String translationKey = "guide_book." + section.name + "_" + i;
				Text translatedText = Text.translatable(translationKey);
				
				if(!translatedText.getString().equals(translationKey))
					textList.put(i, translatedText);
			}
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static class GuideBookSection
	{
		public HashMap<Integer, Identifier> imageList = new HashMap<Integer, Identifier>();
		public String name;
		public int pageCount;
		
		public GuideBookSection(String name, int pageCount)
		{
			this.name = name;
			this.pageCount = pageCount;
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

		public GuideBookButtonWidget(int x, int y, Text message, GuideBookScreen screen, int section)
		{
			super(x, y, 128, 12, message);
			this.screen = screen;
			this.section = section;
			this.setMessage(message);
		}

		@Override
		public void onPress()
		{
			screen.sectionIndex = section;
			screen.updatePage();
			this.changeFocus(true);
		}

		@Override
		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta)
		{
			MinecraftClient minecraftClient = MinecraftClient.getInstance();
			TextRenderer textRenderer = minecraftClient.textRenderer;
			int j = this.isHovered() ? 0x229954 : 0x124a29;
			textRenderer.draw(matrices, this.getMessage(), this.x, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0f) << 24);
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