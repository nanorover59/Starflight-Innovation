package space.block.entity;

import java.util.ArrayList;
import java.util.function.BiPredicate;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.StarflightMod;
import space.block.RocketControllerBlock;
import space.block.RocketThrusterBlock;
import space.block.StarflightBlocks;
import space.entity.MovingCraftEntity;
import space.entity.RocketEntity;
import space.planet.Planet;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.BlockSearch;
import space.vessel.BlockMass;
import space.vessel.MovingCraftBlockData;

public class RocketControllerBlockEntity extends BlockEntity
{
	protected ArrayList<MovingCraftBlockData> blockDataList = new ArrayList<MovingCraftBlockData>();
	private Vec3d centerOfMass = Vec3d.ZERO;
	private Vec3d momentOfInertia1 = Vec3d.ZERO;
	private Vec3d momentOfInertia2 = Vec3d.ZERO;
	private double mass;
	private double thrust;
	private double thrustVacuum;
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
		thrust = 0.0;
		thrustVacuum = 0.0;
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
        BlockSearch.movingCraftSearch(world, getPos(), positionList, BlockSearch.MAX_VOLUME);
		
        if(positionList.size() >= BlockSearch.MAX_VOLUME)
        {
        	positionList.clear();
        	return;
        }
        
        // Find the center of mass in world coordinates.
        for(BlockPos pos : positionList)
        {
        	double blockMass = BlockMass.getMass(world, pos);
        	BlockEntity blockEntity = world.getBlockEntity(pos);
        	mass += blockMass;
        	Vec3d centerPos = pos.toCenterPos();
        	centerOfMass = centerOfMass.add(centerPos.getX() * blockMass, centerPos.getY() * blockMass, centerPos.getZ() * blockMass);
        	
        	if(blockEntity != null)
        	{
        		if(blockEntity instanceof HydrogenTankBlockEntity)
        		{
        			HydrogenTankBlockEntity hydrogenTank = (HydrogenTankBlockEntity) blockEntity;
        			double hydrogenTankMass = hydrogenTank.getStoredFluid();
        			mass += hydrogenTankMass;
        			centerPos = hydrogenTank.getCenterOfMass().toCenterPos();
        			centerOfMass = centerOfMass.add(centerPos.getX() * hydrogenTankMass, centerPos.getY() * hydrogenTankMass, centerPos.getZ() * hydrogenTankMass);
        		}
        		else if(blockEntity instanceof OxygenTankBlockEntity)
        		{
        			OxygenTankBlockEntity oxygenTank = (OxygenTankBlockEntity) blockEntity;
        			double oxygenTankMass = oxygenTank.getStoredFluid();
        			mass += oxygenTankMass;
        			centerPos = oxygenTank.getCenterOfMass().toCenterPos();
        			centerOfMass = centerOfMass.add(centerPos.getX() * oxygenTankMass, centerPos.getY() * oxygenTankMass, centerPos.getZ() * oxygenTankMass);
        		}
        	}
        }
        
        centerOfMass = centerOfMass.multiply(1.0 / mass);
        
        // Find the fuel supply, thrusters, and components of the moment of inertia.
        double massFlowSum = 0;
        
