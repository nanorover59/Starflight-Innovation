package space.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class SealedDoorBlock extends DoorBlock
{
	public SealedDoorBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
	{
		super.neighborUpdate(state, world, pos, block, fromPos, notify);
		
		for(Direction direction : Direction.values())
		{
			BlockPos offsetPos = pos.offset(direction);
			
			if(world.getBlockState(offsetPos).getBlock() == StarflightBlocks.HABITABLE_AIR)
				world.updateNeighbor(offsetPos, Blocks.AIR, pos);
		}
	}
}
