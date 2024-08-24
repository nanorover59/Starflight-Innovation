package space.block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

import org.joml.Vector3f;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import space.entity.LinearPlatformEntity;
import space.entity.MovingCraftEntity;
import space.util.BlockSearch;
import space.vessel.MovingCraftBlockData;

public class LinearActuatorBlock extends SimpleFacingBlock
{
	public LinearActuatorBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
		if(world.isClient || !world.isReceivingRedstonePower(pos))
			return;
		
		Direction direction = state.get(FACING);
		BlockPos offsetPos = pos.offset(direction.getOpposite());
		BlockState offsetState = world.getBlockState(offsetPos);
		
		if(offsetState.getBlock() == StarflightBlocks.LINEAR_TRACK || offsetState.getBlock() == StarflightBlocks.CALL_TRACK)
		{
			Direction redstoneDirection = Direction.fromVector(pos.getX() - sourcePos.getX(), pos.getY() - sourcePos.getY(), pos.getZ() - sourcePos.getZ());
			
			if(offsetState.getBlock() == StarflightBlocks.LINEAR_TRACK && !offsetState.get(PillarBlock.AXIS).test(redstoneDirection))
				return;
			Mutable mutable = offsetPos.mutableCopy();
			int distance = 0;
			boolean junctionFlag = false;

			while((world.getBlockState(mutable).getBlock() == StarflightBlocks.LINEAR_TRACK && world.getBlockState(mutable).get(PillarBlock.AXIS).test(redstoneDirection)) || world.getBlockState(mutable).getBlock() == StarflightBlocks.CALL_TRACK)
			{
				if(!mutable.equals(offsetPos) && world.getBlockState(mutable).getBlock() == StarflightBlocks.CALL_TRACK)
					junctionFlag = true;
				
				if(!junctionFlag)
					distance++;
				
				mutable.move(redstoneDirection);
			}
			
			mutable = offsetPos.mutableCopy();
			
			while((world.getBlockState(mutable).getBlock() == StarflightBlocks.LINEAR_TRACK && world.getBlockState(mutable).get(PillarBlock.AXIS).test(redstoneDirection)) || world.getBlockState(mutable).getBlock() == StarflightBlocks.CALL_TRACK)
				mutable.move(redstoneDirection.getOpposite());
			
			if(!junctionFlag)
				distance--;
			
			if(distance > 0)
				spawnEntity(world, pos, pos.offset(redstoneDirection, distance), offsetPos);
		}
    }
	
	public static void spawnEntity(World world, BlockPos pos, BlockPos targetPos, BlockPos trackPos)
	{
		// Exclude blocks that are part of the track.
		BiPredicate<World, BlockPos> include = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return blockState.getBlock() == StarflightBlocks.LINEAR_TRACK || blockState.getBlock() == StarflightBlocks.CALL_TRACK;
		};
		
		ArrayList<BlockPos> positionList = new ArrayList<BlockPos>();
		BlockSearch.search(world, trackPos, positionList, include, BlockSearch.MAX_VOLUME, false);
		Set<BlockPos> set = new HashSet<BlockPos>();
		set.addAll(positionList);
		
		// Detect blocks to be included in the craft construction.
		positionList = new ArrayList<BlockPos>();
		BlockSearch.movingCraftSearch(world, pos, positionList, set, BlockSearch.MAX_VOLUME, BlockSearch.MAX_DISTANCE);
		
		if(positionList.isEmpty())
			return;
		
		ArrayList<MovingCraftBlockData> blockDataList = MovingCraftEntity.captureBlocks(world, new BlockPos(MathHelper.floor(pos.getX()), MathHelper.floor(pos.getY()), MathHelper.floor(pos.getZ())), positionList);
		LinearPlatformEntity entity = new LinearPlatformEntity(world, pos, blockDataList, 0.0, 0.0, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f), targetPos);
		MovingCraftEntity.removeBlocksFromWorld(world, pos, blockDataList);
		world.spawnEntity(entity);
	}
}