        for(BlockPos pos : positionList)
        {
        	boolean redstone = world.isReceivingRedstonePower(pos);
        	double blockMass = BlockMass.getMass(world, pos);
        	BlockEntity blockEntity = world.getBlockEntity(pos);
        	Vec3d centerPos = pos.toCenterPos().subtract(centerOfMass);
        	// Square distance to the center of mass coordinates.
        	double sqyz = centerPos.getY() * centerPos.getY() + centerPos.getZ() * centerPos.getZ();
        	double sqxz = centerPos.getX() * centerPos.getX() + centerPos.getZ() * centerPos.getZ();
        	double sqxy = centerPos.getX() * centerPos.getX() + centerPos.getY() * centerPos.getY();
        	// Sum components for the moment of inertia tensor.
        	momentOfInertia1 = momentOfInertia1.subtract(blockMass * sqyz, blockMass * sqxz, blockMass * sqxy);
        	momentOfInertia2 = momentOfInertia2.subtract(-blockMass * centerPos.getX() * centerPos.getY(), -blockMass * centerPos.getX() * centerPos.getZ(), -blockMass * centerPos.getY() * centerPos.getZ());
        	
        	if(blockEntity != null)
        	{
        		if(blockEntity instanceof HydrogenTankBlockEntity)
        		{
        			HydrogenTankBlockEntity hydrogenTank = (HydrogenTankBlockEntity) blockEntity;
        			
        			if(!redstone)
        			{
        				hydrogen += hydrogenTank.getStoredFluid();
            			hydrogenCapacity += hydrogenTank.getStorageCapacity();
        			}
        			
                	for(Direction direction : Direction.values())
            		{
                		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>(); // Check list to avoid ensure each block is only checked once.
            			
            			BiPredicate<WorldAccess, BlockPos> include = (w, p) -> {
            				return world.getBlockState(p).getBlock() == StarflightBlocks.FLUID_TANK_INSIDE;
            			};
            			
            			BlockSearch.search(world, pos.offset(direction), checkList, include, BlockSearch.MAX_VOLUME, true);
            			
            			if(checkList.size() > 0 && checkList.size() < BlockSearch.MAX_VOLUME)
            			{
            				double unitMass = hydrogenTank.getStoredFluid() / checkList.size();
            				
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
            			}
            		}
        		}
        		else if(blockEntity instanceof OxygenTankBlockEntity)
        		{
        			OxygenTankBlockEntity oxygenTank = (OxygenTankBlockEntity) blockEntity;
        			
        			if(!redstone)
        			{
        				oxygen += oxygenTank.getStoredFluid();
        				oxygenCapacity += oxygenTank.getStorageCapacity();
        			}
        			
        			for(Direction direction : Direction.values())
            		{
                		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>(); // Check list to avoid ensure each block is only checked once.
            			
            			BiPredicate<WorldAccess, BlockPos> include = (w, p) -> {
            				return world.getBlockState(p).getBlock() == StarflightBlocks.FLUID_TANK_INSIDE;
            			};
            			
            			BlockSearch.search(world, pos.offset(direction), checkList, include, BlockSearch.MAX_VOLUME, true);
            			
            			if(checkList.size() > 0 && checkList.size() < BlockSearch.MAX_VOLUME)
            			{
            				double unitMass = oxygenTank.getStoredFluid() / checkList.size();
            				
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
            			}
            		}
        		}
        	}
        	else if(world.getBlockState(pos).getBlock() instanceof RocketThrusterBlock && !redstone)
        	{
        		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
        		double pressure = 0.0;
        		
        		if(data != null && !data.isOrbit())
        			pressure = data.getPressure();
        		
        		thrust += ((RocketThrusterBlock) world.getBlockState(pos).getBlock()).getThrust(pressure);
        		thrustVacuum += ((RocketThrusterBlock) world.getBlockState(pos).getBlock()).getThrust(0.0);
        		massFlowSum += ((RocketThrusterBlock) world.getBlockState(pos).getBlock()).getMassFlow();
        	}
        }
        
