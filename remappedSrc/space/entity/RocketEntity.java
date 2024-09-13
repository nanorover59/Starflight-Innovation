 package space.entity;

import java.util.ArrayList;

import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.StarflightMod;
import space.block.FluidTankControllerBlock;
import space.block.ReactionControlThrusterBlock;
import space.block.ReactionWheelBlock;
import space.block.RocketThrusterBlock;
import space.block.StarflightBlocks;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.RocketControllerBlockEntity;
import space.client.gui.SpaceNavigationScreen;
import space.particle.StarflightParticleTypes;
import space.planet.Planet;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.QuaternionUtil;
import space.util.StarflightEffects;
import space.util.VectorUtil;
import space.vessel.MovingCraftBlockData;
import space.vessel.MovingCraftBlockRenderData;
import space.vessel.MovingCraftRenderList;

public class RocketEntity extends MovingCraftEntity
{
	private static final int TRAVEL_CEILING = 1024;
	private static final int TRAVEL_CEILING_ORBIT = 512;
	private static final int PARKING_HEIGHT_ORBIT = 64;
	private static final double ZERO_G_SPEED = 2.0;
	private static final double SG = 9.80665; // Standard gravity for ISP calculations.
	
	private static final TrackedData<Boolean> USER_INPUT = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Integer> HOLD_STOP = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Float> THROTTLE = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> ALTITUDE = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> OXYGEN_LEVEL = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> HYDROGEN_LEVEL = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Vector3f> ATTITUDE_CONTROL = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	private static final TrackedData<Vector3f> TRANSLATION_CONTROL = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	
	private ArrayList<Thruster> mainThrusters = new ArrayList<Thruster>();
	private ArrayList<Thruster> rcsThrusters = new ArrayList<Thruster>();
	private BlockPos arrivalPos;
	private int arrivalDirection;
	private double nominalThrust;
	private double nominalISP;
	private double averageVEVacuum;
	private double hydrogenSupply;
	private double hydrogenCapacity;
	private double oxygenSupply;
	private double oxygenCapacity;
	private double lowerHeight;
	private double upperHeight;
	private double maxWidth;
	private float rollTorque;
	private float pitchTorque;
	private float yawTorque;
	private float rollControl;
	private float pitchControl;
	private float yawControl;
	private float rollPreviousError;
	private float pitchPreviousError;
	private float yawPreviousError;
	private float rollIntegral;
	private float pitchIntegral;
	private float yawIntegral;
	private float xControl;
	private float yControl;
	private float zControl;
	public double throttle;
	public float throttlePrevious;
	private int autoState;
	private int soundEffectTimer;
	private boolean pausePhysics;
	
	public RocketEntity(EntityType<? extends RocketEntity> entityType, World world)
	{
        super(entityType, world);
    }
	
	public RocketEntity(World world, BlockPos blockPos, ArrayList<MovingCraftBlockData> blockDataList, Direction forward, double mass, Vector3f momentOfInertia1, Vector3f momentOfInertia2, double hydrogenSupply, double hydrogenCapacity, double oxygenSupply, double oxygenCapacity)
	{
		super(StarflightEntities.ROCKET, world, blockPos, blockDataList, mass, momentOfInertia1, momentOfInertia2);
		this.pausePhysics = false;
		this.autoState = 1;
		this.arrivalDirection = forward.getHorizontal();
		this.arrivalPos = new BlockPos(blockPos.getX(), -9999, blockPos.getZ());
		this.hydrogenSupply = hydrogenSupply;
		this.hydrogenCapacity = hydrogenCapacity;
		this.oxygenSupply = oxygenSupply;
		this.oxygenCapacity = oxygenCapacity;
		setForwardDirection(forward.getHorizontal());
		BlockPos min = new BlockPos(blockDataList.get(0).getPosition());
		BlockPos max = new BlockPos(blockDataList.get(0).getPosition());
    	
		for(MovingCraftBlockData blockData : blockDataList)
		{
			BlockPos pos = blockData.getPosition();
			
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
		}
		
		lowerHeight = -min.getY();
		upperHeight = max.getY();
		maxWidth = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
		Box box = new Box(min, max.up()).offset(blockPos);
		
		for(Entity entity : world.getOtherEntities(this, box))
		{
			if(entity instanceof LivingEntity)
			{
				BlockPos offset = entity.getBlockPos().subtract(blockPos);
				pickUpEntity(entity, offset);
			}
		}
		
		initializePropulsion();
	}
	
