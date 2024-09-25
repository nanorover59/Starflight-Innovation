package space.block;

import net.minecraft.block.PotatoesBlock;
import net.minecraft.item.ItemConvertible;
import space.item.StarflightItems;

public class MarsPotatoesBlock extends PotatoesBlock
{
	public MarsPotatoesBlock(Settings settings)
	{
		super(settings);
	}

	@Override
	protected ItemConvertible getSeedsItem()
	{
		return StarflightItems.MARS_POTATO;
	}
}