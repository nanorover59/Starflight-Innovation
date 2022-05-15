package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;

public class FluidTankInterfaceBlockEntity extends BlockEntity
{
	private boolean active;
	private BlockPos fluidTankController;
	
	public FluidTankInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		active = false;
		fluidTankController = new BlockPos(0, 0, 0);
	}
	
	public String getFluidName()
	{
		return "null";
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public FluidTankControllerBlockEntity getFluidTankController()
	{
		BlockEntity blockEntity = getWorld().getBlockEntity(fluidTankController);
		
		if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
			return (FluidTankControllerBlockEntity) blockEntity;
		else
			return null;
	}
	
	public void setActive(boolean b)
	{
		active = b;
	}
	
	public void setControllerPosition(BlockPos position)
	{
		fluidTankController = position;
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.active = nbt.getBoolean("active");
		this.fluidTankController = NbtHelper.toBlockPos(nbt.getCompound("fluidTankController"));
	}

	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putBoolean("active", active);
		nbt.put("fluidTankController", NbtHelper.fromBlockPos(fluidTankController));
	}
}
