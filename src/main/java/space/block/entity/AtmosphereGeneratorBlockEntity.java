package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import space.block.AtmosphereGeneratorBlock;
import space.block.HabitableAirBlock;
import space.block.StarflightBlocks;

public class AtmosphereGeneratorBlockEntity extends BlockEntity implements PoweredBlockEntity
{
	private int powerState;
	
	public AtmosphereGeneratorBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.ATMOSPHERE_GENERATOR_BLOCK_ENTITY, blockPos, blockState);
	}
	
	@Override
	public void setPowerState(int i)
	{
		powerState = i;
		
		if(i == 0)
		{
			BlockState state = world.getBlockState(pos);
			Direction direction = state.get(AtmosphereGeneratorBlock.FACING);
			BlockPos frontPos = pos.offset(direction);
			BlockState frontState = world.getBlockState(frontPos);
			
			if(frontState == StarflightBlocks.HABITABLE_AIR.getDefaultState())
				HabitableAirBlock.setUnstable(world, frontPos, frontState);
		}
	}
	
	@Override
	public int getPowerState()
	{
		return powerState;
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
	}

	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
	}
}