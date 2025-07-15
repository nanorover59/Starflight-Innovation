package space.screen;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import space.block.entity.ElectricCrafterBlockEntity;
import space.item.StarflightItems;

public class ElectricCrafterScreenHandler extends ScreenHandler implements ScreenHandlerListener
{
	private final PropertyDelegate propertyDelegate;
	private final PlayerEntity player;
	private final RecipeInputInventory inventory;
	public final ScreenHandlerContext context;
	public BlockPos blockPos;

	public ElectricCrafterScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos blockPos)
	{
		super(StarflightScreens.ELECTRIC_CRAFTER_SCREEN_HANDLER, syncId);
		this.player = playerInventory.player;
		this.propertyDelegate = new ArrayPropertyDelegate(3);
		this.inventory = new CraftingInventory(this, 3, 3, DefaultedList.ofSize(20, ItemStack.EMPTY));
		this.context = ScreenHandlerContext.EMPTY;
		this.blockPos = blockPos;
		this.addSlots(playerInventory);
	}

	public ElectricCrafterScreenHandler(int syncId, PlayerInventory playerInventory, RecipeInputInventory inventory, PropertyDelegate propertyDelegate, ScreenHandlerContext context)
	{
		super(StarflightScreens.ELECTRIC_CRAFTER_SCREEN_HANDLER, syncId);
		this.player = playerInventory.player;
		this.propertyDelegate = propertyDelegate;
		this.inventory = inventory;
		this.context = context;
		checkSize(inventory, 20);
		inventory.onOpen(playerInventory.player);
		this.addSlots(playerInventory);
		this.addListener(this);
	}

	private void addSlots(PlayerInventory playerInventory)
	{
		this.addSlot(new Slot(inventory, 0, 8, 53));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				int k = j + i * 3 + 1;
				this.addSlot(new Slot(this.inventory, k, 30 + j * 18, 17 + i * 18));
			}
		}
		
		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(this.inventory, 10 + i, 8 + i * 18, 75));
		
		this.addSlot(new Slot(this.inventory, 19, 124, 35)
		{
			@Override
			public boolean canInsert(ItemStack stack)
			{
				return false;
			}
		});

		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
				this.addSlot(new ProgrammingItemSlot(playerInventory, j + i * 9 + 9, 8 + j * 18, 110 + i * 18));
		}

		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 168));
		
		this.addProperties(this.propertyDelegate);
		this.updateResult();
	}

	public boolean isTriggered()
	{
		return this.propertyDelegate.get(9) == 1;
	}
	
	public int getCookProgress()
	{
		int i = this.propertyDelegate.get(1);
		int j = this.propertyDelegate.get(2);
		return j != 0 && i != 0 ? i * 24 / j : 0;
	}

	public int getCharge()
	{
		return this.propertyDelegate.get(0);
	}
	
	public ItemStack getPreviewStack()
	{
		BlockEntity blockEntity = this.player.getWorld().getBlockEntity(blockPos);
		
		if(blockEntity != null && blockEntity instanceof ElectricCrafterBlockEntity)
		{
			ElectricCrafterBlockEntity electricCrafter = (ElectricCrafterBlockEntity) blockEntity;
			return electricCrafter.getPreviewStack();
		}
		
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot)
	{
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot2 = this.slots.get(slot);
		
		if(slot2 != null && slot2.hasStack())
		{
			ItemStack itemStack2 = slot2.getStack();
			itemStack = itemStack2.copy();
			
			if(slot == 19)
			{
				if(!this.insertItem(itemStack2, 20, 55, true))
					return ItemStack.EMPTY;

				slot2.onQuickTransfer(itemStack2, itemStack);
			}
			else if(slot > 19)
			{
				if(itemStack2.contains(StarflightItems.ENERGY))
				{
					if(!this.insertItem(itemStack2, 0, 1, false))
						return ItemStack.EMPTY;
				}
				else if(!this.insertItem(itemStack2, 10, 19, false))
					return ItemStack.EMPTY;
				else if(slot >= 19 && slot < 47)
				{
					if(!this.insertItem(itemStack2, 47, 55, false))
						return ItemStack.EMPTY;
				}
				else if(slot >= 47 && slot < 55 && !this.insertItem(itemStack2, 20, 47, false))
					return ItemStack.EMPTY;
			}
			else if(!this.insertItem(itemStack2, 20, 55, false))
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
	public boolean canUse(PlayerEntity player)
	{
		return this.inventory.canPlayerUse(player);
	}

	private void updateResult()
	{
		context.run((world, pos) -> {
			BlockEntity blockEntity = world.getBlockEntity(pos);

			if(blockEntity != null && blockEntity instanceof ElectricCrafterBlockEntity)
				((ElectricCrafterBlockEntity) blockEntity).updateResult();
		});
	}

	public Inventory getinventory()
	{
		return this.inventory;
	}

	@Override
	public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack)
	{
		this.updateResult();
	}

	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value)
	{
	}
	
	static class ProgrammingItemSlot extends Slot
	{
		public ProgrammingItemSlot(Inventory inventory, int i, int j, int k)
		{
			super(inventory, i, j, k);
		}

		@Override
		public int getMaxItemCount()
		{
			return 1;
		}
	}
}