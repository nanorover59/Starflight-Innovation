package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.OxygenPipeBlock;
import space.block.StarflightBlocks;

public class OxygenOutletValveBlockEntity extends FluidTankInterfaceBlockEntity
{
	public OxygenOutletValveBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.OXYGEN_OUTLET_VALVE_BLOCK_ENTITY, pos, state);
	}
	
	public String getFluidName()
	{
		return "oxygen";
	}

	public double getStorageCapacity()
	{
		return StarflightBlocks.OXYGEN_TANK_CAPACITY;
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, FluidTankInterfaceBlockEntity blockEntity)
	{
		if(world.isClient || blockEntity.getFluidTankController() == null || blockEntity.getFluidTankController().getStoredFluid() <= 0)
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
				
				if(adjecentFluid < adjecentCapacity)
				{
					double deltaFluid = Math.min(blockEntity.getFluidTankController().getStoredFluid(), adjecentCapacity - adjecentFluid);
					blockEntity.getFluidTankController().changeStoredFluid(-deltaFluid);
					adjacentBlockEntity.changeStoredFluid(deltaFluid);
					blockEntity.markDirty();
					adjacentBlockEntity.markDirty();
					
					/*ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
					double remaining = ElectrolyzerBlockEntity.recursiveSpread(world, offsetPos, checkList, StarflightBlocks.OXYGEN_PIPE_CAPACITY, "oxygen", 2048);
					blockEntity.getFluidTankController().changeStoredFluid(remaining - StarflightBlocks.OXYGEN_PIPE_CAPACITY);*/
				}
			}
		}
    }
}