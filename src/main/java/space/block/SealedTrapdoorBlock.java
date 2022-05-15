package space.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class SealedTrapdoorBlock extends TrapdoorBlock
{
	public SealedTrapdoorBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
	{
		super.neighborUpdate(state, world, pos, block, fromPos, notify);
		
		BlockPos upPos = pos.offset(Direction.UP);
		BlockPos downPos = pos.offset(Direction.DOWN);
		
		if(world.getBlockState(upPos).getBlock() == StarflightBlocks.HABITABLE_AIR)
			world.updateNeighbor(upPos, Blocks.AIR, pos);
		
		if(world.getBlockState(downPos).getBlock() == StarflightBlocks.HABITABLE_AIR)
			world.updateNeighbor(downPos, Blocks.AIR, pos);
	}
}
