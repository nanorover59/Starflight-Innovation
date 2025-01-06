package space.client.gui;

import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import space.StarflightMod;

@Environment(EnvType.CLIENT)
public class CraftingTreeScreen extends Screen
{
	private static final Identifier BACKGROUND_TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/block/riveted_aluminum.png");
	private final Map<String, ProgressNode> nodes;
	private final int guiWidth = 256;
	private final int guiHeight = 170;

	private int guiLeft;
	private int guiTop;

	public CraftingTreeScreen(Map<String, ProgressNode> nodes)
	{
		super(Text.of("Progress Tree"));
		this.nodes = nodes;
	}

	@Override
	protected void init()
	{
		this.guiLeft = (this.width - guiWidth) / 2;
		this.guiTop = (this.height - guiHeight) / 2;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		this.renderBackground(context, mouseX, mouseY, delta);
		RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
		context.drawTexture(BACKGROUND_TEXTURE, guiLeft, guiTop, 0, 0, guiWidth, guiHeight, guiWidth, guiHeight);

		renderNodes(context, mouseX, mouseY);
		super.render(context, mouseX, mouseY, delta);
	}

	private void renderNodes(DrawContext context, int mouseX, int mouseY)
	{
		for(ProgressNode node : nodes.values())
		{
			renderConnections(context, node);
			renderNode(context, node);
		}
		
		renderTooltips(context, mouseX, mouseY);
	}

	private void renderNode(DrawContext context, ProgressNode node)
	{
		int x = guiLeft + node.getX();
		int y = guiTop + node.getY();
		ItemStack itemStack = new ItemStack(node.getItem());
		context.drawItem(itemStack, x, y);
		
		if(node.isLocked())
			context.fill(x, y, x + 16, y + 16, 0xAA000000); // Overlay for locked
	}

	private void renderConnections(DrawContext context, ProgressNode node)
	{
		for(String parentId : node.getDependencies())
		{
			ProgressNode parent = nodes.get(parentId);
			
			if(parent != null)
			{
				int startX = guiLeft + node.getX() + 8;
				int startY = guiTop + node.getY() + 8;
				int endX = guiLeft + parent.getX() + 8;
				int endY = guiTop + parent.getY() + 8;
				
				context.drawHorizontalLine(startX, endX, startY, 0xFFFFFFFF);
				context.drawVerticalLine(endX, startY, endY, 0xFFFFFFFF);
			}
		}
	}

	private void renderTooltips(DrawContext context, int mouseX, int mouseY)
	{
		for(ProgressNode node : nodes.values())
		{
			int x = guiLeft + node.getX();
			int y = guiTop + node.getY();
			
			if(mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16)
				context.drawTooltip(textRenderer, Text.of(node.getName()), mouseX, mouseY);
		}
	}

	@Override
	public boolean shouldPause()
	{
		return false;
	}

	public static class ProgressNode
	{
		private final String name;
		private final Item item;
		private final List<String> dependencies;
		private final int x;
		private final int y;
		private final boolean locked;

		public ProgressNode(String name, Item item, List<String> dependencies, int x, int y, boolean locked)
		{
			this.name = name;
			this.item = item;
			this.dependencies = dependencies;
			this.x = x;
			this.y = y;
			this.locked = locked;
		}

		public String getName()
		{
			return name;
		}

		public net.minecraft.item.Item getItem()
		{
			return item;
		}

		public List<String> getDependencies()
		{
			return dependencies;
		}

		public int getX()
		{
			return x;
		}

		public int getY()
		{
			return y;
		}

		public boolean isLocked()
		{
			return locked;
		}
	}
}