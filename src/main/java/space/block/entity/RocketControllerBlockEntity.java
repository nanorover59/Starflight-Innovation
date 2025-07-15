package space.block.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.ReactionControlThrusterBlock;
import space.block.RocketControllerBlock;
import space.block.RocketThrusterBlock;
import space.block.RocketThrusterExtensionBlock;
import space.block.StarflightBlocks;
import space.craft.MovingCraftBlock;
import space.craft.Thruster;
import space.entity.MovingCraftEntity;
import space.entity.RocketEntity;
import space.network.c2s.RocketControllerButtonC2SPacket;
import space.network.s2c.RocketControllerDataS2CPacket;
import space.planet.Planet;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.screen.RocketControllerScreenHandler;
import space.util.BlockSearch;
import space.util.FluidResourceType;

public class RocketControllerBlockEntity extends LockableContainerBlockEntity implements SidedInventory, EnergyBlockEntity
{
	public DefaultedList<ItemStack> inventory;
	public final PropertyDelegate propertyDelegate;
	private long energy;
	private int scanProgress;
	protected ArrayList<MovingCraftBlock> blockDataList = new ArrayList<MovingCraftBlock>();
	private ArrayList<Thruster> mainThrusters = new ArrayList<Thruster>();
	private ArrayList<Thruster> rcsThrusters = new ArrayList<Thruster>();
	private Vec3d centerOfMass = Vec3d.ZERO;
	private Vec3d momentOfInertia1 = Vec3d.ZERO;
	private Vec3d momentOfInertia2 = Vec3d.ZERO;
	private double mass;
	private double volume;
	private double thrust;
	private double thrustVacuum;
	private double buoyancy;
	private double averageVE;
	private double averageVEVacuum;
	private double hydrogen;
	private double hydrogenCapacity;
	private double oxygen;
	private double oxygenCapacity;
	private double deltaV;
	private double deltaVCapacity;
	private double requiredDeltaV1;
	private double requiredDeltaV2;
	
