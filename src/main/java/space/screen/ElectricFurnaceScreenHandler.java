package space.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class ElectricFurnaceScreenHandler extends ScreenHandler
{
	private final Inventory inventory;
	private final PropertyDelegate propertyDelegate;
	protected final World world;

	public ElectricFurnaceScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(2), new ArrayPropertyDelegate(3));
	}

	public ElectricFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate)
	{
		super(StarflightScreens.ELECTRIC_FURNACE_SCREEN_HANDLER, syncId);
		checkSize(inventory, 2);
		checkDataCount(propertyDelegate, 3);
		this.inventory = inventory;
		this.propertyDelegate = propertyDelegate;
		this.world = playerInventory.player.world;
		this.addSlot(new Slot(inventory, 0, 56, 35));
		this.addSlot(new FurnaceOutputSlot(playerInventory.player, inventory, 1, 116, 35));

		int k;
		for(k = 0; k < 3; ++k)
		{
			for(int j = 0; j < 9; ++j)
			{
				this.addSlot(new Slot(playerInventory, j + k * 9 + 9, 8 + j * 18, 84 + k * 18));
			}
		}

		for(k = 0; k < 9; ++k)
		{
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
		}

		this.addProperties(propertyDelegate);
	}

	public void populateRecipeFinder(RecipeMatcher finder)
	{
		if(this.inventory instanceof RecipeInputProvider)
			((RecipeInputProvider) this.inventory).provideRecipeInputs(finder);
	}

	public void clearCraftingSlots()
	{
		this.getSlot(0).setStack(ItemStack.EMPTY);
	}

	public boolean matches(Recipe<? super Inventory> recipe)
	{
		return recipe.matches(this.inventory, this.world);
	}

	public int getCraftingResultSlotIndex()
	{
		return 1;
	}

	public int getCraftingWidth()
	{
		return 1;
	}

	public int getCraftingHeight()
	{
		return 1;
	}

	public int getCraftingSlotCount()
	{
		return 2;
	}

	public boolean canUse(PlayerEntity player)
	{
		return this.inventory.canPlayerUse(player);
	}

	public ItemStack transferSlot(PlayerEntity player, int index)
	{
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = (Slot) this.slots.get(index);
		if(slot != null && slot.hasStack())
		{
			ItemStack itemStack2 = slot.getStack();
			itemStack = itemStack2.copy();
			if(index == 1)
			{
				if(!this.insertItem(itemStack2, 2, 38, true))
					return ItemStack.EMPTY;

				slot.onQuickTransfer(itemStack2, itemStack);
			}
			else if(index != 0)
			{
				if(this.isSmeltable(itemStack2))
				{
					if(!this.insertItem(itemStack2, 0, 1, false))
						return ItemStack.EMPTY;
				}
				else if(index >= 2 && index < 29)
				{
					if(!this.insertItem(itemStack2, 29, 38, false))
						return ItemStack.EMPTY;
				}
				else if(index >= 29 && index < 38 && !this.insertItem(itemStack2, 2, 29, false))
					return ItemStack.EMPTY;
			}
			else if(!this.insertItem(itemStack2, 2, 38, false))
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

	protected boolean isSmeltable(ItemStack itemStack)
	{
		return this.world.getRecipeManager().getFirstMatch(RecipeType.BLASTING, new SimpleInventory(new ItemStack[] {itemStack}), this.world).isPresent()
			|| this.world.getRecipeManager().getFirstMatch(RecipeType.SMOKING, new SimpleInventory(new ItemStack[] {itemStack}), this.world).isPresent()
			|| this.world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, new SimpleInventory(new ItemStack[] {itemStack}), this.world).isPresent();
	}

	public int getCookProgress()
	{
		int i = this.propertyDelegate.get(1);
		int j = this.propertyDelegate.get(2);
		return j != 0 && i != 0 ? i * 24 / j : 0;
	}

	public boolean isBurning()
	{
		return this.propertyDelegate.get(0) == 1;
	}

	public boolean canInsertIntoSlot(int index)
	{
		return index != 1;
	}
}
