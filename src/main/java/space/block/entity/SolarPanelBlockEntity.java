package space.block.entity;

import java.util.ArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

public class SolarPanelBlockEntity extends BlockEntity implements EnergyBlockEntity
{
	private ArrayList<BlockPos> outputs = new ArrayList<BlockPos>();
	private double energy;
	
	public SolarPanelBlockEntity(BlockPos blockPos, BlockState blockState)
	{
        super(StarflightBlocks.SOLAR_PANEL_BLOCK_ENTITY, blockPos, blockState);
    }

	@Override
	public double getEnergyStored()
	{
		return energy;
	}

	@Override
	public double getEnergyCapacity()
	{
		return 10;
	}
	
	@Override
	public double getOutput()
	{
		return (((EnergyBlock) getCachedState().getBlock()).getOutput() / world.getTickManager().getTickRate());
	}
	
	@Override
	public double getInput()
	{
		return 0.0;
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
		return outputs;
	}

	@Override
	public void addOutput(BlockPos output)
	{
		outputs.add(output);
	}

	@Override
	public void clearOutputs()
	{
		outputs.clear();
	}
	
	@Override
	public void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		EnergyBlockEntity.outputsToNBT(outputs, nbt);
		nbt.putDouble("energy", this.energy);
	}
		
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.outputs = EnergyBlockEntity.outputsFromNBT(nbt);
		this.energy = nbt.getDouble("energy");
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, SolarPanelBlockEntity blockEntity)
	{
		if(!world.getDimension().hasSkyLight() || !(blockEntity instanceof SolarPanelBlockEntity))
			return;
		
		double solarMultiplier = 1.0;
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);

		if(data != null)
			solarMultiplier = data.getPlanet().getSolarMultiplier() * (1.0f - ((data.getPlanet().hasWeather() && !data.isOrbit()) ? world.getRainGradient(1.0f) : 0.0f));
		else
			solarMultiplier = 1.0f - world.getRainGradient(1.0f);

		// Calculate the output of this solar panel at Earth's distance to to the sun taking the sky angle into account.
		float f = world.getSkyAngle(1.0f);
		float highLimit1 = 0.05f;
		float highLimit2 = 1.0f - highLimit1;
		float lowLimit1 = 0.25f;
		float lowLimit2 = 1.0f - lowLimit1;
		double angleMultiplier;

		if(f < highLimit1 || f > highLimit2)
			angleMultiplier = 1.0;
		else if(f < lowLimit1)
			angleMultiplier = Math.pow(1.0 - ((f - highLimit1) / (lowLimit1 - highLimit1)), 1.0 / 3.0);
		else if(f > lowLimit2)
			angleMultiplier = Math.pow(1.0 - ((f - highLimit2) / (lowLimit2 - highLimit2)), 1.0 / 3.0);
		else
			return;
		
		// The final energy output per tick.9
		double output = blockEntity.getOutput() * solarMultiplier * angleMultiplier;
		blockEntity.changeEnergy(output);
		EnergyBlockEntity.transferEnergy(blockEntity, output);
	}
}