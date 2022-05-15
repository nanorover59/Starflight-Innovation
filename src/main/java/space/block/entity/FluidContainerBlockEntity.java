package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class FluidContainerBlockEntity extends BlockEntity
{
	private double storedFluid;
	
	public FluidContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	public String getFluidName()
	{
		return "null";
	}

	public double getStorageCapacity()
	{
		return 4.0;
	}
	
	public double getStoredFluid()
	{
		return storedFluid;
	}
	
	public void changeStoredFluid(double d)
	{
		storedFluid += d;
		
		if(storedFluid < 0.0)
			storedFluid = 0.0;
		else if(storedFluid > getStorageCapacity())
			storedFluid = getStorageCapacity();
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.storedFluid = nbt.getDouble("storedFluid");
	}

	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putDouble("storedFluid", storedFluid);
	}
}
