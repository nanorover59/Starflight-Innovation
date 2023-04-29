package space.energy;

import java.util.ArrayList;

import net.darkhax.ess.DataCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class EnergyNode
{
	private BlockPos position;
	private RegistryKey<World> dimension;
	private boolean isProducer;
	private boolean isConsumer;
	private double powerOutput;
	private double powerSourceLoad;
	private ArrayList<EnergyNode> inputs = new ArrayList<EnergyNode>();
	private ArrayList<EnergyNode> outputs = new ArrayList<EnergyNode>();
	private ArrayList<BlockPos> breakers = new ArrayList<BlockPos>();
	
	public EnergyNode(BlockPos position_, RegistryKey<World> dimension_, boolean isProducer_, boolean isConsumer_)
	{
		position = position_;
		dimension = dimension_;
		isProducer = isProducer_;
		isConsumer = isConsumer_;
	}
	
	@Override
    public boolean equals(Object object)
	{
		if(object == null || object.getClass() != this.getClass())
			return false;
		
		EnergyNode other = (EnergyNode) object;
		
		if(other.getPosition().equals(getPosition()) && other.getDimension().equals(getDimension()))
			return true;
		
		return false;	
	}
	
	public BlockPos getPosition()
	{
		return position;
	}
	
	public RegistryKey<World> getDimension()
	{
		return dimension;
	}
	
	public boolean isProducer()
	{
		return isProducer;
	}
	
	public boolean isConsumer()
	{
		return isConsumer;
	}
	
	public String getName()
	{
		return position.getX() + "_" + position.getY() + "_" + position.getZ() + "_" + dimension.getValue().getPath();
	}

	public double getPowerSourceLoad()
	{
		return powerSourceLoad;
	}

	public void setPowerSourceLoad(double powerSourceLoad)
	{
		this.powerSourceLoad = powerSourceLoad;
	}
	
	public void setPowerOutput(double powerOutput)
	{
		this.powerOutput = powerOutput;
	}
	
	public double getPowerOutput()
	{
		return powerOutput;
	}
	
	public boolean isPowerSourceOverloaded()
	{
		return powerOutput < powerSourceLoad;
	}

	public boolean hasSufficientPower()
	{
		for(EnergyNode producer : inputs)
		{
			if(producer.getPowerOutput() > 0 && !producer.isPowerSourceOverloaded())
				return true;
		}
		
		return false;
	}

	public ArrayList<EnergyNode> getInputs()
	{
		return inputs;
	}

	public ArrayList<EnergyNode> getOutputs()
	{
		return outputs;
	}
	
	public ArrayList<BlockPos> getBreakers()
	{
		return breakers;
	}
	
	public void distributePowerLoad(double powerToDistribute)
	{
		double totalPowerSupply = 0.0D;
		
		for(EnergyNode n : inputs)
			totalPowerSupply += n.getPowerOutput();
		
		if(totalPowerSupply <= 0)
			return;
		
		for(EnergyNode n : inputs)
		{
			double powerFraction = n.getPowerOutput() / totalPowerSupply;
			n.powerSourceLoad += powerToDistribute * powerFraction;
		}
	}
	
	public DataCompound saveData(DataCompound data)
	{
		DataCompound energyData = new DataCompound();
		energyData.setValue("x", position.getX());
		energyData.setValue("y", position.getY());
		energyData.setValue("z", position.getZ());
		energyData.setValue("dimension", dimension.getValue().toString());
		energyData.setValue("isProducer", isProducer);
		energyData.setValue("isConsumer", isConsumer);
		int inputCount = inputs.size();
		int outputCount = outputs.size();
		int breakerCount = breakers.size();
		int[] ix = new int[inputCount];
		int[] iy = new int[inputCount];
		int[] iz = new int[inputCount];
		int[] ox = new int[outputCount];
		int[] oy = new int[outputCount];
		int[] oz = new int[outputCount];
		int[] bx = new int[breakerCount];
		int[] by = new int[breakerCount];
		int[] bz = new int[breakerCount];
		
		for(int i = 0; i < inputs.size(); i++)
		{
			ix[i] = inputs.get(i).getPosition().getX();
			iy[i] = inputs.get(i).getPosition().getY();
			iz[i] = inputs.get(i).getPosition().getZ();
		}
		
		for(int i = 0; i < outputs.size(); i++)
		{
			ox[i] = outputs.get(i).getPosition().getX();
			oy[i] = outputs.get(i).getPosition().getY();
			oz[i] = outputs.get(i).getPosition().getZ();
		}
		
		for(int i = 0; i < breakers.size(); i++)
		{
			bx[i] = breakers.get(i).getX();
			by[i] = breakers.get(i).getY();
			bz[i] = breakers.get(i).getZ();
		}
		
		energyData.setValue("inputCount", inputCount);
		energyData.setValue("outputCount", outputCount);
		energyData.setValue("breakerCount", breakerCount);
		energyData.setValue("ix", ix);
		energyData.setValue("iy", iy);
		energyData.setValue("iz", iz);
		energyData.setValue("ox", ox);
		energyData.setValue("oy", oy);
		energyData.setValue("oz", oz);
		energyData.setValue("bx", bx);
		energyData.setValue("by", by);
		energyData.setValue("bz", bz);
		data.setValue(getName(), energyData);
		return data;
	}
	
	public static EnergyNode loadData(DataCompound data)
	{
		EnergyNode energyNode = new EnergyNode(new BlockPos(data.getInt("x"), data.getInt("y"), data.getInt("z")), RegistryKey.of(Registry.WORLD_KEY, new Identifier(data.getString("dimension"))), data.getBoolean("isProducer"), data.getBoolean("isConsumer"));
		int inputCount = data.getInt("inputCount");
		int outputCount = data.getInt("outputCount");
		int breakerCount = data.getInt("breakerCount");
		int[] ix = data.getIntArray("ix");
		int[] iy = data.getIntArray("iy");
		int[] iz = data.getIntArray("iz");
		int[] ox = data.getIntArray("ox");
		int[] oy = data.getIntArray("oy");
		int[] oz = data.getIntArray("oz");
		int[] bx = data.getIntArray("bx");
		int[] by = data.getIntArray("by");
		int[] bz = data.getIntArray("bz");
		
		for(int i = 0; i < inputCount; i++)
			energyNode.getInputs().add(new EnergyNode(new BlockPos(ix[i], iy[i], iz[i]), RegistryKey.of(Registry.WORLD_KEY, new Identifier(data.getString("dimension"))), false, false));
		
		for(int i = 0; i < outputCount; i++)
			energyNode.getOutputs().add(new EnergyNode(new BlockPos(ox[i], oy[i], oz[i]), RegistryKey.of(Registry.WORLD_KEY, new Identifier(data.getString("dimension"))), false, false));
		
		for(int i = 0; i < breakerCount; i++)
			energyNode.getBreakers().add(new BlockPos(bx[i], by[i], bz[i]));
		
		return energyNode;
	}
}