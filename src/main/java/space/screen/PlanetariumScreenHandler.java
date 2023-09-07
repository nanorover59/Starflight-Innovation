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
import space.item.NavigationCardItem;
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
		super(StarflightScreens.PLANETARIUM_SCREEN_HANDLER, syncId);
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
			return itemStack.getItem() instanceof NavigationCardItem;
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
	
	@Override
	public boolean onButtonClick(PlayerEntity player, int id)
	{
		if(id > PlanetList.getPlanets().size())
		{
			Util.error(player.getName() + " pressed invalid button id: " + id);
			return false;
		}
		
		ItemStack itemStack = getSlot(0).getStack();
		
		if(!itemStack.isEmpty() && itemStack.getItem() instanceof NavigationCardItem)
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
		public boolean canInsert(ItemStack itemStack)
		{
			return CardSlot.matches(itemStack);
		}

		public static boolean matches(ItemStack itemStack)
		{
			return itemStack.getItem() instanceof NavigationCardItem;
		}

		@Override
		public int getMaxItemCount()
		{
			return 1;
		}
	}
}