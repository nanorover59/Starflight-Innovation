package space.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import space.item.StarflightItems;

public class BatteryScreenHandler extends ScreenHandler
{
	public static final int SLOT_COUNT = 5;
	private final Inventory inventory;

	public BatteryScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(5));
	}

	public BatteryScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory)
	{
		super(StarflightScreens.BATTERY_SCREEN_HANDLER, syncId);
		this.inventory = inventory;
		HopperScreenHandler.checkSize(inventory, 5);
		inventory.onOpen(playerInventory.player);

		for(int i = 0; i < 5; i++)
			this.addSlot(new BatteryItemSlot(inventory, i, 44 + i * 18, 20));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, i * 18 + 51));
		}
		
		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 109));
	}

	@Override
	public boolean canUse(PlayerEntity player)
	{
		return this.inventory.canPlayerUse(player);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index)
	{
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = (Slot) this.slots.get(index);
		
		if(slot != null && slot.hasStack())
		{
			ItemStack itemStack2 = slot.getStack();
			itemStack = itemStack2.copy();
			
			if(index < this.inventory.size() ? !this.insertItem(itemStack2, this.inventory.size(), this.slots.size(), true) : !this.insertItem(itemStack2, 0, this.inventory.size(), false))
				return ItemStack.EMPTY;
			
			if(itemStack2.isEmpty())
				slot.setStack(ItemStack.EMPTY);
			else
				slot.markDirty();
		}
		
		return itemStack;
	}

	@Override
	public void onClosed(PlayerEntity player)
	{
		super.onClosed(player);
		this.inventory.onClose(player);
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
			return stack.isOf(StarflightItems.BATTERY_CELL) && stack.hasNbt();
		}

		@Override
		public int getMaxItemCount()
		{
			return 1;
		}
	}
}
