package space.block.entity;

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
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.World;
import space.block.StarflightBlocks;
import space.screen.StirlingEngineScreenHandler;

public class StirlingEngineBlockEntity extends LockableContainerBlockEntity implements SidedInventory
{
	public DefaultedList<ItemStack> inventory;
	int powerState;
	int burnTime;
	int fuelTime;
	protected final PropertyDelegate propertyDelegate;

	public StirlingEngineBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.STIRLING_ENGINE_BLOCK_ENTITY, blockPos, blockState);
		this.inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
		this.propertyDelegate = new PropertyDelegate()
		{
			public int get(int index)
			{
				switch(index)
				{
				case 0:
					return StirlingEngineBlockEntity.this.powerState;
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
					StirlingEngineBlockEntity.this.powerState = value;
				case 1:
					StirlingEngineBlockEntity.this.burnTime = value;
					break;
				case 2:
					StirlingEngineBlockEntity.this.fuelTime = value;
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
		for(RegistryEntry<Item> registryEntry : Registry.ITEM.iterateEntries(tag))
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

	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, this.inventory);
		this.burnTime = nbt.getShort("BurnTime");
		this.fuelTime = this.getFuelTime((ItemStack) this.inventory.get(0));
	}

	public void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putShort("BurnTime", (short) this.burnTime);
		Inventories.writeNbt(nbt, this.inventory);
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, StirlingEngineBlockEntity blockEntity)
	{
		boolean bl = blockEntity.isBurning();
		boolean bl2 = false;

		if(blockEntity.isBurning())
		{
			blockEntity.burnTime--;
			blockEntity.powerState = 1;
		}
		else
			blockEntity.powerState = 0;

		ItemStack itemStack = (ItemStack) blockEntity.inventory.get(0);
		if(blockEntity.isBurning() || !itemStack.isEmpty() && !((ItemStack) blockEntity.inventory.get(0)).isEmpty())
		{
			if(!blockEntity.isBurning())
			{
				blockEntity.burnTime = blockEntity.getFuelTime(itemStack);
				blockEntity.fuelTime = blockEntity.burnTime;
				if(blockEntity.isBurning())
				{
					bl2 = true;
					if(!itemStack.isEmpty())
					{
						Item item = itemStack.getItem();
						itemStack.decrement(1);
						if(itemStack.isEmpty())
						{
							Item item2 = item.getRecipeRemainder();
							blockEntity.inventory.set(0, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
						}
					}
				}
			}
		}

		if(bl != blockEntity.isBurning())
		{
			bl2 = true;
			state = (BlockState) state.with(AbstractFurnaceBlock.LIT, blockEntity.isBurning());
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
		}

		if(bl2)
			markDirty(world, pos, state);
	}

	protected int getFuelTime(ItemStack fuel)
	{
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
		return new StirlingEngineScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
	}
}
