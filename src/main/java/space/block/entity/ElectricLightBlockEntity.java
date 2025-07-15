package space.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.LightColumnBlock;
import space.block.StarflightBlocks;

public class ElectricLightBlockEntity extends BlockEntity implements EnergyBlockEntity
{
	private long energy;
	
	public ElectricLightBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.ELECTRIC_LIGHT_BLOCK_ENTITY, pos, state);
	}
	
	@Override
	public long getEnergyCapacity()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getEnergyCapacity();
	}
	
	@Override
	public long getEnergy()
	{
		return energy;
	}

	@Override
	public void setEnergy(long energy)
	{
		this.energy = energy;
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putLong("energy", this.energy);
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.energy = nbt.getLong("energy");
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, ElectricLightBlockEntity blockEntity)
	{
		blockEntity.removeEnergy(((EnergyBlock) state.getBlock()).getInput(), true);
		
		if(state.get(LightColumnBlock.LIT) != blockEntity.energy > 0)
		{
			state = (BlockState) state.with(LightColumnBlock.LIT, blockEntity.energy > 0);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			markDirty(world, pos, state);
		}
	}
}