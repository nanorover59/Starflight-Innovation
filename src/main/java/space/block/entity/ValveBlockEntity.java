package space.block.entity;

import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import space.block.StarflightBlocks;
import space.block.ValveBlock;
import space.util.FluidResourceType;

public class ValveBlockEntity extends BlockEntity implements FluidStorageBlockEntity
{
	private FluidResourceType fluid;
	private BlockPos fluidTankController;
	
	public ValveBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.VALVE_BLOCK_ENTITY, pos, state);
		fluid = null;
		fluidTankController = new BlockPos(0, 0, 0);
	}
	
	public int getMode()
	{
		return world.getBlockState(getPos()).get(ValveBlock.MODE);
	}
	
	public FluidResourceType getFluidType()
	{
		return fluid;
	}
	
	public FluidTankControllerBlockEntity getFluidTankController()
	{
		BlockEntity blockEntity = getWorld().getBlockEntity(fluidTankController);
		
		if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
		{
			fluid = ((FluidTankControllerBlockEntity) blockEntity).getFluidType();
			return (FluidTankControllerBlockEntity) blockEntity;
		}
		else
		{
			fluid = null;
			return null;
		}
	}
	
	public void setControllerPosition(BlockPos position)
	{
		fluidTankController = position;
	}
	
	@Override
	public long getFluidCapacity(FluidResourceType fluidType)
	{
		FluidTankControllerBlockEntity fluidTank = getFluidTankController();
		
		if(fluidTank != null && fluidType == fluid)
			return fluidTank.getStorageCapacity();
		else
			return 0;
	}

	@Override
	public long getFluid(FluidResourceType fluidType)
	{
		FluidTankControllerBlockEntity fluidTank = getFluidTankController();
		
		if(fluidTank != null && fluidType == fluid)
			return fluidTank.getStoredFluid();
		else
			return 0;
	}

	@Override
	public void setFluid(FluidResourceType fluidType, long fluid)
	{
		FluidTankControllerBlockEntity fluidTank = getFluidTankController();
		
		if(fluidTank != null && fluidType == this.fluid)
			fluidTank.setStoredFluid(fluid);
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.put("fluidTankController", NbtHelper.fromBlockPos(fluidTankController));
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		Optional<BlockPos> fluidTankControllerPos = NbtHelper.toBlockPos(nbt, "fluidTankController");
		
		if(fluidTankControllerPos.isPresent())
			this.fluidTankController = fluidTankControllerPos.get();
	}
	
	/*public static void serverTick(World world, BlockPos pos, BlockState state, ValveBlockEntity blockEntity)
	{
		if(world.isClient || blockEntity.getMode() != 1 || blockEntity.getFluidTankController() == null || blockEntity.getFluidTankController().getStoredFluid() <= 0)
			return;
		
		double fluidRemaining = Math.min(blockEntity.getFluidTankController().getStoredFluid(), blockEntity.fluid.getStorageDensity() / 36.0);
		
		for(Direction direction : Direction.values())
		{
			BlockPos offsetPos = pos.offset(direction);
			BlockState adjacentState = world.getBlockState(offsetPos);
			
			if(adjacentState.getBlock() instanceof FluidPipeBlock && ((FluidPipeBlock) adjacentState.getBlock()).getFluidType().getID() == blockEntity.getFluidType().getID())
			{
				ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
				double fluidToTransfer = fluidRemaining;
				fluidRemaining = PumpBlockEntity.recursiveSpread(world, offsetPos, checkList, fluidToTransfer, blockEntity.fluid, 2048);
				blockEntity.getFluidTankController().changeStoredFluid(-(fluidToTransfer - fluidRemaining));
				blockEntity.markDirty();
			}
		}
    }*/
}
