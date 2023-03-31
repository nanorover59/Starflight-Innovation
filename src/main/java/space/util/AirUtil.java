package space.util;

import java.util.ArrayList;
import java.util.function.BiPredicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.OxygenPipeBlock;
import space.block.SealedDoorBlock;
import space.block.SealedTrapdoorBlock;
import space.block.StarflightBlocks;
import space.block.entity.FluidContainerBlockEntity;
import space.block.entity.OxygenOutletValveBlockEntity;
import space.planet.PlanetDimensionData;

public class AirUtil
{
	public static final int MAX_VOLUME = 4096;
	
	/**
	 * Get the air resistance multiplier for the atmospheric conditions at the given location.
	 */
	public static double getAirResistanceMultiplier(World world, PlanetDimensionData data, BlockPos pos)
	{
		if(data == null)
			return 1.0;
		
		if(world.getBlockState(pos).getBlock() == StarflightBlocks.HABITABLE_AIR)
			return 0.9;
		
		return data.isOrbit() ? 0.0 : data.getPressure();
	}
	
	/**
	 * Return true if the given entity can breathe in its current location.
	 */
	public static boolean canEntityBreathe(LivingEntity entity, PlanetDimensionData data)
	{
		World world = entity.getWorld();
		BlockPos pos = entity.getBlockPos();
		
		if(data == null)
			return true;
		
		if(data.getPressure() > 0.5 && data.hasOxygen() && !data.isOrbit())
			return true;
		
		for(Direction direction : Direction.values())
		{
			if(world.getBlockState(pos.offset(direction)).getBlock() == StarflightBlocks.HABITABLE_AIR)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Find a closed volume that can be filled with habitable air.
	 */
	public static void findVolume(World world, BlockPos position, ArrayList<BlockPos> checkList, int limit)
	{
		BiPredicate<World, BlockPos> include = (w, p) -> {
			return !airBlocking(w, p);
		};
		
		BlockSearch.search(world, position, checkList, include, limit, true);
	}
	
	/**
	 * Fill the provided list of block positions with habitable air.
	 */
	public static void fillVolume(World world, ArrayList<BlockPos> posList)
	{
		for(BlockPos pos : posList)
		{
			if(world.getBlockState(pos).getBlock() == Blocks.AIR)
				world.setBlockState(pos, StarflightBlocks.HABITABLE_AIR.getDefaultState());
		}
	}
	
	/**
	 * Remove habitable air blocks.
	 */
	public static void remove(World world, BlockPos position, ArrayList<BlockPos> checkList, int limit)
	{
		BiPredicate<World, BlockPos> include = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return blockState.getBlock() != Blocks.AIR && (!AirUtil.airBlocking(w, p) || blockState.getBlock() == StarflightBlocks.HABITABLE_AIR);
		};
		
		BlockSearch.search(world, position, checkList, include, limit, true);
		
		for(BlockPos pos : checkList)
		{
			BlockState blockState = world.getBlockState(pos);
			
			if(blockState.getBlock() == StarflightBlocks.HABITABLE_AIR)
			{
				FluidState fluidState = world.getFluidState(pos);
				world.setBlockState(pos, fluidState.getBlockState());
				
				/*for(Direction direction : Direction.values())
				{
					BlockPos offset = pos.offset(direction);
					BlockState state = world.getBlockState(offset);
					
					if(state.isIn(StarflightBlocks.INSTANT_REMOVE_TAG))
					{
						world.removeBlock(offset, false);
						Block.dropStacks(blockState, world, offset, world.getBlockEntity(offset));
					}
					else
						state.neighborUpdate(world, offset, StarflightBlocks.HABITABLE_AIR, pos, true);
				}*/
			}
		}
	}
	
	/**
	 * Returns true if the block at the given location is a valid wall for a closed volume.
	 */
	public static boolean airBlocking(World world, BlockPos position)
	{
		BlockState blockState = world.getBlockState(position);
		return airBlockingState(world, position, blockState);
	}
	
	public static boolean airBlockingState(World world, BlockPos position, BlockState blockState)
	{
		Block block = blockState.getBlock();
		
		if(block == Blocks.AIR)
			return false;
		else if(block == StarflightBlocks.HABITABLE_AIR)
			return true;
		else if((block instanceof PaneBlock) && blockState.getMaterial() == Material.GLASS)
			return true;
		else if((block instanceof SealedDoorBlock) && !(blockState.get(SealedDoorBlock.OPEN) || blockState.get(SealedDoorBlock.POWERED)))
			return true;
		else if((block instanceof SealedTrapdoorBlock) && !(blockState.get(SealedDoorBlock.OPEN) || blockState.get(SealedDoorBlock.POWERED)))
			return true;
		else
			return blockState.isFullCube(world, position);
	}
	
	/**
	 * Return true and use oxygen if a large enough supply is found, otherwise return false.
	 */
	public static boolean requestSupply(World world, BlockPos pos, double required, Block activeBlock)
	{
		if(world.getBlockState(pos).getBlock() == activeBlock)
		{
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			double supply = searchSupply(world, pos, checkList, BlockSearch.MAX_VOLUME, activeBlock);
			
			if(supply >= required)
			{
				useSupply(world, checkList, required);
				return true;
			}
			else
				return false;
		}
		else
			return false;
	}
	
	/**
	 * Find the total amount of oxygen available from connected pipes and tanks in kilograms.
	 */
	public static double searchSupply(World world, BlockPos position, ArrayList<BlockPos> checkList, int limit, Block activeBlock)
	{
		BiPredicate<World, BlockPos> include = (w, p) -> {
			BlockState blockState = world.getBlockState(p);
			return blockState.getBlock() instanceof OxygenPipeBlock || blockState.getBlock() == StarflightBlocks.OXYGEN_OUTLET_VALVE || blockState.getBlock() == activeBlock;
		};
		
		BlockSearch.search(world, position, checkList, include, BlockSearch.MAX_VOLUME, false);
		double oxygen = 0;
		
		for(BlockPos pos : checkList)
		{
			BlockState blockState = world.getBlockState(pos);
			
			if(blockState.getBlock() instanceof OxygenPipeBlock)
			{
				FluidContainerBlockEntity blockEntity = (FluidContainerBlockEntity) world.getBlockEntity(pos);
				oxygen += blockEntity.getStoredFluid();
			}
			else if(blockState.getBlock() == StarflightBlocks.OXYGEN_OUTLET_VALVE)
			{
				OxygenOutletValveBlockEntity blockEntity = (OxygenOutletValveBlockEntity) world.getBlockEntity(pos);
				
				if(blockEntity.getFluidTankController() != null)
					oxygen += blockEntity.getFluidTankController().getStoredFluid();
			}
		}
		
		return oxygen;
	}
	
	/**
	 * Deplete the required amount of oxygen from connected pipes and tanks.
	 */
	public static void useSupply(World world, ArrayList<BlockPos> actionList, double toUse)
	{
		for(BlockPos pos : actionList)
		{
			BlockState blockState = world.getBlockState(pos);
			
			if(blockState.getBlock() instanceof OxygenPipeBlock)
			{
				FluidContainerBlockEntity blockEntity = (FluidContainerBlockEntity) world.getBlockEntity(pos);
				
				if(toUse < blockEntity.getStoredFluid())
				{
					blockEntity.changeStoredFluid(-toUse);
					return;
				}
				else
				{
					toUse -= blockEntity.getStoredFluid();
					blockEntity.changeStoredFluid(-blockEntity.getStoredFluid());
				}
			}
			else if(blockState.getBlock() == StarflightBlocks.OXYGEN_OUTLET_VALVE)
			{
				OxygenOutletValveBlockEntity blockEntity = (OxygenOutletValveBlockEntity) world.getBlockEntity(pos);
				
				if(blockEntity.getFluidTankController() != null)
				{
					if(toUse < blockEntity.getFluidTankController().getStoredFluid())
					{
						blockEntity.getFluidTankController().changeStoredFluid(-toUse);
						return;
					}
					else
					{
						toUse -= blockEntity.getFluidTankController().getStoredFluid();
						blockEntity.getFluidTankController().setStoredFluid(0.0);
					}
				}
			}
		}
	}
}