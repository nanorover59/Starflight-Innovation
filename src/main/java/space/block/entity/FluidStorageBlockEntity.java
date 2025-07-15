package space.block.entity;

import net.minecraft.util.math.MathHelper;
import space.util.FluidResourceType;

public interface FluidStorageBlockEntity
{
	long getFluidCapacity(FluidResourceType fluidType);
	
	long getFluid(FluidResourceType fluidType);
	
	void setFluid(FluidResourceType fluidType, long fluid);
	
	default long addFluid(FluidResourceType fluidType, long amount, boolean change)
	{
		long fluid = getFluid(fluidType);
		long newFluid = fluid + amount;
		fluid = MathHelper.clamp(newFluid, 0, getFluidCapacity(fluidType));
		
		if(change)
			setFluid(fluidType, fluid);
		
		return amount - (newFluid - fluid);
	}
	
	default long removeFluid(FluidResourceType fluidType, long amount, boolean change)
	{
		return -addFluid(fluidType, -amount, change);
	}
}