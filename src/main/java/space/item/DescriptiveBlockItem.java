package space.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public class DescriptiveBlockItem extends BlockItem
{
	public List<Text> description;
	public List<Text> hiddenDescription;
	
	public DescriptiveBlockItem(Block block, Settings settings, List<Text> description, List<Text> hiddenDescription)
	{
		super(block, settings);
		this.description = description;
		this.hiddenDescription = hiddenDescription;
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		if(!description.isEmpty())
			tooltip.addAll(description);
		
		if(!hiddenDescription.isEmpty())
			StarflightItems.hiddenItemTooltip(tooltip, hiddenDescription);
	}
}
