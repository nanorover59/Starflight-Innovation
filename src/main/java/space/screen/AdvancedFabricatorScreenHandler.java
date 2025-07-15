package space.screen;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import space.recipe.MetalFabricatorRecipe;

public class AdvancedFabricatorScreenHandler extends ScreenHandler
{
	private final Inventory inventory;
	private final PropertyDelegate propertyDelegate;
	public final ScreenHandlerContext context;
	private List<RecipeEntry<MetalFabricatorRecipe>> availableRecipes = Lists.newArrayList();

	public AdvancedFabricatorScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(8), new ArrayPropertyDelegate(4), ScreenHandlerContext.EMPTY);
	}

	public AdvancedFabricatorScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate, ScreenHandlerContext context)
	{
		super(StarflightScreens.FABRICATION_STATION_SCREEN_HANDLER, syncId);
		checkSize(inventory, 8);
		checkDataCount(propertyDelegate, 4);
		this.inventory = inventory;
		this.propertyDelegate = propertyDelegate;
		this.context = context;
		this.addSlot(new Slot(inventory, 0, 17, 148));
		
		for(int i = 1; i < 7; i++)
			this.addSlot(new Slot(inventory, i, 29 + i * 18, 125));
		
		this.addSlot(new FurnaceOutputSlot(playerInventory.player, inventory, 7, 189, 125)
		{
			@Override
			public void setStack(ItemStack stack, ItemStack previousStack)
			{
				super.setStack(stack, previousStack);
				
				/*if(stack.isEmpty())
				{
					availableRecipes = MetalFabricatorBlockEntity.listAvailableRecipes(world, inventory.getStack(2), stack);
					propertyDelegate.set(3, -1);
				}*/
			}
		});
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 47 + j * 18, i * 18 + 148));
		}
		
		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(playerInventory, i, 47 + i * 18, 206));
		
		this.addProperties(propertyDelegate);
	}

	public int getAvailableRecipeCount()
	{
		return this.availableRecipes.size();
	}
	
	@Override
	public boolean canUse(PlayerEntity player)
	{
		return inventory.canPlayerUse(player);
	}

	public int getMachiningProgress()
	{
		int i = this.propertyDelegate.get(1);
		int j = this.propertyDelegate.get(2);
		return j != 0 && i != 0 ? i * 24 / j : 0;
	}

	public int getCharge()
	{
		return this.propertyDelegate.get(0);
	}
	
	public int getRecipeIndex()
	{
		return this.propertyDelegate.get(3);
	}
	
	public void setRecipeIndex(int index)
	{
		this.propertyDelegate.set(3, index);
	}

	public boolean canInsertIntoSlot(int index)
	{
		return index != 7;
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
}