package space.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import space.block.entity.StirlingEngineBlockEntity;
import space.item.StarflightItems;

public class StirlingEngineScreenHandler extends ScreenHandler
{
	private final Inventory inventory;
	private final PropertyDelegate propertyDelegate;
	protected final World world;

	public StirlingEngineScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(2), new ArrayPropertyDelegate(3));
	}

	public StirlingEngineScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate)
	{
		super(StarflightScreens.STIRLING_ENGINE_SCREEN_HANDLER, syncId);
		checkSize(inventory, 1);
		checkDataCount(propertyDelegate, 3);
		this.inventory = inventory;
		this.propertyDelegate = propertyDelegate;
		this.world = playerInventory.player.getWorld();
		this.addSlot(new Slot(inventory, 0, 80, 17));
		this.addSlot(new Slot(inventory, 1, 80, 53));
		
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

	public boolean matches(Recipe<? super RecipeInput> recipe)
	{
		return recipe.matches(new SingleStackRecipeInput(this.inventory.getStack(0)), this.world);
	}

	public boolean canUse(PlayerEntity player)
	{
		return this.inventory.canPlayerUse(player);
	}

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
				if(itemStack2.contains(StarflightItems.ENERGY))
				{
					if(!this.insertItem(itemStack2, 0, 1, false))
						return ItemStack.EMPTY;
				}
				else if(StirlingEngineBlockEntity.canUseAsFuel(itemStack2))
				{
					if(!this.insertItem(itemStack2, 1, 2, false))
						return ItemStack.EMPTY;
				}
				else if(slot >= 2 && slot < 29)
				{
					if(!this.insertItem(itemStack2, 29, 37, false))
						return ItemStack.EMPTY;
				}
				else if(slot >= 29 && slot < 37 && !this.insertItem(itemStack2, 2, 29, false))
					return ItemStack.EMPTY;
			}
			else if(!this.insertItem(itemStack2, 2, 37, false))
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
	
	public int getCharge()
	{
		return this.propertyDelegate.get(0);
	}

	public int getFuelProgress()
	{
		int i = this.propertyDelegate.get(2);
		
		if(i == 0)
			i = 200;

		return this.propertyDelegate.get(1) * 13 / i;
	}

	public boolean isBurning()
	{
		return this.propertyDelegate.get(1) > 0;
	}

	public boolean canInsertIntoSlot(int index)
	{
		return true;
	}
}