package space.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class ElectrolyzerScreenHandler extends ScreenHandler
{
	private final Inventory inventory;
	private final PropertyDelegate propertyDelegate;
	protected final World world;

	public ElectrolyzerScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(2), new ArrayPropertyDelegate(11));
	}

	public ElectrolyzerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate)
	{
		super(StarflightScreens.ELECTROLYZER_SCREEN_HANDLER, syncId);
		checkSize(inventory, 2);
		checkDataCount(propertyDelegate, 7);
		this.inventory = inventory;
		this.propertyDelegate = propertyDelegate;
		this.world = playerInventory.player.getWorld();
		this.addSlot(new Slot(inventory, 0, 26, 18));
		this.addSlot(new Slot(inventory, 1, 26, 54));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
		}
 
		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));

		this.addProperties(propertyDelegate);
	}

	public boolean canUse(PlayerEntity player)
	{
		return this.inventory.canPlayerUse(player);
	}

	public ItemStack quickMove(PlayerEntity player, int index)
	{
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = (Slot) this.slots.get(index);
		
		if(slot != null && slot.hasStack())
		{
			ItemStack itemStack2 = slot.getStack();
			itemStack = itemStack2.copy();

			if(itemStack2.isOf(Items.WATER_BUCKET))
			{
				if(!this.insertItem(itemStack2, 0, 0, false))
					return ItemStack.EMPTY;
			}
			else if(index > 0 && index < 28)
			{
				if(!this.insertItem(itemStack2, 28, 37, false))
					return ItemStack.EMPTY;
			}
			else if(index >= 28 && index < 37 && !this.insertItem(itemStack2, 1, 28, false))
				return ItemStack.EMPTY;

			if(itemStack2.isEmpty())
				slot.setStack(ItemStack.EMPTY);
			else
				slot.markDirty();

			if(itemStack2.getCount() == itemStack.getCount())
				return ItemStack.EMPTY;

			slot.onTakeItem(player, itemStack2);
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
	
	public int getWater()
	{
		return this.propertyDelegate.get(1);
	}
	
	public long getOxygen()
	{
		return combineShorts(this.propertyDelegate.get(2), this.propertyDelegate.get(3));
	}
	
	public long getHydrogen()
	{
		return combineShorts(this.propertyDelegate.get(4), this.propertyDelegate.get(5));
	}
	
	public long getOxygenCapacity()
	{
		return combineShorts(this.propertyDelegate.get(6), this.propertyDelegate.get(7));
	}
	
	public long getHydrogenCapacity()
	{
		return combineShorts(this.propertyDelegate.get(8), this.propertyDelegate.get(9));
	}

	public boolean isActive()
	{
		return this.propertyDelegate.get(10) > 0;
	}

	private long combineShorts(int low, int high)
	{
        return ((long) high << 16) | (low & 0xFFFFL);
    }
}