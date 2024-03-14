package space.client.gui;

import java.util.ArrayList;
import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import space.block.StarflightBlocks;
import space.item.StarflightItems;

@Environment(EnvType.CLIENT)
public class GuideBookScreen extends Screen
{
	public static final Identifier BOOK_TEXTURE = new Identifier("space:textures/gui/guide/guide_book_screen.png");
	private static final ItemStack[] ALUMINUM = {new ItemStack(StarflightBlocks.BAUXITE_ORE), new ItemStack(StarflightItems.BAUXITE), new ItemStack(StarflightItems.ALUMINUM_INGOT), new ItemStack(StarflightBlocks.ALUMINUM_BLOCK), new ItemStack(StarflightBlocks.ALUMINUM_FRAME), new ItemStack(StarflightBlocks.STRUCTURAL_ALUMINUM)};
	private static final ItemStack[] SULFUR = {new ItemStack(StarflightBlocks.SULFUR_ORE), new ItemStack(StarflightItems.SULFUR), new ItemStack(StarflightBlocks.SULFUR_BLOCK)};
	private static final ItemStack[] RUBBER_SAP = {new ItemStack(StarflightBlocks.RUBBER_LOG), new ItemStack(StarflightBlocks.TREE_TAP), new ItemStack(StarflightItems.RUBBER_SAP)};
	private static final ItemStack[] RUBBER = {new ItemStack(StarflightItems.RUBBER_RESIN), new ItemStack(StarflightItems.RUBBER), new ItemStack(StarflightBlocks.REINFORCED_FABRIC)};
	private static final ItemStack[] TITANIUM = {new ItemStack(StarflightBlocks.ILMENITE_ORE), new ItemStack(StarflightItems.ILMENITE), new ItemStack(StarflightItems.TITANIUM_INGOT), new ItemStack(StarflightBlocks.TITANIUM_BLOCK)};
	private static final ItemStack[] HEMATITE = {new ItemStack(StarflightBlocks.HEMATITE_ORE), new ItemStack(StarflightItems.HEMATITE), new ItemStack(StarflightBlocks.HEMATITE_BLOCK)};
	protected static final int WIDTH = 256;
    protected static final int HEIGHT = 256;
    protected static final int PAGE_LENGTH = 16;
    protected static final int PAGE_COUNT = 6;
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
		sectionList.add(new GuideBookSection("resources", 2).addItemIcons(0, ALUMINUM).addItemIcons(8, SULFUR).addItemIcons(16, RUBBER_SAP).addItemIcons(24, RUBBER).addItemIcons(32, TITANIUM).addItemIcons(48, HEMATITE));
		sectionList.add(new GuideBookSection("machines", 2).addImage(0, "machines").addImage(16, "solar").addImage(48, "breakers"));
		sectionList.add(new GuideBookSection("electrolysis", 2).addImage(0, "electrolysis").addImage(16, "fluid_tanks").addImage(48, "ice"));
		sectionList.add(new GuideBookSection("r_construction", 2).addImage(0, "rocket").addImage(16, "propulsion"));
		sectionList.add(new GuideBookSection("r_flight", 2).addImage(0, "rocket_flight").addImage(32, "keyboard"));
		sectionList.add(new GuideBookSection("survival", 2).addImage(0, "suit").addImage(16, "base").addImage(48, "orbit"));
		sectionIndex = -1;
		pageIndex = 0;
	}
	
	@Override
    protected void init()
	{
		int x = (this.width - WIDTH) / 2 - 28;
        int y = (this.height - HEIGHT) / 2 + 16;
        this.clearChildren();
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).dimensions(this.width / 2 - 100, y + 184, 200, 20).build());
		nextPageButton = this.addDrawableChild(new PageTurnWidget(x + 262, y + 158, true, button -> this.goToNextPage(), true));
		previousPageButton = this.addDrawableChild(new PageTurnWidget(x + 24, y + 158, false, button -> this.goToPreviousPage(), true));
		
		for(int i = 0; i < PAGE_COUNT; i++)
			buttonList.add(this.addDrawableChild(new GuideBookButtonWidget(x + 172, y + 14 + (12 * i), Text.translatable("guide_book.menu_" + i).formatted(Formatting.BOLD), this, i)));
		
		this.updatePage();
	}

	@Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
        this.renderBackground(context, mouseX, mouseY, delta);
        int x = (this.width - WIDTH) / 2 - 28;
        int y = (this.height - HEIGHT) / 2 + 16;
        context.drawTexture(BOOK_TEXTURE, x, y, 0, 0, WIDTH, 180, WIDTH, HEIGHT);
        context.drawTexture(BOOK_TEXTURE, x + 54, y, 0, 0, WIDTH, 180, -WIDTH, HEIGHT);
        
        if(sectionIndex >= 0 && sectionIndex < sectionList.size())
        {
	        GuideBookSection section = sectionList.get(sectionIndex);
	        
	        for(int i : textList.keySet())
	        	context.drawText(textRenderer, textList.get(i), (i % (PAGE_LENGTH * 2)) < PAGE_LENGTH ? (x + PAGE_LENGTH) : (x + 172), y + PAGE_LENGTH + (i % PAGE_LENGTH) * this.textRenderer.fontHeight, 0, false);
	        
	        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
	        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	        
	        for(int i : section.imageList.keySet())
			{
	        	if(i >= pageIndex * PAGE_LENGTH * 2 && (i - pageIndex * PAGE_LENGTH * 2) / (PAGE_LENGTH * 2) < pageIndex + 1)
	        		context.drawTexture(section.imageList.get(i), ((i % (PAGE_LENGTH * 2)) < PAGE_LENGTH ? (x + PAGE_LENGTH) : (x + 172)) + 14, y + PAGE_LENGTH + (i % PAGE_LENGTH) * this.textRenderer.fontHeight, 0, 0.0f, 0.0f, 96, 48, 96, 48);
			}
	        
	        for(int i : section.itemIconList.keySet())
			{
	        	if(i >= pageIndex * PAGE_LENGTH * 2 && (i - pageIndex * PAGE_LENGTH * 2) / (PAGE_LENGTH * 2) < pageIndex + 1)
	        	{
	        		ItemStack[] stacks = section.itemIconList.get(i);
	        		int iconWidth = 40;
	        		int inline = 0;
	        		
	        		for(ItemStack itemStack : stacks)
	        			context.drawItem(itemStack, ((i % (PAGE_LENGTH * 2)) < PAGE_LENGTH ? (x + PAGE_LENGTH) : (x + 172)) + ((inline++ * iconWidth) / 2), y + PAGE_LENGTH + (i % PAGE_LENGTH) * this.textRenderer.fontHeight);
	        	}
			}
        }
        else
        {
        	context.drawTexture(BOOK_TEXTURE, x + 32, y + 56, 0, 192, 96, 64, WIDTH, HEIGHT);
        }
        
        super.render(context, mouseX, mouseY, delta);
	}

	protected void goToPreviousPage()
	{
		if(pageIndex > 0)
			pageIndex--;
		else
			sectionIndex = -1;
		
		this.clearAndInit();
		updatePage();
	}

	protected void goToNextPage()
	{
		if(pageIndex < sectionList.get(sectionIndex).pageCount)
			pageIndex++;
		
		this.clearAndInit();
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
	class GuideBookSection
	{
		public HashMap<Integer, Identifier> imageList = new HashMap<Integer, Identifier>();
		public HashMap<Integer, ItemStack[]> itemIconList = new HashMap<Integer, ItemStack[]>();
		public String name;
		public int pageCount;
		
		public GuideBookSection(String name, int pageCount)
		{
			this.name = name;
			this.pageCount = pageCount;
		}
		
		public GuideBookSection addImage(int position, String image)
		{
			imageList.put(position, new Identifier("space:textures/gui/guide/" + image + ".png"));
			return this;
		}
		
		public GuideBookSection addItemIcons(int position, ItemStack ... stacks)
		{
			itemIconList.put(position, stacks);
			return this;
		}
	}
	
	@Environment(EnvType.CLIENT)
	class GuideBookButtonWidget extends PressableWidget
	{
		//public static final TooltipSupplier EMPTY = (button, matrices, mouseX, mouseY) -> {};
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
		}

		@Override
		public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta)
		{
			MinecraftClient minecraftClient = MinecraftClient.getInstance();
			TextRenderer textRenderer = minecraftClient.textRenderer;
			int j = this.isHovered() ? 0x229954 : 0x124a29;
			context.drawText(textRenderer, this.getMessage(), this.getX(), this.getY() + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0f) << 24, false);
		}
		
		@Override
		public void playDownSound(SoundManager soundManager)
		{
			soundManager.play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0f));
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder var1)
		{
		}
	}
}