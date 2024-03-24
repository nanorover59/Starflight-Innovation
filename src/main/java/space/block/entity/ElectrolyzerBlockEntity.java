package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import space.block.ElectrolyzerBlock;
import space.block.EnergyBlock;
import space.block.FluidUtilityBlock;
import space.block.StarflightBlocks;
import space.util.FluidResourceType;

public class ElectrolyzerBlockEntity extends BlockEntity implements EnergyBlockEntity
{
	private static double WATER_FLOW = 1.0; // Mass of water to electrolyze per tick.
	private static double OXYGEN_FLOW = WATER_FLOW * (8.0 / 9.0);
	private static double HYDROGEN_FLOW = WATER_FLOW * (1.0 / 9.0);
	private double energy;
	private boolean hasWater;
	
	public ElectrolyzerBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.ELECTROLYZER_BLOCK_ENTITY, pos, state);
		hasWater = false;
	}
	
	public static double recursiveSpread(World world, BlockPos position, ArrayList<BlockPos> checkList, double toSpread, FluidResourceType fluidType, int limit)
	{
		if(checkList.contains(position) || checkList.size() >= limit)
			return toSpread;
		
		if(world.getBlockEntity(position) instanceof FluidPipeBlockEntity)
		{
			FluidPipeBlockEntity blockEntity = ((FluidPipeBlockEntity) world.getBlockEntity(position));
			double capacity = blockEntity.getStorageCapacity();
			double fluid = blockEntity.getStoredFluid();
			checkList.add(position);
			
			if(blockEntity.getFluidType().getID() == fluidType.getID())
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
						toSpread = recursiveSpread(world, position.offset(direction), checkList, toSpread, fluidType, limit);
				}
			}
		}
		else if(world.getBlockEntity(position) instanceof ValveBlockEntity)
		{
			FluidTankControllerBlockEntity blockEntity = ((ValveBlockEntity) world.getBlockEntity(position)).getFluidTankController();
			
			if(blockEntity == null)
				return toSpread;
			
			double capacity = blockEntity.getStorageCapacity();
			double fluid = blockEntity.getStoredFluid();
			checkList.add(position);
			
			if(blockEntity.getFluidType().getID() == fluidType.getID())
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
		else if(world.getBlockEntity(position) instanceof WaterTankBlockEntity)
		{
			WaterTankBlockEntity blockEntity = ((WaterTankBlockEntity) world.getBlockEntity(position));
			double capacity = blockEntity.getStorageCapacity();
			double fluid = blockEntity.getStoredFluid();
			checkList.add(position);

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
				toSpread = recursiveSpread(world, position.down(), checkList, toSpread, fluidType, limit);
				toSpread = recursiveSpread(world, position.up(), checkList, toSpread, fluidType, limit);
			}
			
			blockEntity.markDirty();
			world.updateListeners(position, blockEntity.getCachedState(), blockEntity.getCachedState(), Block.NOTIFY_LISTENERS);
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
	public double getOutput()
	{
		return 0.0;
	}
	
	@Override
	public double getInput()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getInput() / world.getTickManager().getTickRate();
	}
	
	@Override
	public double getEnergyStored()
	{
		return energy;
	}

	@Override
	public double getEnergyCapacity()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getEnergyCapacity();
	}

	@Override
	public double changeEnergy(double amount)
	{
		double newEnergy = energy + amount;
		energy = MathHelper.clamp(newEnergy, 0, getEnergyCapacity());
		return amount - (newEnergy - energy);
	}

	@Override
	public ArrayList<BlockPos> getOutputs()
	{
		return null;
	}

	@Override
	public void addOutput(BlockPos output)
	{
	}

	@Override
	public void clearOutputs()
	{
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putDouble("energy", this.energy);
		nbt.putBoolean("hasWater", this.hasWater);
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.energy = nbt.getDouble("energy");
		this.hasWater = nbt.getBoolean("hasWater");
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, ElectrolyzerBlockEntity blockEntity)
	{
		int waterInlets = 0;
		int oxygenOutlets = 0;
		int hydrogenOutlets = 0;
		
		for(Direction direction : Direction.values())
		{
			BlockPos offset = pos.offset(direction);
			Block offsetBlock = world.getBlockState(offset).getBlock();
			
			if(offsetBlock instanceof FluidUtilityBlock)
			{
				BlockEntity offsetBlockEntity = world.getBlockEntity(offset);
				
				if(offsetBlockEntity instanceof FluidPipeBlockEntity)
				{
					if(offsetBlock == StarflightBlocks.WATER_PIPE && ((FluidPipeBlockEntity) offsetBlockEntity).getStoredFluid() > WATER_FLOW)
						waterInlets++;
					else if(offsetBlock == StarflightBlocks.OXYGEN_PIPE)
						oxygenOutlets++;
					else if(offsetBlock == StarflightBlocks.HYDROGEN_PIPE)
						hydrogenOutlets++;
				}
			}
		}
		
		if(waterInlets > 0 && (oxygenOutlets > 0 || hydrogenOutlets > 0))
		{
			blockEntity.changeEnergy(-blockEntity.getInput());
			
			if(blockEntity.energy > 0)
			{
				if(!world.getBlockState(pos).get(ElectrolyzerBlock.LIT))
				{
					state = (BlockState) state.with(ElectrolyzerBlock.LIT, true);
					world.setBlockState(pos, state, Block.NOTIFY_ALL);
					markDirty(world, pos, state);
				}
				
				for(Direction direction : Direction.values())
				{
					BlockPos offset = pos.offset(direction);
					Block offsetBlock = world.getBlockState(offset).getBlock();
					BlockEntity offsetBlockEntity = world.getBlockEntity(offset);
					
					if(offsetBlock == StarflightBlocks.WATER_PIPE)
						((FluidPipeBlockEntity) offsetBlockEntity).changeStoredFluid(-WATER_FLOW / waterInlets);
					else if(offsetBlock == StarflightBlocks.OXYGEN_PIPE)
						((FluidPipeBlockEntity) offsetBlockEntity).changeStoredFluid(OXYGEN_FLOW / oxygenOutlets);
					else if(offsetBlock == StarflightBlocks.HYDROGEN_PIPE)
						((FluidPipeBlockEntity) offsetBlockEntity).changeStoredFluid(HYDROGEN_FLOW / hydrogenOutlets);
				}
				
				return;
			}
		}
		
		if(world.getBlockState(pos).get(ElectrolyzerBlock.LIT))
		{
			state = (BlockState) state.with(ElectrolyzerBlock.LIT, false);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			markDirty(world, pos, state);
		}
    }
}