package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import space.block.StarflightBlocks;
import space.inventory.ImplementedInventory;
import space.item.BatteryCellItem;
import space.screen.BatteryScreenHandler;

public class BatteryBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory
{
	private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
	
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
	
	public boolean canCharge()
	{
		if(this.inventory.size() == 0)
			return false;
		else
		{
			for(ItemStack stack : this.inventory)
			{
				if(!stack.isEmpty() && stack.getNbt().getDouble("charge") < ((BatteryCellItem) stack.getItem()).getMaxCharge())
					return true;
			}
		}
		
		return false;
	}
	
	public boolean hasAnyCharge()
	{
		if(this.inventory.size() == 0)
			return false;
		else
		{
			for(ItemStack stack : this.inventory)
			{
				if(!stack.isEmpty() && stack.getNbt().getDouble("charge") > 0)
					return true;
			}
		}
		
		return false;
	}
	
	public void charge(double amount)
	{
		int batteryCount = 0;
		
		for(ItemStack stack : this.inventory)
		{
			if(stack.isEmpty())
				continue;
			
			double charge = stack.getNbt().getDouble("charge");
			
			if(charge < ((BatteryCellItem) stack.getItem()).getMaxCharge())
				batteryCount++;
		}
		
		if(batteryCount == 0)
			return;
		
		for(ItemStack stack : this.inventory)
		{
			if(stack.isEmpty())
				continue;
			
			double maxCharge = ((BatteryCellItem) stack.getItem()).getMaxCharge();
			double previousCharge = stack.getNbt().getDouble("charge");
			
			if(previousCharge + (amount / batteryCount) >= maxCharge)
				stack.getNbt().putDouble("charge", maxCharge);
			else
				stack.getNbt().putDouble("charge", previousCharge + (amount / batteryCount));
		}
	}
	
	public void discharge(double amount)
	{
		int batteryCount = 0;
		
		for(ItemStack stack : this.inventory)
		{
			if(stack.isEmpty())
				continue;
			
			double charge = stack.getNbt().getDouble("charge");
			
			if(charge > 0)
				batteryCount++;
		}
		
		if(batteryCount == 0)
			return;
		
		for(ItemStack stack : this.inventory)
		{
			if(stack.isEmpty())
				continue;
			
			double previousCharge = stack.getNbt().getDouble("charge");
			
			if(previousCharge - (amount / batteryCount) <= 0)
				stack.getNbt().putDouble("charge", 0.0);
			else
				stack.getNbt().putDouble("charge", previousCharge - (amount / batteryCount));
		}
	}
	
	public double getChargeCapacity()
	{
		double d = 0.0;
		
		for(ItemStack stack : this.inventory)
		{
			if(stack.isEmpty())
				continue;
			else
				d += ((BatteryCellItem) stack.getItem()).getMaxCharge();
		}
		
		return d;
	}
	
	public double getCharge()
	{
		double d = 0.0;
		
		for(ItemStack stack : this.inventory)
		{
			if(stack.isEmpty())
				continue;
			else
				d += stack.getNbt().getDouble("charge");
		}
		
		return d;
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		Inventories.readNbt(nbt, this.inventory);
	}
	
	@Override
	public void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, this.inventory);
	}
}
