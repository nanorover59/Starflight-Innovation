package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import space.block.StarflightBlocks;

public class SolarHubBlockEntity extends BlockEntity
{
	private int panelCount;
	private double powerFraction;
	
	public SolarHubBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.SOLAR_HUB_BLOCK_ENTITY, pos, state);
	}
	
	public void setPanelCount(int i)
	{
		panelCount = i;
	}
	
	public void setPowerFraction(double d)
	{
		powerFraction = d;
	}
	
	public int getPanelCount()
	{
		return panelCount;
	}
	
	public double getPowerFraction()
	{
		return powerFraction;
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.panelCount = nbt.getInt("panelCount");
		this.powerFraction = nbt.getDouble("powerFraction");
	}

	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putInt("panelCount", this.panelCount);
		nbt.putDouble("powerFraction", this.powerFraction);
	}
}