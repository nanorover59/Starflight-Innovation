package space.block;

import java.util.ArrayList;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.util.AirUtil;
import space.util.StarflightEffects;

public class HabitableAirBlock extends AirBlock
{
	public static final double DENSITY = 2.0; // The density of habitable air in kilograms per cubic meter.
	
	public HabitableAirBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
		if(world.isClient)
			return;
		
		if(!AirUtil.airBlocking(world, fromPos))
		{
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
			AirUtil.recursiveVolume(world, fromPos, checkList, foundList, AirUtil.MAX_VOLUME);
			checkList.clear();
			BlockPos sourcePos = recursiveFindSource(world, pos, checkList, AirUtil.MAX_VOLUME);
			checkList.clear();
			
			if(sourcePos.equals(new BlockPos(-9999, -9999, -9999)))
			{
				AirUtil.recursiveRemove(world, pos, checkList, AirUtil.MAX_VOLUME);
				StarflightEffects.sendOutgas(world, pos, fromPos, true);
			}
			else if(AirUtil.requestSupply(world, sourcePos, foundList.size() * DENSITY, StarflightBlocks.ATMOSPHERE_GENERATOR))
				AirUtil.fillVolume(world, foundList);
			else
			{
				AirUtil.recursiveRemove(world, pos, checkList, AirUtil.MAX_VOLUME);
				StarflightEffects.sendOutgas(world, pos, fromPos, true);
			}
		}
    }
	
	/**
	 * Recursively find the block position of an atmosphere generator. return the impossible coordinates (-9999, -9999, -9999) if none are found.
	 */
	private BlockPos recursiveFindSource(World world, BlockPos position, ArrayList<BlockPos> checkList, int limit)
	{
		if(world.getBlockState(position).getBlock() != StarflightBlocks.HABITABLE_AIR || checkList.contains(position))
		{
			if(world.getBlockState(position).getBlock() == StarflightBlocks.ATMOSPHERE_GENERATOR && world.getBlockState(position).get(AtmosphereGeneratorBlock.LIT))
				return position;
			else
				return new BlockPos(-9999, -9999, -9999);
		}
		
		checkList.add(position);
		
		if(checkList.size() >= limit)
			return new BlockPos(-9999, -9999, -9999);;
		
		for(Direction direction : Direction.values())
		{
			BlockPos otherPosition = recursiveFindSource(world, position.offset(direction), checkList, limit);
			
			if(!otherPosition.equals(new BlockPos(-9999, -9999, -9999)))
				return otherPosition;
		}
		
		return new BlockPos(-9999, -9999, -9999);
	}
}