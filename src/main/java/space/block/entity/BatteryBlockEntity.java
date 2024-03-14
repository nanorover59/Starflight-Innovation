package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.inventory.ImplementedInventory;
import space.item.BatteryCellItem;
import space.screen.BatteryScreenHandler;

public class BatteryBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, EnergyBlockEntity
{
	private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
	private ArrayList<BlockPos> outputs = new ArrayList<BlockPos>();
	
	public BatteryBlockEntity(BlockPos blockPos, BlockState blockState)
	{
        super(StarflightBlocks.BATTERY_BLOCK_ENTITY, blockPos, blockState);
    }

	@Override
	public Text getDisplayName()
	{
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	public DefaultedList<ItemStack> getItems()
	{
		return inventory;
	}
	
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player)
	{
		return new BatteryScreenHandler(syncId, playerInventory, this);
	}
	
	@Override
	public double getOutput()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getOutput() / world.getTickManager().getTickRate();
	}
	
	@Override
	public double getInput()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getInput() / world.getTickManager().getTickRate();
	}
	
	@Override
	public double getEnergyStored()
	{
		double d = 0.0;
		
		for(ItemStack stack : this.inventory)
		{
			if(stack.isEmpty())
				continue;
			else
				d += stack.getNbt().getDouble("charge");
		}
		
		return d;
	}

	@Override
	public double getEnergyCapacity()
	{
		double d = 0.0;
		
		for(ItemStack stack : this.inventory)
		{
			if(stack.isEmpty())
				continue;
			else
				d += ((BatteryCellItem) stack.getItem()).getMaxCharge();
		}
		
		return d;
	}

	@Override
	public double changeEnergy(double amount)
	{
		int batteryCount = 0;
		
		for(ItemStack stack : this.inventory)
		{
			if(stack.isEmpty())
				continue;
			
			double charge = stack.getNbt().getDouble("charge");
			
			if(charge < ((BatteryCellItem) stack.getItem()).getMaxCharge())
				batteryCount++;
		}
		
		if(batteryCount == 0)
			return 0;
		
		double fraction = amount / batteryCount;
		
		for(ItemStack stack : this.inventory)
		{
			if(stack.isEmpty())
				continue;
			
			double maxCharge = ((BatteryCellItem) stack.getItem()).getMaxCharge();
			double previousCharge = stack.getNbt().getDouble("charge");
			
			if(previousCharge + fraction > maxCharge)
			{
				stack.getNbt().putDouble("charge", maxCharge);
				amount -= (previousCharge + fraction) - maxCharge;
			}
			else if(previousCharge + fraction < 0)
			{
				stack.getNbt().putDouble("charge", 0);
				amount -= previousCharge + fraction;
			}
			else
				stack.getNbt().putDouble("charge", previousCharge + fraction);
		}
		
		world.updateComparators(pos, world.getBlockState(pos).getBlock());
		return amount;
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
	public void clearOutputs()
	{
		outputs.clear();
	}
	
	@Override
	public void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, this.inventory);
		EnergyBlockEntity.outputsToNBT(outputs, nbt);
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		Inventories.readNbt(nbt, this.inventory);
		outputs.addAll(EnergyBlockEntity.outputsFromNBT(nbt));
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, BatteryBlockEntity blockEntity)
	{
		EnergyBlockEntity.transferEnergy(blockEntity, 0.5);
	}
}