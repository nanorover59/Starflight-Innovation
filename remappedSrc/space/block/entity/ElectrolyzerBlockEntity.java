package space.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.ElectrolyzerBlock;
import space.block.StarflightBlocks;

public class ElectrolyzerBlockEntity extends BlockEntity implements PoweredBlockEntity
{
	private boolean hasWater;
	private int powerState;
	
	public ElectrolyzerBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.ELECTROLYZER_BLOCK_ENTITY, pos, state);
		hasWater = false;
		powerState = 0;
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, ElectrolyzerBlockEntity blockEntity)
	{
		if(!blockEntity.getWater())
			return;
		
		if(state != (BlockState) state.with(ElectrolyzerBlock.LIT, blockEntity.getPowerState() > 0))
		{
			state = (BlockState) state.with(ElectrolyzerBlock.LIT, blockEntity.getPowerState() > 0);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
		}
		
		if(blockEntity.getPowerState() < 1)
			return;
		
		BlockPos leftSide = pos.offset(state.get(HorizontalFacingBlock.FACING).rotateYClockwise());
		BlockPos rightSide = pos.offset(state.get(HorizontalFacingBlock.FACING).rotateYCounterclockwise());
		
		if(world.getBlockState(leftSide).getBlock() == StarflightBlocks.OXYGEN_PIPE)
		{
			FluidContainerBlockEntity adjacentBlockEntity = ((FluidContainerBlockEntity) world.getBlockEntity(leftSide));
			double adjecentCapacity = adjacentBlockEntity.getStorageCapacity();
			double adjecentFluid = adjacentBlockEntity.getStoredFluid();
			
			if(adjecentFluid < adjecentCapacity)
				adjacentBlockEntity.changeStoredFluid(adjecentCapacity - adjecentFluid);
		}
		
		if(world.getBlockState(rightSide).getBlock() == StarflightBlocks.HYDROGEN_PIPE)
		{
			FluidContainerBlockEntity adjacentBlockEntity = ((FluidContainerBlockEntity) world.getBlockEntity(rightSide));
			double adjecentCapacity = adjacentBlockEntity.getStorageCapacity();
			double adjecentFluid = adjacentBlockEntity.getStoredFluid();
			
			if(adjecentFluid < adjecentCapacity)
				adjacentBlockEntity.changeStoredFluid(adjecentCapacity - adjecentFluid);
		}
    }
	
	public void setWater(boolean b)
	{
		hasWater = b;
	}

	public boolean getWater()
	{
		return hasWater;
	}
	
	@Override
	public void setPowerState(int i)
	{
		powerState = i;
	}

	@Override
	public int getPowerState()
	{
		return powerState;
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		hasWater = nbt.getBoolean("hasWater");
		super.readNbt(nbt);
	}

	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		nbt.putBoolean("hasWater", hasWater);
		super.writeNbt(nbt);
	}
}
