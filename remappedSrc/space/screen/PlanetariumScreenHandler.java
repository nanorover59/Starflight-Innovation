package space.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Util;
import space.client.StarflightModClient;
import space.item.StarflightItems;
import space.planet.Planet;
import space.planet.PlanetList;

public class PlanetariumScreenHandler extends ScreenHandler
{
	private final Inventory inventory;

	public PlanetariumScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(1));
	}

	public PlanetariumScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory)
	{
		super(StarflightModClient.PLANETARIUM_SCREEN_HANDLER, syncId);
		checkSize(inventory, 1);
		this.inventory = inventory;
		inventory.onOpen(playerInventory.player);
		int m;
		int l;
		
		// Block Inventory
		this.addSlot(new CardSlot(inventory, 0, 8, 18));
		
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
	public boolean canInsertIntoSlot(ItemStack itemStack, Slot slot)
	{
		if(slot.id != 0)
			return true;
		else
			return itemStack.getItem() == StarflightItems.NAVIGATION_CARD;
	}

	@Override
	public ItemStack transferSlot(PlayerEntity player, int inventorySlot)
	{
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(inventorySlot);
		if(slot != null && slot.hasStack())
		{
			ItemStack originalStack = slot.getStack();
			newStack = originalStack.copy();
			if(inventorySlot < this.inventory.size())
			{
				if(!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true))
					return ItemStack.EMPTY;
			}
			else if(!this.insertItem(originalStack, 0, this.inventory.size(), false))
				return ItemStack.EMPTY;

			if(originalStack.isEmpty())
				slot.setStack(ItemStack.EMPTY);
			else
				slot.markDirty();
		}

		return newStack;
	}
	
	@Override
	public boolean onButtonClick(PlayerEntity player, int id)
	{
		if(id > PlanetList.getPlanets().size())
		{
			Util.error(player.getName() + " pressed invalid button id: " + id);
			return false;
		}
		
		ItemStack itemStack = getSlot(0).getStack();
		
		if(!itemStack.isEmpty() && itemStack.getItem() == StarflightItems.NAVIGATION_CARD)
		{
			Planet selectedPlanet = PlanetList.getPlanets().get(id);
			NbtCompound nbt = new NbtCompound();
			nbt.putString("planet", selectedPlanet.getName());
			itemStack.setNbt(nbt);
			return true;
		}
		
		return false;
	}
	
	static class CardSlot extends Slot
	{
		public CardSlot(Inventory inventory, int i, int j, int k)
		{
			super(inventory, i, j, k);
		}

		@Override
		public boolean canInsert(ItemStack stack)
		{
			return CardSlot.matches(stack);
		}

		public static boolean matches(ItemStack stack)
		{
			return stack.isOf(StarflightItems.NAVIGATION_CARD);
		}

		@Override
		public int getMaxItemCount()
		{
			return 1;
		}
	}
}