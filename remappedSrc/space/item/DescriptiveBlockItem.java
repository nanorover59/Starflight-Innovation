package space.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import space.client.StarflightModClient;

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
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(!description.isEmpty())
			tooltip.addAll(description);
		
		if(!hiddenDescription.isEmpty())
			StarflightModClient.hiddenItemTooltip(tooltip, hiddenDescription);
	}
}
