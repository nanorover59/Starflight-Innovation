package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.screen.BatteryScreenHandler;

public class BatteryBlockEntity extends LockableContainerBlockEntity implements SidedInventory, EnergyBlockEntity
{
	private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
	private ArrayList<BlockPos> outputs = new ArrayList<BlockPos>();
	protected final PropertyDelegate propertyDelegate;
	private long energy;
	
	public BatteryBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.BATTERY_BLOCK_ENTITY, blockPos, blockState);
		this.inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
		this.propertyDelegate = new PropertyDelegate()
		{
			public int get(int index)
			{
				switch(index)
				{
				case 0:
					return (int) BatteryBlockEntity.this.energy;
				default:
					return 0;
				}
			}

			public void set(int index, int value)
			{
				switch(index)
				{
				case 0:
					BatteryBlockEntity.this.energy = value;
					break;
				}

			}

			public int size()
			{
				return 1;
			}
		};
    }
	
	@Override
	public int size()
	{
		return 2;
	}

	@Override
	public int[] getAvailableSlots(Direction side)
	{
		return new int[] {0, 1};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, Direction dir)
	{
		return false;
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
		return new BatteryScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
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
	public ArrayList<BlockPos> getOutputs()
	{
		return outputs;
	}
	
	@Override
	public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		Inventories.writeNbt(nbt, this.inventory, registryLookup);
		EnergyBlockEntity.outputsToNBT(outputs, nbt);
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		Inventories.readNbt(nbt, this.inventory, registryLookup);
		outputs.addAll(EnergyBlockEntity.outputsFromNBT(nbt));
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, BatteryBlockEntity blockEntity)
	{
		ItemStack dischargeStack = (ItemStack) blockEntity.inventory.get(0);
		ItemStack chargeStack = (ItemStack) blockEntity.inventory.get(1);
		blockEntity.dischargeItem(dischargeStack, ((EnergyBlock) blockEntity.getCachedState().getBlock()).getInput());
		blockEntity.chargeItem(chargeStack, ((EnergyBlock) blockEntity.getCachedState().getBlock()).getOutput());
		blockEntity.transferEnergy(((EnergyBlock) state.getBlock()).getOutput());
	}
}