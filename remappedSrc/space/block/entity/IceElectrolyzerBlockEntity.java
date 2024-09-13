package space.block.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
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
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.IceElectrolyzerBlock;
import space.block.StarflightBlocks;
import space.screen.IceElectrolyzerScreenHandler;

public class IceElectrolyzerBlockEntity extends LockableContainerBlockEntity implements SidedInventory, PoweredBlockEntity
{
	public static Map<Item, Integer> iceMap = Maps.newLinkedHashMap();
	public DefaultedList<ItemStack> inventory;
	private int powerState;
	private int time;
	private int totalTime;
	protected final PropertyDelegate propertyDelegate;

	public IceElectrolyzerBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.ICE_ELECTROLYZER_BLOCK_ENTITY, blockPos, blockState);
		this.inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
		this.propertyDelegate = new PropertyDelegate()
		{
			public int get(int index)
			{
				switch(index)
				{
				case 0:
					return IceElectrolyzerBlockEntity.this.powerState;
				case 1:
					return IceElectrolyzerBlockEntity.this.time;
				case 2:
					return IceElectrolyzerBlockEntity.this.totalTime;
				default:
					return 0;
				}
			}

			public void set(int index, int value)
			{
				switch(index)
				{
				case 0:
					IceElectrolyzerBlockEntity.this.powerState = value;
					break;
				case 1:
					IceElectrolyzerBlockEntity.this.time = value;
				case 2:
					IceElectrolyzerBlockEntity.this.totalTime = value;
					break;
				}

			}

			public int size()
			{
				return 3;
			}
		};
		
		if(iceMap.isEmpty())
			iceMap = createIceMap();
	}
	
	public static Map<Item, Integer> createIceMap()
	{
        LinkedHashMap<Item, Integer> map = Maps.newLinkedHashMap();
        addIce(map, Blocks.ICE, 1000);
        addIce(map, Blocks.PACKED_ICE, 1200);
        addIce(map, Blocks.BLUE_ICE, 1200);
        addIce(map, Blocks.SNOW_BLOCK, 40);
        addIce(map, Blocks.POWDER_SNOW, 10);
        addIce(map, Items.SNOWBALL, 10);
        return map;
    }

	public static void addIce(Map<Item, Integer> iceMap, ItemConvertible item, int iceMass)
	{
		iceMap.put(item.asItem(), iceMass);
	}
	
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, this.inventory);
		this.time = nbt.getShort("time");
		this.totalTime = nbt.getShort("totalTime");
	}

	public void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putShort("time", (short) this.time);
		nbt.putShort("totalTime", (short) this.totalTime);
		Inventories.writeNbt(nbt, this.inventory);
	}
	
	public boolean hasValidItem()
	{
		ItemStack itemStack = (ItemStack) inventory.get(0);
		return iceMap.containsKey(itemStack.getItem());
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, IceElectrolyzerBlockEntity blockEntity)
	{
		ItemStack itemStack = (ItemStack) blockEntity.inventory.get(0);
		
		if(blockEntity.powerState == 1 && blockEntity.hasValidItem())
		{
			if(blockEntity.time == 0)
			{
				blockEntity.time = iceMap.get(itemStack.getItem()) / 5;
				blockEntity.totalTime = blockEntity.time;
			}
			else
			{
				if(blockEntity.time == 1)
				{
					double totalMassFlow = iceMap.get(itemStack.getItem()); // Kilograms per tick.
					double oxygen = totalMassFlow * (8.0 / 9.0);
					double hydrogen = totalMassFlow * (1.0 / 9.0);
					BlockPos leftSide = pos.offset(state.get(HorizontalFacingBlock.FACING).rotateYClockwise());
					BlockPos rightSide = pos.offset(state.get(HorizontalFacingBlock.FACING).rotateYCounterclockwise());
					ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
					ElectrolyzerBlockEntity.recursiveSpread(world, leftSide, checkList, oxygen, "oxygen", 2048);
					checkList.clear();
					ElectrolyzerBlockEntity.recursiveSpread(world, rightSide, checkList, hydrogen, "hydrogen", 2048);
					blockEntity.inventory.get(0).decrement(1);
					blockEntity.totalTime = 0;
				}
				
				blockEntity.time--;
			}
		}
		else
		{
			blockEntity.time = 0;
			blockEntity.totalTime = 0;
		}
		
		if(world.getBlockState(pos).get(IceElectrolyzerBlock.LIT) != (blockEntity.powerState > 0 && !itemStack.isEmpty()))
		{
			state = (BlockState) state.with(IceElectrolyzerBlock.LIT, blockEntity.powerState > 0 && !itemStack.isEmpty());
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			markDirty(world, pos, state);
		}
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
	public void setPowerState(int i)
	{
		powerState = i;
	}

	@Override
	public int getPowerState()
	{
		return powerState;
	}
	
	@Override
	public Text getContainerName()
	{
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		return new IceElectrolyzerScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
	}
}