        averageVE = 9.80665 * (thrust / massFlowSum);
        averageVEVacuum = 9.80665 * (thrustVacuum / massFlowSum);
        deltaV = availableDV(mass, hydrogen, oxygen, averageVEVacuum);
        deltaVCapacity = availableDV(mass + (hydrogenCapacity - hydrogen) + (oxygenCapacity - oxygen), hydrogenCapacity, oxygenCapacity, averageVEVacuum);
        PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
		Planet currentPlanet = data.getPlanet();
		requiredDeltaV1 = data.isSky() ? currentPlanet.dVSkyToOrbit() : currentPlanet.dVSurfaceToOrbit();
		requiredDeltaV2 = currentPlanet.dVOrbitToSurface();
		blockDataList = MovingCraftEntity.captureBlocks(world, new BlockPos(MathHelper.floor(centerOfMass.getX()), MathHelper.floor(centerOfMass.getY()), MathHelper.floor(centerOfMass.getZ())), positionList);
		markDirty();
	}
	
	private double availableDV(double initialMass, double hydrogen, double oxygen, double averageVE)
	{
		double fuelMass = Math.min(oxygen + (oxygen / 8.0), hydrogen + (hydrogen * 8.0));
		double finalMass = initialMass - fuelMass;
		return averageVE * Math.log(initialMass / finalMass);
	}
	
	public static void receiveButtonPress(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender)
	{
		RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(buffer.readString()));
		BlockPos position = buffer.readBlockPos();
		int action = buffer.readInt();
		
		server.execute(() -> {
			ServerWorld world = server.getWorld(worldKey);
			BlockEntity blockEntity = world.getBlockEntity(position);
			
			if(blockEntity != null && blockEntity instanceof RocketControllerBlockEntity)
			{
				RocketControllerBlockEntity rocketController = (RocketControllerBlockEntity) blockEntity;
				
				if(action == 0)
				{
					rocketController.runScan();
					rocketController.sendDisplayData(player);
				}
				else if(action == 1 && !rocketController.blockDataList.isEmpty())
				{
					BlockPos centerOfMass = BlockPos.ofFloored(rocketController.centerOfMass);
					RocketEntity entity = new RocketEntity(world, centerOfMass, rocketController.blockDataList, world.getBlockState(position).get(RocketControllerBlock.FACING), rocketController.mass, rocketController.momentOfInertia1.toVector3f(), rocketController.momentOfInertia2.toVector3f(), rocketController.hydrogen, rocketController.hydrogenCapacity, rocketController.oxygen, rocketController.oxygenCapacity);
					MovingCraftEntity.removeBlocksFromWorld(world, centerOfMass, rocketController.blockDataList);
					world.spawnEntity(entity);
				}
			}
		});
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, RocketControllerBlockEntity blockEntity)
	{
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
	public void writeNbt(NbtCompound nbt)
	{
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
		int blockCount = blockDataList.size();
		nbt.putInt("blockCount", blockCount);
		int[] x = new int[blockCount];
		int[] y = new int[blockCount];
		int[] z = new int[blockCount];

		for(int i = 0; i < blockCount; i++)
		{
			MovingCraftBlockData blockData = blockDataList.get(i);
			x[i] = blockData.getPosition().getX();
			y[i] = blockData.getPosition().getY();
			z[i] = blockData.getPosition().getZ();
			blockData.saveData(nbt);
		}
		
		nbt.putIntArray("x", x);
		nbt.putIntArray("y", y);
		nbt.putIntArray("z", z);
	}

	@Override
	public void readNbt(NbtCompound nbt)
	{
		centerOfMass = new Vec3d(nbt.getDouble("cx"), nbt.getDouble("cy"), nbt.getDouble("cz"));
		momentOfInertia1 = new Vec3d(nbt.getDouble("mix1"), nbt.getDouble("miy1"), nbt.getDouble("miz1"));
		momentOfInertia2 = new Vec3d(nbt.getDouble("mix2"), nbt.getDouble("miy2"), nbt.getDouble("miz2"));
		mass = nbt.getDouble("mass");
		thrust = nbt.getDouble("thrust");
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
		int blockCount = nbt.getInt("blockCount");
		int[] x = nbt.getIntArray("x");
		int[] y = nbt.getIntArray("y");
		int[] z = nbt.getIntArray("z");

		for(int i = 0; i < blockCount; i++)
		{
			BlockPos dataPos = new BlockPos(x[i], y[i], z[i]);
			blockDataList.add(MovingCraftBlockData.loadData(nbt.getCompound(dataPos.toShortString())));
		}
	}
	
	public void sendDisplayData(ServerPlayerEntity player)
	{
		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeDouble(mass);
		buffer.writeDouble(thrust);
		buffer.writeDouble(hydrogen);
		buffer.writeDouble(hydrogenCapacity);
		buffer.writeDouble(oxygen);
		buffer.writeDouble(oxygenCapacity);
		buffer.writeDouble(deltaV);
		buffer.writeDouble(deltaVCapacity);
		buffer.writeDouble(requiredDeltaV1);
		buffer.writeDouble(requiredDeltaV2);
		ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "rocket_controller_data"), buffer);
	}
}