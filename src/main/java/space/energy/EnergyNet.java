package space.energy;

import java.util.ArrayList;

import net.darkhax.ess.DataCompound;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import space.block.BatteryBlock;
import space.block.BreakerSwitchBlock;
import space.block.EnergyBlock;
import space.block.EnergyCableBlock;
import space.block.SolarPanelBlock;
import space.block.entity.BatteryBlockEntity;
import space.block.entity.PoweredBlockEntity;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

public class EnergyNet
{
	private static final Direction[] DIRECTIONS = Direction.values();
	private static ArrayList<EnergyNode> energyProducers = new ArrayList<EnergyNode>();
	private static ArrayList<EnergyNode> energyConsumers = new ArrayList<EnergyNode>();
	
	public static void addProducer(World world, BlockPos position, double storedEnergy)
	{
		if(world.isClient())
			return;
		
		EnergyNode energyNode = new EnergyNode(position, world.getRegistryKey(), true, false);
		
		if(!energyProducers.contains(energyNode))
		{
			energyProducers.add(energyNode);
			connectProducer(world, energyNode);
		}
	}
	
	public static void addConsumer(World world, BlockPos position, double storedEnergy)
	{
		if(world.isClient())
			return;
		
		EnergyNode energyNode = new EnergyNode(position, world.getRegistryKey(), false, true);
		
		if(!energyConsumers.contains(energyNode))
		{
			energyConsumers.add(energyNode);
			connectConsumer(world, energyNode);
		}
	}
	
	public static void addDual(World world, BlockPos position, double storedEnergy)
	{
		if(world.isClient())
			return;
		
		EnergyNode energyNode = new EnergyNode(position, world.getRegistryKey(), true, true);
		
		if(!energyProducers.contains(energyNode))
		{
			energyProducers.add(energyNode);
			connectProducer(world, energyNode);
		}
		
		if(!energyConsumers.contains(energyNode))
		{
			energyConsumers.add(energyNode);
			connectConsumer(world, energyNode);
		}
	}
	
	public static void addProducer(World world, BlockPos position)
	{
		addProducer(world, position, 0.0);
	}
	
	public static void addConsumer(World world, BlockPos position)
	{
		addConsumer(world, position, 0.0);
	}
	
	public static void addDual(World world, BlockPos position)
	{
		addDual(world, position, 0.0);
	}
	
	public static void removeProducer(World world, BlockPos position)
	{
		EnergyNode producer = getProducer(position, world.getRegistryKey());
		
		for(EnergyNode consumer : energyConsumers)
			consumer.getInputs().remove(producer);
		
		energyProducers.remove(producer);
	}
	
	public static void removeConsumer(World world, BlockPos position)
	{
		EnergyNode consumer = getConsumer(position, world.getRegistryKey());
		
		for(EnergyNode producer : energyProducers)
			producer.getInputs().remove(consumer);
		
		energyConsumers.remove(consumer);
	}
	
	public static ArrayList<EnergyNode> getEnergyProducers()
	{
		return energyProducers;
	}
	
	public static ArrayList<EnergyNode> getEnergyConsumers()
	{
		return energyConsumers;
	}
	
	public static EnergyNode getProducer(BlockPos position, RegistryKey<World> dimension)
	{
		for(EnergyNode energyNode : energyProducers)
		{
			if(energyNode.getPosition().equals(position) && energyNode.getDimension().getValue().equals(dimension.getValue()))
				return energyNode;
		}
		
		return null;
	}
	
	public static EnergyNode getConsumer(BlockPos position, RegistryKey<World> dimension)
	{
		for(EnergyNode energyNode : energyConsumers)
		{
			if(energyNode.getPosition().equals(position) && energyNode.getDimension().getValue().equals(dimension.getValue()))
				return energyNode;
		}
		
		return null;
	}
	
