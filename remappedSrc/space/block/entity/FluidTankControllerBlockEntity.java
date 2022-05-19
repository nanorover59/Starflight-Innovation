package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;

public class FluidTankControllerBlockEntity extends BlockEntity
{
	private boolean active;
	private double storageCapacity;
	private double storedFluid;
	private BlockPos centerOfMass;
	
	public FluidTankControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		active = false;
		storageCapacity = 0;
		storedFluid = 0;
		centerOfMass = new BlockPos(0, 0, 0);
	}
	
	public String getFluidName()
	{
		return "null";
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public double getStorageCapacity()
	{
		return storageCapacity;
	}
	
	/**
	 * The amount of stored fluid in kilograms.
	 */
	public double getStoredFluid()
	{
		return storedFluid;
	}
	
	public BlockPos getCenterOfMass()
	{
		return centerOfMass;
	}
	
	public void setActive(boolean b)
	{
		active = b;
	}
	
	public void setStorageCapacity(double d)
	{
		storageCapacity = d;
	}
	
	public void setStoredFluid(double d)
	{
		storedFluid = d;
	}
	
	public void changeStoredFluid(double d)
	{
		storedFluid += d;
		
		if(storedFluid < 0.0)
			storedFluid = 0.0;
		else if(storedFluid > storageCapacity)
			storedFluid = storageCapacity;
	}
	
	public void setCenterOfMass(BlockPos position)
	{
		centerOfMass = position;
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		this.active = nbt.getBoolean("active");
		this.storageCapacity = nbt.getDouble("storageCapacity");
		this.storedFluid = nbt.getDouble("storedFluid");
		this.centerOfMass = NbtHelper.toBlockPos(nbt.getCompound("centerOfMass"));
	}

	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		nbt.putBoolean("active", active);
		nbt.putDouble("storageCapacity", storageCapacity);
		nbt.putDouble("storedFluid", storedFluid);
		nbt.put("centerOfMass", NbtHelper.fromBlockPos(centerOfMass));
	}
}
