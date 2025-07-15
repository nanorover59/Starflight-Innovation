package space.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import space.item.BatteryCellItem;
import space.item.StarflightItems;

public class BatteryScreenHandler extends ScreenHandler
{
	private final Inventory inventory;
	private final PropertyDelegate propertyDelegate;

	public BatteryScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(2), new ArrayPropertyDelegate(1));
	}

	public BatteryScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate)
	{
		super(StarflightScreens.BATTERY_SCREEN_HANDLER, syncId);
		checkSize(inventory, 2);
		checkDataCount(propertyDelegate, 1);
		this.inventory = inventory;
		this.propertyDelegate = propertyDelegate;
		this.addSlot(new BatteryItemSlot(inventory, 0, 31, 38));
		this.addSlot(new BatteryItemSlot(inventory, 1, 129, 38));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
		}

		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
		
		this.addProperties(propertyDelegate);
	}

	@Override
	public boolean canUse(PlayerEntity player)
	{
		return this.inventory.canPlayerUse(player);
	}

	public ItemStack quickMove(PlayerEntity player, int slot)
	{
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot2 = this.slots.get(slot);
		
		if(slot2 != null && slot2.hasStack())
		{
			ItemStack itemStack2 = slot2.getStack();
			itemStack = itemStack2.copy();
			
			if(slot != 1 && slot != 0)
			{
				if(itemStack2.contains(StarflightItems.ENERGY))
				{
					if(itemStack2.get(StarflightItems.ENERGY) > 0)
					{
						if(!this.insertItem(itemStack2, 0, 1, false))
							return ItemStack.EMPTY;
					}
					else
					{
						if(!this.insertItem(itemStack2, 1, 2, false))
							return ItemStack.EMPTY;
					}
				}
				else if(slot >= 2 && slot < 29)
				{
					if(!this.insertItem(itemStack2, 29, 37, false))
						return ItemStack.EMPTY;
				}
				else if(slot >= 29 && slot < 37 && !this.insertItem(itemStack2, 2, 29, false))
					return ItemStack.EMPTY;
			}
			else if(!this.insertItem(itemStack2, 2, 37, false))
				return ItemStack.EMPTY;

			if(itemStack2.isEmpty())
				slot2.setStack(ItemStack.EMPTY);
			else
				slot2.markDirty();

			if(itemStack2.getCount() == itemStack.getCount())
				return ItemStack.EMPTY;

			slot2.onTakeItem(player, itemStack2);
		}

		return itemStack;
	}

	@Override
	public void onClosed(PlayerEntity player)
	{
		super.onClosed(player);
		this.inventory.onClose(player);
	}
	
	public int getCharge()
	{
		return this.propertyDelegate.get(0);
	}
	
	static class BatteryItemSlot extends Slot
	{
		public BatteryItemSlot(Inventory inventory, int i, int j, int k)
		{
			super(inventory, i, j, k);
		}

		@Override
		public boolean canInsert(ItemStack stack)
		{
			return BatteryItemSlot.matches(stack);
		}

		public static boolean matches(ItemStack stack)
		{
			return stack.getItem() instanceof BatteryCellItem;
		}

		@Override
		public int getMaxItemCount()
		{
			return 1;
		}
	}
}