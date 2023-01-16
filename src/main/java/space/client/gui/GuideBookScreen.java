package space.client.gui;

import java.util.ArrayList;
import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class GuideBookScreen extends Screen
{
	public static final Identifier BOOK_TEXTURE = new Identifier("space:textures/gui/guide/guide_book_screen.png");
	protected static final int WIDTH = 256;
    protected static final int HEIGHT = 180;
    private ArrayList<GuideBookPage> pageList = new ArrayList<GuideBookPage>();
	private int pageIndex;
	
	public GuideBookScreen()
	{
		super(NarratorManager.EMPTY);
		pageList.add(new GuideBookPage().addText("machines_0_0", 0).addText("machines_0_1", 1));
		pageIndex = 0;
	}
	
	@Override
    protected void init()
	{
		
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
        
        if(pageIndex >= 0 && pageIndex < pageList.size())
        {
	        GuideBookPage page = pageList.get(pageIndex);
	        
	        for(int i : page.textList.keySet())
			{
		        boolean lhs = i < 14;
		        int textY = y + (lhs ? this.textRenderer.fontHeight * i : (this.textRenderer.fontHeight * i) - this.textRenderer.fontHeight * 13);
				this.textRenderer.draw(matrices, page.textList.get(i), x, textY, 0);
			}
        }
        else
        {
        	
        }
        
        super.render(matrices, mouseX, mouseY, delta);
	}
	
	@Environment(EnvType.CLIENT)
	public static class GuideBookPage
	{
		public HashMap<Integer, Text> textList = new HashMap<Integer, Text>();
		public HashMap<Integer, Identifier> imageList = new HashMap<Integer, Identifier>();
		
		public GuideBookPage()
		{
			
		}
		
		public GuideBookPage addText(String translationKey, int position)
		{
			textList.put(position, Text.translatable("guide_book." + translationKey));
			return this;
		}
		
		public GuideBookPage addImage(String image, int position)
		{
			imageList.put(position, new Identifier("space:textures/gui/guide/" + image + ".png"));
			return this;
		}
	}
}