	public static void doEnergyFlow(MinecraftServer server)
	{
		for(EnergyNode energyNode : energyProducers)
			energyNode.setPowerSourceLoad(0.0D);
		
		for(ServerWorld world : server.getWorlds())
		{
			ArrayList<BlockPos> removalListProducers = new ArrayList<BlockPos>();
			ArrayList<BlockPos> removalListConsumers = new ArrayList<BlockPos>();
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
			double solarMultiplier = 1.0;
			
			if(data != null)
				solarMultiplier = data.getPlanet().getSolarMultiplier() * (1.0f - ((data.getPlanet().hasWeather() && !data.isOrbit()) ? world.getRainGradient(1.0f) : 0.0f));
			else
				solarMultiplier = 1.0f - world.getRainGradient(1.0f);
			
			// Update the power output of all energy producers.
			for(EnergyNode energyNode : energyProducers)
			{
				if(energyNode.getDimension() != world.getRegistryKey())
					continue;
				
				BlockState state = world.getBlockState(energyNode.getPosition());
				
				if(state.getBlock() instanceof EnergyBlock)
				{
					if(state.getBlock() instanceof BatteryBlock)
					{
						BatteryBlockEntity batteryBlockEntity = (BatteryBlockEntity) world.getBlockEntity(energyNode.getPosition());
						
						if(!batteryBlockEntity.hasAnyCharge())
							energyNode.setPowerOutput(0.0D);
						else
							energyNode.setPowerOutput(BatteryBlock.POWER_OUTPUT);
					}
					else if(state.getBlock() instanceof SolarPanelBlock)
						energyNode.setPowerOutput(((EnergyBlock) state.getBlock()).getPowerOutput(world, energyNode.getPosition(), state) * solarMultiplier);
					else
						energyNode.setPowerOutput(((EnergyBlock) state.getBlock()).getPowerOutput(world, energyNode.getPosition(), state));
				}
				else
					removalListProducers.add(energyNode.getPosition());
			}
			
			// Distribute the power use of each energy consumer across any connected energy producers.
			for(EnergyNode energyNode : energyConsumers)
			{
				if(energyNode.getDimension() != world.getRegistryKey())
					continue;
				
				BlockState state = world.getBlockState(energyNode.getPosition());
				
				if(state.getBlock() instanceof EnergyBlock)
					energyNode.distributePowerLoad(((EnergyBlock) state.getBlock()).getPowerDraw(world, energyNode.getPosition(), state));
				else
					removalListConsumers.add(energyNode.getPosition());
			}
			
			// Determine whether or not each energy consumer has enough power to function.
			for(EnergyNode energyNode : energyConsumers)
			{
				if(energyNode.getDimension() != world.getRegistryKey())
					continue;
				
				BlockState state = world.getBlockState(energyNode.getPosition());	
				
				if(state.getBlock() instanceof EnergyBlock)
				{
					// Trip breaker switch blocks.
					boolean overloaded = false;
					
					for(EnergyNode producer : energyNode.getInputs())
					{
						if(producer.isPowerSourceOverloaded())
						{
							overloaded = true;
							break;
						}
					}
					
					if(overloaded && !energyNode.getBreakers().isEmpty())
					{
						for(BlockPos breakerPos : energyNode.getBreakers())
						{
							BlockState breakerState = world.getBlockState(breakerPos);
							
							if(breakerState.getBlock() instanceof BreakerSwitchBlock)
							{
								world.setBlockState(breakerPos, breakerState.with(BreakerSwitchBlock.LIT, false));
								ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
								EnergyNet.updateEnergyNodes(world, breakerPos, checkList);
							}
						}
					}
					
					// Charge battery blocks if enough power is available.
					if(state.getBlock() instanceof BatteryBlock && !energyNode.getInputs().isEmpty() && energyNode.hasSufficientPower())
					{
						BatteryBlockEntity batteryBlockEntity = (BatteryBlockEntity) world.getBlockEntity(energyNode.getPosition());
						batteryBlockEntity.charge(((BatteryBlock) state.getBlock()).getPowerDraw(world, energyNode.getPosition(), state) / 20.0);
					}
					
					BlockEntity blockEntity = world.getBlockEntity(energyNode.getPosition());
					
					if(blockEntity instanceof PoweredBlockEntity)
					{
						if(!energyNode.getInputs().isEmpty() && energyNode.hasSufficientPower())
							((PoweredBlockEntity) blockEntity).setPowerState(1);
						else
							((PoweredBlockEntity) blockEntity).setPowerState(0);
					}
				}
			}
			
			// Deal damage to energy producers that are overloaded and remove stored energy from battery blocks that have a power load.
			for(EnergyNode energyNode : energyProducers)
			{
				if(energyNode.getDimension() != world.getRegistryKey())
					continue;
				
				BlockState state = world.getBlockState(energyNode.getPosition());
				
				if(state.getBlock() instanceof BatteryBlock)
				{
					BatteryBlockEntity batteryBlockEntity = (BatteryBlockEntity) world.getBlockEntity(energyNode.getPosition());
					batteryBlockEntity.discharge(energyNode.getPowerSourceLoad() / 20.0);
					
					// Update the "lit" state of battery blocks.
					if(state != (BlockState) state.with(BatteryBlock.LIT, batteryBlockEntity.hasAnyCharge()))
					{
						state = (BlockState) state.with(BatteryBlock.LIT, batteryBlockEntity.hasAnyCharge());
						world.setBlockState(energyNode.getPosition(), state, Block.NOTIFY_ALL);
					}
				}
			}
			
			for(BlockPos position : removalListProducers)
				removeProducer(world, position);
			
			for(BlockPos position : removalListConsumers)
				removeConsumer(world, position);
		}
	}
	
