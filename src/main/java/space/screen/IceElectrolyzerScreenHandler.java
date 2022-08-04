package space.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import space.client.StarflightModClient;

public class IceElectrolyzerScreenHandler extends ScreenHandler
{
	private final Inventory inventory;
	private final PropertyDelegate propertyDelegate;
	protected final World world;

	public IceElectrolyzerScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(1), new ArrayPropertyDelegate(2));
	}

	public IceElectrolyzerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate)
	{
		super(StarflightModClient.ICE_ELECTROLYZER_SCREEN_HANDLER, syncId);
		checkSize(inventory, 1);
		checkDataCount(propertyDelegate, 2);
		this.inventory = inventory;
		this.propertyDelegate = propertyDelegate;
		this.world = playerInventory.player.world;
		this.addSlot(new Slot(inventory, 0, 79, 31));

		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
		}

		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));

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

			if(index != 0)
			{
				if(index >= 1 && index < 28)
				{
					if(!this.insertItem(itemStack2, 28, 37, false))
						return ItemStack.EMPTY;
				}
				else if(index >= 28 && index < 37 && !this.insertItem(itemStack2, 1, 28, false))
					return ItemStack.EMPTY;
			}
			else if(!this.insertItem(itemStack2, 1, 37, false))
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

	public boolean isBurning()
	{
		return this.propertyDelegate.get(1) > 0;
	}
	
	public int getProgress()
	{
		return this.propertyDelegate.get(1);
	}

	public boolean canInsertIntoSlot(int index)
	{
		return true;
	}
}