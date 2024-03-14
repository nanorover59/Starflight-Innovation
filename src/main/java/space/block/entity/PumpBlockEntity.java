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
import space.block.EnergyBlock;
import space.block.PumpBlock;
import space.block.StarflightBlocks;
import space.util.FluidResourceType;

public class PumpBlockEntity extends BlockEntity implements EnergyBlockEntity
{
	private double energy;
	private boolean hasWater;
	
	public PumpBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.PUMP_BLOCK_ENTITY, pos, state);
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
		else if(fluidType.getID() == FluidResourceType.WATER.getID() && world.getBlockEntity(position) instanceof WaterTankBlockEntity)
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
	
	public static void serverTick(World world, BlockPos pos, BlockState state, PumpBlockEntity blockEntity)
	{
		Direction direction = state.get(PumpBlock.FACING);
		BlockPos inletPos = pos.offset(direction.getOpposite());
		BlockEntity inletBlockEntity = world.getBlockEntity(inletPos);
		FluidResourceType fluid = null;
		double toTransfer = 0;
		
		if(inletBlockEntity == null)
			return;
		
		if(inletBlockEntity instanceof FluidPipeBlockEntity)
		{
			FluidPipeBlockEntity pipeBlockEntity = ((FluidPipeBlockEntity) inletBlockEntity);
			fluid = pipeBlockEntity.getFluidType();
			toTransfer = Math.min(fluid.getStorageDensity() / 25.0, pipeBlockEntity.getStoredFluid());
		}
		else if(inletBlockEntity instanceof ValveBlockEntity)
		{
			FluidTankControllerBlockEntity tankBlockEntity = ((ValveBlockEntity) inletBlockEntity).getFluidTankController();
			fluid = tankBlockEntity.getFluidType();
			toTransfer = Math.min(fluid.getStorageDensity() / 25.0, tankBlockEntity.getStoredFluid());
		}
		else if(world.getBlockEntity(inletPos) instanceof WaterTankBlockEntity)
		{
			WaterTankBlockEntity tankBlockEntity = ((WaterTankBlockEntity) world.getBlockEntity(inletPos));
			fluid = FluidResourceType.WATER;	
			toTransfer = Math.min(fluid.getStorageDensity() / 25.0, tankBlockEntity.getStoredFluid());
		}
		
		if(fluid == null || toTransfer == 0)
			return;
		
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		double remaining = recursiveSpread(world, pos, checkList, toTransfer, fluid, 2048);
		
		if(inletBlockEntity instanceof FluidPipeBlockEntity)
		{
			FluidPipeBlockEntity pipeBlockEntity = ((FluidPipeBlockEntity) inletBlockEntity);
			pipeBlockEntity.changeStoredFluid(toTransfer - remaining);
		}
		else if(inletBlockEntity instanceof ValveBlockEntity)
		{
			FluidTankControllerBlockEntity tankBlockEntity = ((ValveBlockEntity) inletBlockEntity).getFluidTankController();
			tankBlockEntity.changeStoredFluid(toTransfer - remaining);
		}
		else if(world.getBlockEntity(inletPos) instanceof WaterTankBlockEntity)
		{
			WaterTankBlockEntity tankBlockEntity = ((WaterTankBlockEntity) world.getBlockEntity(inletPos));
			tankBlockEntity.changeStoredFluid(toTransfer - remaining);
		}
    }
}