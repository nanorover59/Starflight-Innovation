package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.inventory.ImplementedInventory;
import space.item.StarflightItems;
import space.screen.BatteryScreenHandler;

public class BatteryBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, EnergyBlockEntity
{
	private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
	private ArrayList<BlockPos> outputs = new ArrayList<BlockPos>();
	
	public BatteryBlockEntity(BlockPos blockPos, BlockState blockState)
	{
        super(StarflightBlocks.BATTERY_BLOCK_ENTITY, blockPos, blockState);
    }

	@Override
	public Text getDisplayName()
	{
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	public DefaultedList<ItemStack> getItems()
	{
		return inventory;
	}
	
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player)
	{
		return new BatteryScreenHandler(syncId, playerInventory, this);
	}
	
	@Override
	public double getOutput()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getOutput() / world.getTickManager().getTickRate();
	}
	
	@Override
	public double getInput()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getInput() / world.getTickManager().getTickRate();
	}
	
	@Override
	public double getEnergyStored()
	{
		double d = 0.0;
		
		for(ItemStack stack : this.inventory)
		{
			if(stack.isEmpty() || !stack.contains(StarflightItems.ENERGY))
				continue;
			else
				d += stack.get(StarflightItems.ENERGY);
		}
		
		return d;
	}

	@Override
	public double getEnergyCapacity()
	{
		double d = 0.0;
		
		for(ItemStack stack : this.inventory)
		{
			if(stack.isEmpty() || !stack.contains(StarflightItems.MAX_ENERGY))
				continue;
			else
				d += stack.get(StarflightItems.MAX_ENERGY);
		}
		
		return d;
	}

	@Override
	public double changeEnergy(double amount)
	{
		int batteryCount = 0;
		
		if(amount > 0.0)
		{
			for(ItemStack stack : this.inventory)
			{
				if(stack.isEmpty() || !stack.contains(StarflightItems.ENERGY) || !stack.contains(StarflightItems.MAX_ENERGY))
					continue;
				
				double charge = stack.get(StarflightItems.ENERGY);
				
				if(charge < stack.get(StarflightItems.MAX_ENERGY))
					batteryCount++;
			}
			
			if(batteryCount == 0)
				return 0;
			
			float fraction = (float) (amount / batteryCount);
			
			for(ItemStack stack : this.inventory)
			{
				if(stack.isEmpty() || !stack.contains(StarflightItems.ENERGY) || !stack.contains(StarflightItems.MAX_ENERGY))
					continue;
				
				float maxCharge = stack.get(StarflightItems.MAX_ENERGY);
				float previousCharge = stack.get(StarflightItems.ENERGY);
				
				if(previousCharge + fraction > maxCharge)
				{
					stack.set(StarflightItems.ENERGY, maxCharge);
					amount -= (previousCharge + fraction) - maxCharge;
				}
				else
					stack.set(StarflightItems.ENERGY, previousCharge + fraction);
			}
		}
		else
		{
			for(ItemStack stack : this.inventory)
			{
				if(stack.isEmpty() || !stack.contains(StarflightItems.ENERGY) || !stack.contains(StarflightItems.MAX_ENERGY))
					continue;
				
				double charge = stack.get(StarflightItems.ENERGY);
				
				if(charge > 0.0)
					batteryCount++;
			}
			
			if(batteryCount == 0)
				return 0;
			
			float fraction = (float) (amount / batteryCount);
			
			for(ItemStack stack : this.inventory)
			{
				if(stack.isEmpty() || !stack.contains(StarflightItems.ENERGY) || !stack.contains(StarflightItems.MAX_ENERGY))
					continue;
				
				float previousCharge = stack.get(StarflightItems.ENERGY);
				
				if(previousCharge + fraction < 0)
				{
					stack.set(StarflightItems.ENERGY, 0.0f);
					amount -= previousCharge + fraction;
				}
				else
					stack.set(StarflightItems.ENERGY, previousCharge + fraction);
			}
		}
		
		world.updateComparators(pos, world.getBlockState(pos).getBlock());
		return amount;
	}

	@Override
	public ArrayList<BlockPos> getOutputs()
	{
		return outputs;
	}

	@Override
	public void addOutput(BlockPos output)
	{
		outputs.add(output);
	}

	@Override
	public void clearOutputs()
	{
		outputs.clear();
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
		EnergyBlockEntity.transferEnergy(blockEntity, blockEntity.getOutput());
	}
}