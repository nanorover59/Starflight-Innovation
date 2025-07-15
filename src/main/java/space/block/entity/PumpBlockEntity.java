package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.FluidPipeBlock;
import space.block.PumpBlock;
import space.block.StarflightBlocks;
import space.util.BlockSearch;
import space.util.FluidResourceType;

public class PumpBlockEntity extends BlockEntity implements EnergyBlockEntity
{
	public ArrayList<BlockPos> fluidSources = new ArrayList<BlockPos>();
	public ArrayList<BlockPos> fluidSinks = new ArrayList<BlockPos>();
	private FluidResourceType fluidType;
	private long energy;
	private int onTimer = 0;
	
	public PumpBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.PUMP_BLOCK_ENTITY, pos, state);
	}
	
	public void fluidConnectionSearch()
	{
		Direction facing = getCachedState().get(PumpBlock.FACING);
		BlockPos frontPos = pos.offset(facing);
		BlockState blockState = world.getBlockState(frontPos);
		fluidType = null;
		
		if(blockState.getBlock() instanceof FluidPipeBlock)
			fluidType = ((FluidPipeBlock) blockState.getBlock()).fluid;
		else
		{
			BlockEntity blockEntity = world.getBlockEntity(frontPos);
			
			if(blockEntity instanceof FluidStorageBlockEntity)
			{
				for(FluidResourceType type : FluidResourceType.ALL)
				{
					if(((FluidStorageBlockEntity) blockEntity).getFluidCapacity(type) > 0)
						fluidType = type;
				}
			}
		}
		
		if(fluidType != null)
			BlockSearch.fluidConnectionSearch(world, pos, fluidType);
	}
	
	@Override
	public long getEnergyCapacity()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getEnergyCapacity();
	}
	
	@Override
	public long getEnergy()
	{
		return energy;
	}
	
	@Override
	public void setEnergy(long energy)
	{
		this.energy = energy;
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putString("fluidType", fluidType == null ? "null" : fluidType.getName());
		nbt.putLong("energy", this.energy);
		NbtList fluidSourcesListNBT = new NbtList();
		NbtList fluidSinksListNBT = new NbtList();
		
		for(BlockPos source : fluidSources)
			fluidSourcesListNBT.add(NbtHelper.fromBlockPos(source));
		
		for(BlockPos sink : fluidSinks)
			fluidSinksListNBT.add(NbtHelper.fromBlockPos(sink));

		nbt.put("fluidSources", fluidSourcesListNBT);
		nbt.put("fluidSinks", fluidSinksListNBT);
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.fluidType = FluidResourceType.getForName(nbt.getString("fluidType"));
		this.energy = nbt.getLong("energy");
		fluidSources.clear();
		fluidSinks.clear();
		NbtList fluidSourcesListNBT = nbt.getList("fluidSources", NbtList.INT_ARRAY_TYPE);
		NbtList fluidSinksListNBT = nbt.getList("fluidSinks", NbtList.INT_ARRAY_TYPE);
		
		for(int i = 0; i < fluidSourcesListNBT.size(); i++)
		{
			int[] array = fluidSourcesListNBT.getIntArray(i);
			fluidSources.add(new BlockPos(array[0], array[1], array[2]));
		}
		
		for(int i = 0; i < fluidSinksListNBT.size(); i++)
		{
			int[] array = fluidSinksListNBT.getIntArray(i);
			fluidSinks.add(new BlockPos(array[0], array[1], array[2]));
		}
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, PumpBlockEntity blockEntity)
	{
		long power = ((EnergyBlock) state.getBlock()).getInput();
		
		if(blockEntity.fluidType != null && blockEntity.removeEnergy(power, false) == power)
		{
			long flowRate = 20;
			long pull = pullFluid(world, blockEntity.fluidSources, flowRate, false, blockEntity.fluidType);
			
			if(state.get(PumpBlock.WATER) && blockEntity.fluidType == FluidResourceType.WATER)
				pull = flowRate;
			
			long push = pushFluid(world, blockEntity.fluidSinks, flowRate, false, blockEntity.fluidType);
			long transfer = Math.min(pull, push);
			
			if(transfer > 0)
			{
				pullFluid(world, blockEntity.fluidSources, transfer, true, blockEntity.fluidType);
				pushFluid(world, blockEntity.fluidSinks, transfer, true, blockEntity.fluidType);
				blockEntity.removeEnergy(power, true);
				blockEntity.onTimer = 5;
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
	
	public static long pullFluid(World world, ArrayList<BlockPos> storagePositions, long amount, boolean drain, FluidResourceType fluidType)
	{
		ArrayList<FluidStorageBlockEntity> storageList = new ArrayList<>();
		long sumStored = 0;
		
		for(BlockPos pos : storagePositions)
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);
			
			if(!(blockEntity instanceof FluidStorageBlockEntity))
				continue;
			
			FluidStorageBlockEntity fluidStorage = (FluidStorageBlockEntity) blockEntity;
			long stored = fluidStorage.getFluid(fluidType);
			
			if(stored > 0)
			{
				storageList.add(fluidStorage);
				sumStored += stored;
			}
		}
		
		if(storageList.isEmpty() || sumStored == 0)
			return 0;
		
		long pulled = 0;
	    long remainder = amount;
	    double fraction = (double) amount / (double) sumStored;
	    
		for(FluidStorageBlockEntity storage : storageList)
		{
			long stored = storage.getFluid(fluidType);
			long want = (long) (stored * fraction);
			long canDrain = storage.removeFluid(fluidType, want, drain);
			pulled += canDrain;
			remainder -= want;
		}
		
		for(FluidStorageBlockEntity storage : storageList)
		{
			if(remainder <= 0)
				break;
			
			if(storage.getFluid(fluidType) > 0)
			{
				storage.removeFluid(fluidType, 1, drain);
				pulled++;
				remainder--;
			}
		}
		
		return pulled;
	}
	
	public static long pushFluid(World world, ArrayList<BlockPos> storagePositions, long amount, boolean fill, FluidResourceType fluidType)
	{
		ArrayList<FluidStorageBlockEntity> storageList = new ArrayList<>();
		long sumCapacity = 0;
		
		for(BlockPos pos : storagePositions)
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);
			
			if(!(blockEntity instanceof FluidStorageBlockEntity))
				continue;
			
			FluidStorageBlockEntity fluidStorage = (FluidStorageBlockEntity) blockEntity;
			long capacity = fluidStorage.getFluidCapacity(fluidType);
			long stored = fluidStorage.getFluid(fluidType);
			
			if(stored < capacity)
			{
				storageList.add(fluidStorage);
				sumCapacity += capacity - stored;
			}
		}
		
		if(storageList.isEmpty() || sumCapacity == 0)
			return 0;
		
		long pushed = 0;
	    long remainder = amount;
	    double fraction = (double) amount / (double) sumCapacity;
	    
		for(FluidStorageBlockEntity storage : storageList)
		{
			long capacity = storage.getFluidCapacity(fluidType) - storage.getFluid(fluidType);
			long want = (long) (capacity * fraction);
			long canFill = storage.addFluid(fluidType, want, fill);
			pushed += canFill;
			remainder -= canFill;
		}
		
		for(FluidStorageBlockEntity storage : storageList)
		{
			if(remainder <= 0)
				break;
			
			if(storage.getFluidCapacity(fluidType) - storage.getFluid(fluidType) > 0)
			{
				storage.addFluid(fluidType, 1, fill);
				pushed++;
				remainder--;
			}
		}
		
		return pushed;
	}
}