package space.block.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.block.ElectricFurnaceBlock;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.screen.ElectricFurnaceScreenHandler;

public class ElectricFurnaceBlockEntity extends LockableContainerBlockEntity implements SidedInventory, RecipeUnlocker, RecipeInputProvider, EnergyBlockEntity
{
	public DefaultedList<ItemStack> inventory;
	private double energy;
	int chargeState;
	int cookTime;
	int cookTimeTotal;
	public final PropertyDelegate propertyDelegate;
	private final Object2IntOpenHashMap<Identifier> recipesUsed;
	public Set<RecipeType<?>> recipeTypes;

	public ElectricFurnaceBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.ELECTRIC_FURNACE_BLOCK_ENTITY, blockPos, blockState);
		this.inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
		this.propertyDelegate = new PropertyDelegate()
		{
			public int get(int index)
			{
				switch(index)
				{
				case 0:
					return ElectricFurnaceBlockEntity.this.chargeState;
				case 1:
					return ElectricFurnaceBlockEntity.this.cookTime;
				case 2:
					return ElectricFurnaceBlockEntity.this.cookTimeTotal;
				default:
					return 0;
				}
			}

			public void set(int index, int value)
			{
				switch(index)
				{
				case 0:
					ElectricFurnaceBlockEntity.this.chargeState = value;
				case 1:
					ElectricFurnaceBlockEntity.this.cookTime = value;
					break;
				case 2:
					ElectricFurnaceBlockEntity.this.cookTimeTotal = value;
				}

			}

			public int size()
			{
				return 3;
			}
		};

		this.recipesUsed = new Object2IntOpenHashMap<Identifier>();
		this.recipeTypes = new HashSet<RecipeType<?>>();
		this.recipeTypes.add(RecipeType.SMELTING);
		this.recipeTypes.add(RecipeType.BLASTING);
		this.recipeTypes.add(RecipeType.SMOKING);
	}

	private static boolean canAcceptRecipeOutput(DynamicRegistryManager registryManager, @Nullable RecipeEntry<?> recipe, DefaultedList<ItemStack> slots, int count)
	{
		if(!slots.get(0).isEmpty() && recipe != null)
		{
			ItemStack itemStack = recipe.value().getResult(registryManager);

			if(itemStack.isEmpty())
				return false;
			else
			{
				ItemStack itemStack2 = slots.get(1);

				if(itemStack2.isEmpty())
					return true;
				else if(!ItemStack.areItemsEqual(itemStack2, itemStack))
					return false;
				else if(itemStack2.getCount() < count && itemStack2.getCount() < itemStack2.getMaxCount())
					return true;
				else
					return itemStack2.getCount() < itemStack.getMaxCount();
			}
		}
		else
			return false;
	}

	private static boolean craftRecipe(DynamicRegistryManager registryManager, @Nullable RecipeEntry<?> recipe, DefaultedList<ItemStack> slots, int count)
	{
		if(recipe != null && canAcceptRecipeOutput(registryManager, recipe, slots, count))
		{
			ItemStack itemStack = slots.get(0);
			ItemStack itemStack2 = recipe.value().getResult(registryManager);
			ItemStack itemStack3 = slots.get(1);

			if(itemStack3.isEmpty())
				slots.set(1, itemStack2.copy());
			else if(itemStack3.isOf(itemStack2.getItem()))
				itemStack3.increment(1);

			itemStack.decrement(1);
			return true;
		}
		else
			return false;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Optional<RecipeEntry<?>> getFirstMatchRecipeOptional()
	{
		for(RecipeType recipeType : recipeTypes)
		{
			Optional<RecipeEntry<?>> firstMatch = world.getRecipeManager().getFirstMatch(recipeType, new SingleStackRecipeInput(inventory.get(0)), world);
			
			if(!firstMatch.isEmpty())
				return firstMatch;
		}
		
		return Optional.empty();
	}
	
	public RecipeEntry<?> getFirstMatchRecipe()
	{
		Optional<RecipeEntry<?>> firstMatch = getFirstMatchRecipeOptional();
		return firstMatch.isEmpty() ? null : getFirstMatchRecipeOptional().get();
	}
	
	public boolean hasValidItem()
	{
		return ElectricFurnaceBlockEntity.canAcceptRecipeOutput(world.getRegistryManager(), getFirstMatchRecipe(), inventory, getMaxCountPerStack());
	}

	public int getCookTime()
	{
		return (Integer) getFirstMatchRecipeOptional().map(recipe -> ((AbstractCookingRecipe) recipe.value()).getCookingTime()).orElse(200) / 2;
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

			itemStack = var1.next();
		} while(itemStack.isEmpty());

		return false;
	}

	public ItemStack getStack(int slot)
	{
		return this.inventory.get(slot);
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
		ItemStack itemStack = this.inventory.get(slot);
		boolean bl = !stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(itemStack, stack);
		this.inventory.set(slot, stack);

		if(stack.getCount() > this.getMaxCountPerStack())
			stack.setCount(this.getMaxCountPerStack());

		if(slot == 0 && !bl)
		{
			this.cookTimeTotal = getCookTime();
			this.cookTime = 0;
			this.markDirty();
		}
	}

	public boolean canPlayerUse(PlayerEntity player)
	{
		if(this.world.getBlockEntity(this.pos) != this)
			return false;
		else
			return player.squaredDistanceTo((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
	}

	public boolean isValid(int slot, ItemStack stack)
	{
		return slot != 1;
	}

	public void clear()
	{
		this.inventory.clear();
	}

	public void provideRecipeInputs(RecipeMatcher finder)
	{
		Iterator<ItemStack> var2 = this.inventory.iterator();

		while(var2.hasNext())
		{
			ItemStack itemStack = var2.next();
			finder.addInput(itemStack);
		}

	}

	public void setLastRecipe(@Nullable RecipeEntry<?> recipe)
	{
		if(recipe != null)
		{
			Identifier identifier = recipe.id();
			this.recipesUsed.addTo(identifier, 1);
		}
	}

	@Nullable
	public RecipeEntry<?> getLastRecipe()
	{
		return null;
	}

	public int[] getAvailableSlots(Direction side)
	{
		if(side == Direction.DOWN)
			return new int[] {1};
		else
			return new int[] {0};
	}

	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir)
	{
		return this.isValid(slot, stack);
	}

	public boolean canExtract(int slot, ItemStack stack, Direction dir)
	{
		return true;
	}

	public void dropExperienceForRecipesUsed(ServerPlayerEntity player)
	{
		List<RecipeEntry<?>> list = this.getRecipesUsedAndDropExperience(player.getServerWorld(), player.getPos());
		player.unlockRecipes((Collection<RecipeEntry<?>>) list);
		this.recipesUsed.clear();
	}

	public List<RecipeEntry<?>> getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos)
	{
		List<RecipeEntry<?>> list = Lists.newArrayList();
		ObjectIterator<Entry<Identifier>> var4 = this.recipesUsed.object2IntEntrySet().iterator();

		while(var4.hasNext())
		{
			Entry<Identifier> entry = var4.next();
			
			world.getRecipeManager().get(entry.getKey()).ifPresent((recipe) ->
			{
				list.add(recipe);
				dropExperience(world, pos, entry.getIntValue(), ((AbstractCookingRecipe) recipe.value()).getExperience());
			});
		}

		return list;
	}

	private static void dropExperience(ServerWorld world, Vec3d pos, int multiplier, float experience)
	{
		int i = MathHelper.floor((float) multiplier * experience);
		float f = MathHelper.fractionalPart((float) multiplier * experience);
		
		if(f != 0.0F && Math.random() < (double) f)
			i++;

		ExperienceOrbEntity.spawn(world, pos, i);
	}

	@Override
	public Text getContainerName()
	{
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		return new ElectricFurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
	}
	
	@Override
	public double getOutput()
	{
		return 0.0;
	}
	
	@Override
	public double getInput()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getInput() / world.getTickManager().getTickRate();
	}
	
	@Override
	public double getEnergyStored()
	{
		return energy;
	}

	@Override
	public double getEnergyCapacity()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getEnergyCapacity();
	}

	@Override
	public double changeEnergy(double amount)
	{
		double newEnergy = energy + amount;
		energy = MathHelper.clamp(newEnergy, 0, getEnergyCapacity());
		return amount - (newEnergy - energy);
	}

	@Override
	public ArrayList<BlockPos> getOutputs()
	{
		return null;
	}

	@Override
	public void addOutput(BlockPos output)
	{
	}

	@Override
	public void clearOutputs()
	{
	}
	
	@Override
	public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putDouble("energy", this.energy);
		nbt.putShort("CookTime", (short) this.cookTime);
		nbt.putShort("CookTimeTotal", (short) this.cookTimeTotal);
		Inventories.writeNbt(nbt, this.inventory, registryLookup);
		NbtCompound nbtCompound = new NbtCompound();
		this.recipesUsed.forEach((identifier, integer) ->
		{
			nbtCompound.putInt(identifier.toString(), integer);
		});
		nbt.put("RecipesUsed", nbtCompound);
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, this.inventory, registryLookup);
		this.energy = nbt.getDouble("energy");
		this.cookTime = nbt.getShort("CookTime");
		this.cookTimeTotal = nbt.getShort("CookTimeTotal");
		NbtCompound nbtCompound = nbt.getCompound("RecipesUsed");
		Iterator<String> var3 = nbtCompound.getKeys().iterator();
		
		while(var3.hasNext())
		{
			String string = var3.next();
			this.recipesUsed.put(Identifier.of(string), nbtCompound.getInt(string));
		}
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity blockEntity)
	{
		ItemStack itemStack = (ItemStack) blockEntity.inventory.get(0);
		boolean bl2 = false;
		blockEntity.chargeState = (int) Math.ceil((blockEntity.energy / blockEntity.getEnergyCapacity()) * 14.0);
		
		if(!itemStack.isEmpty())
		{
			RecipeEntry<?> recipe = blockEntity.getFirstMatchRecipe();
			int i = blockEntity.getMaxCountPerStack();

			if(blockEntity.energy > 0 && canAcceptRecipeOutput(world.getRegistryManager(), recipe, blockEntity.inventory, i))
			{
				blockEntity.changeEnergy(-blockEntity.getInput());
				blockEntity.cookTime++;

				if(blockEntity.cookTime == blockEntity.cookTimeTotal)
				{
					blockEntity.cookTime = 0;
					blockEntity.cookTimeTotal = blockEntity.getCookTime();

					if(craftRecipe(world.getRegistryManager(), recipe, blockEntity.inventory, i))
						blockEntity.setLastRecipe(recipe);

					bl2 = true;
				}
			}
			else
				blockEntity.cookTime = 0;
		}
		
		if(blockEntity.cookTime > 0 && (itemStack.isEmpty() || blockEntity.energy == 0))
			blockEntity.cookTime = MathHelper.clamp((int) (blockEntity.cookTime - 2), (int) 0, (int) blockEntity.cookTimeTotal);
		
		if(world.getBlockState(pos).get(ElectricFurnaceBlock.LIT) != (blockEntity.energy > 0 && blockEntity.hasValidItem()))
		{
			state = (BlockState) state.with(ElectricFurnaceBlock.LIT, blockEntity.energy > 0 && blockEntity.hasValidItem());
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			bl2 = true;
		}
		
		if(bl2)
			markDirty(world, pos, state);
	}
}
