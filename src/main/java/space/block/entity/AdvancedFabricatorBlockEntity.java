package space.block.entity;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import space.block.AdvancedFabricatorBlock;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.network.c2s.AdvancedFabricatorButtonC2SPacket;
import space.recipe.AdvancedFabricatorRecipe;
import space.recipe.StarflightRecipes;
import space.screen.AdvancedFabricatorScreenHandler;

public class AdvancedFabricatorBlockEntity extends LockableContainerBlockEntity implements SidedInventory, EnergyBlockEntity
{
	private DefaultedList<ItemStack> inventory;
	private RecipeEntry<AdvancedFabricatorRecipe> recipe;
	private long energy;
	private int time;
	private int totalTime;
	private int recipeIndex;
	private AdvancedFabricatorScreenHandler screen;
	protected final PropertyDelegate propertyDelegate;

	public AdvancedFabricatorBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.ADVANCED_FABRICATOR_BLOCK_ENTITY, blockPos, blockState);
		this.inventory = DefaultedList.ofSize(8, ItemStack.EMPTY);
		this.recipeIndex = -1;
		this.propertyDelegate = new PropertyDelegate()
		{
			public int get(int index)
			{
				switch(index)
				{
				case 0:
					return (int) AdvancedFabricatorBlockEntity.this.energy;
				case 1:
					return AdvancedFabricatorBlockEntity.this.time;
				case 2:
					return AdvancedFabricatorBlockEntity.this.totalTime;
				case 3:
					return AdvancedFabricatorBlockEntity.this.recipeIndex;
				default:
					return 0;
				}
			}

			public void set(int index, int value)
			{
				switch(index)
				{
				case 0:
					AdvancedFabricatorBlockEntity.this.energy = value;
					break;
				case 1:
					AdvancedFabricatorBlockEntity.this.time = value;
					break;
				case 2:
					AdvancedFabricatorBlockEntity.this.totalTime = value;
					break;
				case 3:
					AdvancedFabricatorBlockEntity.this.recipeIndex = value;
					break;
				}
			}

			public int size()
			{
				return 4;
			}
		};
	}
	
	private static boolean canAcceptRecipeOutput(DynamicRegistryManager registryManager, AdvancedFabricatorRecipe recipe, DefaultedList<ItemStack> slots)
	{
		if(recipe != null)
		{
			ItemStack itemStack = recipe.getResult(registryManager);

			if(itemStack.isEmpty())
				return false;
			else
			{
				ItemStack itemStack2 = slots.get(6);

				if(itemStack2.isEmpty())
					return true;
				else if(!ItemStack.areItemsEqual(itemStack2, itemStack))
					return false;
				else if(itemStack2.getCount() < itemStack.getCount() && itemStack2.getCount() < itemStack2.getMaxCount())
					return true;
				else
					return itemStack2.getCount() < itemStack.getMaxCount();
			}
		}
		else
			return false;
	}

	private static boolean craftRecipe(DynamicRegistryManager registryManager, AdvancedFabricatorRecipe recipe, DefaultedList<Ingredient> ingredients, DefaultedList<ItemStack> inputStacks)
	{
		if(recipe != null)
		{
			ItemStack itemStack2 = recipe.getResult(registryManager);
			ItemStack itemStack3 = inputStacks.get(6);

			if(itemStack3.isEmpty())
				inputStacks.set(6, itemStack2.copy());
			else if(itemStack3.isOf(itemStack2.getItem()))
				itemStack3.increment(itemStack2.getCount());
			
			for(Ingredient ingredient : ingredients)
			{
				ItemStack ingredientStack = ingredient.getMatchingStacks()[0];
				
				for(ItemStack inputStack : inputStacks)
				{
					if(inputStack.isOf(ingredientStack.getItem()) && inputStack.getCount() >= ingredientStack.getCount())
					{
						inputStack.decrement(ingredientStack.getCount());
						break;
					}
				}
			}

			return true;
		}
		else
			return false;
	}
	
	public static List<RecipeEntry<AdvancedFabricatorRecipe>> listAllRecipes(RecipeManager recipeManager)
	{
		return recipeManager.listAllOfType(StarflightRecipes.ADVANCED_FABRICATOR).stream().sorted(new RecipeComparator()).collect(Collectors.toList());
	}
	
	public void findRecipe()
	{
		List<RecipeEntry<AdvancedFabricatorRecipe>> recipeEntries = listAllRecipes(getWorld().getRecipeManager());
		
		if(recipeIndex > -1 && recipeIndex < recipeEntries.size())
			recipe = recipeEntries.get(recipeIndex);
	}
	
	public int getTime()
	{
		return 200;
	}
	
	public int[] getAvailableSlots(Direction side)
	{
		return new int[] {0};
	}

	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir)
	{
		return this.isValid(slot, stack);
	}

	public boolean canExtract(int slot, ItemStack stack, Direction dir)
	{
		return false;
	}

	public int size()
	{
		return this.inventory.size();
	}
	
	@Override
	protected DefaultedList<ItemStack> getHeldStacks()
	{
		return this.inventory;
	}

	@Override
	protected void setHeldStacks(DefaultedList<ItemStack> inventory)
	{
		this.inventory = inventory;
	}

	public boolean isEmpty()
	{
		Iterator<ItemStack> var1 = this.inventory.iterator();

		ItemStack itemStack;
		do
		{
			if(!var1.hasNext())
				return true;

			itemStack = (ItemStack) var1.next();
		} while(itemStack.isEmpty());

		return false;
	}

	public ItemStack getStack(int slot)
	{
		return (ItemStack) this.inventory.get(slot);
	}

	public ItemStack removeStack(int slot, int amount)
	{
		return Inventories.splitStack(this.inventory, slot, amount);
	}

	public ItemStack removeStack(int slot)
	{
		return Inventories.removeStack(this.inventory, slot);
	}

	public void setStack(int slot, ItemStack stack)
	{
		this.inventory.set(slot, stack);
		
		if(stack.getCount() > this.getMaxCountPerStack())
			stack.setCount(this.getMaxCountPerStack());
	}

	public boolean canPlayerUse(PlayerEntity player)
	{
		if(this.world.getBlockEntity(this.pos) != this)
			return false;
		else
			return player.squaredDistanceTo((double) this.pos.getX() + 0.5, (double) this.pos.getY() + 0.5, (double) this.pos.getZ() + 0.5) <= 64.0;
	}

	public boolean isValid(int slot, ItemStack stack)
	{
		return slot == 0;
	}

	public void clear()
	{
		this.inventory.clear();
	}
	
	@Override
	public Text getContainerName()
	{
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		screen = new AdvancedFabricatorScreenHandler(syncId, playerInventory, this, this.propertyDelegate, ScreenHandlerContext.create(this.world, this.getPos()));
		return screen;
	}
	
	@Override
    public void markDirty()
	{
        super.markDirty();
        
        if(screen != null)
        	screen.onContentChanged(this);
    }

	@Override
	public long getEnergyCapacity()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getEnergyCapacity();
	}
	
	@Override
	public long getEnergy()
	{
		return energy;
	}

	@Override
	public void setEnergy(long energy)
	{
		this.energy = energy;
	}
	
	public long getEnergyUse()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getInput();
	}
	
	@Override
	public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putLong("energy", this.energy);
		nbt.putShort("time", (short) this.time);
		nbt.putShort("totalTime", (short) this.totalTime);
		nbt.putShort("recipeIndex", (short) this.recipeIndex);
		Inventories.writeNbt(nbt, this.inventory, registryLookup);
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, this.inventory, registryLookup);
		this.energy = nbt.getLong("energy");
		this.time = nbt.getShort("time");
		this.totalTime = nbt.getShort("totalTime");
		this.recipeIndex = nbt.getShort("recipeIndex");
	}
	
	public static void receiveRecipe(AdvancedFabricatorButtonC2SPacket payload, ServerPlayNetworking.Context context)
	{
		ServerPlayerEntity player = context.player();
		int index = payload.index();
		
		if(player.currentScreenHandler != null && player.currentScreenHandler instanceof AdvancedFabricatorScreenHandler)
		{
			((AdvancedFabricatorScreenHandler) player.currentScreenHandler).context.run((world, pos) -> {
				BlockEntity blockEntity = world.getBlockEntity(pos);
				
				if(blockEntity != null && blockEntity instanceof AdvancedFabricatorBlockEntity)
				{
					AdvancedFabricatorBlockEntity advancedFabricator = (AdvancedFabricatorBlockEntity) blockEntity;
					advancedFabricator.recipeIndex = index;
					
					if(index > -1)
						advancedFabricator.findRecipe();
					else
						advancedFabricator.recipe = null;
				}
			});
		}
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, AdvancedFabricatorBlockEntity blockEntity)
	{
		ItemStack batteryStack = (ItemStack) blockEntity.inventory.get(0);
		long power = blockEntity.getEnergyUse();
		
		if(blockEntity.screen != null)
			blockEntity.screen.setRecipeIndex(blockEntity.recipeIndex);
		
		if(blockEntity.recipeIndex > -1 && blockEntity.recipe == null)
			blockEntity.findRecipe();
		
		boolean isWorking = false;
		
		if(blockEntity.recipe != null)
		{
			CraftingRecipeInput input = CraftingRecipeInput.create(6, 1, blockEntity.inventory.subList(1, 7));
			
			if(blockEntity.energy > 0 && blockEntity.recipe.value().matches(input, world) && canAcceptRecipeOutput(world.getRegistryManager(), blockEntity.recipe.value(), blockEntity.inventory) && blockEntity.removeEnergy(power, true) == power)
			{
				isWorking = true;
				blockEntity.time++;
				blockEntity.totalTime = blockEntity.getTime();
				
				if(blockEntity.time == blockEntity.totalTime)
				{
					blockEntity.time = 0;
					craftRecipe(world.getRegistryManager(), blockEntity.recipe.value(), blockEntity.recipe.value().getIngredients(), blockEntity.inventory);
				}
			}
		}
		
		if(blockEntity.time > 0 && !isWorking)
			blockEntity.time = MathHelper.clamp((int) (blockEntity.time - 2), (int) 0, (int) blockEntity.totalTime);
		
		if(world.getBlockState(pos).get(AdvancedFabricatorBlock.LIT) != isWorking)
		{
			state = (BlockState) state.with(AdvancedFabricatorBlock.LIT, isWorking);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			markDirty(world, pos, state);
		}
		
		blockEntity.dischargeItem(batteryStack, blockEntity.getEnergyUse() * 2);
	}
	
	public static class RecipeComparator implements Comparator<RecipeEntry<AdvancedFabricatorRecipe>>
	{
		@Override
		public int compare(RecipeEntry<AdvancedFabricatorRecipe> r0, RecipeEntry<AdvancedFabricatorRecipe> r1)
		{
			int id0 = r0.id().hashCode();
			int id1 = r1.id().hashCode();
			return id0 < id1 ? -1 : id0 > id1 ? 1 : 0;
		}
	}
}