package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import space.block.FluidTankControllerBlock;
import space.block.StarflightBlocks;
import space.util.FluidResourceType;

public class FluidTankControllerBlockEntity extends BlockEntity
{
	private final FluidResourceType fluid;
	private double storageCapacity;
	private double storedFluid;
	private BlockPos centerOfMass;
	
	public FluidTankControllerBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.FLUID_TANK_CONTROLLER_BLOCK_ENTITY, pos, state);
		this.fluid = ((FluidTankControllerBlock) state.getBlock()).getFluidType();
		this.storageCapacity = 0;
		this.storedFluid = 0;
		this.centerOfMass = new BlockPos(0, 0, 0);
	}
	
	public FluidResourceType getFluidType()
	{
		return fluid;
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
		this.storageCapacity = nbt.getDouble("storageCapacity");
		this.storedFluid = nbt.getDouble("storedFluid");
		this.centerOfMass = NbtHelper.toBlockPos(nbt.getCompound("centerOfMass"));
	}

	@Override
	public void writeNbt(NbtCompound nbt)
	{
		nbt.putDouble("storageCapacity", storageCapacity);
		nbt.putDouble("storedFluid", storedFluid);
		nbt.put("centerOfMass", NbtHelper.fromBlockPos(centerOfMass));
	}
}
