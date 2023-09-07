package space.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import space.item.ArrivalCardItem;
import space.item.NavigationCardItem;

public class RocketControllerScreenHandler extends ScreenHandler
{
	private final Inventory inventory;

	public RocketControllerScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(2));
	}

	public RocketControllerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory)
	{
		super(StarflightScreens.ROCKET_CONTROLLER_SCREEN_HANDLER, syncId);
		checkSize(inventory, 2);
		this.inventory = inventory;
		inventory.onOpen(playerInventory.player);
		int m;
		int l;
		
		// Block Inventory
		this.addSlot(new NavigationCardSlot(inventory, 0, 8, 18));
		this.addSlot(new ArrivalCardSlot(inventory, 1, 8, 40));
		
		// Player Inventory
		for(m = 0; m < 3; ++m)
		{
			for(l = 0; l < 9; ++l)
				this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 140 + m * 18));
		}
		
		// Player Hotbar
		for(m = 0; m < 9; ++m)
			this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 198));
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
	
	static class NavigationCardSlot extends Slot
	{
		public NavigationCardSlot(Inventory inventory, int i, int j, int k)
		{
			super(inventory, i, j, k);
		}

		@Override
		public boolean canInsert(ItemStack stack)
		{
			return NavigationCardSlot.matches(stack);
		}

		public static boolean matches(ItemStack stack)
		{
			return stack.getItem() instanceof NavigationCardItem && stack.hasNbt();
		}

		@Override
		public int getMaxItemCount()
		{
			return 1;
		}
	}
	
	static class ArrivalCardSlot extends Slot
	{
		public ArrivalCardSlot(Inventory inventory, int i, int j, int k)
		{
			super(inventory, i, j, k);
		}

		@Override
		public boolean canInsert(ItemStack stack)
		{
			return ArrivalCardSlot.matches(stack);
		}

		public static boolean matches(ItemStack stack)
		{
			return stack.getItem() instanceof ArrivalCardItem && stack.hasNbt();
		}

		@Override
		public int getMaxItemCount()
		{
			return 1;
		}
	}
}
