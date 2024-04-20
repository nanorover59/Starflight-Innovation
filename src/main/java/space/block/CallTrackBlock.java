package space.block;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CallTrackBlock extends Block
{
	public CallTrackBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
		if(world.isClient || world.getReceivedRedstonePower(pos) == 0)
			return;
		
		Set<BlockPos> set = new HashSet<BlockPos>();
		BlockPos actuatorPos = null;
		BlockPos targetPos = null;
		
		for(Direction direction : DIRECTIONS)
		{
			Mutable mutable = pos.mutableCopy();

			while((world.getBlockState(mutable).getBlock() == StarflightBlocks.LINEAR_TRACK && world.getBlockState(mutable).get(PillarBlock.AXIS).test(direction)) || world.getBlockState(mutable).getBlock() == StarflightBlocks.CALL_TRACK)
			{
				if(actuatorPos == null)
				{
					for(Direction checkDirection : DIRECTIONS)
					{
						if(checkDirection != direction)
						{
							BlockState checkState = world.getBlockState(mutable.offset(checkDirection));

							if(checkState.getBlock() == StarflightBlocks.LINEAR_ACTUATOR)
							{
								actuatorPos = mutable.offset(checkDirection).toImmutable();
								targetPos = pos.offset(checkDirection);
								break;
							}
						}
					}
				}

				set.add(mutable.toImmutable());
				mutable.move(direction);
			}
		}
		
		if(actuatorPos != null && actuatorPos.getSquaredDistance(targetPos) > 0)
			LinearActuatorBlock.spawnEntity(world, actuatorPos, targetPos, set);
    }
}