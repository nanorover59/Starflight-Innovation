package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.ElectrolyzerBlock;
import space.block.StarflightBlocks;

public class ElectrolyzerBlockEntity extends BlockEntity implements PoweredBlockEntity
{
	private boolean hasWater;
	private int powerState;
	
	public ElectrolyzerBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.ELECTROLYZER_BLOCK_ENTITY, pos, state);
		hasWater = false;
		powerState = 0;
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, ElectrolyzerBlockEntity blockEntity)
	{
		if(!blockEntity.getWater())
			return;
		
		if(state != (BlockState) state.with(ElectrolyzerBlock.LIT, blockEntity.getPowerState() > 0))
		{
			state = (BlockState) state.with(ElectrolyzerBlock.LIT, blockEntity.getPowerState() > 0);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
		}
		
		if(blockEntity.getPowerState() < 1)
			return;
		
		BlockPos leftSide = pos.offset(state.get(HorizontalFacingBlock.FACING).rotateYClockwise());
		BlockPos rightSide = pos.offset(state.get(HorizontalFacingBlock.FACING).rotateYCounterclockwise());
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		double totalMassFlow = 16.0; // Kilograms per tick.
		double oxygen = totalMassFlow * (8.0 / 9.0);
		double hydrogen = totalMassFlow * (1.0 / 9.0);
		double remaining = 0.0;
		
		if(world.getBlockState(leftSide).getBlock() == StarflightBlocks.OXYGEN_PIPE)
		{
			checkList.clear();
			remaining += recursiveSpread(world, leftSide, checkList, oxygen, "oxygen", 2048);
		}
		
		if(world.getBlockState(rightSide).getBlock() == StarflightBlocks.HYDROGEN_PIPE)
		{
			checkList.clear();
			remaining += recursiveSpread(world, rightSide, checkList, hydrogen, "hydrogen", 2048);
		}
    }
	
	public static double recursiveSpread(World world, BlockPos position, ArrayList<BlockPos> checkList, double toSpread, String fluidName, int limit)
	{
		if(checkList.contains(position) || checkList.size() >= limit)
			return toSpread;
		
		if(world.getBlockEntity(position) instanceof FluidContainerBlockEntity)
		{
			FluidContainerBlockEntity blockEntity = ((FluidContainerBlockEntity) world.getBlockEntity(position));
			double capacity = blockEntity.getStorageCapacity();
			double fluid = blockEntity.getStoredFluid();
			checkList.add(position);
			
			if(blockEntity.getFluidName().contains(fluidName))
			{
				if(fluid + toSpread < capacity)
				{
					blockEntity.changeStoredFluid(toSpread);
					toSpread = 0;
				}
				else
				{
					double difference = capacity - fluid;
					blockEntity.changeStoredFluid(difference);
					toSpread -= difference;
					
					for(Direction direction : Direction.values())
						toSpread = recursiveSpread(world, position.offset(direction), checkList, toSpread, fluidName, limit);
				}
			}
		}
		else if(world.getBlockEntity(position) instanceof HydrogenInletValveBlockEntity || world.getBlockEntity(position) instanceof OxygenInletValveBlockEntity)
		{
			FluidTankControllerBlockEntity blockEntity = ((FluidTankInterfaceBlockEntity) world.getBlockEntity(position)).getFluidTankController();
			
			if(blockEntity == null)
				return toSpread;
			
			double capacity = blockEntity.getStorageCapacity();
			double fluid = blockEntity.getStoredFluid();
			checkList.add(position);
			
			if(blockEntity.getFluidName().contains(fluidName))
			{
				if(fluid + toSpread < capacity)
				{
					blockEntity.changeStoredFluid(toSpread);
					toSpread = 0;
				}
				else
				{
					double difference = capacity - fluid;
					blockEntity.changeStoredFluid(difference);
					toSpread -= difference;
				}
			}
		}
		
		return toSpread;
	}
	
	public void setWater(boolean b)
	{
		hasWater = b;
	}

	public boolean getWater()
	{
		return hasWater;
	}
	
	@Override
	public void setPowerState(int i)
	{
		powerState = i;
	}

	@Override
	public int getPowerState()
	{
		return powerState;
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		hasWater = nbt.getBoolean("hasWater");
		super.readNbt(nbt);
	}

	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		nbt.putBoolean("hasWater", hasWater);
		super.writeNbt(nbt);
	}
}