	public static void connectProducer(World world, EnergyNode energyNode)
	{
		if(energyNode.getDimension() != world.getRegistryKey())
			return;
		
		ArrayList<EnergyNode> found = new ArrayList<EnergyNode>();
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		BlockPos position = energyNode.getPosition();
		BlockState blockState = world.getBlockState(position);
		energyNode.getOutputs().clear();
		checkList.add(position);
		
		if(!(blockState.getBlock() instanceof EnergyBlock))
			return;
		
		EnergyBlock energyBlock = (EnergyBlock) blockState.getBlock();
		energyNode.getOutputs().clear();
		
		for(Direction direction : Direction.values())
		{
			if(energyBlock.isSideOutput(world, position, blockState, direction))
				checkDirectionProducer(world, position, direction, checkList, found, energyNode);
		}
		
		for(EnergyNode consumer : found)
		{
			if(!energyNode.getOutputs().contains(consumer))
				energyNode.getOutputs().add(consumer);
			
			if(!consumer.getInputs().contains(energyNode))
				consumer.getInputs().add(energyNode);
		}
	}
	
	public static void checkDirectionProducer(World world, BlockPos position, Direction direction, ArrayList<BlockPos> checkList, ArrayList<EnergyNode> found, EnergyNode producer)
	{
		BlockPos adjacentPosition = position.offset(direction);
		
		if(checkList.contains(adjacentPosition))
			return;
		
		checkList.add(adjacentPosition);
		BlockState adjacentBlockState = world.getBlockState(adjacentPosition);
		
		if(adjacentBlockState.getBlock() instanceof EnergyBlock)
		{
			EnergyBlock energyBlock = (EnergyBlock) adjacentBlockState.getBlock();
			
			if(energyBlock.isSideInput(world, adjacentPosition, adjacentBlockState, direction.getOpposite()))
			{
				EnergyNode adjacentEnergyNode = getConsumer(adjacentPosition, world.getRegistryKey());
				
				if(adjacentEnergyNode != null)
					found.add(adjacentEnergyNode);
			}
		}
		else if(adjacentBlockState.getBlock() instanceof EnergyCableBlock)
		{
			// Connect through energy cable blocks.
			EnergyCableBlock energyCableBlock = (EnergyCableBlock) adjacentBlockState.getBlock();
			
			if(energyCableBlock.isConnected(world, adjacentPosition, adjacentBlockState, direction.getOpposite()))
			{
				for(Direction adjacentDirection : DIRECTIONS)
				{
					if(energyCableBlock.isConnected(world, adjacentPosition, adjacentBlockState, adjacentDirection))
						checkDirectionProducer(world, adjacentPosition, adjacentDirection, checkList, found, producer);
				}
			}
		}
		else if(adjacentBlockState.getBlock() instanceof BreakerSwitchBlock && adjacentBlockState.get(BreakerSwitchBlock.LIT))
		{
			// Connect through breaker switch blocks.
			Direction breakerDirection = adjacentBlockState.get(BreakerSwitchBlock.FACING);
			
			if(direction == breakerDirection)
				checkDirectionProducer(world, adjacentPosition, breakerDirection, checkList, found, producer);
			else if(direction == breakerDirection.getOpposite())
				checkDirectionProducer(world, adjacentPosition, breakerDirection.getOpposite(), checkList, found, producer);
		}
		else if(adjacentBlockState.getBlock() instanceof SolarPanelBlock)
		{
			// Connect through horizontally adjacent solar panel blocks.
			for(Direction adjacentDirection : DIRECTIONS)
			{
				if(adjacentDirection == Direction.DOWN || adjacentDirection == Direction.UP)
					continue;
				else if(world.getBlockState(adjacentPosition.offset(adjacentDirection)).getBlock() instanceof SolarPanelBlock)
					checkDirectionProducer(world, adjacentPosition, adjacentDirection, checkList, found, producer);
			}	
		}
	}
	
