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
import space.block.ElectrolyzerBlock;
import space.block.EnergyBlock;
import space.block.FluidUtilityBlock;
import space.block.StarflightBlocks;
import space.util.FluidResourceType;

public class ElectrolyzerBlockEntity extends BlockEntity implements EnergyBlockEntity
{
	private static double WATER_FLOW = 20.0; // Mass of water to electrolyze per tick.
	private static double OXYGEN_FLOW = WATER_FLOW * (8.0 / 9.0);
	private static double HYDROGEN_FLOW = WATER_FLOW * (1.0 / 9.0);
	private double energy;
	private int onTimer = 0;
	
	public ElectrolyzerBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.ELECTROLYZER_BLOCK_ENTITY, pos, state);
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
	
	public static void serverTick(World world, BlockPos pos, BlockState state, ElectrolyzerBlockEntity blockEntity)
	{
		if(blockEntity.energy > 0)
		{
			BlockPos waterInletPos = null;
			BlockPos oxygenOutletPos = null;
			BlockPos hydrogenOutletPos = null;
			
			for(Direction direction : Direction.values())
			{
				BlockPos offset = pos.offset(direction);
				Block offsetBlock = world.getBlockState(offset).getBlock();
				
				if(offsetBlock instanceof FluidUtilityBlock)
				{
					BlockEntity offsetBlockEntity = world.getBlockEntity(offset);
					
					if(offsetBlockEntity instanceof FluidPipeBlockEntity)
					{
						FluidPipeBlockEntity pipeBlockEntity = ((FluidPipeBlockEntity) offsetBlockEntity);
						
						if(pipeBlockEntity.getFluidType() == FluidResourceType.WATER && pipeBlockEntity.getStoredFluid() > WATER_FLOW)
							waterInletPos = offset;
						else if(pipeBlockEntity.getFluidType() == FluidResourceType.OXYGEN)
							oxygenOutletPos = offset;
						else if(pipeBlockEntity.getFluidType() == FluidResourceType.HYDROGEN)
							hydrogenOutletPos = offset;
					}
					else if(offsetBlockEntity instanceof ValveBlockEntity)
					{
						ValveBlockEntity valveBlockEntity = (ValveBlockEntity) offsetBlockEntity;
						FluidTankControllerBlockEntity tankBlockEntity = ((ValveBlockEntity) offsetBlockEntity).getFluidTankController();
						
						if(valveBlockEntity.getMode() == 0 && tankBlockEntity.getStoredFluid() < tankBlockEntity.getStorageCapacity())
						{
							if(tankBlockEntity.getFluidType() == FluidResourceType.OXYGEN)
								oxygenOutletPos = offset;
							else if(tankBlockEntity.getFluidType() == FluidResourceType.HYDROGEN)
								hydrogenOutletPos = offset;
						}
					}
					else if(offsetBlockEntity instanceof WaterTankBlockEntity)
					{
						WaterTankBlockEntity tankBlockEntity = ((WaterTankBlockEntity) offsetBlockEntity);
						
						if(tankBlockEntity.getStoredFluid() > WATER_FLOW)
							waterInletPos = offset;
					}
				}
				
				if(waterInletPos != null && oxygenOutletPos != null && hydrogenOutletPos != null)
					break;
			}
			
			if(waterInletPos != null && oxygenOutletPos != null && hydrogenOutletPos != null)
			{
				ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
				double oxygenRemaining = PumpBlockEntity.recursiveSpread(world, oxygenOutletPos, checkList, OXYGEN_FLOW, FluidResourceType.OXYGEN, 2048);
				checkList.clear();
				double hydrogenRemaining = PumpBlockEntity.recursiveSpread(world, hydrogenOutletPos, checkList, HYDROGEN_FLOW, FluidResourceType.HYDROGEN, 2048);
				
				if(oxygenRemaining < OXYGEN_FLOW || hydrogenRemaining < HYDROGEN_FLOW)
				{
					BlockEntity waterBlockEntity = world.getBlockEntity(waterInletPos);
					blockEntity.changeEnergy(-blockEntity.getInput());
					blockEntity.onTimer = 5;
					
					if(waterBlockEntity instanceof FluidPipeBlockEntity)
					{
						FluidPipeBlockEntity pipeBlockEntity = ((FluidPipeBlockEntity) waterBlockEntity);
						
						if(pipeBlockEntity.getFluidType() == FluidResourceType.WATER)
							pipeBlockEntity.changeStoredFluid(-WATER_FLOW);
					}
					else if(waterBlockEntity instanceof WaterTankBlockEntity)
					{
						WaterTankBlockEntity tankBlockEntity = ((WaterTankBlockEntity) waterBlockEntity);
						tankBlockEntity.changeStoredFluid(-WATER_FLOW);
					}
				}
			}
		}
		
		if(world.getBlockState(pos).get(ElectrolyzerBlock.LIT) != blockEntity.onTimer > 0)
		{
			state = (BlockState) state.with(ElectrolyzerBlock.LIT, blockEntity.onTimer > 0);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			markDirty(world, pos, state);
		}
		
		if(blockEntity.onTimer > 0)
			blockEntity.onTimer--;
    }
}