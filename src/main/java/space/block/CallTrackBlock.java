package space.block;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class CallTrackBlock extends Block
{
	public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
	
	public CallTrackBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(TRIGGERED, false));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		super.appendProperties(builder);
		builder.add(TRIGGERED);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
		if(world.isClient)
			return;
		
		if(world.isReceivingRedstonePower(pos) && !state.get(TRIGGERED))
		{
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
				LinearActuatorBlock.spawnEntity(world, actuatorPos, targetPos, pos);
			
			world.setBlockState(pos, state.with(TRIGGERED, true), Block.NOTIFY_LISTENERS);
		}
		else if(!world.isReceivingRedstonePower(pos) && state.get(TRIGGERED))
			world.setBlockState(pos, state.with(TRIGGERED, false), Block.NOTIFY_LISTENERS);
    }
	
	@Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		
	}
}