	public static void connectConsumer(World world, EnergyNode energyNode)
	{
		if(energyNode.getDimension() != world.getRegistryKey())
			return;
		
		ArrayList<EnergyNode> found = new ArrayList<EnergyNode>();
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();	
		BlockPos position = energyNode.getPosition();
		BlockState blockState = world.getBlockState(position);
		energyNode.getOutputs().clear();
		checkList.add(position);
		
		if(!(blockState.getBlock() instanceof EnergyBlock))
			return;
		
		EnergyBlock energyBlock = (EnergyBlock) blockState.getBlock();
		energyNode.getInputs().clear();
		
		for(Direction direction : Direction.values())
		{
			if(energyBlock.isSideInput(world, position, blockState, direction))
				checkDirectionConsumer(world, position, direction, checkList, found, energyNode);
		}

		for(EnergyNode producer : found)
		{
			if(!energyNode.getInputs().contains(producer))
				energyNode.getInputs().add(producer);
			
			if(!producer.getOutputs().contains(energyNode))
				producer.getOutputs().add(energyNode);
		}
		
		// Recursively find breaker switches.
		energyNode.getBreakers().clear();
		checkList.clear();
		ArrayList<BlockPos> breakerList = new ArrayList<BlockPos>();
		findBreakerSwitches(world, position, checkList, breakerList);
		energyNode.getBreakers().addAll(breakerList);
	}
	
