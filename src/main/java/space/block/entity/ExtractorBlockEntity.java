package space.block.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
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
import space.block.EnergyBlock;
import space.block.ExtractorBlock;
import space.block.FluidUtilityBlock;
import space.block.StarflightBlocks;
import space.recipe.ExtractorRecipe;
import space.recipe.StarflightRecipes;
import space.screen.ExtractorScreenHandler;
import space.util.FluidResourceType;

public class ExtractorBlockEntity extends LockableContainerBlockEntity implements SidedInventory, RecipeUnlocker, RecipeInputProvider, EnergyBlockEntity
{
	public static Map<Item, Integer> iceMap = Maps.newLinkedHashMap();
	public DefaultedList<ItemStack> inventory;
	private double energy;
	private int chargeState;
	private int time;
	private int totalTime;
	protected final PropertyDelegate propertyDelegate;
	private final Object2IntOpenHashMap<Identifier> recipesUsed;

	public ExtractorBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.EXTRACTOR_BLOCK_ENTITY, blockPos, blockState);
		this.inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
		this.propertyDelegate = new PropertyDelegate()
		{
			public int get(int index)
			{
				switch(index)
				{
				case 0:
					return ExtractorBlockEntity.this.chargeState;
				case 1:
					return ExtractorBlockEntity.this.time;
				case 2:
					return ExtractorBlockEntity.this.totalTime;
				default:
					return 0;
				}
			}

			public void set(int index, int value)
			{
				switch(index)
				{
				case 0:
					ExtractorBlockEntity.this.chargeState = value;
					break;
				case 1:
					ExtractorBlockEntity.this.time = value;
				case 2:
					ExtractorBlockEntity.this.totalTime = value;
					break;
				}

			}

			public int size()
			{
				return 3;
			}
		};
		
		this.recipesUsed = new Object2IntOpenHashMap<Identifier>();

		if(iceMap.isEmpty())
			iceMap = createIceMap();
	}

	public static Map<Item, Integer> createIceMap()
	{
		LinkedHashMap<Item, Integer> map = Maps.newLinkedHashMap();
		addIce(map, Blocks.ICE, 1000);
		addIce(map, Blocks.PACKED_ICE, 1000);
		addIce(map, Blocks.BLUE_ICE, 1000);
		addIce(map, Blocks.SNOW_BLOCK, 40);
		addIce(map, Blocks.POWDER_SNOW, 10);
		addIce(map, Items.SNOWBALL, 10);
		return map;
	}

	public static void addIce(Map<Item, Integer> iceMap, ItemConvertible item, int iceMass)
	{
		iceMap.put(item.asItem(), iceMass);
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
			
			if(!itemStack.isOf(itemStack2.getItem()))
			{
				if(itemStack3.isEmpty())
					slots.set(1, itemStack2.copy());
				else if(itemStack3.isOf(itemStack2.getItem()))
					itemStack3.increment(1);
			}

			itemStack.decrement(1);
			return true;
		}
		else
			return false;
	}
	
	public Optional<RecipeEntry<ExtractorRecipe>> getFirstMatchRecipeOptional()
	{
		Optional<RecipeEntry<ExtractorRecipe>> firstMatch = world.getRecipeManager().getFirstMatch(StarflightRecipes.EXTRACTOR, new SingleStackRecipeInput(inventory.get(0)), world);
		
		if(!firstMatch.isEmpty())
			return firstMatch;
		
		return Optional.empty();
	}
	
	public RecipeEntry<ExtractorRecipe> getFirstMatchRecipe()
	{
		Optional<RecipeEntry<ExtractorRecipe>> firstMatch = getFirstMatchRecipeOptional();
		return firstMatch.isEmpty() ? null : getFirstMatchRecipeOptional().get();
	}
	
	public boolean hasValidItem()
	{
		return ExtractorBlockEntity.canAcceptRecipeOutput(world.getRegistryManager(), getFirstMatchRecipe(), inventory, getMaxCountPerStack());
	}

	public int getTime()
	{
		return (Integer) getFirstMatchRecipeOptional().map(recipe -> ((ExtractorRecipe) recipe.value()).getCookingTime()).orElse(200) / 2;
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
			this.totalTime = getTime();
			this.time = 0;
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
		return slot == 1;
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
				dropExperience(world, pos, entry.getIntValue(), ((ExtractorRecipe) recipe.value()).getExperience());
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
		return new ExtractorScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
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
		nbt.putShort("time", (short) this.time);
		nbt.putShort("totalTime", (short) this.totalTime);
		Inventories.writeNbt(nbt, this.inventory, registryLookup);
	}

	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, this.inventory, registryLookup);
		this.energy = nbt.getDouble("energy");
		this.time = nbt.getShort("time");
		this.totalTime = nbt.getShort("totalTime");
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, ExtractorBlockEntity blockEntity)
	{
		ItemStack itemStack = (ItemStack) blockEntity.inventory.get(0);
		boolean bl2 = false;
		blockEntity.chargeState = (int) Math.ceil((blockEntity.energy / blockEntity.getEnergyCapacity()) * 14.0);
		
		if(!itemStack.isEmpty())
		{
			RecipeEntry<ExtractorRecipe> recipe = blockEntity.getFirstMatchRecipe();
			int i = blockEntity.getMaxCountPerStack();

			if(blockEntity.energy > 0 && canAcceptRecipeOutput(world.getRegistryManager(), recipe, blockEntity.inventory, i))
			{
				blockEntity.changeEnergy(-blockEntity.getInput());
				blockEntity.time++;

				if(blockEntity.time == blockEntity.totalTime)
				{
					blockEntity.time = 0;
					blockEntity.totalTime = blockEntity.getTime();

					if(craftRecipe(world.getRegistryManager(), recipe, blockEntity.inventory, i))
						blockEntity.setLastRecipe(recipe);
					
					double water = recipe.value().getWater();
					double oxygen = recipe.value().getWater();
					double hydrogen = recipe.value().getWater();

					for(Direction direction : Direction.values())
					{
						if(direction == state.get(HorizontalFacingBlock.FACING))
							continue;

						BlockPos offset = pos.offset(direction);
						Block offsetBlock = world.getBlockState(offset).getBlock();
						
						if(offsetBlock instanceof FluidUtilityBlock)
						{
							BlockEntity offsetBlockEntity = world.getBlockEntity(offset);
							
							if(offsetBlockEntity instanceof FluidPipeBlockEntity)
							{
								FluidPipeBlockEntity pipeBlockEntity = ((FluidPipeBlockEntity) offsetBlockEntity);
								
								if(water > 0.0 && pipeBlockEntity.getFluidType() == FluidResourceType.WATER)
								{
									ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
									water = PumpBlockEntity.recursiveSpread(world, offset, checkList, water, FluidResourceType.WATER, 2048);
								}
								
								if(oxygen > 0.0 && pipeBlockEntity.getFluidType() == FluidResourceType.OXYGEN)
								{
									ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
									oxygen = PumpBlockEntity.recursiveSpread(world, offset, checkList, water, FluidResourceType.OXYGEN, 2048);
								}
								
								if(hydrogen > 0.0 && pipeBlockEntity.getFluidType() == FluidResourceType.WATER)
								{
									ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
									hydrogen = PumpBlockEntity.recursiveSpread(world, offset, checkList, water, FluidResourceType.WATER, 2048);
								}
							}
							else if(water > 0.0 && offsetBlockEntity instanceof WaterTankBlockEntity)
							{
								ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
								water = PumpBlockEntity.recursiveSpread(world, offset, checkList, water, FluidResourceType.WATER, 2048);
							}
						}
					}

					bl2 = true;
				}
			}
			else
				blockEntity.time = 0;
		}
		
		if(blockEntity.time > 0 && (itemStack.isEmpty() || blockEntity.energy == 0))
			blockEntity.time = MathHelper.clamp((int) (blockEntity.time - 2), (int) 0, (int) blockEntity.totalTime);
		
		if(world.getBlockState(pos).get(ExtractorBlock.LIT) != (blockEntity.energy > 0 && blockEntity.hasValidItem()))
		{
			state = (BlockState) state.with(ExtractorBlock.LIT, blockEntity.energy > 0 && blockEntity.hasValidItem());
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			bl2 = true;
		}
		
		if(bl2)
			markDirty(world, pos, state);
	}
}