	@Override
	protected Box calculateBoundingBox()
	{
        return new Box(getPos().add(-maxWidth, -lowerHeight, -maxWidth), getPos().add(maxWidth, upperHeight, maxWidth));
    }
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		this.dataTracker.startTracking(USER_INPUT, Boolean.valueOf(false));
		this.dataTracker.startTracking(HOLD_STOP, Integer.valueOf(0));
		this.dataTracker.startTracking(THROTTLE, Float.valueOf(0.0f));
		this.dataTracker.startTracking(ALTITUDE, Float.valueOf(0.0f));
		this.dataTracker.startTracking(OXYGEN_LEVEL, Float.valueOf(0.0f));
		this.dataTracker.startTracking(HYDROGEN_LEVEL, Float.valueOf(0.0f));
		this.dataTracker.startTracking(ATTITUDE_CONTROL, new Vector3f());
		this.dataTracker.startTracking(TRANSLATION_CONTROL, new Vector3f());
	}
	
	public void setUserInput(boolean b)
	{
		this.dataTracker.set(USER_INPUT, Boolean.valueOf(b));
	}
	
	public void setHoldStop(Integer i)
	{
		this.dataTracker.set(HOLD_STOP, Integer.valueOf(i));
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
	
	public void setAttitudeControl(float rollControl, float pitchControl, float yawControl)
	{
		this.dataTracker.set(ATTITUDE_CONTROL, new Vector3f(rollControl, pitchControl, yawControl));
	}
	
	public void setTranslationControl(float xControl, float yControl, float zControl)
	{
		this.dataTracker.set(TRANSLATION_CONTROL, new Vector3f(xControl, yControl, zControl));
	}
	
	public boolean getUserInput()
	{
		return this.dataTracker.get(USER_INPUT);
	}
	
	public int getHoldStop()
	{
		return this.dataTracker.get(HOLD_STOP);
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
	
	public Vector3f getAttitudeControl()
	{
		return this.dataTracker.get(ATTITUDE_CONTROL);
	}
	
	public Vector3f getTranslationControl()
	{
		return this.dataTracker.get(TRANSLATION_CONTROL);
	}
	
	@Override
    public void tick()
	{
		// Run client-side actions and then return.
		if(this.clientMotion())
		{
			// Spawn thruster particles.
			spawnThrusterParticles();
			
			this.clientQuaternionPrevious = this.clientQuaternion;
			this.clientQuaternion = this.getQuaternion();
	        this.throttlePrevious = (float) this.throttle;
			this.throttle = this.getThrottle();
			return;
		}
		
		if(pausePhysics)
			return;
		
		// Play the rocket engine sound effect.
		if(getThrottle() > 0.0f && soundEffectTimer <= 0)
		{
			playSound(StarflightEffects.THRUSTER_SOUND_EVENT, 1e6F, 0.8F + this.random.nextFloat() * 0.01f);
			soundEffectTimer = 5;
		}
		else
			soundEffectTimer--;
		
		tickPhysics();
		checkDimensionChange();
		
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(method_48926());
		Vector3f trackedVelocity = getTrackedVelocity();
		float craftSpeed = (float) MathHelper.magnitude(trackedVelocity.x(), trackedVelocity.y(), trackedVelocity.z()) * 20.0f; // Get the craft's speed in m/s.
		
		// Turn back into blocks when landed.
		if(this.age > 10 && (verticalCollision || horizontalCollision || ((autoState == 2 || getHoldStop() > 20) && data.isOrbit() && craftSpeed < 2.0)))
		{
			boolean crashLandingFlag = false;
			
			// Crash landing effects go here.
			if((verticalCollision || horizontalCollision) && craftSpeed > 10.0)
			{
				BlockPos bottom = getBlockPos().add(0, (int) -lowerHeight, 0);
				float power = Math.min(craftSpeed / 5.0f, 10.0f);
				int count = random.nextBetween(4, 5);
				
				method_48926().createExplosion(this, bottom.getX() + 0.5, bottom.getY() + 0.5, bottom.getZ() + 0.5, power, false, World.ExplosionSourceType.NONE);
				
				/*for(int i = 0; i < count; i++)
				{
					Vec3d offset = new Vec3d(maxWidth * random.nextDouble(), 3.0 * (0.5 - random.nextDouble()), 0.0);
					offset = offset.rotateY((float) Math.PI * 2.0f * random.nextFloat());
					world.createExplosion(null, bottom.getX() + 0.5 + offset.getX(), bottom.getY() + 0.5 + offset.getY(), bottom.getZ() + 0.5 + offset.getZ(), power, fire, DestructionType.DESTROY);
				}*/
				
				crashLandingFlag = true;
			}
			
			// Extra displacement to avoid clipping into the ground.
			/*if(verticalCollision)
			{
				for(int i = 0; i < 128; i++)
				{
					if(!verticalCollision)
						break;
					
					this.move(MovementType.SELF, new Vec3d(0.0, 0.1, 0.0));
				}
			}*/
			
			if(crashLandingFlag)
			{
				int yOffset = random.nextBetween(MathHelper.floor(craftSpeed / 10.0f), MathHelper.floor(craftSpeed / 10.0f) + 1) * (trackedVelocity.y() > 0.0f ? 1 : -1);
				this.setPosition(getPos().add(0.0, yOffset, 0.0));
			}
			
			sendRenderData(true);
			this.releaseBlocks();
		}
		else
			sendRenderData(false);
		
		// Update thruster state tracked data.
		setThrottle((float) throttle);
		setUserInput(autoState == 0);
		setAltitude((float) (getY() - lowerHeight - arrivalPos.getY()));
		setTrackedVelocity(getVelocity().toVector3f());
		setTrackedAngularVelocity(getAngularVelocity());
		setOxygenLevel((float) (oxygenSupply / oxygenCapacity));
		setHydrogenLevel((float) (hydrogenSupply / hydrogenCapacity));
		setAttitudeControl(rollControl, pitchControl, yawControl);
		setTranslationControl(xControl, yControl, zControl);
	}
	
	/**
	 * Initialize parameters for rocket propulsion.
	 */
	public void initializePropulsion()
	{
		PlanetDimensionData planetDimensionData = PlanetList.getDimensionDataForWorld(method_48926());
		double pressure = planetDimensionData.getPressure();
		double massFlowSumVacuum = 0.0;
		double massFlowSum = 0.0;
		double nominalThrustVacuum = 0.0;
		nominalThrust = 0.0;
		mainThrusters.clear();
		rcsThrusters.clear();
		rollTorque = 30e3f;
		pitchTorque = 30e3f;
		yawTorque = 30e3f;
		
		for(MovingCraftBlockData blockData : blockDataList)
		{
			BlockState blockState = blockData.getBlockState();
			Block block = blockState.getBlock();
			BlockPos blockPos = blockData.getPosition();
			
			if(block instanceof RocketThrusterBlock && !blockData.redstonePower())
        	{
				double thrustVacuum = ((RocketThrusterBlock) block).getThrust(0.0);
        		double thrust = ((RocketThrusterBlock) block).getThrust(pressure);
        		double ispVacuum = ((RocketThrusterBlock) block).getISP(0.0);
        		double isp = ((RocketThrusterBlock) block).getISP(pressure);
        		mainThrusters.add(new Thruster(new Vector3f(blockPos.getX(), blockPos.getY(), blockPos.getZ()), new Vector3f(0.0f, -1.0f, 0.0f), thrust, isp));
        		nominalThrustVacuum += thrustVacuum;
        		nominalThrust += thrust;
        		massFlowSumVacuum += thrustVacuum / (SG * ispVacuum);
        		massFlowSum += thrust / (SG * isp);
        	}
			else if(block instanceof ReactionControlThrusterBlock)
			{
				Quaternionf blockFacingQuaternion = blockState.get(FacingBlock.FACING).getRotationQuaternion();
				ReactionControlThrusterBlock rcsBlock = (ReactionControlThrusterBlock) block;
				
				for(Pair<Vector3f, Vector3f> thruster : rcsBlock.getThrusters())
				{
					Vector3f position = new Vector3f(thruster.getLeft()).rotate(blockFacingQuaternion).add(blockPos.getX(), blockPos.getY(), blockPos.getZ());
					Vector3f direction = new Vector3f(thruster.getRight()).rotate(blockFacingQuaternion);
					rcsThrusters.add(new Thruster(position, direction, 10000.0, 280.0));
				}
			}
			else if(block instanceof ReactionWheelBlock)
			{
				float torque = ((ReactionWheelBlock) block).getTorque();
				Direction direction = blockState.get(FacingBlock.FACING);
				boolean b = getForwardDirection() == Direction.NORTH || getForwardDirection() == Direction.SOUTH;
				
				if(direction == Direction.NORTH || direction == Direction.SOUTH)
				{
					if(b)
						rollTorque += torque;
					else
						pitchTorque += torque;
				}
				else if(direction == Direction.EAST || direction == Direction.WEST)
				{
					if(b)
						pitchTorque += torque;
					else
						rollTorque += torque;
				}
				else if(direction == Direction.UP || direction == Direction.DOWN)
					yawTorque += torque;
			}
		}
		
		nominalISP = nominalThrust / massFlowSum;
		averageVEVacuum = SG * (nominalThrustVacuum / massFlowSumVacuum);
	}

	/**
	 * Get the currently available delta-V.
	 */
	private double getDeltaV()
	{
		double fuelMass = Math.min(oxygenSupply + (oxygenSupply / 8.0), hydrogenSupply + (hydrogenSupply * 8.0));
		double finalMass = getMass() - fuelMass;
		return averageVEVacuum * Math.log(getMass() / finalMass);
	}
	
	/**
	 * Deplete fuel for a given amount of delta-V.
	 */
	private void useDeltaV(double deltaV)
	{
		double fuelToUse = getMass() - (getMass() * Math.exp(-deltaV / averageVEVacuum));
		double hydrogenToUse = fuelToUse * (1.0 / 9.0);
        double oxygenToUse = fuelToUse * (8.0 / 9.0);
		hydrogenSupply -= hydrogenToUse;
		oxygenSupply -= oxygenToUse;
		changeMass(-fuelToUse);
	}
	
	private void getStabilityAssist(float rollTorqueMax, float pitchTorqueMax, float yawTorqueMax)
	{
		Vector3f av = getAngularVelocity();
		boolean b = getForwardDirection() == Direction.NORTH || getForwardDirection() == Direction.SOUTH;
		float roll = b ? av.z() : av.x();
		float pitch = b ? av.x() : av.z();
		float yaw = av.y();
		
		float p = 1000.0f;
		float i = 0.0f;
		float d = 0.0f;
		float rollError = roll;
		float pitchError = pitch;
		float yawError = yaw;
		rollIntegral = rollIntegral + rollError * 0.05f;
		pitchIntegral = pitchIntegral + pitchError * 0.05f;
		yawIntegral = yawIntegral + yawError * 0.05f;
		
		if(rollControl == 0.0f && roll != 0.0f)
		{
			float derivative = (rollError - rollPreviousError) / 0.05f;
			rollControl = MathHelper.clamp(p * rollError + i * rollIntegral + d * derivative, -1.0f, 1.0f);
			rollPreviousError = rollError;
			
		}
		
		if(pitchControl == 0.0f && pitch != 0.0f)
		{
			float derivative = (pitchError - pitchPreviousError) / 0.05f;
			pitchControl = MathHelper.clamp(p * pitchError + i * pitchIntegral + d * derivative, -1.0f, 1.0f);
			pitchPreviousError = pitchError;
		}
		
		if(yawControl == 0.0f && yaw != 0.0f)
		{
			float derivative = (yawError - yawPreviousError) / 0.05f;
			yawControl = MathHelper.clamp(p * yawError + i * yawIntegral + d * derivative, -1.0f, 1.0f);
			yawPreviousError = yawError;
		}
	}
	
	private boolean checkRCS(Vector3f position, Vector3f direction, float rollControl, float pitchControl, float yawControl, float xControl, float yControl, float zControl)
	{
		if((position.x() == 0 && Math.abs(direction.x()) == 1.0f) || (position.y() == 0 && Math.abs(direction.y()) == 1.0f) || (position.z() == 0 && Math.abs(direction.z()) == 1.0f))
			return false;
		
		boolean b = getForwardDirection() == Direction.NORTH || getForwardDirection() == Direction.SOUTH;
		
		if(xControl < 0.0f && ((b && direction.x() < 0.0f) || (!b && direction.z() < 0.0f)))
			return true;
		else if(xControl > 0.0f && ((b && direction.x() > 0.0f) || (!b && direction.z() > 0.0f)))
			return true;
		
		if(yControl < 0.0f && direction.y() < 0.0f)
			return true;
		else if(yControl > 0.0f && direction.y() > 0.0f)
			return true;
		
		if(zControl < 0.0f && ((b && direction.z() < 0.0f) || (!b && direction.x() < 0.0f)))
			return true;
		else if(zControl > 0.0f && ((b && direction.z() > 0.0f) || (!b && direction.x() > 0.0f)))
			return true;
		
		Vector3f momentDirection = new Vector3f(position).cross(direction).negate();
		float rollMoment = b ? momentDirection.z() : momentDirection.x();
		float pitchMoment = b ? momentDirection.x() : momentDirection.z();
		float yawMoment = momentDirection.y();
		
		if(rollControl == 1.0f && rollMoment < 0.0f)
			return true;
		else if(rollControl == -1.0f && rollMoment > 0.0f)
			return true;
		
		if(pitchControl == 1.0f && pitchMoment < 0.0f)
			return true;
		else if(pitchControl == -1.0f && pitchMoment > 0.0f)
			return true;
		
		if(yawControl == 1.0f && yawMoment < 0.0f)
			return true;
		else if(yawControl == -1.0f && yawMoment > 0.0f)
			return true;
		
		return false;
	}
	
	private void tickPhysics()
	{
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(method_48926());
		Vector3f netForce = new Vector3f();
		Vector3f netMoment = new Vector3f();
		Quaternionf quaternion = getQuaternion();
		double gravity = 0.0;
		double totalMassFlow = 0.0;
		double hydrogenFlow = 0.0;
		double oxygenFlow = 0.0;
		
		if(data == null)
			gravity = 9.80665;
		else if(!data.isOrbit())
			gravity = 9.80665 * data.getPlanet().getSurfaceGravity();
		
		// Set the target landing altitude if necessary.
		if(arrivalPos.getY() == -9999)
		{
			boolean noBlocks = true;

			for(int i = method_48926().getTopY(); i > method_48926().getBottomY() + 1; i--)
			{
				BlockPos pos = new BlockPos(arrivalPos.getX(), i - 1, arrivalPos.getZ());

				if(method_48926().getBlockState(pos).isSolidBlock(method_48926(), pos))
				{
					arrivalPos = new BlockPos(arrivalPos.getX(), i, arrivalPos.getZ());
					noBlocks = false;
					break;
				}
			}

			if(noBlocks)
				arrivalPos = new BlockPos(arrivalPos.getX(), PARKING_HEIGHT_ORBIT, arrivalPos.getZ());
		}

		if(autoState == 1)
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
				// The maximum G-force a rocket is allowed to experience during launch. Used to
				// lower the throttle if necessary.
				// A lower constraint of 4m/s^2 is applied.
				double maxG = gravity * 2.0 < 4.0 ? 4.0 : gravity * 2.0;

				if(nominalThrust / getMass() > maxG)
					throttle = (getMass() * maxG) / nominalThrust;
				else
					throttle = 1.0;
			}
		}
		else if(autoState == 2)
		{
			double vy = getVelocity().getY() * 20.0; // Vertical velocity in m/s.
			double currentHeight = getAltitude();

			// Find the necessary throttle to cancel vertical velocity.
			double t = Math.min(((0.5 * vy * vy) + (gravity * currentHeight)) / ((nominalThrust / getMass()) * currentHeight), 1.0);
			throttle = t > 0.5 ? t : 0.0;
		}
		
		// Stability Assist System
		getStabilityAssist(rollTorque, pitchTorque, yawTorque);
		
		// Apply pure moments about the center of mass.
		boolean b = getForwardDirection() == Direction.NORTH || getForwardDirection() == Direction.SOUTH;
		netMoment.add(b ? pitchControl * pitchTorque : rollControl * rollTorque, yawControl * yawTorque, b ? rollControl * rollTorque : pitchControl * pitchTorque);
		
		if(throttle > 0.0 && oxygenSupply > 0.0 && hydrogenSupply > 0.0)
		{
			for(Thruster thruster : mainThrusters)
			{
				Vector3f position = thruster.getPosition();
				Vector3f force = thruster.getForce(quaternion, throttle);
				forceAtPosition(force, position, netForce, netMoment);
				totalMassFlow += thruster.getMassFlow(throttle);
			}
		}
		
		if(rollControl != 0.0f || pitchControl != 0.0f || yawControl != 0.0f || xControl != 0.0 || yControl != 0.0f || zControl != 0.0f)
		{
			for(Thruster thruster : rcsThrusters)
			{
				if(!checkRCS(thruster.position, thruster.direction, rollControl, pitchControl, yawControl, xControl, yControl, zControl))
					continue;
				
				Vector3f position = thruster.getPosition();
				Vector3f force = thruster.getForce(quaternion, 1.0);
				forceAtPosition(force, position, netForce, netMoment);
				totalMassFlow += thruster.getMassFlow(1.0);
			}
		}
		
		hydrogenFlow += totalMassFlow * (1.0 / 9.0);
		oxygenFlow += totalMassFlow * (8.0 / 9.0);
		
		// Apply the net force and moment then move.
		netForce.rotate(quaternion);
		netForce.add(0.0f, (float) (-gravity * getMass()), 0.0f);
		applyForce(netForce);
		applyMomentXYZ(netMoment);
		move(MovementType.SELF, getVelocity());
		setQuaternion(QuaternionUtil.hamiltonProduct(quaternion, QuaternionUtil.fromEulerXYZ(getAngularVelocity().x(), getAngularVelocity().y(), getAngularVelocity().z())));
		setBoundingBox(calculateBoundingBox());
		fallDistance = 0.0f;
		
		// Decrease mass and fuel supply last.
		changeMass(-totalMassFlow);
		hydrogenSupply -= hydrogenFlow;
		oxygenSupply -= oxygenFlow;
	}
	
	private void checkDimensionChange()
	{
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(method_48926());
		int travelCeiling = data.isOrbit() ? TRAVEL_CEILING_ORBIT : TRAVEL_CEILING;
		ServerWorld nextWorld = null;
		double requiredDeltaV = 0.0;
		int arrivalY = TRAVEL_CEILING_ORBIT;
		
		if(data.isOrbit())
		{
			if(getBlockPos().getY() > travelCeiling || getBlockPos().getY() < method_48926().getBottomY())
			{
				openTravelScreen();
				return;
			}
		}
		else if(getBlockPos().getY() > travelCeiling)
		{
			nextWorld = getServer().getWorld(data.getPlanet().getOrbit().getWorldKey());
			requiredDeltaV = data.isSky() ? data.getPlanet().dVSkyToOrbit() : data.getPlanet().dVSurfaceToOrbit();
			setVelocity(0.0, -ZERO_G_SPEED, 0.0);
			arrivalY = TRAVEL_CEILING_ORBIT;
			autoState = 2;
		}
		else
		{
			int topThreshold = method_48926().getTopY() - 8;
			int bottomThreshold = method_48926().getBottomY() + 8;
			
			if(data.isSky() && data.getPlanet().getSurface() != null && getBlockY() < bottomThreshold)
			{
				nextWorld = getServer().getWorld(data.getPlanet().getSurface().getWorldKey());
				arrivalY = topThreshold - 1;
			}
			else if(!data.isOrbit() && !data.isSky() && data.getPlanet().getSky() != null && getBlockY() > topThreshold)
			{
				nextWorld = getServer().getWorld(data.getPlanet().getSky().getWorldKey());
				arrivalY = topThreshold + 1;
			}
		}
		
		if(nextWorld != null && requiredDeltaV < getDeltaV())
		{
			useDeltaV(requiredDeltaV);
			float arrivalYaw = (Direction.fromHorizontal(arrivalDirection).asRotation() - getForwardDirection().getOpposite().asRotation()) * (float) (Math.PI / 180.0);
			this.changeDimension(nextWorld, new Vec3d(arrivalPos.getX() + 0.5, arrivalY, arrivalPos.getZ() + 0.5), arrivalYaw);
		}
	}
	
	@Override
	public void onDimensionChanged(ServerWorld destination, Vec3d arrivalLocation, float arrivalYaw)
	{
		initializePropulsion();
		setQuaternion(new Quaternionf().fromAxisAngleRad(0.0f, 1.0f, 0.0f, arrivalYaw));
	}
	
	@Override
	public void onBlockReleased(MovingCraftBlockData blockData, BlockPos worldPos)
	{
		if(blockData.getStoredFluid() > 0)
		{
			BlockEntity blockEntity = method_48926().getBlockEntity(worldPos);
			
			if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
			{
				FluidTankControllerBlockEntity fluidTank = (FluidTankControllerBlockEntity) blockEntity;
				((FluidTankControllerBlock) blockData.getBlockState().getBlock()).initializeFluidTank(method_48926(), worldPos, fluidTank);
				
				if(blockData.redstonePower())
					fluidTank.setStoredFluid(blockData.getStoredFluid());
				else
				{
					if(blockData.getBlockState().getBlock() == StarflightBlocks.HYDROGEN_TANK)
						fluidTank.setStoredFluid(fluidTank.getStorageCapacity() * (hydrogenSupply / hydrogenCapacity));
					else if(blockData.getBlockState().getBlock() == StarflightBlocks.OXYGEN_TANK)
						fluidTank.setStoredFluid(fluidTank.getStorageCapacity() * (oxygenSupply / oxygenCapacity));
				}
			}
			else if(blockEntity != null && blockEntity instanceof RocketControllerBlockEntity)
				((RocketControllerBlockEntity) blockEntity).runScan();
		}
	}
	
	private void openTravelScreen()
	{
		for(Entity entity : getPassengerList())
		{
			if(entity instanceof ServerPlayerEntity)
			{
				ServerPlayerEntity player = (ServerPlayerEntity) entity;
				PacketByteBuf buffer = PacketByteBufs.create();
				buffer.writeDouble(getDeltaV());
				ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "rocket_open_travel_screen"), buffer);
				pausePhysics = true;
				return;
			}
		}
	}
	
	private void spawnThrusterParticles()
	{
		ArrayList<MovingCraftBlockRenderData> blocks = MovingCraftRenderList.getBlocksForEntity(getUuid());
		Quaternionf quaternion = getQuaternion();
		Vector3f upAxis = new Vector3f(0.0f, 1.0f, 0.0f).rotate(quaternion);
		Vector3f craftVelocity = getTrackedVelocity();
		Vector3f craftAngularVelocity = getTrackedAngularVelocity();
		
		if(blocks == null)
			return;
		
		for(MovingCraftBlockRenderData block : blocks)
		{
			if(getThrottle() > 0.0f && block.getBlockState().getBlock() instanceof RocketThrusterBlock && !block.redstonePower())
			{
				BlockPos pos = block.getPosition();
				Vector3f rotated = new Vector3f(pos.getX(), pos.getY() - 3.0f, pos.getZ()).rotate(quaternion);
				Vector3f rotationVelocity = new Vector3f(craftAngularVelocity).cross(rotated);
				Vec3d velocity = new Vec3d(-upAxis.x(), -upAxis.y(), -upAxis.z()).multiply(0.5 + this.random.nextDouble() * 0.1).add(craftVelocity.x() + rotationVelocity.x(), craftVelocity.y() + rotationVelocity.y(), craftVelocity.z() + rotationVelocity.z());
				
				for(int i = 0; i < 4; i++)
				{
					method_48926().addParticle(StarflightParticleTypes.THRUSTER, true, (float) getX() + rotated.x(), (float) getY() + rotated.y(), (float) getZ() + rotated.z(), velocity.getX(), velocity.getY(), velocity.getZ());
					rotated.add((this.random.nextFloat() - this.random.nextFloat()) * 0.25f, (this.random.nextFloat() - this.random.nextFloat()) * 0.25f, (this.random.nextFloat() - this.random.nextFloat()) * 0.25f);
				}
			}
			
			if(block.getBlockState().getBlock() == StarflightBlocks.RCS_BLOCK && !block.redstonePower())
			{
				Quaternionf blockFacingQuaternion = block.getBlockState().get(FacingBlock.FACING).getRotationQuaternion();

				for(Pair<Vector3f, Vector3f> thruster : ((ReactionControlThrusterBlock) block.getBlockState().getBlock()).getThrusters())
				{
					Vector3f position = new Vector3f(thruster.getLeft()).rotate(blockFacingQuaternion);
					Vector3f direction = new Vector3f(thruster.getRight()).rotate(blockFacingQuaternion);
					BlockPos blockPos = block.getPosition();
					Vector3f thrusterPos = new Vector3f(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(position);
					Vector3f attitudeControl = getAttitudeControl();
					Vector3f translationControl = getTranslationControl();
					
					if(!checkRCS(thrusterPos, direction, attitudeControl.x(), attitudeControl.y(), attitudeControl.z(), translationControl.x(), translationControl.y(), translationControl.z()))
						continue;
					
					thrusterPos.rotate(quaternion);	
					Vector3f rotationVelocity = new Vector3f(craftAngularVelocity).cross(thrusterPos);
					Vector3f globalDirection = direction.rotate(quaternion);
					Vec3d velocity = new Vec3d(globalDirection.x(), globalDirection.y(), globalDirection.z()).add(craftVelocity.x() + rotationVelocity.x(), craftVelocity.y() + rotationVelocity.y(), craftVelocity.z() + rotationVelocity.z());
					method_48926().addParticle(StarflightParticleTypes.RCS_THRUSTER, true, (float) getX() + thrusterPos.x(), (float) getY() + thrusterPos.y(), (float) getZ() + thrusterPos.z(), velocity.getX(), velocity.getY(), velocity.getZ());
				}
			}
		}
	}
	
	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("arrivalX", arrivalPos.getX());
		nbt.putInt("arrivalY", arrivalPos.getY());
		nbt.putInt("arrivalZ", arrivalPos.getZ());
		nbt.putInt("arrivalDirection", arrivalDirection);
		nbt.putInt("autoState", autoState);
		nbt.putDouble("nominalThrust", nominalThrust);
		nbt.putDouble("nominalISP", nominalISP);
		nbt.putDouble("averageVEVacuum", averageVEVacuum);
		nbt.putDouble("throttle", throttle);
		nbt.putDouble("hydrogenCapacity", hydrogenCapacity);
		nbt.putDouble("oxygenCapacity", oxygenCapacity);
		nbt.putDouble("hydrogenSupply", hydrogenSupply);
		nbt.putDouble("oxygenSupply", oxygenSupply);
		nbt.putDouble("lowerHeight", lowerHeight);
		nbt.putDouble("upperHeight", upperHeight);
		nbt.putDouble("maxWidth", maxWidth);
		nbt.putFloat("rollTorque", rollTorque);
		nbt.putFloat("pitchTorque", pitchTorque);
		nbt.putFloat("yawTorque", yawTorque);
		nbt.putInt("thrusterCount", mainThrusters.size());
		nbt.putInt("rcsCount", rcsThrusters.size());
		
		for(int i = 0; i < mainThrusters.size(); i++)
		{
			NbtCompound thrusterNBT = new NbtCompound();
			Thruster thruster = mainThrusters.get(i);
			thruster.writeCustomDataToNbt(thrusterNBT);
			nbt.put("main" + i, thrusterNBT);
		}
		
		for(int i = 0; i < rcsThrusters.size(); i++)
		{
			NbtCompound rcsNBT = new NbtCompound();
			Thruster thruster = rcsThrusters.get(i);
			thruster.writeCustomDataToNbt(rcsNBT);
			nbt.put("rcs" + i, rcsNBT);
		}
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		arrivalPos = new BlockPos(nbt.getInt("arrivalX"), nbt.getInt("arrivalY"), nbt.getInt("arrivalZ"));
		arrivalDirection = nbt.getInt("arrivalDirection");
		autoState = nbt.getInt("autoState");
		nominalThrust = nbt.getDouble("nominalThrust");
		nominalISP = nbt.getDouble("nominalISP");
		averageVEVacuum = nbt.getDouble("averageVEVacuum");
		throttle = nbt.getDouble("throttle");
		hydrogenCapacity = nbt.getDouble("hydrogenCapacity");
		oxygenCapacity = nbt.getDouble("oxygenCapacity");
		hydrogenSupply = nbt.getDouble("hydrogenSupply");
		oxygenSupply = nbt.getDouble("oxygenSupply");
		lowerHeight = nbt.getDouble("lowerHeight");
		upperHeight = nbt.getDouble("upperHeight");
		maxWidth = nbt.getDouble("maxWidth");
		rollTorque = nbt.getFloat("rollTorque");
		pitchTorque = nbt.getFloat("pitchTorque");
		yawTorque = nbt.getFloat("yawTorque");
		int thrusterCount = nbt.getInt("thrusterCount");
		int rcsCount = nbt.getInt("rcsCount");
		
		for(int i = 0; i < thrusterCount; i++)
			mainThrusters.add(new Thruster(nbt.getCompound("main" + i)));
		
		for(int i = 0; i < rcsCount; i++)
			rcsThrusters.add(new Thruster(nbt.getCompound("rcs" + i)));
	}
	
	public static void receiveInput(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender)
	{
		int throttleState = buffer.readInt();
		int rollState = buffer.readInt();
		int pitchState = buffer.readInt();
		int yawState = buffer.readInt();
		int xState = buffer.readInt();
		int yState = buffer.readInt();
		int zState = buffer.readInt();
		int stopState = buffer.readInt();
		
		server.execute(() -> {
			Entity entity = player.getVehicle();
			
			if(entity instanceof RocketEntity)
			{
				RocketEntity rocketEntity = (RocketEntity) entity;
				
				if(throttleState != 0 || rollState != 0 || pitchState != 0 || yawState != 0 || xState != 0 || yState != 0 || zState != 0 || stopState != 0)
					rocketEntity.autoState = 0;
				
				int holdStop = rocketEntity.getHoldStop();
				
				if(stopState > 0)
					rocketEntity.setHoldStop(holdStop + 1);
				else if(holdStop > 0)
					rocketEntity.setHoldStop(holdStop - 1);
				
				if(throttleState == 1)
					rocketEntity.throttle += 0.01;
				else if(throttleState == -1)
					rocketEntity.throttle -= 0.01;
				else if(throttleState == 2)
					rocketEntity.throttle = 1.0;
				else if(throttleState == -2)
					rocketEntity.throttle = 0.0;
				
				if(rocketEntity.throttle < 0.0)
					rocketEntity.throttle = 0.0;
				else if(rocketEntity.throttle > 1.0)
					rocketEntity.throttle = 1.0;
				
				Direction forward = rocketEntity.getForwardDirection();
				rocketEntity.rollControl = (forward == Direction.NORTH || forward == Direction.WEST) ? rollState : -rollState;
				rocketEntity.pitchControl = (forward == Direction.SOUTH || forward == Direction.WEST) ? pitchState : -pitchState;
				rocketEntity.yawControl = yawState;
				rocketEntity.xControl = xState;
				rocketEntity.yControl = yState;
				rocketEntity.zControl = zState;
			}
		});
	}
	
	public static void receiveTravelInput(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender)
	{
		String planetName = buffer.readString();
		double requiredDeltaV = buffer.readDouble();
		boolean landing = buffer.readBoolean();
		
		server.execute(() -> {
			Entity entity = player.getVehicle();
			
			if(entity != null && entity instanceof RocketEntity)
			{
				RocketEntity rocketEntity = (RocketEntity) entity;
				Planet planet = PlanetList.getByName(planetName);
				int arrivalY = TRAVEL_CEILING;
	
				if(planet != null)
				{
					Planet currentPlanet = PlanetList.getDimensionDataForWorld(rocketEntity.method_48926()).getPlanet();
					ServerWorld nextWorld = null;
					
					if(landing)
					{
						// Travel to the surface dimension.
						if(currentPlanet.getSky() != null)
							nextWorld = rocketEntity.getServer().getWorld(currentPlanet.getSky().getWorldKey());
						else if(currentPlanet.getSurface() != null)
							nextWorld = rocketEntity.getServer().getWorld(currentPlanet.getSurface().getWorldKey());

						rocketEntity.setVelocity(0.0, -ZERO_G_SPEED / 2.0, 0.0);
					}
					else
					{
						if(planet == currentPlanet)
						{
							// Stay in the same orbit dimension but loop around to the opposite Y threshold.
							Vec3d planePoint = new Vec3d(0.0, rocketEntity.getY() < 0.0 ? TRAVEL_CEILING_ORBIT : rocketEntity.method_48926().getBottomY(), 0.0);
							Vec3d heading = rocketEntity.getVelocity().negate().normalize();
							Vec3d moveTo = null;
							double xzDistance = 0.0;
							
							while(moveTo == null || xzDistance > 256.0)
							{
								moveTo = VectorUtil.linePlaneIntersection(rocketEntity.getPos(), rocketEntity.getPos().add(heading), planePoint, new Vec3d(0.0, 1.0, 0.0));
								xzDistance = moveTo.add(0.0, -moveTo.getY(), 0.0).distanceTo(rocketEntity.getPos().add(0.0, -rocketEntity.getPos().getY(), 0.0));
								
								if(xzDistance > 256.0)
									heading = heading.add(0.0, rocketEntity.getY() < 0.0 ? 0.1 : -0.1, 0.0).normalize();
							}
							
							rocketEntity.requestTeleport(moveTo.getX(), moveTo.getY(), moveTo.getZ());
							rocketEntity.setVelocity(heading.multiply(-rocketEntity.getVelocity().length()));
							rocketEntity.velocityDirty = true;
							rocketEntity.pausePhysics = false;
							rocketEntity.throttle = 0.0;
						}
						else
						{
							// Travel to the next planet's orbit dimension.
							nextWorld = rocketEntity.getServer().getWorld(planet.getOrbit().getWorldKey());
							rocketEntity.setVelocity(0.0, -ZERO_G_SPEED, 0.0);
							arrivalY = TRAVEL_CEILING_ORBIT;
						}
					}

					if(nextWorld != null)
					{
						if(requiredDeltaV > 0.0)
							rocketEntity.useDeltaV(requiredDeltaV);

						rocketEntity.autoState = 2;
						float arrivalYaw = (Direction.fromHorizontal(rocketEntity.arrivalDirection).asRotation() - rocketEntity.getForwardDirection().getOpposite().asRotation()) * (float) (Math.PI / 180.0);
						rocketEntity.changeDimension(nextWorld, new Vec3d(rocketEntity.arrivalPos.getX() + 0.5, arrivalY, rocketEntity.arrivalPos.getZ() + 0.5), arrivalYaw);
					}
				}
			}
		});
	}
	
	public static void receiveOpenTravelScreen(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
		double deltaV = buffer.readDouble();
		client.execute(() -> client.setScreen(new SpaceNavigationScreen(deltaV)));
	}
	
	private static class Thruster
	{
		private Vector3f position;
		private Vector3f direction;
		private double thrust;
		private double isp;
		
		public Thruster(Vector3f position, Vector3f direction, double thrust, double isp)
		{
			this.position = position;
			this.direction = direction;
			this.thrust = thrust;
			this.isp = isp;
		}
		
		public Thruster(NbtCompound nbt)
		{
			readCustomDataFromNbt(nbt);
		}
		
		public Vector3f getPosition()
		{
			return new Vector3f(position);
		}
		
		public Vector3f getForce(Quaternionf quaternion, double throttle)
		{
			return new Vector3f(direction).mul((float) (-thrust * throttle));
		}
		
		public double getMassFlow(double throttle)
		{
			return ((thrust * throttle) / (SG * isp)) * 0.05;
		}
		
		public void writeCustomDataToNbt(NbtCompound nbt)
		{
			nbt.putFloat("px", position.x());
			nbt.putFloat("py", position.y());
			nbt.putFloat("pz", position.z());
			nbt.putFloat("dx", direction.x());
			nbt.putFloat("dy", direction.y());
			nbt.putFloat("dz", direction.z());
			nbt.putDouble("thrust", thrust);
			nbt.putDouble("isp", isp);
		}
		
		public void readCustomDataFromNbt(NbtCompound nbt)
		{
			position = new Vector3f(nbt.getFloat("px"), nbt.getFloat("py"), nbt.getFloat("pz"));
			direction = new Vector3f(nbt.getFloat("dx"), nbt.getFloat("dy"), nbt.getFloat("dz"));
			thrust = nbt.getDouble("thrust");
			isp = nbt.getDouble("isp");
		}
	}
}