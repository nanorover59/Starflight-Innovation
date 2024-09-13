package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.HydrogenPipeBlock;
import space.block.StarflightBlocks;

public class HydrogenOutletValveBlockEntity extends FluidTankInterfaceBlockEntity
{
	public HydrogenOutletValveBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.HYDROGEN_OUTLET_VALVE_BLOCK_ENTITY, pos, state);
	}
	
	public String getFluidName()
	{
		return "hydrogen";
	}
	
	public double getStorageCapacity()
	{
		return StarflightBlocks.HYDROGEN_TANK_CAPACITY;
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, FluidTankInterfaceBlockEntity blockEntity)
	{
		if(world.isClient || blockEntity.getFluidTankController() == null || blockEntity.getFluidTankController().getStoredFluid() <= 0)
			return;
		
		for(Direction direction : Direction.values())
		{
			BlockPos offsetPos = pos.offset(direction);
			BlockState adjacentState = world.getBlockState(offsetPos);
			
			if(adjacentState.getBlock() instanceof HydrogenPipeBlock)
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
					double remaining = ElectrolyzerBlockEntity.recursiveSpread(world, offsetPos, checkList, StarflightBlocks.HYDROGEN_PIPE_CAPACITY, "hydrogen", 2048);
					blockEntity.getFluidTankController().changeStoredFluid(remaining - StarflightBlocks.HYDROGEN_PIPE_CAPACITY);*/
				}
			}
		}
    }
}