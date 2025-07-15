package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import space.item.StarflightItems;

public interface EnergyBlockEntity
{
	long getEnergyCapacity();

	long getEnergy();

	void setEnergy(long energy);

	default ArrayList<BlockPos> getOutputs()
	{
		return null;
	}

	default void addOutput(BlockPos output) {}

	default long addEnergy(long amount, boolean change)
	{
		long energy = getEnergy();
		long newEnergy = energy + amount;
		energy = MathHelper.clamp(newEnergy, 0, getEnergyCapacity());

		if(change)
			setEnergy(energy);

		return amount - (newEnergy - energy);
	}

	default long removeEnergy(long amount, boolean change)
	{
		return -addEnergy(-amount, change);
	}
	
	default void chargeItem(ItemStack stack, long amount)
	{
		if(!stack.contains(StarflightItems.ENERGY) || !stack.contains(StarflightItems.MAX_ENERGY))
			return;
		
		amount = Math.min(amount, Math.min(getEnergy(), stack.get(StarflightItems.MAX_ENERGY) - stack.get(StarflightItems.ENERGY)));
		
		if(amount == 0)
			return;
		
		long transferred = removeEnergy(amount, true);
		stack.set(StarflightItems.ENERGY, stack.get(StarflightItems.ENERGY) + (int) transferred);
	}
	
	default void dischargeItem(ItemStack stack, long amount)
	{
		if(!stack.contains(StarflightItems.ENERGY) || !stack.contains(StarflightItems.MAX_ENERGY))
			return;
		
		amount = Math.min(amount, Math.min(getEnergyCapacity() - getEnergy(), stack.get(StarflightItems.ENERGY)));
		
		if(amount == 0)
			return;
		
		long transferred = addEnergy(amount, true);
		stack.set(StarflightItems.ENERGY, stack.get(StarflightItems.ENERGY) - (int) transferred);
	}
	
	/**
	 * Attempt to push the given amount of energy to any connected outputs.
	 */
	default void transferEnergy(long amount)
	{
		amount = Math.min(amount, getEnergy());
		
		if(amount == 0)
			return;
		
		ArrayList<EnergyBlockEntity> storageList = new ArrayList<>();
		long sumCapacity = 0;

		for(BlockPos pos : getOutputs())
		{
			BlockEntity blockEntity = ((BlockEntity) this).getWorld().getBlockEntity(pos);

			if(!(blockEntity instanceof EnergyBlockEntity))
				continue;

			EnergyBlockEntity energyStorage = (EnergyBlockEntity) blockEntity;
			long capacity = energyStorage.getEnergyCapacity();
			long stored = energyStorage.getEnergy();

			if(stored < capacity)
			{
				storageList.add(energyStorage);
				sumCapacity += capacity - stored;
			}
		}

		if(storageList.isEmpty() || sumCapacity == 0)
			return;

		long pushed = 0;
		long remainder = amount;
		double fraction = (double) amount / (double) sumCapacity;

		for(EnergyBlockEntity storage : storageList)
		{
			long capacity = storage.getEnergyCapacity() - storage.getEnergy();
			long want = (long) (capacity * fraction);
			long canPush = storage.addEnergy(want, true);
			pushed += canPush;
			remainder -= canPush;
		}

		for(EnergyBlockEntity storage : storageList)
		{
			if(remainder <= 0)
				break;

			if(storage.getEnergyCapacity() - storage.getEnergy() > 0)
			{
				storage.addEnergy(1, true);
				pushed++;
				remainder--;
			}
		}

		removeEnergy(pushed, true);
	}

	/**
	 * Attempt to push the given amount of energy to any connected outputs.
	 */
	/*public static long transferEnergy(World world, EnergyBlockEntity source, long amount, boolean change)
	{
		ArrayList<EnergyBlockEntity> storageList = new ArrayList<>();
		long sumCapacity = 0;

		for(BlockPos pos : source.getOutputs())
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);

			if(!(blockEntity instanceof EnergyBlockEntity))
				continue;

			EnergyBlockEntity energyStorage = (EnergyBlockEntity) blockEntity;
			long capacity = energyStorage.getEnergyCapacity();
			long stored = energyStorage.getEnergy();

			if(stored < capacity)
			{
				storageList.add(energyStorage);
				sumCapacity += capacity - stored;
			}
		}

		if(storageList.isEmpty() || sumCapacity == 0)
			return 0;

		long pushed = 0;
		long remainder = amount;
		double fraction = (double) amount / (double) sumCapacity;

		for(EnergyBlockEntity storage : storageList)
		{
			long capacity = storage.getEnergyCapacity() - storage.getEnergy();
			long want = (long) (capacity * fraction);
			long canPush = storage.addEnergy(want, change);
			pushed += canPush;
			remainder -= canPush;
		}

		for(EnergyBlockEntity storage : storageList)
		{
			if(remainder <= 0)
				break;

			if(storage.getEnergyCapacity() - storage.getEnergy() > 0)
			{
				storage.addEnergy(1, change);
				pushed++;
				remainder--;
			}
		}

		source.removeEnergy(pushed, change);
		return pushed;
	}*/

	public static void outputsToNBT(ArrayList<BlockPos> outputs, NbtCompound nbt)
	{
		NbtList outputListNBT = new NbtList();

		for(BlockPos output : outputs)
			outputListNBT.add(NbtHelper.fromBlockPos(output));

		nbt.put("outputs", outputListNBT);
	}

	public static ArrayList<BlockPos> outputsFromNBT(NbtCompound nbt)
	{
		ArrayList<BlockPos> outputs = new ArrayList<BlockPos>();
		NbtList outputListNBT = nbt.getList("outputs", NbtList.INT_ARRAY_TYPE);

		for(int i = 0; i < outputListNBT.size(); i++)
		{
			int[] array = outputListNBT.getIntArray(i);
			outputs.add(new BlockPos(array[0], array[1], array[2]));
		}

		return outputs;
	}
}