package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import space.block.AtmosphereGeneratorBlock;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;

public class AtmosphereGeneratorBlockEntity extends BlockEntity implements EnergyBlockEntity
{
	private double energy;
	
	public AtmosphereGeneratorBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.ATMOSPHERE_GENERATOR_BLOCK_ENTITY, blockPos, blockState);
	}
	
	@Override
	public double getOutput()
	{
		return 0.0;
	}
	
	@Override
	public double getInput()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getInput() / world.getTickManager().getTickRate();
	}
	
	@Override
	public double getEnergyStored()
	{
		return energy;
	}

	@Override
	public double getEnergyCapacity()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getEnergyCapacity();
	}

	@Override
	public double changeEnergy(double amount)
	{
		double newEnergy = energy + amount;
		energy = MathHelper.clamp(newEnergy, 0, getEnergyCapacity());
		return amount - (newEnergy - energy);
	}

	@Override
	public ArrayList<BlockPos> getOutputs()
	{
		return null;
	}

	@Override
	public void addOutput(BlockPos output)
	{
	}

	@Override
	public void clearOutputs()
	{
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putDouble("energy", this.energy);
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.energy = nbt.getDouble("energy");
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
			
			blockEntity.changeEnergy(-0.125);
		}
	}
}