package space.block.entity;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.MetalFabricatorBlock;
import space.block.StarflightBlocks;
import space.item.StarflightItems;
import space.recipe.MetalFabricatorRecipe;
import space.recipe.StarflightRecipes;
import space.screen.MetalFabricatorScreenHandler;

public class MetalFabricatorBlockEntity extends LockableContainerBlockEntity implements SidedInventory, EnergyBlockEntity
{
	private List<RecipeEntry<MetalFabricatorRecipe>> availableRecipes = Lists.newArrayList();
	private DefaultedList<ItemStack> inventory;
	private ItemStack lastInputStack = ItemStack.EMPTY;
	private ItemStack lastDrawingStack = ItemStack.EMPTY;
	private long energy;
	private int chargeState;
	private int time;
	private int totalTime;
	private int selectedRecipe;
	private String selectedGroup = "default";
	private MetalFabricatorScreenHandler screen;
	protected final PropertyDelegate propertyDelegate;

	public MetalFabricatorBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.METAL_FABRICATOR_BLOCK_ENTITY, blockPos, blockState);
		this.inventory = DefaultedList.ofSize(4, ItemStack.EMPTY);
		this.selectedRecipe = -1;
		this.propertyDelegate = new PropertyDelegate()
		{
			public int get(int index)
			{
				switch(index)
				{
				case 0:
					return MetalFabricatorBlockEntity.this.chargeState;
				case 1:
					return MetalFabricatorBlockEntity.this.time;
				case 2:
					return MetalFabricatorBlockEntity.this.totalTime;
				case 3:
					return MetalFabricatorBlockEntity.this.selectedRecipe;
				default:
					return 0;
				}
			}

			public void set(int index, int value)
			{
				switch(index)
				{
				case 0:
					MetalFabricatorBlockEntity.this.chargeState = value;
					break;
				case 1:
					MetalFabricatorBlockEntity.this.time = value;
					break;
				case 2:
					MetalFabricatorBlockEntity.this.totalTime = value;
					break;
				case 3:
					MetalFabricatorBlockEntity.this.selectedRecipe = value;
					break;
				}
			}

			public int size()
			{
				return 4;
			}
		};
	}
	
	private static boolean canAcceptRecipeOutput(DynamicRegistryManager registryManager, MetalFabricatorRecipe recipe, DefaultedList<ItemStack> slots)
	{
		if(recipe != null && !slots.get(2).isEmpty())
		{
			ItemStack itemStack = recipe.getResult(registryManager);

			if(itemStack.isEmpty())
				return false;
			else
			{
				ItemStack itemStack2 = slots.get(3);

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

	private static boolean craftRecipe(DynamicRegistryManager registryManager, MetalFabricatorRecipe recipe, DefaultedList<ItemStack> slots)
	{
		if(recipe != null)
		{
			ItemStack itemStack2 = recipe.getResult(registryManager);
			ItemStack itemStack3 = slots.get(3);

			if(itemStack3.isEmpty())
				slots.set(3, itemStack2.copy());
			else if(itemStack3.isOf(itemStack2.getItem()))
				itemStack3.increment(itemStack2.getCount());

			return true;
		}
		else
			return false;
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
	
	private boolean hasSelectedGroup()
	{
		if(selectedGroup.equals("default"))
			return true;
		
		ItemStack drawingStack = inventory.get(0);
		
		if(drawingStack.contains(StarflightItems.PART_DRAWING_GROUPS))
		{
			String[] names = drawingStack.get(StarflightItems.PART_DRAWING_GROUPS).split(",");
			
			for(String name : names)
			{
				if(name.equals(selectedGroup))
					return true;
			}
		}
		
		return false;
	}
	
	public static List<RecipeEntry<MetalFabricatorRecipe>> listAllRecipes(World world)
	{
		return world.getRecipeManager().listAllOfType(StarflightRecipes.METAL_FABRICATOR).stream().sorted(new RecipeComparator()).collect(Collectors.toList());
	}
	
	public static List<RecipeEntry<MetalFabricatorRecipe>> listAvailableRecipes(World world, ItemStack inputStack, ItemStack drawingStack)
	{
		List<RecipeEntry<MetalFabricatorRecipe>> allRecipes = world.getRecipeManager().listAllOfType(StarflightRecipes.METAL_FABRICATOR).stream().sorted(new RecipeComparator()).collect(Collectors.toList());
		List<RecipeEntry<MetalFabricatorRecipe>> availableRecipes = Lists.newArrayList();
		String drawingGroups = "default";
		
		if(drawingStack.contains(StarflightItems.PART_DRAWING_GROUPS))
			drawingGroups = drawingStack.get(StarflightItems.PART_DRAWING_GROUPS);
		
		for(RecipeEntry<MetalFabricatorRecipe> recipeEntry : allRecipes)
		{
			Ingredient ingredient = recipeEntry.value().getIngredients().getFirst();
			
			if(ingredient.test(inputStack) && (recipeEntry.value().getGroup().equals("default") || drawingGroups.contains(recipeEntry.value().getGroup())))
				availableRecipes.add(recipeEntry);
		}
		
		return availableRecipes;
	}
	
	@Override
	public Text getContainerName()
	{
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		screen = new MetalFabricatorScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
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
	
	@Override
	public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putLong("energy", this.energy);
		nbt.putShort("time", (short) this.time);
		nbt.putShort("totalTime", (short) this.totalTime);
		nbt.putShort("selectedRecipe", (short) this.selectedRecipe);
		nbt.putString("selectedGroup", selectedGroup);
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
		this.selectedRecipe = nbt.getShort("selectedRecipe");
		this.selectedGroup = nbt.getString("selectedGroup");
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, MetalFabricatorBlockEntity blockEntity)
	{
		ItemStack drawingStack = (ItemStack) blockEntity.inventory.get(0);
		ItemStack endMillStack = (ItemStack) blockEntity.inventory.get(1);
		ItemStack inputStack = (ItemStack) blockEntity.inventory.get(2);
		blockEntity.chargeState = (int) Math.ceil((blockEntity.energy / blockEntity.getEnergyCapacity()) * 14);
		MetalFabricatorRecipe recipe = null;
		
		if(blockEntity.availableRecipes.isEmpty() || !ItemStack.areEqual(inputStack, blockEntity.lastInputStack) || !ItemStack.areEqual(drawingStack, blockEntity.lastDrawingStack))
		{
			blockEntity.availableRecipes = listAllRecipes(world);
			blockEntity.lastInputStack = inputStack.copy();
			blockEntity.lastDrawingStack = drawingStack.copy();
		}
			
		if(blockEntity.selectedRecipe > -1 && blockEntity.selectedRecipe < blockEntity.availableRecipes.size())
			recipe = blockEntity.availableRecipes.get(blockEntity.selectedRecipe).value();
		
		boolean isWorking = false;
		long power = ((EnergyBlock) state.getBlock()).getInput();
		
		if(blockEntity.energy >= power && recipe != null && blockEntity.hasSelectedGroup() && !endMillStack.isEmpty() && endMillStack.getDamage() < endMillStack.getMaxDamage())
		{
			if(blockEntity.totalTime == 0 && recipe.getIngredients().get(0).test(inputStack) && canAcceptRecipeOutput(world.getRegistryManager(), recipe, blockEntity.inventory))
			{
				blockEntity.time = 0;
				blockEntity.totalTime = recipe.getMachiningTime();
				blockEntity.selectedGroup = recipe.getGroup();
				inputStack.decrement(1);
			}
			
			if(blockEntity.totalTime > 0)
			{
				blockEntity.removeEnergy(power, true);
				
				if(blockEntity.energy > 0)
				{
					blockEntity.time++;
					endMillStack.setDamage(endMillStack.getDamage() + 1);
					isWorking = true;
					
					if(blockEntity.time == blockEntity.totalTime)
					{
						craftRecipe(world.getRegistryManager(), recipe, blockEntity.inventory);
						blockEntity.time = 0;
						blockEntity.totalTime = 0;
						blockEntity.selectedGroup = "default";
					}
					
					if(endMillStack.getDamage() >= endMillStack.getMaxDamage())
					{
						BlockPos forwardPos = pos.offset(state.get(MetalFabricatorBlock.FACING));
						endMillStack.decrement(1);
						((ServerWorld) world).playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f, world.random.nextLong());
						((ServerWorld) world).spawnParticles(ParticleTypes.ELECTRIC_SPARK, forwardPos.getX() + 0.5, forwardPos.getY() + 0.5, forwardPos.getZ() + 0.5, 4 + world.random.nextInt(4), 0.5, 0.5, 0.5, 0.01);
					}
				}
			}
		}
		
		if(world.getBlockState(pos).get(MetalFabricatorBlock.LIT) != isWorking)
		{
			state = (BlockState) state.with(MetalFabricatorBlock.LIT, isWorking);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			markDirty(world, pos, state);
		}
	}
	
	public static class RecipeComparator implements Comparator<RecipeEntry<MetalFabricatorRecipe>>
	{
		@Override
		public int compare(RecipeEntry<MetalFabricatorRecipe> r0, RecipeEntry<MetalFabricatorRecipe> r1)
		{
			int id0 = r0.id().hashCode();
			int id1 = r1.id().hashCode();
			return id0 < id1 ? -1 : id0 > id1 ? 1 : 0;
		}
	}
}