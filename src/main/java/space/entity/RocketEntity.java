package space.entity;

import java.util.ArrayList;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;
import space.block.FluidTankControllerBlock;
import space.block.RocketThrusterBlock;
import space.block.StarflightBlocks;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.HydrogenTankBlockEntity;
import space.block.entity.OxygenTankBlockEntity;
import space.block.entity.RocketControllerBlockEntity;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.AirUtil;
import space.util.StarflightEffects;
import space.vessel.BlockMass;
import space.vessel.MovingCraftBlockData;
import space.vessel.MovingCraftBlockRenderData;
import space.vessel.MovingCraftRenderList;

public class RocketEntity extends MovingCraftEntity
{
	private static final int TRAVEL_CEILING = 1024;
	private static final double ZERO_G_SPEED = 2.0;
	private static final double SG = 9.80665; // Standard gravity for ISP calculations.
	
	private static final TrackedData<Boolean> THRUST_UNDEREXPANDED = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Boolean> USER_INPUT = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Float> THROTTLE = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> ALTITUDE = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> OXYGEN_LEVEL = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> HYDROGEN_LEVEL = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.FLOAT);
	
	private ArrayList<BlockPos> activeFluidTanks = new ArrayList<BlockPos>();
	private RegistryKey<World> nextDimension;
	private BlockPos arrivalPos;
	private int arrivalDirection;
	private int throttleState;
	private int rollState;
	private int pitchState;
	private int yawState;
	private boolean changedDimension;
	private boolean userInput;
	private double craftMass;
	private double craftMassInitial;
	private double gravity;
	private double nominalThrustStart;
	private double nominalThrustEnd;
	private double startISP;
	private double endISP;
	private double hydrogenCapacity;
	private double oxygenCapacity;
	private double hydrogenSupply;
	private double oxygenSupply;
	private double fuelToUse;
	private double lowerHeight;
	private double upperHeight;
	private double maxWidth;
	public double throttle;
	public float throttlePrevious;
	private float rollSpeed;
	private float pitchSpeed;
	private float yawSpeed;
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
		this.userInput = false;
		this.fuelToUse = fuelToUse;
		this.arrivalPos = arrivalPos;
		this.arrivalDirection = arrivalDirection;
		setForwardDirection(forward.getHorizontal());
		setQuaternion(Vec3f.POSITIVE_Y.getRadialQuaternion((float) (forward.asRotation() * (Math.PI / 180.0))));
		Vec3d centerOfMass = Vec3d.ZERO;
		BlockPos min = new BlockPos(blockPosList.get(0));
		BlockPos max = new BlockPos(blockPosList.get(0));
		
		// Define maximum thrust values for the atmospheric pressure in the start dimension and the end dimension.
		PlanetDimensionData dataStart = PlanetList.getDimensionDataForWorld(world);
		PlanetDimensionData dataEnd = PlanetList.getDimensionDataForWorld(world.getServer().getWorld(nextDimension));
    	double atmosphereStart = dataStart.isOrbit() ? 0.0 : dataStart.getPressure();
    	double atmosphereEnd = dataEnd.isOrbit() ? 0.0 : dataEnd.getPressure();
    	double massFlowSumStart = 0;
    	double massFlowSumEnd = 0;
    	
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
        		double thrustStart = ((RocketThrusterBlock) block).getThrust(atmosphereStart);
        		double thrustEnd = ((RocketThrusterBlock) block).getThrust(atmosphereEnd);
        		nominalThrustStart += thrustStart;
        		nominalThrustEnd += thrustEnd;
        		massFlowSumStart += thrustStart / (SG * ((RocketThrusterBlock) block).getISP(atmosphereStart));
        		massFlowSumEnd += thrustEnd / (SG * ((RocketThrusterBlock) block).getISP(atmosphereEnd));
        	}
		}
		
		startISP = nominalThrustStart / massFlowSumStart;
		endISP = nominalThrustEnd / massFlowSumEnd;
		craftMassInitial = craftMass;
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
		this.dataTracker.startTracking(USER_INPUT, Boolean.valueOf(false));
		this.dataTracker.startTracking(THROTTLE, Float.valueOf(0.0f));
		this.dataTracker.startTracking(ALTITUDE, Float.valueOf(0.0f));
		this.dataTracker.startTracking(OXYGEN_LEVEL, Float.valueOf(0.0f));
		this.dataTracker.startTracking(HYDROGEN_LEVEL, Float.valueOf(0.0f));
	}
	
	public void setThrustUnderexpanded(boolean b)
	{
		this.dataTracker.set(THRUST_UNDEREXPANDED, Boolean.valueOf(b));
	}
	
	public void setUserInput(boolean b)
	{
		this.dataTracker.set(USER_INPUT, Boolean.valueOf(b));
	}
	
	public void setThrottle(float throttle)
	{
		this.dataTracker.set(THROTTLE, Float.valueOf(throttle));
	}
	
	public void setAltitude(float altitude)
	{
		this.dataTracker.set(ALTITUDE, Float.valueOf(altitude));
	}
	
	public void setOxygenLevel(float oxygenLevel)
	{
		this.dataTracker.set(OXYGEN_LEVEL, Float.valueOf(oxygenLevel));
	}
	
	public void setHydrogenLevel(float hydrogenLevel)
	{
		this.dataTracker.set(HYDROGEN_LEVEL, Float.valueOf(hydrogenLevel));
	}
	
	public boolean getThrustUnderexpanded()
	{
		return this.dataTracker.get(THRUST_UNDEREXPANDED);
	}
	
	public boolean getUserInput()
	{
		return this.dataTracker.get(USER_INPUT);
	}
	
	public float getThrottle()
	{
		return this.dataTracker.get(THROTTLE);
	}
	
	public float getAltitude()
	{
		return this.dataTracker.get(ALTITUDE);
	}
	
	public float getOxygenLevel()
	{
		return this.dataTracker.get(OXYGEN_LEVEL);
	}
	
	public float getHydrogenLevel()
	{
		return this.dataTracker.get(HYDROGEN_LEVEL);
	}
	
	@Override
    public void tick()
	{
		// Run client-side actions and then return.
		if(this.clientMotion())
		{
			// Spawn thruster particles.
			if(getThrottle() > 0.0f)
				spawnThrusterParticles();
			
			this.clientQuaternionPrevious = this.clientQuaternion;
			this.clientQuaternion = this.getCraftQuaternion();
	        this.throttlePrevious = (float) this.throttle;
			this.throttle = this.getThrottle();
			updateEulerAngles();
			return;
		}
		
		// Play the rocket engine sound effect.
		if(getThrottle() > 0.0f && soundEffectTimer <= 0)
		{
			playSound(StarflightEffects.THRUSTER_SOUND_EVENT, 1e6F, 0.8F + this.random.nextFloat() * 0.01f);
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
		
		updateEulerAngles();
		
		// Automatically control thrust according to either launch or landing behavior. Otherwise, apply manual control from the player.
		if(!userInput)
		{
			if(changedDimension)
				verticalLandingAutoThrottle();
			else
				launchAnimation();
		}
		else
			applyUserInput();
		
		applyGravity();
		applyThrust();
		
		move(MovementType.SELF, getVelocity());
		setBoundingBox(calculateBoundingBox());
		fallDistance = 0.0f;
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
		
		// Move to the next dimension when a high enough altitude is reached.
		if(this.getBlockPos().getY() > TRAVEL_CEILING && !changedDimension)
		{
			// Use hydrogen and oxygen at the 1:8 ratio for water.
			fuelToUse -= craftMassInitial - craftMass;
			double factor = (hydrogenSupply + oxygenSupply - fuelToUse) / (hydrogenSupply + oxygenSupply);
			hydrogenSupply *= factor;
			oxygenSupply *= factor;
			craftMass -= fuelToUse;
			setVelocity(0.0, data.isOrbit() ? -ZERO_G_SPEED : -ZERO_G_SPEED / 2.0, 0.0);
			changedDimension = true;
			userInput = false;
			float arrivalYaw = (Direction.fromHorizontal(arrivalDirection).asRotation() - getForwardDirection().getOpposite().asRotation()) * (float) (Math.PI / 180.0);
			this.changeDimension(world.getServer().getWorld(nextDimension), new Vec3d(arrivalPos.getX() + 0.5, getY(), arrivalPos.getZ() + 0.5), arrivalYaw);
			return;
		}
		
		// Turn back into blocks when landed.
		if(this.age > 10 && (verticalCollision || horizontalCollision || (changedDimension && gravity == 0.0 && (this.getBlockPos().getY() - lowerHeight <= arrivalPos.getY() || getVelocity().getY() >= -0.01))))
		{
			Vec3f trackedVelocity = getTrackedVelocity();
			float craftSpeed = (float) MathHelper.magnitude(trackedVelocity.getX(), trackedVelocity.getY(), trackedVelocity.getZ()) * 20.0f; // Get the craft's speed in m/s.
			boolean crashLandingFlag = false;
			
			// Crash landing effects go here.
			if((verticalCollision || horizontalCollision) && craftSpeed > 10.0)
			{
				BlockPos bottom = getBlockPos().add(0, -lowerHeight, 0);
				float power = Math.min(craftSpeed / 5.0f, 10.0f);
				boolean fire = !data.isOrbit() && data.hasOxygen();
				int count = random.nextBetween(4, 5);
				
				world.createExplosion(null, bottom.getX() + 0.5, bottom.getY() + 0.5, bottom.getZ() + 0.5, power, fire, DestructionType.DESTROY);
				
				/*for(int i = 0; i < count; i++)
				{
					Vec3d offset = new Vec3d(maxWidth * random.nextDouble(), 3.0 * (0.5 - random.nextDouble()), 0.0);
					offset = offset.rotateY((float) Math.PI * 2.0f * random.nextFloat());
					world.createExplosion(null, bottom.getX() + 0.5 + offset.getX(), bottom.getY() + 0.5 + offset.getY(), bottom.getZ() + 0.5 + offset.getZ(), power, fire, DestructionType.DESTROY);
				}*/
				
				crashLandingFlag = true;
			}
			
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
			
			if(crashLandingFlag)
			{
				int yOffset = random.nextBetween(MathHelper.floor(craftSpeed / 10.0f), MathHelper.floor(craftSpeed / 10.0f) + 1) * (trackedVelocity.getY() > 0.0f ? 1 : -1);
				this.setPosition(getPos().add(0.0, yOffset, 0.0));
			}
			
			sendRenderData(true);
			this.releaseBlocks();
		}
		else
			sendRenderData(false);
		
		// Update thruster state tracked data.
		setThrustUnderexpanded(AirUtil.getAirResistanceMultiplier(world, PlanetList.getDimensionDataForWorld(world), getBlockPos()) > 0.25);
		setThrottle((float) throttle);
		int yCheck = world.getTopY();
		
		// Monitor the current altitude.
		while(!world.getBlockState(new BlockPos(getX(), yCheck, getZ())).getMaterial().blocksMovement() && yCheck > world.getBottomY())
			yCheck--;
		
		if(gravity == 0.0 && yCheck < arrivalPos.getY())
			yCheck = arrivalPos.getY();
		
		setUserInput(userInput);
		setAltitude((float) (getY() - lowerHeight - yCheck));
		setTrackedVelocity(new Vec3f(getVelocity()));
		setOxygenLevel((float) (oxygenSupply / oxygenCapacity));
		setHydrogenLevel((float) (hydrogenSupply / hydrogenCapacity));
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
		if(throttle == 0.0 || oxygenSupply <= 0.0 || hydrogenSupply <= 0.0)
			return;
		
		double force = changedDimension ? nominalThrustEnd * throttle : nominalThrustStart * throttle;
		double acc = (force / craftMass) * 0.0025;
		Vec3f direction = Vec3f.POSITIVE_Y.copy();
		/*float rotationRoll = getCraftRoll();
		float rotationPitch = getCraftPitch();
		float rotationYaw = getCraftYaw();
		
		switch(getForwardDirection())
		{
		case NORTH:
			direction.rotate(Vec3f.NEGATIVE_Z.getRadialQuaternion(rotationRoll));
			direction.rotate(Vec3f.NEGATIVE_X.getRadialQuaternion(rotationPitch));
			direction.rotate(Vec3f.NEGATIVE_Y.getRadialQuaternion(rotationYaw));
			break;
		case EAST:
			direction.rotate(Vec3f.POSITIVE_X.getRadialQuaternion(rotationRoll));
			direction.rotate(Vec3f.POSITIVE_Z.getRadialQuaternion(rotationPitch));
			direction.rotate(Vec3f.POSITIVE_Y.getRadialQuaternion(rotationYaw));
			break;
		case SOUTH:
			direction.rotate(Vec3f.POSITIVE_Z.getRadialQuaternion(rotationRoll));
			direction.rotate(Vec3f.POSITIVE_X.getRadialQuaternion(rotationPitch));
			direction.rotate(Vec3f.POSITIVE_Y.getRadialQuaternion(rotationYaw));
			break;
		case WEST:
			direction.rotate(Vec3f.NEGATIVE_X.getRadialQuaternion(rotationRoll));
			direction.rotate(Vec3f.NEGATIVE_Z.getRadialQuaternion(rotationPitch));
			direction.rotate(Vec3f.NEGATIVE_Y.getRadialQuaternion(rotationYaw));
			break;
		default:
			break;
		}*/
		
		Quaternion quaternion = getCraftQuaternion();
		direction.rotate(quaternion);
        this.addVelocity(direction.getX() * acc, direction.getY() * acc, direction.getZ() * acc);
        
        // Decrease mass and fuel supply.
        double totalMassFlow = (force / (SG * (changedDimension ? endISP : startISP))) * 0.05;
        craftMass -= totalMassFlow;
        double factor = (hydrogenSupply + oxygenSupply - totalMassFlow) / (hydrogenSupply + oxygenSupply);
		hydrogenSupply *= factor;
		oxygenSupply *= factor;
	}
	
	/**
	 * Change the throttle and rotation according to user input.
	 */
	private void applyUserInput()
	{
		if(throttleState == 1)
			throttle += 0.01;
		else if(throttleState == -1)
			throttle -= 0.01;
		else if(throttleState == 2)
			throttle = 1.0;
		else if(throttleState == -2)
			throttle = 0.0;
		
		if(throttle < 0.0)
			throttle = 0.0;
		else if(throttle > 1.0)
			throttle = 1.0;
		
		// Invert the controls for north and west front directions.
		float rotationRate = 0.001f;
		float rollRate = rotationRate;
		float pitchRate = (getForwardDirection() == Direction.NORTH || getForwardDirection() == Direction.SOUTH) ? -rotationRate : rotationRate;
		float yawRate = 2.0f * ((getForwardDirection() == Direction.NORTH || getForwardDirection() == Direction.WEST) ? -rotationRate : rotationRate);
		
		if(rollState == 1)
			rollSpeed += rollRate;
		else if(rollState == -1)
			rollSpeed -= rollRate;
		else
			rollSpeed *= 0.9f;
		
		if(pitchState == 1)
			pitchSpeed += pitchRate;
		else if(pitchState == -1)
			pitchSpeed -= pitchRate;
		else
			pitchSpeed *= 0.9f;
		
		if(yawState == 1)
			yawSpeed += yawRate;
		else if(yawState == -1)
			yawSpeed -= yawRate;
		else
			yawSpeed *= 0.9f;
		
		if(MathHelper.abs(rollSpeed) < 0.0001f)
			rollSpeed = 0.0f;
		
		if(MathHelper.abs(pitchSpeed) < 0.0001f)
			pitchSpeed = 0.0f;
		
		if(MathHelper.abs(yawSpeed) < 0.0001f)
			yawSpeed = 0.0f;
		
		Quaternion quaternion = getCraftQuaternion();
		Quaternion speedQuaternion = Quaternion.fromEulerXyz(rollSpeed, pitchSpeed, yawSpeed);
		quaternion.hamiltonProduct(speedQuaternion);
		setQuaternion(quaternion);
		
		/*if(rollSpeed != 0.0f || pitchSpeed != 0.0f || yawSpeed != 0.0f)
		{
			quaternion.hamiltonProduct(speedQuaternion);
			setQuaternion(quaternion);
		}
		else
		{
			System.out.println(quaternion.toString());
			System.out.println(speedQuaternion.toString());
		}*/
		
		System.out.println(quaternion.toString());
	}
	
	/**
	 * Update the value of gravitational acceleration per tick.
	 */
	public void storeGravity()
	{
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
		
		if(data == null)
			gravity = 9.80665 * 0.0025;
		else if(data.isOrbit())
		{
			gravity = 0.0;
			return;
		}
		else
			gravity = 9.80665 * 0.0025 * data.getPlanet().getSurfaceGravity();
	}
	
	private void launchAnimation()
	{
		if(gravity == 0.0)
		{
			if(getVelocity().getY() < ZERO_G_SPEED)
				throttle = 1.0;
			else
				throttle = 0.0;
			
			if(getVelocity().getY() > ZERO_G_SPEED)
				setVelocity(0.0, ZERO_G_SPEED, 0.0);
		}
		else
		{
			// The maximum G-force a rocket is allowed to experience during launch. Used to lower the throttle if necessary.
			// A lower constraint of 4m/s^2 is applied.
			double maxG = (gravity / 0.0025) * 2.0 < 4.0 ? 4.0 : (gravity / 0.0025) * 2.0;
			
			if((changedDimension ? nominalThrustEnd : nominalThrustStart) / craftMass > maxG)
				throttle = changedDimension ? (craftMass * maxG) / nominalThrustEnd : (craftMass * maxG) / nominalThrustStart;
			else
				throttle = 1.0;
		}
	}
	
	/**
	 * Activate the thrusters at the correct altitude.
	 */
	private void verticalLandingAutoThrottle()
	{
		double vy = getVelocity().getY(); // Vertical velocity in m/tick.
		double currentHeight = (getPos().getY() - lowerHeight) - arrivalPos.getY();
		
		// Find the necessary throttle to cancel vertical velocity.
		double t = Math.min(((Math.pow(vy, 2.0) * 0.5) + (gravity * currentHeight)) / (((nominalThrustEnd / craftMass) * 0.0025) * currentHeight), 1.0);
		
		// Activate the engines if the necessary throttle is significantly high and the vehicle has a little bit of altitude.
		if(gravity > 0.0)
		{
			if((t > 0.8 || throttle > 0.0) && currentHeight > 0.15)
				throttle = t;
			else
				throttle = 0.0;
		}
		else
			throttle = t > 0.8 && currentHeight > 0.1 ? t : 0.0;
	}
	
	private void spawnThrusterParticles()
	{
		ArrayList<MovingCraftBlockRenderData> blocks = MovingCraftRenderList.getBlocksForEntity(getUuid());
		ArrayList<BlockPos> thrusterOffsets = new ArrayList<BlockPos>();
		ArrayList<Vec3f> thrusterOffsetsRotated = new ArrayList<Vec3f>();
		Vec3f upAxis = new Vec3f(0.0f, 1.0f, 0.0f);
        //float rotationRoll = getCraftRoll();
		//float rotationPitch = getCraftPitch();
		//float rotationYaw = getCraftYaw();
		
		for(MovingCraftBlockRenderData block : blocks)
		{
			if(block.getBlockState().getBlock() instanceof RocketThrusterBlock)
				thrusterOffsets.add(block.getPosition());
		}
		
		/*switch(getForwardDirection())
		{
		case NORTH:
			upAxis.rotate(Vec3f.NEGATIVE_Z.getRadialQuaternion(rotationRoll));
			upAxis.rotate(Vec3f.NEGATIVE_X.getRadialQuaternion(rotationPitch));
			upAxis.rotate(Vec3f.NEGATIVE_Y.getRadialQuaternion(rotationYaw));
			
			for(BlockPos pos : thrusterOffsets)
			{
				Vec3f rotated = new Vec3f(pos.getX(), pos.getY() - 1.0f, pos.getZ());
				rotated.rotate(Vec3f.NEGATIVE_Z.getRadialQuaternion(rotationRoll));
				rotated.rotate(Vec3f.NEGATIVE_X.getRadialQuaternion(rotationPitch));
				rotated.rotate(Vec3f.NEGATIVE_Y.getRadialQuaternion(rotationYaw));
				thrusterOffsetsRotated.add(rotated);
			}
			break;
		case EAST:
			upAxis.rotate(Vec3f.POSITIVE_X.getRadialQuaternion(rotationRoll));
			upAxis.rotate(Vec3f.POSITIVE_Z.getRadialQuaternion(rotationPitch));
			upAxis.rotate(Vec3f.POSITIVE_Y.getRadialQuaternion(rotationYaw));
			
			for(BlockPos pos : thrusterOffsets)
			{
				Vec3f rotated = new Vec3f(pos.getX(), pos.getY() - 1.0f, pos.getZ());
				rotated.rotate(Vec3f.POSITIVE_X.getRadialQuaternion(rotationRoll));
				rotated.rotate(Vec3f.POSITIVE_Z.getRadialQuaternion(rotationPitch));
				rotated.rotate(Vec3f.POSITIVE_Y.getRadialQuaternion(rotationYaw));
				thrusterOffsetsRotated.add(rotated);
			}
			break;
		case SOUTH:
			upAxis.rotate(Vec3f.POSITIVE_Z.getRadialQuaternion(rotationRoll));
			upAxis.rotate(Vec3f.POSITIVE_X.getRadialQuaternion(rotationPitch));
			upAxis.rotate(Vec3f.POSITIVE_Y.getRadialQuaternion(rotationYaw));
			
			for(BlockPos pos : thrusterOffsets)
			{
				Vec3f rotated = new Vec3f(pos.getX(), pos.getY() - 1.0f, pos.getZ());
				rotated.rotate(Vec3f.POSITIVE_Z.getRadialQuaternion(rotationRoll));
				rotated.rotate(Vec3f.POSITIVE_X.getRadialQuaternion(rotationPitch));
				rotated.rotate(Vec3f.POSITIVE_Y.getRadialQuaternion(rotationYaw));
				thrusterOffsetsRotated.add(rotated);
			}
			break;
		case WEST:
			upAxis.rotate(Vec3f.NEGATIVE_X.getRadialQuaternion(rotationRoll));
			upAxis.rotate(Vec3f.NEGATIVE_Z.getRadialQuaternion(rotationPitch));
			upAxis.rotate(Vec3f.NEGATIVE_Y.getRadialQuaternion(rotationYaw));
			
			for(BlockPos pos : thrusterOffsets)
			{
				Vec3f rotated = new Vec3f(pos.getX(), pos.getY() - 1.0f, pos.getZ());
				rotated.rotate(Vec3f.NEGATIVE_X.getRadialQuaternion(rotationRoll));
				rotated.rotate(Vec3f.NEGATIVE_Z.getRadialQuaternion(rotationPitch));
				rotated.rotate(Vec3f.NEGATIVE_Y.getRadialQuaternion(rotationYaw));
				thrusterOffsetsRotated.add(rotated);
			}
			break;
		default:
			break;
		}*/
		
		Quaternion quaternion = getCraftQuaternion();
		upAxis.rotate(quaternion);
		
		for(BlockPos pos : thrusterOffsets)
		{
			Vec3f rotated = new Vec3f(pos.getX(), pos.getY() - 1.0f, pos.getZ());
			rotated.rotate(quaternion);
			thrusterOffsetsRotated.add(rotated);
		}
		
		Vec3f velocity = upAxis.copy();
		velocity.scale(-4.0f + (this.random.nextFloat() * 0.5f));
		velocity.add((float) getVelocity().getX(), (float) getVelocity().getY(), (float) getVelocity().getZ());
		
		for(Vec3f pos : thrusterOffsetsRotated)
		{
			pos.add((float) getX(), (float) getY(), (float) getZ());
			
			for(int i = 0; i < 4; i++)
			{
				world.addParticle(ParticleTypes.POOF, true, pos.getX(), pos.getY(), pos.getZ(), velocity.getX(), velocity.getY(), velocity.getZ());
				pos.add((this.random.nextFloat() - this.random.nextFloat()) * 0.1f, 0.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.1f);
			}
		}
	}
	
	@Override
	protected void releaseBlocks()
	{
		int rotationSteps = getRotationSteps();
		ArrayList<MovingCraftBlockData> toPlaceFirst = new ArrayList<MovingCraftBlockData>();
		ArrayList<MovingCraftBlockData> toPlaceLast = new ArrayList<MovingCraftBlockData>();
		BlockRotation rotation = BlockRotation.NONE;
		
		// Snap the rotation of this entity into place and then dismount all passengers.
		this.fromEulerAngles(0.0f, 0.0f, rotationSteps * (float) (Math.PI / 2.0));
		
		for(Entity passenger : this.getPassengerList())
		{
			updatePassengerPosition(passenger);
			passenger.setPosition(passenger.getPos().add(0.0, (gravity > 0.0 ? 0.5 : 0.0) + getVelocity().getY(), 0.0));
			passenger.setVelocity(Vec3d.ZERO);
			passenger.velocityModified = true;
			passenger.fallDistance = 0.0f;
			passenger.dismountVehicle();
		}
		
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
		nbt.putBoolean("userInput", userInput);
		nbt.putFloat("rollSpeed", rollSpeed);
		nbt.putFloat("pitchSpeed", pitchSpeed);
		nbt.putFloat("yawSpeed", yawSpeed);
		nbt.putDouble("mass", craftMass);
		nbt.putDouble("massInitial", craftMassInitial);
		nbt.putDouble("gravity", gravity);
		nbt.putDouble("nominalThrustStart", nominalThrustStart);
		nbt.putDouble("nominalThrustEnd", nominalThrustEnd);
		nbt.putDouble("startISP", startISP);
		nbt.putDouble("endISP", endISP);
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
		userInput = nbt.getBoolean("userInput");
		rollSpeed = nbt.getFloat("rollSpeed");
		pitchSpeed = nbt.getFloat("pitchSpeed");
		yawSpeed = nbt.getFloat("yawSpeed");
		craftMass = nbt.getDouble("mass");
		craftMassInitial = nbt.getDouble("massInitial");
		gravity = nbt.getDouble("gravity");
		nominalThrustStart = nbt.getDouble("nominalThrustStart");
		nominalThrustEnd = nbt.getDouble("nominalThrustEnd");
		startISP = nbt.getDouble("startISP");
		endISP = nbt.getDouble("endISP");
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
	
	public static void receiveInput(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender)
	{
		int throttleState = buffer.readInt();
		int rollState = buffer.readInt();
		int pitchState = buffer.readInt();
		int yawState = buffer.readInt();
		Entity entity = player.getVehicle();
		
		if(entity instanceof RocketEntity)
		{
			RocketEntity rocketEntity = (RocketEntity) entity;
			rocketEntity.throttleState = throttleState;
			rocketEntity.rollState = rollState;
			rocketEntity.pitchState = pitchState;
			rocketEntity.yawState = yawState;
			
			if((throttleState != 0 || rollState != 0 || pitchState != 0 || yawState != 0) && rocketEntity.gravity > 0.0)
				rocketEntity.userInput = true;
		}
	}
}