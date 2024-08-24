package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.FluidPipeBlock;
import space.block.FluidUtilityBlock;
import space.block.PumpBlock;
import space.block.StarflightBlocks;
import space.block.ValveBlock;
import space.block.WaterTankBlock;
import space.util.FluidResourceType;

public class PumpBlockEntity extends BlockEntity implements EnergyBlockEntity
{
	private double energy;
	private int onTimer = 0;
	
	public PumpBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.PUMP_BLOCK_ENTITY, pos, state);
	}
	
	public static double recursiveSpread(World world, BlockPos position, ArrayList<BlockPos> checkList, double toSpread, FluidResourceType fluidType, int limit)
	{
		if(checkList.contains(position) || checkList.size() >= limit)
			return toSpread;
		
		BlockState blockState = world.getBlockState(position);
		
		if(blockState.getBlock() instanceof FluidPipeBlock)
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
					{
						if(((FluidPipeBlock) blockState.getBlock()).isConnected(world, position, blockState, direction))
							toSpread = recursiveSpread(world, position.offset(direction), checkList, toSpread, fluidType, limit);
					}
				}
			}
		}
		else if(blockState.getBlock() instanceof ValveBlock)
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
		else if(fluidType.getID() == FluidResourceType.WATER.getID() && blockState.getBlock() instanceof WaterTankBlock)
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
				
				for(Direction direction : Direction.values())
				{
					BlockPos offsetPos = position.offset(direction);
					BlockState offsetState = world.getBlockState(offsetPos);
					
					if((offsetState.getBlock() instanceof WaterTankBlock && (direction == Direction.UP || direction == Direction.DOWN))
					|| (offsetState.getBlock() instanceof FluidPipeBlock && ((FluidPipeBlock) offsetState.getBlock()).isConnected(world, offsetPos, offsetState, direction.getOpposite())))
						toSpread = recursiveSpread(world, offsetPos, checkList, toSpread, fluidType, limit);
				}
			}
			
			blockEntity.markDirty();
			world.updateListeners(position, blockState, blockState, Block.NOTIFY_LISTENERS);
		}
		
		return toSpread;
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
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putDouble("energy", this.energy);
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.energy = nbt.getDouble("energy");
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, PumpBlockEntity blockEntity)
	{
		Direction direction = state.get(PumpBlock.FACING);
		BlockPos inletPos = pos.offset(direction.getOpposite());
		Block inletBlock = world.getBlockState(inletPos).getBlock();
		FluidResourceType fluid = null;
		double toTransfer = 0;
		
		if(blockEntity.energy < blockEntity.getInput())
		{
			if(state.get(PumpBlock.LIT))
			{
				state = (BlockState) state.with(PumpBlock.LIT, false);
				world.setBlockState(pos, state, Block.NOTIFY_ALL);
				markDirty(world, pos, state);
			}
			
			return;
		}
		
		if(inletBlock instanceof FluidUtilityBlock)
		{
			BlockEntity inletBlockEntity = world.getBlockEntity(inletPos);
			
			if(inletBlockEntity instanceof FluidPipeBlockEntity)
			{
				FluidPipeBlockEntity pipeBlockEntity = ((FluidPipeBlockEntity) inletBlockEntity);
				fluid = pipeBlockEntity.getFluidType();
				toTransfer = Math.min(fluid.getStorageDensity() / 18.0, pipeBlockEntity.getStoredFluid());
			}
			else if(inletBlockEntity instanceof ValveBlockEntity)
			{
				FluidTankControllerBlockEntity tankBlockEntity = ((ValveBlockEntity) inletBlockEntity).getFluidTankController();
				fluid = tankBlockEntity.getFluidType();
				toTransfer = Math.min(fluid.getStorageDensity() / 18.0, tankBlockEntity.getStoredFluid());
			}
			else if(world.getBlockEntity(inletPos) instanceof WaterTankBlockEntity)
			{
				WaterTankBlockEntity tankBlockEntity = ((WaterTankBlockEntity) world.getBlockEntity(inletPos));
				fluid = FluidResourceType.WATER;	
				toTransfer = Math.min(fluid.getStorageDensity() / 18.0, tankBlockEntity.getStoredFluid());
			}
		}
		else if(state.get(PumpBlock.WATER))
		{
			fluid = FluidResourceType.WATER;
			toTransfer = fluid.getStorageDensity() / 18.0;
		}
		
		if(fluid == null || toTransfer < 1e-4)
		{
			if(state.get(PumpBlock.LIT))
			{
				state = (BlockState) state.with(PumpBlock.LIT, false);
				world.setBlockState(pos, state, Block.NOTIFY_ALL);
				markDirty(world, pos, state);
			}
			
			return;
		}
		
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		double remaining = recursiveSpread(world, pos.offset(direction), checkList, toTransfer, fluid, 2048);
		
		if(remaining < toTransfer)
		{
			BlockEntity inletBlockEntity = world.getBlockEntity(inletPos);
			blockEntity.changeEnergy(-blockEntity.getInput());
			blockEntity.onTimer = 5;
			
			if(inletBlockEntity instanceof FluidPipeBlockEntity)
			{
				FluidPipeBlockEntity pipeBlockEntity = ((FluidPipeBlockEntity) inletBlockEntity);
				pipeBlockEntity.changeStoredFluid(remaining - toTransfer);
			}
			else if(inletBlockEntity instanceof ValveBlockEntity)
			{
				FluidTankControllerBlockEntity tankBlockEntity = ((ValveBlockEntity) inletBlockEntity).getFluidTankController();
				tankBlockEntity.changeStoredFluid(remaining - toTransfer);
			}
			else if(inletBlockEntity instanceof WaterTankBlockEntity)
			{
				WaterTankBlockEntity tankBlockEntity = ((WaterTankBlockEntity) inletBlockEntity);
				tankBlockEntity.changeStoredFluid(remaining - toTransfer);
				BlockState inletState = world.getBlockState(inletPos);
				blockEntity.markDirty();
				world.updateListeners(inletPos, inletState, inletState, Block.NOTIFY_LISTENERS);
			}
		}
		
		if(state.get(PumpBlock.LIT) != blockEntity.onTimer > 0)
		{
			state = (BlockState) state.with(PumpBlock.LIT, blockEntity.onTimer > 0);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			markDirty(world, pos, state);
		}
		
		if(blockEntity.onTimer > 0)
			blockEntity.onTimer--;
    }
}