	public static void checkDirectionConsumer(World world, BlockPos position, Direction direction, ArrayList<BlockPos> checkList, ArrayList<EnergyNode> found, EnergyNode consumer)
	{
		BlockPos adjacentPosition = position.offset(direction);
		
		if(checkList.contains(adjacentPosition))
			return;
		
		checkList.add(adjacentPosition);
		BlockState adjacentBlockState = world.getBlockState(adjacentPosition);
		
		if(adjacentBlockState.getBlock() instanceof EnergyBlock)
		{
			EnergyBlock energyBlock = (EnergyBlock) adjacentBlockState.getBlock();
			
			if(energyBlock.isSideOutput(world, adjacentPosition, adjacentBlockState, direction.getOpposite()))
			{
				EnergyNode adjacentEnergyNode = getProducer(adjacentPosition, world.getRegistryKey());
				
				if(adjacentEnergyNode != null)
					found.add(adjacentEnergyNode);
			}
		}
		else if(adjacentBlockState.getBlock() instanceof EnergyCableBlock)
		{
			// Connect through energy cable blocks.
			EnergyCableBlock energyCableBlock = (EnergyCableBlock) adjacentBlockState.getBlock();
			
			if(energyCableBlock.isConnected(world, adjacentPosition, adjacentBlockState, direction.getOpposite()))
			{
				for(Direction adjacentDirection : DIRECTIONS)
				{
					if(energyCableBlock.isConnected(world, adjacentPosition, adjacentBlockState, adjacentDirection))
						checkDirectionConsumer(world, adjacentPosition, adjacentDirection, checkList, found, consumer);
				}
			}
		}
		else if(adjacentBlockState.getBlock() instanceof BreakerSwitchBlock && adjacentBlockState.get(BreakerSwitchBlock.LIT))
		{
			// Connect through breaker switch blocks.
			Direction breakerDirection = adjacentBlockState.get(BreakerSwitchBlock.FACING);
			
			if(direction == breakerDirection)
				checkDirectionConsumer(world, adjacentPosition, breakerDirection, checkList, found, consumer);
			else if(direction == breakerDirection.getOpposite())
				checkDirectionConsumer(world, adjacentPosition, breakerDirection.getOpposite(), checkList, found, consumer);
		}
		else if(adjacentBlockState.getBlock() instanceof SolarPanelBlock)
		{
			// Connect through horizontally adjacent solar panel blocks.
			for(Direction adjacentDirection : DIRECTIONS)
			{
				if(adjacentDirection == Direction.DOWN || adjacentDirection == Direction.UP)
					continue;
				else if(world.getBlockState(adjacentPosition.offset(adjacentDirection)).getBlock() instanceof SolarPanelBlock)
					checkDirectionConsumer(world, adjacentPosition, adjacentDirection, checkList, found, consumer);
			}	
		}
	}
	
	/**
	 * Recursively update energy nodes when an energy conduit is placed or broken.
	 */
	public static void updateEnergyNodes(World world, BlockPos position, ArrayList<BlockPos> checkList)
	{
		if(checkList.contains(position))
			return;
		
		checkList.add(position);
		
		for(Direction direction : Direction.values())
		{
			BlockPos adjacentPosition = position.offset(direction);
			BlockState adjacentState = world.getBlockState(adjacentPosition);
			EnergyNode producer = EnergyNet.getProducer(adjacentPosition, world.getRegistryKey());
			EnergyNode consumer = EnergyNet.getConsumer(adjacentPosition, world.getRegistryKey());
			
			if(adjacentState == null)
				continue;
			
			if(adjacentState.getBlock() instanceof EnergyCableBlock)
				updateEnergyNodes(world, adjacentPosition, checkList);
			else if(adjacentState.getBlock() instanceof BreakerSwitchBlock && adjacentState.get(BreakerSwitchBlock.LIT) && (direction == adjacentState.get(BreakerSwitchBlock.FACING) || direction == adjacentState.get(BreakerSwitchBlock.FACING).getOpposite()))
				updateEnergyNodes(world, adjacentPosition, checkList);
			else if(adjacentState.getBlock() instanceof SolarPanelBlock && direction != Direction.DOWN)
			{
				if(producer != null)
					EnergyNet.connectProducer(world, producer);
				else
					((EnergyBlock) adjacentState.getBlock()).addNode(world, adjacentPosition);
				
				updateEnergyNodes(world, adjacentPosition, checkList);
			}
			else if(adjacentState.getBlock() instanceof EnergyBlock)
			{
				EnergyBlock energyBlock = (EnergyBlock) adjacentState.getBlock();
				
				if(producer != null)
					EnergyNet.connectProducer(world, producer);
				else if(energyBlock.isSideOutput(world, adjacentPosition, adjacentState, direction.getOpposite()))
					energyBlock.addNode(world, adjacentPosition);
				
				if(consumer != null)
					EnergyNet.connectConsumer(world, consumer);
				else if(energyBlock.isSideInput(world, adjacentPosition, adjacentState, direction.getOpposite()))
					energyBlock.addNode(world, adjacentPosition);
			}
		}
	}
	
