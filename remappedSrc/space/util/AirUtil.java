package space.util;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.OxygenPipeBlock;
import space.block.SealedDoorBlock;
import space.block.SealedTrapdoorBlock;
import space.block.StarflightBlocks;
import space.block.entity.FluidContainerBlockEntity;
import space.block.entity.OxygenOutletValveBlockEntity;
import space.planet.Planet;
import space.planet.PlanetList;

public class AirUtil
{
	public static final int MAX_VOLUME = 4096;
	
	/**
	 * Get the air resistance multiplier for the atmospheric conditions at the given location.
	 */
	public static double getAirResistanceMultiplier(World world, BlockPos pos)
	{
		if(world.getBlockState(pos).getBlock() == StarflightBlocks.HABITABLE_AIR)
			return 0.85;
		
		Planet currentPlanet = PlanetList.getPlanetForWorld(world.getRegistryKey());
		boolean inOrbit = PlanetList.isOrbit(world.getRegistryKey());
		
		return inOrbit ? 0.0 : currentPlanet.getSurfacePressure();
	}
	
	/**
	 * Return true if the given entity can breathe in its current location.
	 */
	public static boolean canEntityBreathe(LivingEntity entity)
	{
		World world = entity.getWorld();
		BlockPos pos = entity.getBlockPos();
		Planet currentPlanet = PlanetList.getPlanetForWorld(world.getRegistryKey());
		
		if(currentPlanet == null)
			return false;
		
		if(currentPlanet.getSurfacePressure() > 0.6 && currentPlanet.hasOxygen() && !PlanetList.isOrbit(world.getRegistryKey()))
			return true;
		
		for(Direction direction : Direction.values())
		{
			if(world.getBlockState(pos.offset(direction)).getBlock() == StarflightBlocks.HABITABLE_AIR)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Recursively find a closed volume that can be filled with habitable air.
	 */
	public static void recursiveVolume(World world, BlockPos position, ArrayList<BlockPos> checkList, ArrayList<BlockPos> foundList, int limit)
	{
		if(position.getY() <= world.getBottomY() || position.getY() >= world.getTopY() || checkList.contains(position) || airBlocking(world, position))
			return;
		
		if(world.getBlockState(position).getBlock() == Blocks.AIR)
			foundList.add(position);
		
		checkList.add(position);
		
		if(checkList.size() >= limit)
			return;
		
		for(Direction direction : Direction.values())
			recursiveVolume(world, position.offset(direction), checkList, foundList, limit);
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
	 * Recursively remove habitable air blocks.
	 */
	public static void recursiveRemove(World world, BlockPos position, ArrayList<BlockPos> checkList, int limit)
	{
		if(world.getBlockState(position).getBlock() != StarflightBlocks.HABITABLE_AIR || checkList.contains(position))
			return;
		
		checkList.add(position);
		world.setBlockState(position, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
		
		if(checkList.size() >= limit)
			return;
		
		for(Direction direction : Direction.values())
		{
			if(world.getBlockState(position.offset(direction)).getBlock() != StarflightBlocks.HABITABLE_AIR)
				world.updateNeighbor(position.offset(direction), Blocks.AIR, position);
			else
				recursiveRemove(world, position.offset(direction), checkList, limit);
		}
	}
	
	/**
	 * Returns true if the block at the given location is a valid wall for a closed volume.
	 */
	public static boolean airBlocking(World world, BlockPos position)
	{
		BlockState blockState = world.getBlockState(position);
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
			double supply = recursiveSearchSupply(world, pos, checkList, 32768, activeBlock);
			
			if(supply >= required)
			{
				checkList.clear();
				recursiveUseSupply(world, pos, checkList, 32768, required, activeBlock);
				return true;
			}
			else
				return false;
		}
		else
			return false;
	}
	
	/**
	 * Recursively find the total amount of oxygen available from connected pipes and tanks in kilograms.
	 */
	public static double recursiveSearchSupply(World world, BlockPos position, ArrayList<BlockPos> checkList, int limit, Block activeBlock)
	{
		if(checkList.contains(position))
			return 0;
		
		BlockState blockState = world.getBlockState(position);
		double oxygen = 0;
		
		if(blockState.getBlock() == activeBlock && checkList.isEmpty())
		{
			for(Direction direction : Direction.values())
				oxygen += recursiveSearchSupply(world, position.offset(direction), checkList, limit, activeBlock);
			
			return oxygen;
		}
		
		if(blockState.getBlock() instanceof OxygenPipeBlock)
		{
			FluidContainerBlockEntity blockEntity = (FluidContainerBlockEntity) world.getBlockEntity(position);
			oxygen += blockEntity.getStoredFluid();
		}
		else if(blockState.getBlock() == StarflightBlocks.OXYGEN_OUTLET_VALVE)
		{
			OxygenOutletValveBlockEntity blockEntity = (OxygenOutletValveBlockEntity) world.getBlockEntity(position);
			
			if(blockEntity.getFluidTankController() != null)
				oxygen += blockEntity.getFluidTankController().getStoredFluid();
		}
		
		if(!(world.getBlockState(position).getBlock() instanceof OxygenPipeBlock))
			return oxygen;
		
		checkList.add(position);
		
		if(checkList.size() >= limit)
			return oxygen;
		
		for(Direction direction : Direction.values())
			oxygen += recursiveSearchSupply(world, position.offset(direction), checkList, limit, activeBlock);
		
		return oxygen;
	}
	
	/**
	 * Recursively deplete the required amount of oxygen from connected pipes and tanks.
	 */
	public static void recursiveUseSupply(World world, BlockPos position, ArrayList<BlockPos> checkList, int limit, double toUse, Block activeBlock)
	{
		if(checkList.contains(position))
			return;
		
		BlockState blockState = world.getBlockState(position);
		
		if(blockState.getBlock() == activeBlock && checkList.isEmpty())
		{
			for(Direction direction : Direction.values())
				recursiveUseSupply(world, position.offset(direction), checkList, limit, toUse, activeBlock);
			
			return;
		}
		
		if(blockState.getBlock() instanceof OxygenPipeBlock)
		{
			FluidContainerBlockEntity blockEntity = (FluidContainerBlockEntity) world.getBlockEntity(position);
			
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
			OxygenOutletValveBlockEntity blockEntity = (OxygenOutletValveBlockEntity) world.getBlockEntity(position);
			
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
		
		checkList.add(position);
		
		if(!(world.getBlockState(position).getBlock() instanceof OxygenPipeBlock) || checkList.size() >= limit)
			return;
		
		for(Direction direction : Direction.values())
			recursiveUseSupply(world, position.offset(direction), checkList, limit, toUse, activeBlock);
	}
}