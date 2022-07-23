package space.entity;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import space.block.FluidTankControllerBlock;
import space.block.RocketThrusterBlock;
import space.block.StarflightBlocks;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.HydrogenTankBlockEntity;
import space.block.entity.OxygenTankBlockEntity;
import space.block.entity.RocketControllerBlockEntity;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.AirUtil;
import space.util.StarflightEffects;
import space.vessel.BlockMass;
import space.vessel.MovingCraftBlockData;
import space.vessel.MovingCraftBlockRenderData;
import space.vessel.MovingCraftRenderList;

public class RocketEntity extends MovingCraftEntity
{
	private static final int TRAVEL_CEILING = 512;
	
	private static final TrackedData<Boolean> THRUST_UNDEREXPANDED = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Float> THROTTLE = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.FLOAT);
	
	private ArrayList<BlockPos> activeFluidTanks = new ArrayList<BlockPos>();
	private RegistryKey<World> nextDimension;
	private BlockPos arrivalPos;
	private int arrivalDirection;
	private boolean changedDimension;
	private double craftMass;
	private double gravity;
	private double nominalThrustStart;
	private double nominalThrustEnd;
	private double throttle;
	private double hydrogenCapacity;
	private double oxygenCapacity;
	private double hydrogenSupply;
	private double oxygenSupply;
	private double fuelToUse;
	private double lowerHeight;
	private double upperHeight;
	private double maxWidth;
	private int soundEffectTimer;
	
	public RocketEntity(EntityType<? extends MovingCraftEntity> entityType, World world)
	{
        super(entityType, world);
    }
	
	public RocketEntity(World world, Direction forward, ArrayList<BlockPos> blockPosList, RegistryKey<World> nextDimension, double fuelToUse, BlockPos arrivalPos, int arrivalDirection)
	{
		this((EntityType<? extends RocketEntity>) StarflightEntities.ROCKET, world);
		this.nextDimension = nextDimension;
		this.changedDimension = false;
		this.fuelToUse = fuelToUse;
		this.craftYaw = 0.0f;
		this.craftRoll = 0.0f;
		this.craftPitch = 0.0f;
		this.arrivalPos = arrivalPos;
		this.arrivalDirection = arrivalDirection;
		setForwardDirection(forward.getHorizontal());
		Vec3d centerOfMass = new Vec3d(0.0, 0.0, 0.0);
		BlockPos min = new BlockPos(blockPosList.get(0));
		BlockPos max = new BlockPos(blockPosList.get(0));
		
		// Define maximum thrust values for the atmospheric pressure in the start dimension and the end dimension.
    	double atmosphereStart = PlanetList.isOrbit(world.getRegistryKey()) ? 0.0 : PlanetList.getPlanetForWorld(world.getRegistryKey()).getSurfacePressure();
    	double atmosphereEnd = PlanetList.isOrbit(nextDimension) ? 0.0 : PlanetList.getPlanetForWorld(nextDimension).getSurfacePressure();
    	
		for(BlockPos pos : blockPosList)
		{
			if(pos.getX() < min.getX())
				min = new BlockPos(pos.getX(), min.getY(), min.getZ());
			else if(pos.getX() > max.getX())
				max = new BlockPos(pos.getX(), max.getY(), max.getZ());
			
			if(pos.getY() < min.getY())
				min = new BlockPos(min.getX(), pos.getY(), min.getZ());
			else if(pos.getY() > max.getY())
				max = new BlockPos(max.getX(), pos.getY(), max.getZ());
			
			if(pos.getZ() < min.getZ())
				min = new BlockPos(min.getX(), min.getY(), pos.getZ());
			else if(pos.getZ() > max.getZ())
				max = new BlockPos(max.getX(), max.getY(), pos.getZ());
			
			double blockMass = BlockMass.getMass(world, pos);
			craftMass += blockMass;
			centerOfMass = centerOfMass.add(pos.getX() * blockMass, pos.getY() * blockMass, pos.getZ() * blockMass);
        	BlockEntity blockEntity = world.getBlockEntity(pos);
        	
        	if(blockEntity != null)
        	{
        		if(blockEntity instanceof HydrogenTankBlockEntity)
        		{
        			HydrogenTankBlockEntity hydrogenTank = (HydrogenTankBlockEntity) blockEntity;
        			Vec3d fluidTankCenter = new Vec3d(hydrogenTank.getCenterOfMass().getX() + 0.5, hydrogenTank.getCenterOfMass().getY() + 0.5, hydrogenTank.getCenterOfMass().getZ() + 0.5);
        			hydrogenSupply += hydrogenTank.getStoredFluid();
        			hydrogenCapacity += hydrogenTank.getStorageCapacity();
        			craftMass += hydrogenTank.getStoredFluid();
        			centerOfMass = centerOfMass.add(fluidTankCenter.multiply(hydrogenTank.getStoredFluid()));
        		}
        		else if(blockEntity instanceof OxygenTankBlockEntity)
        		{
        			OxygenTankBlockEntity oxygenTank = (OxygenTankBlockEntity) blockEntity;
        			Vec3d fluidTankCenter = new Vec3d(oxygenTank.getCenterOfMass().getX() + 0.5, oxygenTank.getCenterOfMass().getY() + 0.5, oxygenTank.getCenterOfMass().getZ() + 0.5);
        			oxygenSupply += oxygenTank.getStoredFluid();
        			oxygenCapacity += oxygenTank.getStorageCapacity();
        			craftMass += oxygenTank.getStoredFluid();
        			centerOfMass = centerOfMass.add(fluidTankCenter.multiply(oxygenTank.getStoredFluid()));
        		}
        	}
        	
        	Block block = world.getBlockState(pos).getBlock();
        	
        	if(block instanceof RocketThrusterBlock)
        	{
        		nominalThrustStart += ((RocketThrusterBlock) block).getThrust(atmosphereStart);
        		nominalThrustEnd += ((RocketThrusterBlock) block).getThrust(atmosphereEnd);
        	}
		}
		
		centerOfMass = centerOfMass.multiply(1.0 / craftMass);
		this.setPosition(Math.floor(centerOfMass.getX()) + 0.5, Math.floor(centerOfMass.getY()), Math.floor(centerOfMass.getZ()) + 0.5);
		BlockPos centerBlockPos = new BlockPos(Math.floor(centerOfMass.getX()), Math.floor(centerOfMass.getY()), Math.floor(centerOfMass.getZ()));
		
		// In the absence of an arrival card, set the arrival coordinates to the current coordinates.
		if(arrivalPos.getX() == -9999 && arrivalPos.getY() == -9999 && arrivalPos.getZ() == -9999)
			this.arrivalPos = new BlockPos(centerBlockPos.getX(), -9999, centerBlockPos.getZ());
		
		this.setInitialBlockPos(centerBlockPos);
		this.blockDataList = MovingCraftEntity.captureBlocks(world, centerBlockPos, blockPosList);
		
		if(!blockDataList.isEmpty())
		{
			for(MovingCraftBlockData blockData : blockDataList)
			{
				if(blockData.getBlockState().getBlock() instanceof FluidTankControllerBlock)
					activeFluidTanks.add(blockData.getPosition());
			}
		}
		else
			this.setRemoved(RemovalReason.DISCARDED);
		
		Box box = new Box(min, max.up());
		
		for(Entity entity : world.getOtherEntities(this, box))
		{
			if(entity instanceof LivingEntity)
				pickUpEntity(entity);
		}
		
		lowerHeight = centerBlockPos.getY() - min.getY();
		upperHeight = max.getY() - centerBlockPos.getY();
		maxWidth = (Math.abs(centerBlockPos.getX() - min.getX()) + Math.abs(centerBlockPos.getX() - max.getX()) + Math.abs(centerBlockPos.getZ() - min.getZ()) + Math.abs(centerBlockPos.getZ() - max.getZ())) / 4;
		storeGravity();
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		this.dataTracker.startTracking(THRUST_UNDEREXPANDED, Boolean.valueOf(false));
		this.dataTracker.startTracking(THROTTLE, Float.valueOf(0.0f));
	}
	
	public void setThrustUnderexpanded(boolean b)
	{
		this.dataTracker.set(THRUST_UNDEREXPANDED, Boolean.valueOf(b));
	}
	
	public void setThrottle(float throttle)
	{
		this.dataTracker.set(THROTTLE, Float.valueOf(throttle));
	}
	
	public boolean getThrustUnderexpanded()
	{
		return this.dataTracker.get(THRUST_UNDEREXPANDED);
	}
	
	public float getThrottle()
	{
		return this.dataTracker.get(THROTTLE);
	}
	
	@Override
    public void tick()
	{
		// Run client-side actions and then return.
		if(this.clientMotion())
		{
			// Spawn thruster particles.
			if(getThrottle() > 0.0F)
				spawnThrusterParticles();
			
			return;
		}
		
		// Play the rocket engine sound effect.
		if(getThrottle() > 0.0F && soundEffectTimer <= 0)
		{
			playSound(StarflightEffects.THRUSTER_SOUND_EVENT, 100000.0F, 0.8F + this.random.nextFloat() * 0.05F);
			soundEffectTimer = 5;
		}
		else
			soundEffectTimer--;
			
		// Set the target landing altitude if necessary.
		if(changedDimension && arrivalPos.getY() == -9999)
		{
			boolean noBlocks = true;
			
			for(int i = world.getTopY(); i > world.getBottomY() + 1; i--)
			{
				BlockPos pos = new BlockPos(arrivalPos.getX(), i - 1, arrivalPos.getZ());
				
				if(world.getBlockState(pos).isSolidBlock(world, pos))
				{
					arrivalPos = new BlockPos(arrivalPos.getX(), i, arrivalPos.getZ());
					noBlocks = false;
					break;
				}
			}
			
			if(noBlocks)
				arrivalPos = new BlockPos(arrivalPos.getX(), 64, arrivalPos.getZ());
		}
		
		// Move according to either launch or landing behavior.
		if(changedDimension)
			verticalLandingAutoThrottle();
		else
			launchAnimation();
		
		applyGravity();
		applyThrust();
		
		this.move(MovementType.SELF, this.getVelocity());
		this.setBoundingBox(this.calculateBoundingBox());
		this.fallDistance = 0.0f;
		
		// Move to the next dimension when a high enough altitude is reached.
		if(this.getBlockPos().getY() > TRAVEL_CEILING && !changedDimension)
		{
			// Use hydrogen and oxygen at the 1:8 ratio for water.
			double factor = (hydrogenSupply + oxygenSupply - fuelToUse) / (hydrogenSupply + oxygenSupply);
			hydrogenSupply *= factor;
			oxygenSupply *= factor;
			setVelocity(0.0, -1.0, 0.0);
			changedDimension = true;
			this.changeDimension(world.getServer().getWorld(nextDimension), new Vec3d(arrivalPos.getX() + 0.5, getY(), arrivalPos.getZ() + 0.5), Direction.fromHorizontal(arrivalDirection).asRotation());
			return;
		}
		
		// Turn back into blocks when landed.
		if(this.age > 8 && (verticalCollision || (changedDimension && gravity == 0.0 && (this.getBlockPos().getY() - lowerHeight <= arrivalPos.getY() || getVelocity().getY() >= -0.01))))
		{
			// Extra displacement to avoid clipping into the ground.
			if(verticalCollision)
			{
				for(int i = 0; i < 128; i++)
				{
					if(!verticalCollision)
						break;
					
					this.move(MovementType.SELF, new Vec3d(0.0, 0.1, 0.0));
				}
			}
			
			// Dismount all passengers.
			for(Entity passenger : getPassengerList())
			{
				updatePassengerPosition(passenger);
				passenger.setPosition(passenger.getPos().add(0.0, 0.75, 0.0));
				passenger.setVelocity(Vec3d.ZERO);
				passenger.velocityModified = true;
				passenger.fallDistance = 0.0f;
			}
			
			sendRenderData(true);
			this.releaseBlocks();
		}
		else
			sendRenderData(false);
		
		// Update rotation variables.
		this.setYaw(craftYaw);
		this.setPitch(prevPitch);
		setCraftRoll(craftRoll);
		setCraftPitch(craftPitch);
		setCraftYaw(craftYaw);
		
		// Update thruster state tracked data.
		setThrustUnderexpanded(AirUtil.getAirResistanceMultiplier(world, getBlockPos()) > 0.25);
		setThrottle((float) throttle);
	}
	
	/**
	 * Apply gravity in the negative direction of the global y-axis.
	 */
	private void applyGravity()
	{
		this.addVelocity(0.0, -gravity, 0.0);
	}
	
	/**
	 * Apply thrust in the rotated direction of the craft's local y-axis.
	 */
	private void applyThrust()
	{
		if(throttle == 0.0)
			return;
		
		double force = changedDimension ? nominalThrustEnd * throttle : nominalThrustStart * throttle;
		double acc = (force / craftMass) * 0.0025;
		Vec3d direction = new Vec3d(0.0, 1.0, 0.0);
		float rotationRoll = getCraftRoll();
		float rotationPitch = getCraftPitch();
		float rotationYaw = getCraftYaw();
        
        switch(getForwardDirection())
		{
		case NORTH:
			direction.rotateZ(rotationRoll);
			direction.rotateX(rotationPitch);
			direction.rotateY(rotationYaw);
			break;
		case EAST:
			direction.rotateX(rotationRoll);
			direction.rotateZ(rotationPitch);
			direction.rotateY(rotationYaw);
			break;
		case SOUTH:
			direction.rotateZ(rotationRoll);
			direction.rotateX(rotationPitch);
			direction.rotateY(rotationYaw);
			break;
		case WEST:
			direction.rotateX(rotationRoll);
			direction.rotateZ(rotationPitch);
			direction.rotateY(rotationYaw);
			break;
		default:
			break;
		}
        
        this.addVelocity(direction.getX() * acc, direction.getY() * acc, direction.getZ() * acc);
	}
	
	/**
	 * Update the value of gravitational acceleration per tick.
	 */
	public void storeGravity()
	{
		if(PlanetList.isOrbit(world.getRegistryKey()))
		{
			gravity = 0.0;
			return;
		}
		
		Planet planet = PlanetList.getPlanetForWorld(world.getRegistryKey());
		
		if(planet != null)
			gravity = 9.80665 * 0.0025 * planet.getSurfaceGravity();
		else
			gravity = 9.80665 * 0.0025;
	}
	
	private void launchAnimation()
	{
		throttle = 1.0;
	}
	
	/**
	 * Activate the thrusters at the correct altitude.
	 */
	private void verticalLandingAutoThrottle()
	{
		double vy = getVelocity().getY(); // Vertical velocity in m/tick.
		double currentHeight = (getPos().getY() - lowerHeight) - arrivalPos.getY() - 1.0;
		
		// Find the necessary throttle to cancel vertical velocity.
		double t = ((Math.pow(vy, 2.0) * 0.5) + (gravity * currentHeight)) / (((nominalThrustEnd / craftMass) * 0.0025) * currentHeight);
		
		// Activate the engines if the necessary throttle is significantly high and the vehicle has a little bit of altitude.
		if(t > 0.25 && !(gravity > 0.0 && currentHeight < 0.25))
			throttle = t;
		else
			throttle = 0.0;
	}
	
	private void spawnThrusterParticles()
	{
		ArrayList<MovingCraftBlockRenderData> blocks = MovingCraftRenderList.getBlocksForEntity(getUuid());
		ArrayList<BlockPos> thrusterOffsets = new ArrayList<BlockPos>();
		ArrayList<Vec3f> thrusterOffsetsRotated = new ArrayList<Vec3f>();
		Vec3f upAxis = new Vec3f(0.0F, 1.0F, 0.0F);
        float rotationRoll = getCraftRoll();
		float rotationPitch = getCraftPitch();
		float rotationYaw = getCraftYaw();
		
		for(MovingCraftBlockRenderData block : blocks)
		{
			if(block.getBlockState().getBlock() instanceof RocketThrusterBlock)
				thrusterOffsets.add(block.getPosition());
		}
		
		switch(getForwardDirection())
		{
		case NORTH:
			upAxis.rotate(Vec3f.NEGATIVE_Z.getDegreesQuaternion(rotationRoll));
			upAxis.rotate(Vec3f.NEGATIVE_X.getDegreesQuaternion(rotationPitch));
			upAxis.rotate(Vec3f.NEGATIVE_Y.getDegreesQuaternion(rotationYaw));
			
			for(BlockPos pos : thrusterOffsets)
			{
				Vec3f rotated = new Vec3f(pos.getX(), pos.getY() - 1.0f, pos.getZ());
				rotated.rotate(Vec3f.NEGATIVE_Z.getDegreesQuaternion(rotationRoll));
				rotated.rotate(Vec3f.NEGATIVE_X.getDegreesQuaternion(rotationPitch));
				rotated.rotate(Vec3f.NEGATIVE_Y.getDegreesQuaternion(rotationYaw));
				thrusterOffsetsRotated.add(rotated);
			}
			break;
		case EAST:
			upAxis.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(rotationRoll));
			upAxis.rotate(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotationPitch));
			upAxis.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotationYaw));
			
			for(BlockPos pos : thrusterOffsets)
			{
				Vec3f rotated = new Vec3f(pos.getX(), pos.getY() - 1.0f, pos.getZ());
				rotated.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(rotationRoll));
				rotated.rotate(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotationPitch));
				rotated.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotationYaw));
				thrusterOffsetsRotated.add(rotated);
			}
			break;
		case SOUTH:
			upAxis.rotate(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotationRoll));
			upAxis.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(rotationPitch));
			upAxis.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotationYaw));
			
			for(BlockPos pos : thrusterOffsets)
			{
				Vec3f rotated = new Vec3f(pos.getX(), pos.getY() - 1.0f, pos.getZ());
				rotated.rotate(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotationRoll));
				rotated.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(rotationPitch));
				rotated.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotationYaw));
				thrusterOffsetsRotated.add(rotated);
			}
			break;
		case WEST:
			upAxis.rotate(Vec3f.NEGATIVE_X.getDegreesQuaternion(rotationRoll));
			upAxis.rotate(Vec3f.NEGATIVE_Z.getDegreesQuaternion(rotationPitch));
			upAxis.rotate(Vec3f.NEGATIVE_Y.getDegreesQuaternion(rotationYaw));
			
			for(BlockPos pos : thrusterOffsets)
			{
				Vec3f rotated = new Vec3f(pos.getX(), pos.getY() - 1.0f, pos.getZ());
				rotated.rotate(Vec3f.NEGATIVE_X.getDegreesQuaternion(rotationRoll));
				rotated.rotate(Vec3f.NEGATIVE_Z.getDegreesQuaternion(rotationPitch));
				rotated.rotate(Vec3f.NEGATIVE_Y.getDegreesQuaternion(rotationYaw));
				thrusterOffsetsRotated.add(rotated);
			}
			break;
		default:
			break;
		}
		
		Vec3f velocity = upAxis.copy();
		velocity.scale(-4.0F + (this.random.nextFloat() * 0.5F));
		velocity.add((float) getVelocity().getX(), (float) getVelocity().getY(), (float) getVelocity().getZ());
		
		for(Vec3f pos : thrusterOffsetsRotated)
		{
			pos.add((float) getX(), (float) getY(), (float) getZ());
			
			for(int i = 0; i < 4; i++)
			{
				world.addParticle(ParticleTypes.POOF, true, pos.getX(), pos.getY(), pos.getZ(), velocity.getX(), velocity.getY(), velocity.getZ());
				pos.add((this.random.nextFloat() - this.random.nextFloat()) * 0.1F, 0.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F);
			}
		}
	}
	
	@Override
	protected void releaseBlocks()
	{
		float deltaYaw = this.getYaw();
		int rotationSteps = (int) Math.round(deltaYaw / 90.0f);
		ArrayList<MovingCraftBlockData> toPlaceFirst = new ArrayList<MovingCraftBlockData>();
		ArrayList<MovingCraftBlockData> toPlaceLast = new ArrayList<MovingCraftBlockData>();
		BlockRotation rotation = BlockRotation.NONE;
		
		for(int i = 0; i < rotationSteps; i++)
			rotation = rotation.rotate(BlockRotation.CLOCKWISE_90);
		
		for(MovingCraftBlockData blockData : blockDataList)
		{
			if(blockData.placeFirst())
				toPlaceFirst.add(blockData);
			else
				toPlaceLast.add(blockData);
		}
		
		for(MovingCraftBlockData blockData : toPlaceFirst)
			blockData.toBlock(this.getWorld(), this.getBlockPos(), rotationSteps);
		
		for(MovingCraftBlockData blockData : toPlaceLast)
			blockData.toBlock(this.getWorld(), this.getBlockPos(), rotationSteps);
		
		for(MovingCraftBlockData blockData : toPlaceFirst)
		{
			if(blockData.getStoredFluid() > 0)
			{
				BlockPos blockPos = this.getBlockPos().add(blockData.getPosition().rotate(rotation));
				BlockEntity blockEntity = world.getBlockEntity(blockPos);
				
				if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
				{
					FluidTankControllerBlockEntity fluidTank = (FluidTankControllerBlockEntity) blockEntity;
					((FluidTankControllerBlock) blockData.getBlockState().getBlock()).initializeFluidTank(world, blockPos, fluidTank);
					
					if(activeFluidTanks.contains(blockData.getPosition()))
					{
						if(blockData.getBlockState().getBlock() == StarflightBlocks.HYDROGEN_TANK)
							fluidTank.setStoredFluid(fluidTank.getStorageCapacity() * (hydrogenSupply / hydrogenCapacity));
						else if(blockData.getBlockState().getBlock() == StarflightBlocks.OXYGEN_TANK)
							fluidTank.setStoredFluid(fluidTank.getStorageCapacity() * (oxygenSupply / oxygenCapacity));
					}
					else
						fluidTank.setStoredFluid(blockData.getStoredFluid());
				}
				else if(blockEntity != null && blockEntity instanceof RocketControllerBlockEntity)
					((RocketControllerBlockEntity) blockEntity).runScan();
			}
		}
		
		this.setRemoved(RemovalReason.DISCARDED);
	}
	
	@Override
	protected Box calculateBoundingBox()
	{
        return new Box(getPos().add(-maxWidth, -lowerHeight, -maxWidth), getPos().add(maxWidth, upperHeight, maxWidth));
    }
	
	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putString("destination", nextDimension.getValue().toString());
		nbt.putInt("arrivalX", arrivalPos.getX());
		nbt.putInt("arrivalY", arrivalPos.getY());
		nbt.putInt("arrivalZ", arrivalPos.getZ());
		nbt.putInt("arrivalDirection", arrivalDirection);
		nbt.putBoolean("arrived", changedDimension);
		nbt.putDouble("mass", craftMass);
		nbt.putDouble("gravity", gravity);
		nbt.putDouble("nominalThrustStart", nominalThrustStart);
		nbt.putDouble("nominalThrustEnd", nominalThrustEnd);
		nbt.putDouble("throttle", throttle);
		nbt.putDouble("hydrogenCapacity", hydrogenCapacity);
		nbt.putDouble("oxygenCapacity", oxygenCapacity);
		nbt.putDouble("hydrogenSupply", hydrogenSupply);
		nbt.putDouble("oxygenSupply", oxygenSupply);
		nbt.putDouble("fuelToUse", fuelToUse);
		nbt.putDouble("lowerHeight", lowerHeight);
		nbt.putDouble("upperHeight", upperHeight);
		nbt.putDouble("maxWidth", maxWidth);
		
		int fuelTankCount = activeFluidTanks.size();
		nbt.putInt("fuelTankCount", fuelTankCount);
		int[] tx = new int[fuelTankCount];
		int[] ty = new int[fuelTankCount];
		int[] tz = new int[fuelTankCount];
		
		for(int i = 0; i < fuelTankCount; i++)
		{
			tx[i] = activeFluidTanks.get(i).getX();
			ty[i] = activeFluidTanks.get(i).getY();
			tz[i] = activeFluidTanks.get(i).getZ();
		}
		
		nbt.putIntArray("tx", tx);
		nbt.putIntArray("ty", ty);
		nbt.putIntArray("tz", tz);
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		nextDimension = RegistryKey.of(Registry.WORLD_KEY, new Identifier(nbt.getString("destination")));
		arrivalPos = new BlockPos(nbt.getInt("arrivalX"), nbt.getInt("arrivalY"), nbt.getInt("arrivalZ"));
		arrivalDirection = nbt.getInt("arrivalDirection");
		changedDimension = nbt.getBoolean("arrived");
		craftMass = nbt.getDouble("mass");
		gravity = nbt.getDouble("gravity");
		nominalThrustStart = nbt.getDouble("nominalThrustStart");
		nominalThrustEnd = nbt.getDouble("nominalThrustEnd");
		throttle = nbt.getDouble("throttle");
		hydrogenCapacity = nbt.getDouble("hydrogenCapacity");
		oxygenCapacity = nbt.getDouble("oxygenCapacity");
		hydrogenSupply = nbt.getDouble("hydrogenSupply");
		oxygenSupply = nbt.getDouble("oxygenSupply");
		fuelToUse = nbt.getDouble("fuelToUse");
		lowerHeight = nbt.getDouble("lowerHeight");
		upperHeight = nbt.getDouble("upperHeight");
		maxWidth = nbt.getDouble("maxWidth");
		
		int fuelTankCount = nbt.getInt("fuelTankCount");
		int[] tx = nbt.getIntArray("tx");
		int[] ty = nbt.getIntArray("ty");
		int[] tz = nbt.getIntArray("tz");
		
		for(int i = 0; i < fuelTankCount; i++)
			activeFluidTanks.add(new BlockPos(tx[i], ty[i], tz[i]));
	}
}