	private static void findBreakerSwitches(World world, BlockPos position, ArrayList<BlockPos> checkList, ArrayList<BlockPos> breakerList)
	{
		if(checkList.contains(position))
			return;
		
		checkList.add(position);
		
		for(Direction direction : Direction.values())
		{
			BlockPos adjacentPosition = position.offset(direction);
			BlockState adjacentState = world.getBlockState(adjacentPosition);
			
			if(adjacentState == null)
				continue;
			
			if(adjacentState.getBlock() instanceof EnergyCableBlock || (adjacentState.getBlock() instanceof SolarPanelBlock && direction != Direction.DOWN))
				findBreakerSwitches(world, adjacentPosition, checkList, breakerList);
			else if(adjacentState.getBlock() instanceof BreakerSwitchBlock)
				breakerList.add(adjacentPosition);
		}
	}
	
	/**
	 * Save all world specific energy network data.
	 */
	public static DataCompound saveData()
	{
		DataCompound data = new DataCompound();
		DataCompound energyNodeData = new DataCompound();
		ArrayList<EnergyNode> combinedList = new ArrayList<EnergyNode>();
		
		for(EnergyNode energyProducer : energyProducers)
			combinedList.add(energyProducer);
		
		for(EnergyNode energyConsumer : energyConsumers)
		{
			if(!combinedList.contains(energyConsumer))
				combinedList.add(energyConsumer);
		}
		
		String[] energyNodeNames = new String[combinedList.size()];
		
		for(int i = 0; i < combinedList.size(); i++)
		{
			EnergyNode energyNode = combinedList.get(i);
			energyNode.saveData(energyNodeData);
			energyNodeNames[i] = energyNode.getName();
		}
		
		data.setValue("energyNodes", energyNodeData);
		data.setValue("energyNodeNames", energyNodeNames);
		return data;
	}
	
	/**
	 * Load all world specific energy network data.
	 */
	public static void loadData(DataCompound data)
	{
		energyProducers.clear();
		energyConsumers.clear();
		
		if(data == null)
			return;
		
		DataCompound energyNodeData = data.getDataCompound("energyNodes");
		String[] energyNodeNames = data.getStringArray("energyNodeNames");
		
		for(String dataName : energyNodeNames)
		{
			EnergyNode energyNode = EnergyNode.loadData(energyNodeData.getDataCompound(dataName));
			
			if(energyNode.isProducer())
				energyProducers.add(energyNode);
			
			if(energyNode.isConsumer())
				energyConsumers.add(energyNode);
		}

		for(EnergyNode energyNode : energyProducers)
		{	
			ArrayList<EnergyNode> newOutputs = new ArrayList<EnergyNode>();
			
			for(int i = 0; i < energyNode.getOutputs().size(); i++)
			{
				for(EnergyNode c : energyConsumers)
				{
					if(c.getPosition().equals(energyNode.getOutputs().get(i).getPosition()) && c.getDimension().equals(energyNode.getDimension()))
						newOutputs.add(c);
				}
			}
			
			energyNode.getOutputs().clear();
			energyNode.getOutputs().addAll(newOutputs);
		}
		
		for(EnergyNode energyNode : energyConsumers)
		{	
			ArrayList<EnergyNode> newInputs = new ArrayList<EnergyNode>();
			
			for(int i = 0; i < energyNode.getInputs().size(); i++)
			{
				for(EnergyNode p : energyProducers)
				{
					if(p.getPosition().equals(energyNode.getInputs().get(i).getPosition()) && p.getDimension().equals(energyNode.getDimension()))
						newInputs.add(p);
				}
			}
			
			energyNode.getInputs().clear();
			energyNode.getInputs().addAll(newInputs);
		}
	}
}