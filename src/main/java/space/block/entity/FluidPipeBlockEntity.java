package space.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.FluidPipeBlock;
import space.block.StarflightBlocks;
import space.block.ValveBlock;
import space.block.VentBlock;
import space.block.WaterTankBlock;
import space.util.FluidResourceType;

public class FluidPipeBlockEntity extends BlockEntity
{
	private final FluidResourceType fluid;
	private double storedFluid;
	
	public FluidPipeBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.FLUID_PIPE_BLOCK_ENTITY, pos, state);
		this.fluid = ((FluidPipeBlock) state.getBlock()).getFluidType();
	}

	public FluidResourceType getFluidType()
	{
		return fluid;
	}

	public double getStorageCapacity()
	{
		return fluid.getStorageDensity() / 25.0;
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
	public void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putDouble("storedFluid", storedFluid);
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.storedFluid = nbt.getDouble("storedFluid");
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, FluidPipeBlockEntity blockEntity)
	{
		if(world.isClient || blockEntity.getStoredFluid() <= 0)
			return;
		
		for(Direction direction : Direction.values())
		{
			BlockPos offsetPos = pos.offset(direction);
			BlockState adjacentState = world.getBlockState(offsetPos);
			
			if(adjacentState.getBlock() instanceof FluidPipeBlock && ((FluidPipeBlock) adjacentState.getBlock()).getFluidType().getID() == blockEntity.getFluidType().getID())
			{
				FluidPipeBlockEntity adjacentBlockEntity = ((FluidPipeBlockEntity) world.getBlockEntity(offsetPos));
				double adjecentCapacity = adjacentBlockEntity.getStorageCapacity();
				double adjecentFluid = adjacentBlockEntity.getStoredFluid();
				
				if(adjecentFluid < adjecentCapacity && adjecentFluid < blockEntity.getStoredFluid())
				{
					double deltaFluid = (blockEntity.getStoredFluid() - adjecentFluid) / 2;
					blockEntity.changeStoredFluid(-deltaFluid);
					adjacentBlockEntity.changeStoredFluid(deltaFluid);
					blockEntity.markDirty();
					adjacentBlockEntity.markDirty();
				}
			}
			else if(adjacentState.getBlock() instanceof ValveBlock && adjacentState.get(ValveBlock.MODE) == 0)
			{
				ValveBlockEntity adjacentBlockEntity = ((ValveBlockEntity) world.getBlockEntity(offsetPos));
				FluidTankControllerBlockEntity fluidTankBlockEntity = adjacentBlockEntity.getFluidTankController();
				
				if(fluidTankBlockEntity != null && fluidTankBlockEntity.getFluidType().getID() == blockEntity.getFluidType().getID())
				{
					double adjecentCapacity = fluidTankBlockEntity.getStorageCapacity();
					double adjecentFluid = fluidTankBlockEntity.getStoredFluid();
					
					if(adjecentCapacity > 0 && adjecentFluid < adjecentCapacity && adjecentFluid != blockEntity.getStoredFluid())
					{
						double deltaFluid = Math.min(blockEntity.getStoredFluid(), adjecentCapacity - adjecentFluid);
						blockEntity.changeStoredFluid(-deltaFluid);
						fluidTankBlockEntity.changeStoredFluid(deltaFluid);
						blockEntity.markDirty();
						fluidTankBlockEntity.markDirty();
					}
				}
			}
			else if(adjacentState.getBlock() instanceof WaterTankBlock)
			{
				WaterTankBlockEntity adjacentBlockEntity = ((WaterTankBlockEntity) world.getBlockEntity(offsetPos));
				double deltaFluid = 0;
				
				if(direction == Direction.DOWN)
					deltaFluid = Math.min(blockEntity.getStoredFluid(), adjacentBlockEntity.getStorageCapacity() - adjacentBlockEntity.getStoredFluid());
				else if(direction != Direction.UP && adjacentBlockEntity.getStoredFluid() < adjacentBlockEntity.getStorageCapacity() / 2.0)
					deltaFluid = Math.min(blockEntity.getStoredFluid(), (adjacentBlockEntity.getStorageCapacity() / 2.0) - adjacentBlockEntity.getStoredFluid());
				
				if(deltaFluid > 0)
				{
					blockEntity.changeStoredFluid(-deltaFluid);
					adjacentBlockEntity.changeStoredFluid(deltaFluid);
					adjacentBlockEntity.markDirty();
					world.updateListeners(offsetPos, adjacentState, adjacentState, Block.NOTIFY_LISTENERS);
				}
			}
			else if(blockEntity.getStoredFluid() > 0.05 && adjacentState.getBlock() == StarflightBlocks.VENT && world.isReceivingRedstonePower(offsetPos))
			{
				blockEntity.changeStoredFluid(-blockEntity.getStoredFluid());
				blockEntity.markDirty();
				VentBlock.particleEffect(world, offsetPos);
			}
		}
    }
}