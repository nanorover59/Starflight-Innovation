package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.ElectrolyzerBlock;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.screen.ElectrolyzerScreenHandler;
import space.util.FluidResourceType;

public class ElectrolyzerBlockEntity extends LockableContainerBlockEntity implements SidedInventory, EnergyBlockEntity, FluidStorageBlockEntity
{
	private static long WATER_FLOW = 18; // Mass of water to electrolyze per tick.
	
	public DefaultedList<ItemStack> inventory;
	public final PropertyDelegate propertyDelegate;
	public ArrayList<BlockPos> oxygenOutputs = new ArrayList<BlockPos>();
	public ArrayList<BlockPos> hydrogenOutputs = new ArrayList<BlockPos>();
	private long energy;
	private long water;
	private long oxygen;
	private long hydrogen;
	private long oxygenCapacity;
	private long hydrogenCapacity;
	private int onTimer = 0;
	
	public ElectrolyzerBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.ELECTROLYZER_BLOCK_ENTITY, pos, state);
		this.inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
		this.propertyDelegate = new PropertyDelegate()
		{
			public int get(int index)
			{
				switch(index)
				{
				case 0:
					return (int) ElectrolyzerBlockEntity.this.energy;
				case 1:
					return (int) ElectrolyzerBlockEntity.this.water;
				case 2:
					return (int) (oxygen & 0xFFFF);
				case 3:
					return (int) ((oxygen >> 16) & 0xFFFF);
				case 4:
					return (int) (hydrogen & 0xFFFF);
				case 5:
					return (int) ((hydrogen >> 16) & 0xFFFF);
				case 6:
					return (int) (oxygenCapacity & 0xFFFF);
				case 7:
					return (int) ((oxygenCapacity >> 16) & 0xFFFF);
				case 8:
					return (int) (hydrogenCapacity & 0xFFFF);
				case 9:
					return (int) ((hydrogenCapacity >> 16) & 0xFFFF);
				case 10:
					return ElectrolyzerBlockEntity.this.onTimer;
				default:
					return 0;
				}
			}

			public void set(int index, int value)
			{
				switch(index)
				{
				
				case 0:
					ElectrolyzerBlockEntity.this.energy = value;
					break;
				case 1:
					ElectrolyzerBlockEntity.this.water = value;
					break;
				case 2:
					ElectrolyzerBlockEntity.this.oxygen = (ElectrolyzerBlockEntity.this.oxygen & 0xFFFF0000L) | (value & 0xFFFFL);
					break;
				case 3:
					ElectrolyzerBlockEntity.this.oxygen = (ElectrolyzerBlockEntity.this.oxygen & 0x0000FFFFL) | ((long) (value & 0xFFFFL) << 16);
					break;
				case 4:
					ElectrolyzerBlockEntity.this.hydrogen = (ElectrolyzerBlockEntity.this.hydrogen & 0xFFFF0000L) | (value & 0xFFFFL);
					break;
				case 5:
					ElectrolyzerBlockEntity.this.hydrogen = (ElectrolyzerBlockEntity.this.hydrogen & 0x0000FFFFL) | ((long) (value & 0xFFFFL) << 16);
					break;
				case 6:
					ElectrolyzerBlockEntity.this.oxygenCapacity = (ElectrolyzerBlockEntity.this.oxygenCapacity & 0xFFFF0000L) | (value & 0xFFFFL);
					break;
				case 7:
					ElectrolyzerBlockEntity.this.oxygenCapacity = (ElectrolyzerBlockEntity.this.oxygenCapacity & 0x0000FFFFL) | ((long) (value & 0xFFFFL) << 16);
					break;
				case 8:
					ElectrolyzerBlockEntity.this.hydrogenCapacity = (ElectrolyzerBlockEntity.this.hydrogenCapacity & 0xFFFF0000L) | (value & 0xFFFFL);
					break;
				case 9:
					ElectrolyzerBlockEntity.this.hydrogenCapacity = (ElectrolyzerBlockEntity.this.hydrogenCapacity & 0x0000FFFFL) | ((long) (value & 0xFFFFL) << 16);
					break;
				case 10:
					ElectrolyzerBlockEntity.this.onTimer = value;
				}

			}

			public int size()
			{
				return 11;
			}
		};
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
	public long getFluidCapacity(FluidResourceType fluidType)
	{
		if(fluidType == FluidResourceType.WATER)
			return FluidResourceType.WATER.getStorageDensity();
		else
			return 0;
	}
	
	@Override
	public long getFluid(FluidResourceType fluidType)
	{
		if(fluidType == FluidResourceType.WATER)
			return water;
		else
			return 0;
	}

	@Override
	public void setFluid(FluidResourceType fluidType, long fluid)
	{
		if(fluidType == FluidResourceType.WATER)
			this.water = fluid;
	}
	
	private void updateOxygenHydrogenInfo()
	{
		oxygen = 0;
		hydrogen = 0;
		oxygenCapacity = 0;
		hydrogenCapacity = 0;
		
		for(BlockPos pos : oxygenOutputs)
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);
			
			if(!(blockEntity instanceof FluidStorageBlockEntity))
				continue;
			
			FluidStorageBlockEntity fluidStorage = (FluidStorageBlockEntity) blockEntity;
			long stored = fluidStorage.getFluid(FluidResourceType.OXYGEN);
			long capacity = fluidStorage.getFluidCapacity(FluidResourceType.OXYGEN);
			oxygen += stored;
			oxygenCapacity += capacity;
		}
		
		for(BlockPos pos : hydrogenOutputs)
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);
			
			if(!(blockEntity instanceof FluidStorageBlockEntity))
				continue;
			
			FluidStorageBlockEntity fluidStorage = (FluidStorageBlockEntity) blockEntity;
			long stored = fluidStorage.getFluid(FluidResourceType.HYDROGEN);
			long capacity = fluidStorage.getFluidCapacity(FluidResourceType.HYDROGEN);
			hydrogen += stored;
			hydrogenCapacity += capacity;
		}
	}
	
	@Override
	public int size()
	{
		return this.inventory.size();
	}

	public int[] getAvailableSlots(Direction side)
	{
		return new int[] {0, 1};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, Direction dir)
	{
		return this.isValid(slot, stack);
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir)
	{
		return false;
	}

	@Override
	protected Text getContainerName()
	{
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	protected DefaultedList<ItemStack> getHeldStacks()
	{
		return this.inventory;
	}

	@Override
	protected void setHeldStacks(DefaultedList<ItemStack> inventory)
	{
		this.inventory = inventory;
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		return new ElectrolyzerScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		Inventories.writeNbt(nbt, this.inventory, registryLookup);
		nbt.putLong("energy", this.energy);
		nbt.putLong("water", this.water);
		nbt.putLong("oxygen", this.oxygen);
		nbt.putLong("hydrogen", this.hydrogen);
		nbt.putLong("oxygenCapacity", this.oxygenCapacity);
		nbt.putLong("hydrogenCapacity", this.hydrogenCapacity);
		NbtList oxygenOutputsListNBT = new NbtList();
		NbtList hydrogenOutputsListNBT = new NbtList();
		
		for(BlockPos oxygenOutput : oxygenOutputs)
			oxygenOutputsListNBT.add(NbtHelper.fromBlockPos(oxygenOutput));
		
		for(BlockPos hydrogenOutput : hydrogenOutputs)
			hydrogenOutputsListNBT.add(NbtHelper.fromBlockPos(hydrogenOutput));

		nbt.put("oxygenOutputs", oxygenOutputsListNBT);
		nbt.put("hydrogenOutputs", hydrogenOutputsListNBT);
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, this.inventory, registryLookup);
		this.energy = nbt.getLong("energy");
		this.water = nbt.getLong("water");
		this.oxygen = nbt.getLong("oxygen");
		this.hydrogen = nbt.getLong("hydrogen");
		this.oxygenCapacity = nbt.getLong("oxygenCapacity");
		this.hydrogenCapacity = nbt.getLong("hydrogenCapacity");
		oxygenOutputs.clear();
		hydrogenOutputs.clear();
		NbtList oxygenOutputsListNBT = nbt.getList("oxygenOutputs", NbtList.INT_ARRAY_TYPE);
		NbtList hydrogenOutputsListNBT = nbt.getList("hydrogenOutputs", NbtList.INT_ARRAY_TYPE);
		
		for(int i = 0; i < oxygenOutputsListNBT.size(); i++)
		{
			int[] array = oxygenOutputsListNBT.getIntArray(i);
			oxygenOutputs.add(new BlockPos(array[0], array[1], array[2]));
		}
		
		for(int i = 0; i < hydrogenOutputsListNBT.size(); i++)
		{
			int[] array = hydrogenOutputsListNBT.getIntArray(i);
			hydrogenOutputs.add(new BlockPos(array[0], array[1], array[2]));
		}
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, ElectrolyzerBlockEntity blockEntity)
	{
		long power = ((EnergyBlock) state.getBlock()).getInput();
		blockEntity.updateOxygenHydrogenInfo();
		
		if(blockEntity.water == 0 && blockEntity.inventory.get(0).isOf(Items.WATER_BUCKET))
		{
			blockEntity.water = FluidResourceType.WATER.getStorageDensity();
			blockEntity.inventory.set(0, new ItemStack(Items.BUCKET));
		}
		
		if(blockEntity.water > 0 && blockEntity.oxygen < blockEntity.oxygenCapacity && blockEntity.hydrogen < blockEntity.hydrogenCapacity && blockEntity.removeEnergy(power, true) == power)
		{
			PumpBlockEntity.pushFluid(world, blockEntity.oxygenOutputs, (long) (WATER_FLOW * (8.0 / 9.0)), true, FluidResourceType.OXYGEN);
			PumpBlockEntity.pushFluid(world, blockEntity.hydrogenOutputs, (long) (WATER_FLOW * (1.0 / 9.0)), true, FluidResourceType.HYDROGEN);
			blockEntity.removeFluid(FluidResourceType.WATER, WATER_FLOW, true);
			blockEntity.onTimer = 5;
		}
		
		if(world.getBlockState(pos).get(ElectrolyzerBlock.LIT) != blockEntity.onTimer > 0)
		{
			state = (BlockState) state.with(ElectrolyzerBlock.LIT, blockEntity.onTimer > 0);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			markDirty(world, pos, state);
		}
		
		if(blockEntity.onTimer > 0)
			blockEntity.onTimer--;
		
		blockEntity.dischargeItem(blockEntity.inventory.get(1), power * 2);
    }
}