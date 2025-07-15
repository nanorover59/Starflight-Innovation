package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.AtmosphereGeneratorBlock;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.util.FluidResourceType;

public class AtmosphereGeneratorBlockEntity extends BlockEntity implements EnergyBlockEntity
{
	public ArrayList<BlockPos> oxygenSources = new ArrayList<BlockPos>();
	private long energy;
	
	public AtmosphereGeneratorBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.ATMOSPHERE_GENERATOR_BLOCK_ENTITY, blockPos, blockState);
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
	
	/**
	 * Search storage blocks for an amount of oxygen then use it and return true if it is found.
	 */
	public boolean requestSupply(World world, BlockPos blockPos, long amount)
	{
		if(PumpBlockEntity.pullFluid(world, oxygenSources, amount, false, FluidResourceType.OXYGEN) == amount)
		{
			PumpBlockEntity.pullFluid(world, oxygenSources, amount, true, FluidResourceType.OXYGEN);
			return true;
		}
		else
			return false;
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putLong("energy", this.energy);
		
		NbtList oxygenSourcesListNBT = new NbtList();
		
		for(BlockPos source : oxygenSources)
			oxygenSourcesListNBT.add(NbtHelper.fromBlockPos(source));

		nbt.put("oxygenSources", oxygenSourcesListNBT);
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.energy = nbt.getLong("energy");
		NbtList oxygenSourcesListNBT = nbt.getList("oxygenSources", NbtList.COMPOUND_TYPE);
		
		for(int i = 0; i < oxygenSourcesListNBT.size(); i++)
		{
			int[] array = oxygenSourcesListNBT.getIntArray(i);
			oxygenSources.add(new BlockPos(array[0], array[1], array[2]));
		}
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, AtmosphereGeneratorBlockEntity blockEntity)
	{
		Direction direction = state.get(AtmosphereGeneratorBlock.FACING);
		BlockPos frontPos = pos.offset(direction);
		BlockState frontState = world.getBlockState(frontPos);
		
		if(frontState.getBlock() == StarflightBlocks.HABITABLE_AIR)
		{
			if(blockEntity.energy == 0 && frontState.getBlock() == StarflightBlocks.HABITABLE_AIR)
			{
				MutableText text = Text.translatable("block.space.atmosphere_generator.error_power");
				
				for(PlayerEntity player : world.getPlayers())
				{
		            if(player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 1024.0)
		            	player.sendMessage(text, true);
		        }
			}
			
			blockEntity.removeEnergy(((EnergyBlock) blockEntity.getCachedState().getBlock()).getInput(), true);
		}
	}
}