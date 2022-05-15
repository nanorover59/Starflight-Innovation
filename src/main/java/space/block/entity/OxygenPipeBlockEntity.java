package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.OxygenInletValveBlock;
import space.block.OxygenPipeBlock;
import space.block.StarflightBlocks;
import space.block.VentBlock;

public class OxygenPipeBlockEntity extends FluidContainerBlockEntity
{
	public OxygenPipeBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.OXYGEN_PIPE_BLOCK_ENTITY, pos, state);
	}
	
	public String getFluidName()
	{
		return "oxygen";
	}

	public double getStorageCapacity()
	{
		return StarflightBlocks.OXYGEN_PIPE_CAPACITY;
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, FluidContainerBlockEntity blockEntity)
	{
		if(world.isClient || blockEntity.getStoredFluid() <= 0)
			return;
		
		for(Direction direction : Direction.values())
		{
			BlockPos offsetPos = pos.offset(direction);
			BlockState adjacentState = world.getBlockState(offsetPos);
			
			if(adjacentState.getBlock() instanceof OxygenPipeBlock)
			{
				FluidContainerBlockEntity adjacentBlockEntity = ((FluidContainerBlockEntity) world.getBlockEntity(offsetPos));
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
			else if(adjacentState.getBlock() instanceof OxygenInletValveBlock)
			{
				FluidTankInterfaceBlockEntity adjacentBlockEntity = ((FluidTankInterfaceBlockEntity) world.getBlockEntity(offsetPos));
				FluidTankControllerBlockEntity fluidTankBlockEntity = adjacentBlockEntity.getFluidTankController();
				
				if(fluidTankBlockEntity != null)
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
			else if(adjacentState.getBlock() == StarflightBlocks.VENT && world.isReceivingRedstonePower(offsetPos))
			{
				blockEntity.changeStoredFluid(-blockEntity.getStoredFluid());
				blockEntity.markDirty();
				
				if(blockEntity.getStoredFluid() > 1)
					VentBlock.particleEffect(world, offsetPos);
			}
		}
		
		if(blockEntity.getStoredFluid() < 0.01)
			blockEntity.changeStoredFluid(-blockEntity.getStoredFluid());
    }
}
