package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.FluidPipeBlock;
import space.block.StarflightBlocks;
import space.block.ValveBlock;
import space.util.FluidResourceType;

public class BalloonControllerBlockEntity extends BlockEntity
{
	private double storageCapacity;
	private double storedFluid;
	private BlockPos centerOfMass;
	
	public BalloonControllerBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.BALLOON_CONTROLLER_BLOCK_ENTITY, pos, state);
		this.storageCapacity = 0;
		this.storedFluid = 0;
		this.centerOfMass = new BlockPos(0, 0, 0);
	}
	
	public int getMode()
	{
		return world.getBlockState(getPos()).get(ValveBlock.MODE);
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
	
	public static void serverTick(World world, BlockPos pos, BlockState state, BalloonControllerBlockEntity blockEntity)
	{
		if(world.isClient || blockEntity.getMode() != 1 || blockEntity.getStoredFluid() <= 0)
			return;
		
		for(Direction direction : Direction.values())
		{
			BlockPos offsetPos = pos.offset(direction);
			BlockState adjacentState = world.getBlockState(offsetPos);
			
			if(adjacentState.getBlock() instanceof FluidPipeBlock && ((FluidPipeBlock) adjacentState.getBlock()).getFluidType().getID() == FluidResourceType.HYDROGEN.getID())
			{
				FluidPipeBlockEntity adjacentBlockEntity = ((FluidPipeBlockEntity) world.getBlockEntity(offsetPos));
				double adjecentCapacity = adjacentBlockEntity.getStorageCapacity();
				double adjecentFluid = adjacentBlockEntity.getStoredFluid();
				
				if(adjecentFluid < adjecentCapacity)
				{
					double deltaFluid = Math.min(blockEntity.getStoredFluid(), adjecentCapacity - adjecentFluid);
					blockEntity.changeStoredFluid(-deltaFluid);
					adjacentBlockEntity.changeStoredFluid(deltaFluid);
					blockEntity.markDirty();
					adjacentBlockEntity.markDirty();
				}
			}
		}
    }
}