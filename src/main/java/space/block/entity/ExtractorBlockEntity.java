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
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.ExtractorBlock;
import space.block.FluidUtilityBlock;
import space.block.StarflightBlocks;
import space.screen.ExtractorScreenHandler;
import space.util.FluidResourceType;

public class ExtractorBlockEntity extends LockableContainerBlockEntity implements SidedInventory, EnergyBlockEntity
{
	public static Map<Item, Integer> iceMap = Maps.newLinkedHashMap();
	public DefaultedList<ItemStack> inventory;
	private double energy;
	private int chargeState;
	private int time;
	private int totalTime;
	protected final PropertyDelegate propertyDelegate;

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
	
	public boolean hasValidItem()
	{
		ItemStack itemStack = (ItemStack) inventory.get(0);
		return iceMap.containsKey(itemStack.getItem());
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
	public void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putDouble("energy", this.energy);
		nbt.putShort("time", (short) this.time);
		nbt.putShort("totalTime", (short) this.totalTime);
		Inventories.writeNbt(nbt, this.inventory);
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, this.inventory);
		this.energy = nbt.getDouble("energy");
		this.time = nbt.getShort("time");
		this.totalTime = nbt.getShort("totalTime");
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, ExtractorBlockEntity blockEntity)
	{
		ItemStack itemStack = (ItemStack) blockEntity.inventory.get(0);
		blockEntity.chargeState = (int) Math.ceil((blockEntity.energy / blockEntity.getEnergyCapacity()) * 14.0);
		
		if(blockEntity.hasValidItem())
		{
			if(blockEntity.totalTime == 0)
			{
				blockEntity.time = 0;
				blockEntity.totalTime = iceMap.get(itemStack.getItem()) / 5;
			}
			
			if(blockEntity.energy > 0)
			{
				blockEntity.changeEnergy(-blockEntity.getInput());
				blockEntity.time++;
				
				if(blockEntity.time == blockEntity.totalTime)
				{
					double massFlow = iceMap.get(itemStack.getItem());
					int waterOutlets = 0;
					
					for(Direction direction : Direction.values())
					{
						if(direction == state.get(HorizontalFacingBlock.FACING))
							continue;
						
						BlockPos offset = pos.offset(direction);
						Block offsetBlock = world.getBlockState(offset).getBlock();
						
						if(offsetBlock instanceof FluidUtilityBlock && ((FluidUtilityBlock) offsetBlock).getFluidType().getID() == FluidResourceType.WATER.getID())
						{
							BlockEntity offsetBlockEntity = world.getBlockEntity(offset);
							
							if(offsetBlockEntity instanceof FluidPipeBlockEntity)
								waterOutlets++;
						}
					}
					
					for(Direction direction : Direction.values())
					{
						if(direction == state.get(HorizontalFacingBlock.FACING))
							continue;
						
						BlockPos offset = pos.offset(direction);
						BlockState offsetState = world.getBlockState(offset);
						
						if(offsetState.getBlock() == StarflightBlocks.WATER_PIPE)
						{
							ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
							ElectrolyzerBlockEntity.recursiveSpread(world, offset, checkList, massFlow / waterOutlets, FluidResourceType.WATER, 2048);
						}
					}
					
					itemStack.decrement(1);
					blockEntity.time = 0;
					blockEntity.totalTime = 0;
				}
			}
			else
				blockEntity.time -= 2;
		}
		else
		{
			blockEntity.time = 0;
			blockEntity.totalTime = 0;
		}
		
		if(world.getBlockState(pos).get(ExtractorBlock.LIT) != (blockEntity.chargeState > 0 && !itemStack.isEmpty()))
		{
			state = (BlockState) state.with(ExtractorBlock.LIT, blockEntity.chargeState > 0 && !itemStack.isEmpty());
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			markDirty(world, pos, state);
		}
	}
}