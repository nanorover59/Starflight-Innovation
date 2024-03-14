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

public class ValveBlockEntity extends BlockEntity
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
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.put("fluidTankController", NbtHelper.fromBlockPos(fluidTankController));
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.fluidTankController = NbtHelper.toBlockPos(nbt.getCompound("fluidTankController"));
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, ValveBlockEntity blockEntity)
	{
		if(world.isClient || blockEntity.getMode() != 1 || blockEntity.getFluidTankController() == null || blockEntity.getFluidTankController().getStoredFluid() <= 0)
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
