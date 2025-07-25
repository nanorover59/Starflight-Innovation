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
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import space.item.StarflightItems;
import space.recipe.StarflightRecipes;

public class ElectricFurnaceScreenHandler extends ScreenHandler
{
	private final Inventory inventory;
	private final PropertyDelegate propertyDelegate;
	protected final World world;

	public ElectricFurnaceScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(3), new ArrayPropertyDelegate(3));
	}

	public ElectricFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate)
	{
		super(StarflightScreens.ELECTRIC_FURNACE_SCREEN_HANDLER, syncId);
		checkSize(inventory, 3);
		checkDataCount(propertyDelegate, 3);
		this.inventory = inventory;
		this.propertyDelegate = propertyDelegate;
		this.world = playerInventory.player.getWorld();
		this.addSlot(new Slot(inventory, 0, 56, 17));
		this.addSlot(new Slot(inventory, 1, 56, 53));
		this.addSlot(new FurnaceOutputSlot(playerInventory.player, inventory, 2, 116, 35));
		
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
	
	public boolean matches(Recipe<? super RecipeInput> recipe)
	{
		return recipe.matches(new SingleStackRecipeInput(this.inventory.getStack(0)), this.world);
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

	protected boolean isSmeltable(ItemStack itemStack)
	{
		return this.world.getRecipeManager().getFirstMatch(RecipeType.BLASTING, new SingleStackRecipeInput(itemStack), this.world).isPresent()
			|| this.world.getRecipeManager().getFirstMatch(RecipeType.SMOKING, new SingleStackRecipeInput(itemStack), this.world).isPresent()
			|| this.world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, new SingleStackRecipeInput(itemStack), this.world).isPresent()
			|| this.world.getRecipeManager().getFirstMatch(StarflightRecipes.VACUUM_FURNACE, new SingleStackRecipeInput(itemStack), this.world).isPresent();
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

	public boolean canInsertIntoSlot(int index)
	{
		return index != 1;
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
			
			if(slot != 1 && slot != 0)
			{
				if(this.isSmeltable(itemStack2))
				{
					if(!this.insertItem(itemStack2, 0, 1, false))
						return ItemStack.EMPTY;
				}
				else if(itemStack2.contains(StarflightItems.ENERGY))
				{
					if(!this.insertItem(itemStack2, 1, 2, false))
						return ItemStack.EMPTY;
				}
				else if(slot >= 3 && slot < 30)
				{
					if(!this.insertItem(itemStack2, 30, 38, false))
						return ItemStack.EMPTY;
				}
				else if(slot >= 30 && slot < 38 && !this.insertItem(itemStack2, 3, 30, false))
					return ItemStack.EMPTY;
			}
			else if(!this.insertItem(itemStack2, 3, 38, false))
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
}