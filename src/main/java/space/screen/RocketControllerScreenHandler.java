package space.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class RocketControllerScreenHandler extends ScreenHandler
{
	private final Inventory inventory;
	private final PropertyDelegate propertyDelegate;
	public final ScreenHandlerContext context;
	protected final World world;

	public RocketControllerScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(2), new ArrayPropertyDelegate(2), ScreenHandlerContext.EMPTY);
	}

	public RocketControllerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate, ScreenHandlerContext context)
	{
		super(StarflightScreens.ROCKET_CONTROLLER_SCREEN_HANDLER, syncId);
		checkSize(inventory, 2);
		checkDataCount(propertyDelegate, 2);
		this.inventory = inventory;
		this.propertyDelegate = propertyDelegate;
		this.context = context;
		this.world = playerInventory.player.getWorld();
		this.addSlot(new Slot(inventory, 0, 47, 125));
		this.addSlot(new Slot(inventory, 1, 83, 125));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 47 + j * 18, i * 18 + 148));
		}
		
		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(playerInventory, i, 47 + i * 18, 206));
		
		this.addProperties(propertyDelegate);
	}

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

	public int getCharge()
	{
		return this.propertyDelegate.get(0);
	}
	
	public int getScanProgress()
	{
		return this.propertyDelegate.get(1);
	}
}