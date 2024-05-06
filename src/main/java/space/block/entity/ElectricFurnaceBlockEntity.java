package space.block.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.registry.DynamicRegistryManager;
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
import space.recipe.StarflightRecipes;
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
	}

	private static boolean canAcceptRecipeOutput(DynamicRegistryManager registryManager, @Nullable RecipeEntry<?> recipe, DefaultedList<ItemStack> slots, int count)
	{
		if(!((ItemStack) slots.get(0)).isEmpty() && recipe != null)
		{
			ItemStack itemStack = recipe.value().getResult(registryManager);

			if(itemStack.isEmpty())
				return false;
			else
			{
				ItemStack itemStack2 = (ItemStack) slots.get(1);

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
			ItemStack itemStack = (ItemStack) slots.get(0);
			ItemStack itemStack2 = recipe.value().getResult(registryManager);
			ItemStack itemStack3 = (ItemStack) slots.get(1);

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
	
	public boolean hasValidItem()
	{
		int i = getMaxCountPerStack();
		RecipeEntry<?> recipe;
		
		if(!world.getRecipeManager().getFirstMatch(RecipeType.BLASTING, this, world).isEmpty())
			recipe = (RecipeEntry<?>) world.getRecipeManager().getFirstMatch(RecipeType.BLASTING, this, world).get();
		else if(!world.getRecipeManager().getFirstMatch(RecipeType.SMOKING, this, world).isEmpty())
			recipe = (RecipeEntry<?>) world.getRecipeManager().getFirstMatch(RecipeType.SMOKING, this, world).get();
		else if(!world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, this, world).isEmpty())
			recipe = (RecipeEntry<?>) world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, this, world).get();
		else if(!world.getRecipeManager().getFirstMatch(StarflightRecipes.VACUUM_FURNACE, this, world).isEmpty())
			recipe = (RecipeEntry<?>) world.getRecipeManager().getFirstMatch(StarflightRecipes.VACUUM_FURNACE, this, world).get();
		else
			recipe = null;
		
		return ElectricFurnaceBlockEntity.canAcceptRecipeOutput(world.getRegistryManager(), recipe, inventory, i);
	}

	private static int getCookTime(World world, Inventory inventory)
	{
		//return (Integer) world.getRecipeManager().getFirstMatch(RecipeType.BLASTING, inventory, world).map(AbstractCookingRecipe::getCookTime).orElse(world.getRecipeManager().getFirstMatch(RecipeType.SMOKING, inventory, world).map(AbstractCookingRecipe::getCookTime).orElse(world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, inventory, world).map(AbstractCookingRecipe::getCookTime).orElse(200)));
		return 64;
	}

	public int size()
	{
		return this.inventory.size();
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
		ItemStack itemStack = (ItemStack) this.inventory.get(slot);
		boolean bl = !stack.isEmpty() && ItemStack.canCombine(itemStack, stack);
		this.inventory.set(slot, stack);

		if(stack.getCount() > this.getMaxCountPerStack())
			stack.setCount(this.getMaxCountPerStack());

		if(slot == 0 && !bl)
		{
			this.cookTimeTotal = getCookTime(this.world, this);
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
			ItemStack itemStack = (ItemStack) var2.next();
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
			
			world.getRecipeManager().get((Identifier) entry.getKey()).ifPresent((recipe) ->
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
	
	public void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putDouble("energy", this.energy);
		nbt.putShort("CookTime", (short) this.cookTime);
		nbt.putShort("CookTimeTotal", (short) this.cookTimeTotal);
		Inventories.writeNbt(nbt, this.inventory);
		NbtCompound nbtCompound = new NbtCompound();
		this.recipesUsed.forEach((identifier, integer) ->
		{
			nbtCompound.putInt(identifier.toString(), integer);
		});
		nbt.put("RecipesUsed", nbtCompound);
	}
	
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, this.inventory);
		this.energy = nbt.getDouble("energy");
		this.cookTime = nbt.getShort("CookTime");
		this.cookTimeTotal = nbt.getShort("CookTimeTotal");
		NbtCompound nbtCompound = nbt.getCompound("RecipesUsed");
		Iterator<String> var3 = nbtCompound.getKeys().iterator();
		
		while(var3.hasNext())
		{
			String string = (String) var3.next();
			this.recipesUsed.put(new Identifier(string), nbtCompound.getInt(string));
		}
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity blockEntity)
	{
		ItemStack itemStack = (ItemStack) blockEntity.inventory.get(0);
		boolean bl2 = false;
		blockEntity.chargeState = (int) Math.ceil((blockEntity.energy / blockEntity.getEnergyCapacity()) * 14.0);
		
		if(!itemStack.isEmpty())
		{
			RecipeEntry<?> recipe;

			if(!world.getRecipeManager().getFirstMatch(RecipeType.BLASTING, blockEntity, world).isEmpty())
				recipe = (RecipeEntry<?>) world.getRecipeManager().getFirstMatch(RecipeType.BLASTING, blockEntity, world).get();
			else if(!world.getRecipeManager().getFirstMatch(RecipeType.SMOKING, blockEntity, world).isEmpty())
				recipe = (RecipeEntry<?>) world.getRecipeManager().getFirstMatch(RecipeType.SMOKING, blockEntity, world).get();
			else if(!world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, blockEntity, world).isEmpty())
				recipe = (RecipeEntry<?>) world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, blockEntity, world).get();
			else if(!world.getRecipeManager().getFirstMatch(StarflightRecipes.VACUUM_FURNACE, blockEntity, world).isEmpty())
				recipe = (RecipeEntry<?>) world.getRecipeManager().getFirstMatch(StarflightRecipes.VACUUM_FURNACE, blockEntity, world).get();
			else
				recipe = null;
			
			int i = blockEntity.getMaxCountPerStack();

			if(blockEntity.energy > 0 && canAcceptRecipeOutput(world.getRegistryManager(), recipe, blockEntity.inventory, i))
			{
				blockEntity.changeEnergy(-blockEntity.getInput());
				blockEntity.cookTime++;

				if(blockEntity.cookTime == blockEntity.cookTimeTotal)
				{
					blockEntity.cookTime = 0;
					blockEntity.cookTimeTotal = getCookTime(world, blockEntity);

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
		
		if(world.getBlockState(pos).get(ElectricFurnaceBlock.LIT) != (blockEntity.energy > 0 && !itemStack.isEmpty()))
		{
			state = (BlockState) state.with(ElectricFurnaceBlock.LIT, blockEntity.energy > 0 && !itemStack.isEmpty());
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			bl2 = true;
		}
		
		if(bl2)
			markDirty(world, pos, state);
	}
}
