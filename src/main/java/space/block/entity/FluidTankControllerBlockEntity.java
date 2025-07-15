package space.block.entity;

import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import space.block.FluidTankControllerBlock;
import space.block.StarflightBlocks;
import space.util.FluidResourceType;

public class FluidTankControllerBlockEntity extends BlockEntity
{
	private final FluidResourceType fluid;
	private long storageCapacity;
	private long storedFluid;
	private BlockPos centerOfMass;
	
	public FluidTankControllerBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.FLUID_TANK_CONTROLLER_BLOCK_ENTITY, pos, state);
		this.fluid = ((FluidTankControllerBlock) state.getBlock()).getFluidType();
		this.storageCapacity = 0;
		this.storedFluid = 0;
		this.centerOfMass = null;
	}
	
	public FluidTankControllerBlockEntity(BlockEntityType<?> type, FluidResourceType fluid, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.fluid = fluid;
		this.storageCapacity = 0;
		this.storedFluid = 0;
		this.centerOfMass = null;
	}
	
	public FluidResourceType getFluidType()
	{
		return fluid;
	}
	
	public long getStorageCapacity()
	{
		return storageCapacity;
	}
	
	/**
	 * The amount of stored fluid in kilograms.
	 */
	public long getStoredFluid()
	{
		return storedFluid;
	}
	
	public BlockPos getCenterOfMass()
	{
		return centerOfMass;
	}
	
	public void setStorageCapacity(long amount)
	{
		storageCapacity = amount;
	}
	
	public void setStoredFluid(long amount)
	{
		storedFluid = amount;
	}
	
	public void changeStoredFluid(long d)
	{
		storedFluid += d;
		
		if(storedFluid < 0)
			storedFluid = 0;
		else if(storedFluid > storageCapacity)
			storedFluid = storageCapacity;
	}
	
	public void setCenterOfMass(BlockPos position)
	{
		centerOfMass = position;
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		this.storageCapacity = nbt.getLong("storageCapacity");
		this.storedFluid = nbt.getLong("storedFluid");
		Optional<BlockPos> centerOfMassPos = NbtHelper.toBlockPos(nbt, "centerOfMass");
		
		if(centerOfMassPos.isPresent())
			this.centerOfMass = centerOfMassPos.get();
	}

	@Override
	public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		nbt.putLong("storageCapacity", storageCapacity);
		nbt.putLong("storedFluid", storedFluid);
		
		if(centerOfMass != null)
			nbt.put("centerOfMass", NbtHelper.fromBlockPos(centerOfMass));
	}
}