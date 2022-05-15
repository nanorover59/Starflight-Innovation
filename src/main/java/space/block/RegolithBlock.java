package space.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class RegolithBlock extends FallingBlock
{
	public RegolithBlock(AbstractBlock.Settings settings)
	{
		super(settings);
	}

	public int getColor(BlockState state, BlockView world, BlockPos pos)
	{
		return -8356741;
	}
}
