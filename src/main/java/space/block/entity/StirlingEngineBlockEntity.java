package space.block.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.item.StarflightItems;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.screen.StirlingEngineScreenHandler;

public class StirlingEngineBlockEntity extends LockableContainerBlockEntity implements SidedInventory, EnergyBlockEntity
{
	public DefaultedList<ItemStack> inventory;
	private ArrayList<BlockPos> outputs = new ArrayList<BlockPos>();
	protected final PropertyDelegate propertyDelegate;
	private long energy;
	int burnTime;
	int fuelTime;

	public StirlingEngineBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.STIRLING_ENGINE_BLOCK_ENTITY, blockPos, blockState);
		this.inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
		this.propertyDelegate = new PropertyDelegate()
		{
			public int get(int index)
			{
				switch(index)
				{
				case 0:
					return (int) StirlingEngineBlockEntity.this.energy;
				case 1:
					return StirlingEngineBlockEntity.this.burnTime;
				case 2:
					return StirlingEngineBlockEntity.this.fuelTime;
				default:
					return 0;
				}
			}

			public void set(int index, int value)
			{
				switch(index)
				{
				case 0:
					StirlingEngineBlockEntity.this.energy = value;
					break;
				case 1:
					StirlingEngineBlockEntity.this.burnTime = value;
					break;
				case 2:
					StirlingEngineBlockEntity.this.fuelTime = value;
					break;
				}

			}

			public int size()
			{
				return 3;
			}
		};
	}

	public static Map<Item, Integer> createFuelTimeMap()
	{
        LinkedHashMap<Item, Integer> map = Maps.newLinkedHashMap();
        addFuel(map, Items.LAVA_BUCKET, 20000);
        addFuel(map, Blocks.COAL_BLOCK, 16000);
        addFuel(map, Items.BLAZE_ROD, 2400);
        addFuel(map, Items.COAL, 1600);
        addFuel(map, Items.CHARCOAL, 1600);
        addFuel(map, ItemTags.LOGS, 300);
        addFuel(map, ItemTags.PLANKS, 300);
        addFuel(map, ItemTags.WOODEN_STAIRS, 300);
        addFuel(map, ItemTags.WOODEN_SLABS, 150);
        addFuel(map, ItemTags.WOODEN_TRAPDOORS, 300);
        addFuel(map, ItemTags.WOODEN_PRESSURE_PLATES, 300);
        addFuel(map, Blocks.OAK_FENCE, 300);
        addFuel(map, Blocks.BIRCH_FENCE, 300);
        addFuel(map, Blocks.SPRUCE_FENCE, 300);
        addFuel(map, Blocks.JUNGLE_FENCE, 300);
        addFuel(map, Blocks.DARK_OAK_FENCE, 300);
        addFuel(map, Blocks.ACACIA_FENCE, 300);
        addFuel(map, Blocks.OAK_FENCE_GATE, 300);
        addFuel(map, Blocks.BIRCH_FENCE_GATE, 300);
        addFuel(map, Blocks.SPRUCE_FENCE_GATE, 300);
        addFuel(map, Blocks.JUNGLE_FENCE_GATE, 300);
        addFuel(map, Blocks.DARK_OAK_FENCE_GATE, 300);
        addFuel(map, Blocks.ACACIA_FENCE_GATE, 300);
        addFuel(map, Blocks.NOTE_BLOCK, 300);
        addFuel(map, Blocks.BOOKSHELF, 300);
        addFuel(map, Blocks.LECTERN, 300);
        addFuel(map, Blocks.JUKEBOX, 300);
        addFuel(map, Blocks.CHEST, 300);
        addFuel(map, Blocks.TRAPPED_CHEST, 300);
        addFuel(map, Blocks.CRAFTING_TABLE, 300);
        addFuel(map, Blocks.DAYLIGHT_DETECTOR, 300);
        addFuel(map, ItemTags.BANNERS, 300);
        addFuel(map, Items.BOW, 300);
        addFuel(map, Items.FISHING_ROD, 300);
        addFuel(map, Blocks.LADDER, 300);
        addFuel(map, ItemTags.SIGNS, 200);
        addFuel(map, Items.WOODEN_SHOVEL, 200);
        addFuel(map, Items.WOODEN_SWORD, 200);
        addFuel(map, Items.WOODEN_HOE, 200);
        addFuel(map, Items.WOODEN_AXE, 200);
        addFuel(map, Items.WOODEN_PICKAXE, 200);
        addFuel(map, ItemTags.WOODEN_DOORS, 200);
        addFuel(map, ItemTags.BOATS, 1200);
        addFuel(map, ItemTags.WOOL, 100);
        addFuel(map, ItemTags.WOODEN_BUTTONS, 100);
        addFuel(map, Items.STICK, 100);
        addFuel(map, ItemTags.SAPLINGS, 100);
        addFuel(map, Items.BOWL, 100);
        addFuel(map, ItemTags.WOOL_CARPETS, 67);
        addFuel(map, Blocks.DRIED_KELP_BLOCK, 4001);
        addFuel(map, Items.CROSSBOW, 300);
        addFuel(map, Blocks.BAMBOO, 50);
        addFuel(map, Blocks.DEAD_BUSH, 100);
        addFuel(map, Blocks.SCAFFOLDING, 400);
        addFuel(map, Blocks.LOOM, 300);
        addFuel(map, Blocks.BARREL, 300);
        addFuel(map, Blocks.CARTOGRAPHY_TABLE, 300);
        addFuel(map, Blocks.FLETCHING_TABLE, 300);
        addFuel(map, Blocks.SMITHING_TABLE, 300);
        addFuel(map, Blocks.COMPOSTER, 300);
        addFuel(map, Blocks.AZALEA, 100);
        addFuel(map, Blocks.FLOWERING_AZALEA, 100);
        return map;
    }

	private static boolean isNonFlammableWood(Item item)
	{
		return item.getDefaultStack().isIn(ItemTags.NON_FLAMMABLE_WOOD);
	}

	public static void addFuel(Map<Item, Integer> fuelTimes, TagKey<Item> tag, int fuelTime)
	{
		for(RegistryEntry<Item> registryEntry : Registries.ITEM.iterateEntries(tag))
		{
			if(isNonFlammableWood(registryEntry.value()))
				continue;
			
			fuelTimes.put(registryEntry.value(), fuelTime);
		}
	}

	public static void addFuel(Map<Item, Integer> fuelTimes, ItemConvertible item, int fuelTime)
	{
		fuelTimes.put(item.asItem(), fuelTime);
	}

	public boolean isBurning()
	{
		return this.burnTime > 0;
	}

	protected int getFuelTime(ItemStack fuel)
	{
		if(world != null && !fuel.isIn(StarflightItems.NO_OXYGEN_FUEL_ITEM_TAG))
		{
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
			
			if((data != null && !data.hasOxygen()) || data.isOrbit())
				return 0;
		}
		
		if(fuel.isEmpty())
			return 0;
		else
		{
			Item item = fuel.getItem();
			return (Integer) createFuelTimeMap().getOrDefault(item, 0);
		}
	}

	public static boolean canUseAsFuel(ItemStack stack)
	{
		return createFuelTimeMap().containsKey(stack.getItem());
	}

	public int[] getAvailableSlots(Direction side)
	{
		return new int[] {1};
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
			return player.squaredDistanceTo((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
	}

	public boolean isValid(int slot, ItemStack stack)
	{
		return (slot == 0 && stack.contains(StarflightItems.ENERGY)) || (slot == 1 && canUseAsFuel(stack));
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
		return new StirlingEngineScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
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
	public ArrayList<BlockPos> getOutputs()
	{
		return outputs;
	}

	@Override
	public void addOutput(BlockPos output)
	{
		outputs.add(output);
	}
	
	@Override
	public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		Inventories.writeNbt(nbt, this.inventory, registryLookup);
		EnergyBlockEntity.outputsToNBT(outputs, nbt);
		nbt.putLong("energy", this.energy);
		nbt.putInt("burnTime", this.burnTime);
		nbt.putInt("fuelTime", this.fuelTime);
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, this.inventory, registryLookup);
		this.outputs = EnergyBlockEntity.outputsFromNBT(nbt);
		this.energy = nbt.getLong("energy");
		this.burnTime = nbt.getInt("burnTime");
		this.fuelTime = nbt.getInt("fuelTime");
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, StirlingEngineBlockEntity blockEntity)
	{
		ItemStack batteryStack = (ItemStack) blockEntity.inventory.get(0);
		ItemStack fuelStack = (ItemStack) blockEntity.inventory.get(1);
		boolean bl = blockEntity.isBurning();
		blockEntity.chargeItem(batteryStack, ((EnergyBlock) blockEntity.getCachedState().getBlock()).getOutput());
		blockEntity.transferEnergy(((EnergyBlock) blockEntity.getCachedState().getBlock()).getOutput());
		
		if(!blockEntity.isBurning() && !fuelStack.isEmpty() && blockEntity.getEnergy() < blockEntity.getEnergyCapacity())
		{
			int itemFuelTime = blockEntity.getFuelTime(fuelStack);
			
			if(itemFuelTime > 0)
			{
				blockEntity.fuelTime = itemFuelTime;
				blockEntity.burnTime = itemFuelTime;
				
				if(blockEntity.isBurning() && !fuelStack.isEmpty())
				{
					Item item = fuelStack.getItem();
					fuelStack.decrement(1);
					
					if(fuelStack.isEmpty())
					{
						Item item2 = item.getRecipeRemainder();
						blockEntity.inventory.set(1, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
					}
				}
			}
		}
		
		if(blockEntity.isBurning())
		{
			blockEntity.burnTime--;
			blockEntity.addEnergy(((EnergyBlock) blockEntity.getCachedState().getBlock()).getOutput(), true);
			world.markDirty(pos);
		}
		
		if(bl != blockEntity.isBurning())
		{
			state = (BlockState) state.with(AbstractFurnaceBlock.LIT, blockEntity.isBurning());
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			markDirty(world, pos, state);
		}
	}
}