	public RocketControllerBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.ROCKET_CONTROLLER_BLOCK_ENTITY, pos, state);
		this.inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
		this.propertyDelegate = new PropertyDelegate()
		{
			public int get(int index)
			{
				switch(index)
				{
				case 0:
					return (int) RocketControllerBlockEntity.this.energy;
				case 1:
					return (int) RocketControllerBlockEntity.this.scanProgress;
				default:
					return 0;
				}
			}

			public void set(int index, int value)
			{
				switch(index)
				{
				case 0:
					RocketControllerBlockEntity.this.energy = value;
					break;
				case 1:
					RocketControllerBlockEntity.this.scanProgress = value;
					break;
				}

			}

			public int size()
			{
				return 2;
			}
		};
	}
	
	@Override
	public int size()
	{
		return this.inventory.size();
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

	public int[] getAvailableSlots(Direction side)
	{
		return new int[] {0, 1};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, Direction dir)
	{
		return this.isValid(slot, stack);
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir)
	{
		return false;
	}

	@Override
	protected Text getContainerName()
	{
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	protected DefaultedList<ItemStack> getHeldStacks()
	{
		return this.inventory;
	}

	@Override
	protected void setHeldStacks(DefaultedList<ItemStack> inventory)
	{
		this.inventory = inventory;
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		return new RocketControllerScreenHandler(syncId, playerInventory, this, this.propertyDelegate, ScreenHandlerContext.create(this.world, this.getPos()));
	}
	
	/**
	 * Compile a list of blocks to be included in vehicle construction and determine various parameters.
	 */
	public void runScan()
	{
		// Clear all variables.
		centerOfMass = Vec3d.ZERO;
		momentOfInertia1 = Vec3d.ZERO;
		momentOfInertia2 = Vec3d.ZERO;
		mass = 0.0;
		volume = 0.0;
		thrust = 0.0;
		thrustVacuum = 0.0;
		buoyancy = 0.0;
		averageVE = 0.0;
		averageVEVacuum = 0.0;
		hydrogen = 0.0;
		hydrogenCapacity = 0.0;
		oxygen = 0.0;
		oxygenCapacity = 0.0;
		deltaV = 0.0;
		deltaVCapacity = 0.0;
		requiredDeltaV1 = 0.0;
		requiredDeltaV2 = 0.0;
		blockDataList.clear();
		
		// Detect blocks to be included in the craft construction.
        ArrayList<BlockPos> positionList = new ArrayList<BlockPos>();
        Set<BlockPos> set = new HashSet<BlockPos>();
        BlockSearch.movingCraftSearch(world, pos, positionList, set, null, BlockSearch.MAX_VOLUME, BlockSearch.MAX_DISTANCE);
        
        // Find the center of mass in world coordinates.
        for(BlockPos pos : positionList)
        {
        	double blockMass = MovingCraftBlock.getMassForBlock(world, pos);
        	double blockVolume = MovingCraftBlock.volumeForBlock(world.getBlockState(pos), world, pos);
        	BlockEntity blockEntity = world.getBlockEntity(pos);
        	mass += blockMass;
        	volume += blockVolume;
        	Vec3d centerPos = pos.toCenterPos();
        	centerOfMass = centerOfMass.add(centerPos.getX() * blockMass, centerPos.getY() * blockMass, centerPos.getZ() * blockMass);
        	
        	if(blockEntity != null)
        	{
				if(blockEntity instanceof FluidTankControllerBlockEntity)
				{
					FluidTankControllerBlockEntity fluidTank = (FluidTankControllerBlockEntity) blockEntity;
					
					if(fluidTank.getCenterOfMass() != null)
					{
						double fluidTankMass = fluidTank.getStoredFluid();
						mass += fluidTankMass;
						centerPos = fluidTank.getCenterOfMass().toCenterPos();
						centerOfMass = centerOfMass.add(centerPos.getX() * fluidTankMass, centerPos.getY() * fluidTankMass, centerPos.getZ() * fluidTankMass);
					}
				}
        	}
        }
        
        centerOfMass = centerOfMass.multiply(1.0 / mass);
        
        // Find the fuel supply, thrusters, and components of the moment of inertia.
        double massFlowSum = 0;
        
        for(BlockPos pos : positionList)
        {
        	boolean redstone = world.isReceivingRedstonePower(pos);
        	double blockMass = MovingCraftBlock.getMassForBlock(world, pos);
        	BlockEntity blockEntity = world.getBlockEntity(pos);
        	Vec3d centerPos = pos.toCenterPos().subtract(centerOfMass);
        	// Square distance to the center of mass coordinates.
        	double sqyz = centerPos.getY() * centerPos.getY() + centerPos.getZ() * centerPos.getZ();
        	double sqxz = centerPos.getX() * centerPos.getX() + centerPos.getZ() * centerPos.getZ();
        	double sqxy = centerPos.getX() * centerPos.getX() + centerPos.getY() * centerPos.getY();
        	// Sum components for the moment of inertia tensor.
        	momentOfInertia1 = momentOfInertia1.subtract(blockMass * sqyz, blockMass * sqxz, blockMass * sqxy);
        	momentOfInertia2 = momentOfInertia2.subtract(-blockMass * centerPos.getX() * centerPos.getY(), -blockMass * centerPos.getX() * centerPos.getZ(), -blockMass * centerPos.getY() * centerPos.getZ());
        	
			if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
			{
				FluidTankControllerBlockEntity fluidTank = (FluidTankControllerBlockEntity) blockEntity;

				if(!redstone && !(fluidTank instanceof BalloonControllerBlockEntity))
				{
					if(fluidTank.getFluidType() == FluidResourceType.HYDROGEN)
					{
						hydrogen += fluidTank.getStoredFluid();
						hydrogenCapacity += fluidTank.getStorageCapacity();
					}
					else if(fluidTank.getFluidType() == FluidResourceType.OXYGEN)
					{
						oxygen += fluidTank.getStoredFluid();
						oxygenCapacity += fluidTank.getStorageCapacity();
					}
				}

				for(Direction direction : Direction.values())
				{
					ArrayList<BlockPos> checkList = new ArrayList<BlockPos>(); // Check list to avoid ensure each block is only checked once.

					BiPredicate<World, BlockPos> include = (w, p) -> {
						return world.getBlockState(p).getBlock() == StarflightBlocks.FLUID_TANK_INSIDE;
					};

					BlockSearch.search(world, pos.offset(direction), checkList, include, BlockSearch.MAX_VOLUME, true);

					if(checkList.size() > 0 && checkList.size() < BlockSearch.MAX_VOLUME)
					{
						double unitMass = fluidTank.getStoredFluid() / checkList.size();

						for(BlockPos fluidPos : checkList)
						{
							centerPos = fluidPos.toCenterPos().subtract(centerOfMass);
							// Square distance to the center of mass coordinates.
							sqyz = centerPos.getY() * centerPos.getY() + centerPos.getZ() * centerPos.getZ();
							sqxz = centerPos.getX() * centerPos.getX() + centerPos.getZ() * centerPos.getZ();
							sqxy = centerPos.getX() * centerPos.getX() + centerPos.getY() * centerPos.getY();
							// Sum components for the moment of inertia tensor.
							momentOfInertia1 = momentOfInertia1.subtract(unitMass * sqyz, unitMass * sqxz, unitMass * sqxy);
							momentOfInertia2 = momentOfInertia2.subtract(-unitMass * centerPos.getX() * centerPos.getY(), -unitMass * centerPos.getX() * centerPos.getZ(), -unitMass * centerPos.getY() * centerPos.getZ());
						}
						
						if(fluidTank instanceof BalloonControllerBlockEntity && ((BalloonControllerBlockEntity) fluidTank).getStoredFluid() > ((BalloonControllerBlockEntity) fluidTank).getStorageCapacity() * 0.9)
							volume += checkList.size();
					}
				}
			}
        	else if(world.getBlockState(pos).getBlock() instanceof RocketThrusterBlock && !redstone)
        	{
        		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
        		BlockState blockState = world.getBlockState(pos);
        		BlockState extensionState = getWorld().getBlockState(pos.offset(blockState.get(RocketThrusterBlock.FACING).getOpposite()));
        		RocketThrusterBlock thrusterBlock = ((RocketThrusterBlock) blockState.getBlock());
        		double vacuumISP = thrusterBlock.getVacuumnISP();
				double atmISP = thrusterBlock.getAtmISP();
				double vacuumThrust = thrusterBlock.getVacuumThrust();
        		double pressure = 0.0;
        		
        		if(data != null && !data.isOrbit())
        			pressure = data.getPressure();
        		
        		if(extensionState.getBlock() instanceof RocketThrusterExtensionBlock)
        		{
        			RocketThrusterExtensionBlock thrusterExtensionBlock = ((RocketThrusterExtensionBlock) extensionState.getBlock());
        			vacuumISP *= thrusterExtensionBlock.getVacuumFactor();
        			atmISP *= thrusterExtensionBlock.getAtmFactor();
        			vacuumThrust *= thrusterExtensionBlock.getVacuumFactor();
        		}
        		
        		Thruster thruster = new Thruster(centerPos.toVector3f(), blockState.get(RocketThrusterBlock.FACING).getOpposite().getUnitVector(), vacuumISP, atmISP, vacuumThrust, thrusterBlock.getGimbal());
        		thruster.forAtmosphere(pressure);
        		mainThrusters.add(thruster);
        		thrust += thruster.getThrust();
        		thrustVacuum += vacuumThrust;
        		massFlowSum += thruster.getMassFlow(1.0);
        	}
        	else if(world.getBlockState(pos).getBlock() instanceof ReactionControlThrusterBlock && !redstone)
        	{
        		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
        		BlockState blockState = world.getBlockState(pos);
        		ReactionControlThrusterBlock rcsBlock = ((ReactionControlThrusterBlock) blockState.getBlock());
        		Quaternionf blockFacingQuaternion = blockState.get(FacingBlock.FACING).getRotationQuaternion();
        		double vacuumISP = 400.0;
				double atmISP = 300.0;
				double vacuumThrust = 4.0e4;
        		double pressure = 0.0;
        		
        		if(data != null && !data.isOrbit())
        			pressure = data.getPressure();

				for(Pair<Vector3f, Vector3f> thrusterPair : rcsBlock.getThrusters())
				{
					Vector3f position = new Vector3f(thrusterPair.getLeft()).rotate(blockFacingQuaternion).add(centerPos.toVector3f());
					Vector3f direction = new Vector3f(thrusterPair.getRight()).rotate(blockFacingQuaternion);
					Thruster thruster = new Thruster(position, direction, vacuumISP, atmISP, vacuumThrust, 0.0);
					thruster.forAtmosphere(pressure);
					rcsThrusters.add(thruster);
				}
        	}
        }
        
        PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
		Planet currentPlanet = data.getPlanet();
        
        if(data.getPressure() > 0)
		{
			double t = 90.0 + data.getTemperatureCategory(world.getSkyAngle(1.0f)) * 100.0;
			double airDensity = (float) (data.getPressure() * 101325.0) / (t * 287.05);
			buoyancy = airDensity * volume * data.getGravity() * 9.80665;
		}
        
        averageVE = thrust / massFlowSum;
        averageVEVacuum = thrustVacuum / massFlowSum;
        deltaV = availableDV(mass, hydrogen, oxygen, averageVEVacuum);
        deltaVCapacity = availableDV(mass + (hydrogenCapacity - hydrogen) + (oxygenCapacity - oxygen), hydrogenCapacity, oxygenCapacity, averageVEVacuum);
		requiredDeltaV1 = data.isSky() ? currentPlanet.dVSkyToOrbit() : currentPlanet.dVSurfaceToOrbit();
		requiredDeltaV2 = currentPlanet.dVOrbitToSurface();
		blockDataList = MovingCraftEntity.captureBlocks(world, new BlockPos(MathHelper.floor(centerOfMass.getX()), MathHelper.floor(centerOfMass.getY()), MathHelper.floor(centerOfMass.getZ())), positionList);
		scanProgress = 40;
		markDirty();
	}
	
	private double availableDV(double initialMass, double hydrogen, double oxygen, double averageVE)
	{
		double limitingMass = Math.min(hydrogen + hydrogen * 8.0, oxygen + oxygen / 8.0);
		double finalMass = initialMass - limitingMass;
		return averageVEVacuum * Math.log(initialMass / finalMass);
	}
	
	public static void receiveButtonPress(RocketControllerButtonC2SPacket payload, ServerPlayNetworking.Context context)
	{
		int action = payload.action();
		ServerPlayerEntity player = context.player();
		
		((RocketControllerScreenHandler) player.currentScreenHandler).context.run((world, pos) -> {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			
			if(blockEntity != null && blockEntity instanceof RocketControllerBlockEntity)
			{
				RocketControllerBlockEntity rocketController = (RocketControllerBlockEntity) blockEntity;
				
				if(action == 0)
				{
					rocketController.runScan();
					rocketController.sendDisplayData(player);
					player.currentScreenHandler.sendContentUpdates();
				}
				else if(action == 1 && !rocketController.blockDataList.isEmpty())
				{
					BlockPos centerOfMass = BlockPos.ofFloored(rocketController.centerOfMass);
					RocketEntity entity = new RocketEntity(world, centerOfMass, rocketController.blockDataList, rocketController.mainThrusters, rocketController.rcsThrusters, world.getBlockState(pos).get(RocketControllerBlock.FACING), rocketController.mass, rocketController.volume, rocketController.momentOfInertia1.toVector3f(), rocketController.momentOfInertia2.toVector3f(), rocketController.hydrogen, rocketController.hydrogenCapacity, rocketController.oxygen, rocketController.oxygenCapacity);
					MovingCraftEntity.removeBlocksFromWorld(world, centerOfMass, rocketController.blockDataList);
					world.spawnEntity(entity);
				}
			}
		});
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, RocketControllerBlockEntity blockEntity)
	{
		long power = ((EnergyBlock) state.getBlock()).getInput();
		blockEntity.dischargeItem(blockEntity.inventory.get(0), power * 2);
		
		if(blockEntity.scanProgress > 0)
			blockEntity.scanProgress--;
		
		// Recalculate travel delta-v if a navigation card change is detected.
		/*ItemStack stack = blockEntity.inventory.get(0);
		
		if(stack.isEmpty() == blockEntity.hasCard)
		{
			//blockEntity.runDeltaVCalculations();
			
			for(PlayerEntity player : blockEntity.viewingList)
				blockEntity.sendDisplayData(world.getServer().getPlayerManager().getPlayer(player.getUuid()));
			
			blockEntity.hasCard = !stack.isEmpty();
		}*/
	}
	
	@Override
	public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		Inventories.writeNbt(nbt, inventory, registryLookup);
		nbt.putLong("energy", energy);
		nbt.putDouble("cx", centerOfMass.getX());
		nbt.putDouble("cy", centerOfMass.getY());
		nbt.putDouble("cz", centerOfMass.getZ());
		nbt.putDouble("mix1", momentOfInertia1.getX());
		nbt.putDouble("miy1", momentOfInertia1.getY());
		nbt.putDouble("miz1", momentOfInertia1.getZ());
		nbt.putDouble("mix2", momentOfInertia2.getX());
		nbt.putDouble("miy2", momentOfInertia2.getY());
		nbt.putDouble("miz2", momentOfInertia2.getZ());
		nbt.putDouble("mass", mass);
		nbt.putDouble("volume", volume);
		nbt.putDouble("thrust", thrust);
		nbt.putDouble("thrustVacuum", thrustVacuum);
		nbt.putDouble("averageVE", averageVE);
		nbt.putDouble("averageVEVacuum", averageVEVacuum);
		nbt.putDouble("hydrogen", hydrogen);
		nbt.putDouble("hydrogenCapacity", hydrogenCapacity);
		nbt.putDouble("oxygen", oxygen);
		nbt.putDouble("oxygenCapacity", oxygenCapacity);
		nbt.putDouble("deltaV", deltaV);
		nbt.putDouble("deltaVCapacity", deltaVCapacity);
		nbt.putDouble("requiredDeltaV1", requiredDeltaV1);
		nbt.putDouble("requiredDeltaV2", requiredDeltaV2);
	    NbtList blockDataListNBT = new NbtList();
	    NbtList mainThrusterListNBT = new NbtList();
	    NbtList rcsThrusterListNBT = new NbtList();
	    
	    for(MovingCraftBlock blockData : blockDataList)
	    	blockDataListNBT.add(blockData.saveData(new NbtCompound()));
	    
	    for(Thruster thruster : mainThrusters)
	    	mainThrusterListNBT.add(thruster.writeToNbt(new NbtCompound()));
	    
	    for(Thruster thruster : rcsThrusters)
	    	rcsThrusterListNBT.add(thruster.writeToNbt(new NbtCompound()));
	    
	    nbt.put("blockData", blockDataListNBT);
	    nbt.put("mainThrusters", mainThrusterListNBT);
	    nbt.put("rcsThrusters", rcsThrusterListNBT);
	}

	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, this.inventory, registryLookup);
		energy = nbt.getLong("energy");
		centerOfMass = new Vec3d(nbt.getDouble("cx"), nbt.getDouble("cy"), nbt.getDouble("cz"));
		momentOfInertia1 = new Vec3d(nbt.getDouble("mix1"), nbt.getDouble("miy1"), nbt.getDouble("miz1"));
		momentOfInertia2 = new Vec3d(nbt.getDouble("mix2"), nbt.getDouble("miy2"), nbt.getDouble("miz2"));
		mass = nbt.getDouble("mass");
		volume = nbt.getDouble("volume");
		thrust = nbt.getDouble("thrust");
		buoyancy = nbt.getDouble("buoyancy");
		thrustVacuum = nbt.getDouble("thrustVacuum");
		averageVE = nbt.getDouble("averageVE");
		averageVEVacuum = nbt.getDouble("averageVEVacuum");
		hydrogen = nbt.getDouble("hydrogen");
		hydrogenCapacity = nbt.getDouble("hydrogenCapacity");
		oxygen = nbt.getDouble("oxygen");
		oxygenCapacity = nbt.getDouble("oxygenCapacity");
		deltaV = nbt.getDouble("deltaV");
		deltaVCapacity = nbt.getDouble("deltaVCapacity");
		requiredDeltaV1 = nbt.getDouble("requiredDeltaV1");
		requiredDeltaV2 = nbt.getDouble("requiredDeltaV2");
		NbtList blockDataListNBT = nbt.getList("blockData", NbtList.COMPOUND_TYPE);
		NbtList mainThrusterListNBT = nbt.getList("mainThrusters", NbtList.COMPOUND_TYPE);
		NbtList rcsThrusterListNBT = nbt.getList("rcsThrusters", NbtList.COMPOUND_TYPE);
		
        for(int i = 0; i < blockDataListNBT.size(); i++)
        	blockDataList.add(MovingCraftBlock.loadData(blockDataListNBT.getCompound(i)));
		
        for(int i = 0; i < mainThrusterListNBT.size(); i++)
        	mainThrusters.add(Thruster.readFromNbt(mainThrusterListNBT.getCompound(i)));
		
        for(int i = 0; i < rcsThrusterListNBT.size(); i++)
        	rcsThrusters.add(Thruster.readFromNbt(rcsThrusterListNBT.getCompound(i)));
	}
	
	public void sendDisplayData(ServerPlayerEntity player)
	{
		double[] stats = {
			mass,
			volume,
			thrust,
			buoyancy,
			hydrogen,
			hydrogenCapacity,
			oxygen,
			oxygenCapacity,
			deltaV,
			deltaVCapacity,
			requiredDeltaV1,
			requiredDeltaV2
		};
		
		ServerPlayNetworking.send(player, new RocketControllerDataS2CPacket(stats, this.blockDataList));
	}
}