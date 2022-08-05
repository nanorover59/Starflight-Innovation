package space.block.entity;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
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

public class IceElectrolyzerBlockEntity extends LockableContainerBlockEntity implements SidedInventory
{
	public DefaultedList<ItemStack> inventory;
	int powerState;
	int time;
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
					break;
				}

			}

			public int size()
			{
				return 2;
			}
		};
	}
	
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, this.inventory);
		this.time = nbt.getShort("time");
	}

	public void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putShort("time", (short) this.time);
	}
	
	public boolean hasValidItem()
	{
		return false;
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, IceElectrolyzerBlockEntity blockEntity)
	{
		ItemStack itemStack = (ItemStack) blockEntity.inventory.get(0);
		boolean bl2 = false;
		int processTime = 64;
		
		if(blockEntity.powerState == 1 && !itemStack.isEmpty())
		{
			
		}
		
		if(world.getBlockState(pos).get(IceElectrolyzerBlock.LIT) != (blockEntity.time > 0 && !itemStack.isEmpty()))
		{
			state = (BlockState) state.with(IceElectrolyzerBlock.LIT, blockEntity.time > 0 && !itemStack.isEmpty());
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			bl2 = true;
		}
		
		if(bl2)
			markDirty(world, pos, state);
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
		return new IceElectrolyzerScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
	}
}
