package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface EnergyBlockEntity
{
	double getEnergyStored();
	
	double getEnergyCapacity();
	
	double getOutput();
	
	double getInput();
	
	/** 
	 * Change the stored energy by some positive or negative amount and return the amount successfully transferred.
	 */
	double changeEnergy(double amount);
	
	ArrayList<BlockPos> getOutputs();
	
	void addOutput(BlockPos output);
	
	void clearOutputs();
	
	/**
	 * Attempt to push the given amount of energy to any connected outputs.
	 */
	public static void transferEnergy(BlockEntity blockEntity, double total)
	{
		if(!(blockEntity instanceof EnergyBlockEntity))
			return;
		
		World world = blockEntity.getWorld();
		ArrayList<BlockPos> outputs = ((EnergyBlockEntity) blockEntity).getOutputs();
		total = Math.min(total, ((EnergyBlockEntity) blockEntity).getEnergyStored());
		int count = outputs.size();
		double transferred = 0;
		
		for(BlockPos pos : outputs)
		{
			BlockEntity otherBlockEntity = world.getBlockEntity(pos);
			
			if(otherBlockEntity != null && otherBlockEntity instanceof EnergyBlockEntity)
			{
				double amount = total / count;
				transferred += ((EnergyBlockEntity) otherBlockEntity).changeEnergy(amount);
			}
		}
		
		((EnergyBlockEntity) blockEntity).changeEnergy(-transferred);
	}
	
	public static void outputsToNBT(ArrayList<BlockPos> outputs, NbtCompound nbt)
	{
		int[] x = new int[outputs.size()];
		int[] y = new int[outputs.size()];
		int[] z = new int[outputs.size()];
		int i = 0;
		
		for(BlockPos output : outputs)
		{
			x[i] = output.getX();
			y[i] = output.getY();
			z[i] = output.getZ();
			i++;
		}
		
		nbt.putIntArray("xOut", x);
		nbt.putIntArray("yOut", y);
		nbt.putIntArray("zOut", z);
	}
	
	public static ArrayList<BlockPos> outputsFromNBT(NbtCompound nbt)
	{
		ArrayList<BlockPos> outputs = new ArrayList<BlockPos>();
		int[] x = nbt.getIntArray("xOut");
		int[] y = nbt.getIntArray("yOut");
		int[] z = nbt.getIntArray("zOut");
		
		if(x.length == 0)
			return outputs;
		
		for(int i = 0; i < x.length; i++)
			outputs.add(new BlockPos(x[i], y[i], z[i]));
		
		return outputs;
	}
}