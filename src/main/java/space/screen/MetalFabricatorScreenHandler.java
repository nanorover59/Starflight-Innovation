package space.screen;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import space.block.entity.MetalFabricatorBlockEntity;
import space.item.StarflightItems;
import space.recipe.MetalFabricatorRecipe;
import space.recipe.StarflightRecipes;

public class MetalFabricatorScreenHandler extends ScreenHandler
{
	private final Inventory inventory;
	private final PropertyDelegate propertyDelegate;
	protected final World world;
	private List<RecipeEntry<MetalFabricatorRecipe>> availableRecipes = Lists.newArrayList();

	public MetalFabricatorScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(4), new ArrayPropertyDelegate(4));
	}

	public MetalFabricatorScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate)
	{
		super(StarflightScreens.METAL_FABRICATOR_SCREEN_HANDLER, syncId);
		checkSize(inventory, 4);
		checkDataCount(propertyDelegate, 4);
		this.inventory = inventory;
		this.propertyDelegate = propertyDelegate;
		this.world = playerInventory.player.getWorld();
		
		this.addSlot(new Slot(inventory, 0, 19, 9)
		{
			@Override
			public boolean canInsert(ItemStack stack)
			{
				return propertyDelegate.get(2) == 0 && stack.isOf(StarflightItems.PART_DRAWINGS);
			}
			
			@Override
			public boolean canTakeItems(PlayerEntity player)
			{
				return propertyDelegate.get(2) == 0;
			}

			@Override
			public void setStack(ItemStack stack, ItemStack previousStack)
			{
				super.setStack(stack, previousStack);
				availableRecipes = MetalFabricatorBlockEntity.listAvailableRecipes(world, stack, inventory.getStack(0));
				
				if(propertyDelegate.get(2) == 0)
					propertyDelegate.set(3, -1);
			}
		});
		
		this.addSlot(new Slot(inventory, 1, 143, 9)
		{
			@Override
			public boolean canInsert(ItemStack stack)
			{
				return stack.isOf(StarflightItems.DIAMOND_END_MILL);
			}
			
			@Override
			public boolean canTakeItems(PlayerEntity player)
			{
				return propertyDelegate.get(2) == 0;
			}
		});
		
		this.addSlot(new Slot(inventory, 2, 19, 35)
		{
			@Override
			public void setStack(ItemStack stack, ItemStack previousStack)
			{
				super.setStack(stack, previousStack);
				
				if(propertyDelegate.get(2) == 0)
				{
					availableRecipes = MetalFabricatorBlockEntity.listAvailableRecipes(world, inventory.getStack(2), stack);
					propertyDelegate.set(3, -1);
				}
			}
		});
		
		this.addSlot(new FurnaceOutputSlot(playerInventory.player, inventory, 3, 143, 57)
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
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
		}
		
		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));

		this.addProperties(propertyDelegate);
	}
	
	public int getSelectedRecipe()
	{
		return propertyDelegate.get(3);
	}

	public List<RecipeEntry<MetalFabricatorRecipe>> getAvailableRecipes()
	{
		if(this.availableRecipes.isEmpty())
			this.availableRecipes = MetalFabricatorBlockEntity.listAvailableRecipes(world, inventory.getStack(2), inventory.getStack(0));
		
		return this.availableRecipes;
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
	
	@Override
	public boolean onButtonClick(PlayerEntity player, int id)
	{
		if(this.availableRecipes.isEmpty())
			this.availableRecipes = MetalFabricatorBlockEntity.listAvailableRecipes(world, inventory.getStack(2), inventory.getStack(0));
		
		List<RecipeEntry<MetalFabricatorRecipe>> allRecipes = MetalFabricatorBlockEntity.listAllRecipes(world);
		RecipeEntry<MetalFabricatorRecipe> selectedRecipe = this.availableRecipes.get(id);
		
		for(int i = 0; i < allRecipes.size(); i++)
		{
			if(allRecipes.get(i).equals(selectedRecipe))
				this.propertyDelegate.set(3, i);
		}
		
		return true;
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
				if(this.isWorkPiece(itemStack2))
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

	protected boolean isWorkPiece(ItemStack itemStack)
	{
		return this.world.getRecipeManager().getFirstMatch(StarflightRecipes.METAL_FABRICATOR, new SingleStackRecipeInput(itemStack), this.world).isPresent();
	}

	public int getMachiningProgress()
	{
		int i = this.propertyDelegate.get(1);
		int j = this.propertyDelegate.get(2);
		return j != 0 && i != 0 ? 12 - (i * 12 / j) : 0;
	}
	
	public double getMachiningProgressPercent()
	{
		double i = this.propertyDelegate.get(1);
		double j = this.propertyDelegate.get(2);
		return i == j ? 100.0 : (i / j) * 100.0;
	}

	public int getCharge()
	{
		return this.propertyDelegate.get(0);
	}

	public boolean canInsertIntoSlot(int index)
	{
		return index != 3;
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