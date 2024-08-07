package space.entity;

import java.util.ArrayList;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.block.SimpleFacingBlock;
import space.block.StarflightBlocks;
import space.network.c2s.AirshipInputC2SPacket;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.QuaternionUtil;
import space.vessel.MovingCraftBlockData;

public class AirshipEntity extends MovingCraftEntity
{
	private static final TrackedData<Integer> HOLD_STOP = DataTracker.registerData(AirshipEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Float> ELEVATION_CONTROL = DataTracker.registerData(AirshipEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> FORWARD_REVERSE_CONTROL = DataTracker.registerData(AirshipEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> LATERAL_CONTROL = DataTracker.registerData(AirshipEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> ROTATION_CONTROL = DataTracker.registerData(AirshipEntity.class, TrackedDataHandlerRegistry.FLOAT);
	
	private ArrayList<Thruster> thrusters = new ArrayList<Thruster>();
	
	private float elevationControl;
	private float forwardReverseControl;
	private float lateralControl;
	private float rotationControl;
	
	public AirshipEntity(EntityType<? extends AirshipEntity> entityType, World world)
	{
		super(entityType, world);
	}

	public AirshipEntity(World world, BlockPos blockPos, ArrayList<MovingCraftBlockData> blockDataList, Direction forward, double mass, double volume, Vector3f momentOfInertia1, Vector3f momentOfInertia2)
	{
		super(StarflightEntities.AIRSHIP, world, blockPos, blockDataList, mass, volume, momentOfInertia1, momentOfInertia2);
		setForwardDirection(forward.getHorizontal());
		initializePropulsion();
		pickUpEntities();
	}
	
	@Override
	protected void initDataTracker(DataTracker.Builder builder)
	{
		super.initDataTracker(builder);
		builder.add(HOLD_STOP, Integer.valueOf(0));
		builder.add(ELEVATION_CONTROL, Float.valueOf(0));
		builder.add(FORWARD_REVERSE_CONTROL, Float.valueOf(0));
		builder.add(LATERAL_CONTROL, Float.valueOf(0));
		builder.add(ROTATION_CONTROL, Float.valueOf(0));
	}
	
	public void setHoldStop(Integer i)
	{
		this.dataTracker.set(HOLD_STOP, Integer.valueOf(i));
	}
	
	public void setElevationControl(float elevationControl)
	{
		this.dataTracker.set(ELEVATION_CONTROL, Float.valueOf(elevationControl));
	}
	
	public void setForwardReverseControl(float forwardReverseControl)
	{
		this.dataTracker.set(FORWARD_REVERSE_CONTROL, Float.valueOf(forwardReverseControl));
	}
	
	public void setLateralControl(float lateralControl)
	{
		this.dataTracker.set(LATERAL_CONTROL, Float.valueOf(lateralControl));
	}
	
	public void setRotationControl(float rotationControl)
	{
		this.dataTracker.set(ROTATION_CONTROL, Float.valueOf(rotationControl));
	}
	
	public int getHoldStop()
	{
		return this.dataTracker.get(HOLD_STOP);
	}
	
	public float getElevationControl()
	{
		return this.dataTracker.get(ELEVATION_CONTROL);
	}
	
	public float getForwardReverseControl()
	{
		return this.dataTracker.get(FORWARD_REVERSE_CONTROL);
	}
	
	public float getLateralControl()
	{
		return this.dataTracker.get(LATERAL_CONTROL);
	}
	
	public float getRotationControl()
	{
		return this.dataTracker.get(ROTATION_CONTROL);
	}

	@Override
	public void tick()
	{
		externalEntityCollisions();
		
		// Run client-side actions and then return.
		if(this.clientMotion())
		{
			spawnThrusterParticles();
			this.clientQuaternionPrevious = this.clientQuaternion;
			this.clientQuaternion = this.getQuaternion();
			this.clientAnglesPrevious = this.clientAngles;
			this.clientAngles = this.getTrackedAngles();
			return;
		}

		if(blocks.isEmpty())
		{
			setRemoved(RemovalReason.DISCARDED);
			return;
		}
		
		Vector3f trackedVelocity = getTrackedVelocity();
		float craftSpeed = (float) MathHelper.magnitude(trackedVelocity.x(), trackedVelocity.y(), trackedVelocity.z()) * 20.0f; // Get the craft's speed in m/s.
		
		if(getHoldStop() > 20 && craftSpeed < 4.0)
		{
			sendRenderData(true);
			releaseBlocks();
		}
		else
			sendRenderData(false);
		
		tickPhysics();
		checkDimensionChange();
		setElevationControl(elevationControl);
		setForwardReverseControl(forwardReverseControl);
		setLateralControl(lateralControl);
		setRotationControl(rotationControl);
		setTrackedVelocity(getVelocity().toVector3f());
		setTrackedAngularVelocity(getAngularVelocity());
		updateTrackedBox();
	}
	
	public void initializePropulsion()
	{
		thrusters.clear();
		
		for(MovingCraftBlockData blockData : blocks)
		{
			BlockState blockState = blockData.getBlockState();
			Block block = blockState.getBlock();
			BlockPos blockPos = blockData.getPosition();

			if(block == StarflightBlocks.AIRSHIP_MOTOR)
			{
				Quaternionf blockFacingQuaternion = blockState.get(FacingBlock.FACING).getRotationQuaternion();
				thrusters.add(new Thruster(new Vector3f(blockPos.getX(), blockPos.getY(), blockPos.getZ()), new Vector3f(0.0f, -1.0f, 0.0f).rotate(blockFacingQuaternion), 25000.0f));
			}
		}
	}
	
	private boolean checkThruster(Vector3f position, Vector3f direction)
	{
		float elevationControl = getElevationControl();
		float forwardReverseControl = getForwardReverseControl();
		float lateralControl = getLateralControl();
		float rotationControl = getRotationControl();
		
		if(rotationControl == 0.0f)
		{
			boolean b = getForwardDirection() == Direction.NORTH || getForwardDirection() == Direction.SOUTH;
	
			if(forwardReverseControl < 0.0f && ((b && direction.z() > 0.0f) || (!b && direction.x() > 0.0f)))
				return true;
			else if(forwardReverseControl > 0.0f && ((b && direction.z() < 0.0f) || (!b && direction.x() < 0.0f)))
				return true;
			
			if(lateralControl < 0.0f && ((b && direction.x() > 0.0f) || (!b && direction.z() < 0.0f)))
				return true;
			else if(lateralControl > 0.0f && ((b && direction.x() < 0.0f) || (!b && direction.z() > 0.0f)))
				return true;
	
			if(elevationControl < 0.0f && direction.y() > 0.0f)
				return true;
			else if(elevationControl > 0.0f && direction.y() < 0.0f)
				return true;
		}
		
		Vector3f momentDirection = new Vector3f(position).cross(direction).negate();
		//float rollMoment = b ? momentDirection.z() : momentDirection.x();
		//float pitchMoment = b ? momentDirection.x() : momentDirection.z();
		float yawMoment = momentDirection.y();

		/*if(rollControl == 1.0f && rollMoment < 0.0f)
			return true;
		else if(rollControl == -1.0f && rollMoment > 0.0f)
			return true;

		if(pitchControl == 1.0f && pitchMoment < 0.0f)
			return true;
		else if(pitchControl == -1.0f && pitchMoment > 0.0f)
			return true;*/

		if(rotationControl == -1.0f && yawMoment < 0.0f)
			return true;
		else if(rotationControl == 1.0f && yawMoment > 0.0f)
			return true;

		return false;
	}
	
	private void tickPhysics()
	{
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(getWorld());
		Vector3f netForce = new Vector3f();
		Vector3f netMoment = new Vector3f();
		Quaternionf quaternion = getQuaternion();
		
		for(Thruster thruster : thrusters)
		{
			Vector3f position = thruster.getPosition();
			Vector3f force = thruster.getForce(quaternion, 1.0);
			
			if(checkThruster(thruster.position, thruster.direction))
				forceAtPosition(force, position, netForce, netMoment);
			else if(checkThruster(thruster.position, new Vector3f(thruster.direction).mul(-1.0f)))
				forceAtPosition(new Vector3f(force).mul(-1.0f), position, netForce, netMoment);
		}
		
		Vector3f angles = new Vector3f();
		getQuaternion().getEulerAnglesYXZ(angles);
		boolean b = getForwardDirection() == Direction.NORTH || getForwardDirection() == Direction.SOUTH;
		netMoment.add(angles.x() * 1000000.0f, 0.0f, angles.z() * 1000000.0f);
		
		// Apply the net force and moment then move.
		//setVelocity(getVelocity().multiply(0.98));
		angularVelocity.mul(0.95f);
		netForce.rotate(quaternion);
		
		if(data.getPressure() > 0)
		{
			float t = 90.0f + data.getTemperatureCategory() * 100.0f;
			float airDensity = (float) (data.getPressure() * 101325.0) / (t * 287.05f);
			float drag = 0.5f * airDensity * (float) (Math.min(getXWidth() * getHeight(), getZWidth() * getHeight()) * Math.pow(getVelocity().length() * 10.0, 2.0));
			netForce.add(getVelocity().normalize().toVector3f().mul(-drag));
			//netForce.add(0.0f, (float) (airDensity * gravity * getDisplacementVolume()), 0.0f);
		}
		
		applyForce(netForce);
		applyMomentXYZ(netMoment);
		move(MovementType.SELF, getVelocity());
		Vector3f av = getAngularVelocity();
		setQuaternion(QuaternionUtil.hamiltonProduct(quaternion, QuaternionUtil.fromEulerXYZ(av.x(), av.y(), av.z())));
		integrateLocalAngles(av.x(), av.y(), av.z());
		setBoundingBox(calculateBoundingBox());
		fallDistance = 0.0f;
	}
	
	private void checkDimensionChange()
	{
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(getWorld());
		ServerWorld nextWorld = null;
		int topThreshold = getWorld().getTopY() - 4;
		int bottomThreshold = getWorld().getBottomY() + 4;
		int arrivalY = 0;

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

		if(nextWorld != null)
			this.changeDimension(nextWorld, new Vec3d(getPos().getX(), arrivalY, getPos().getZ()), getYaw());
	}
	
	private void spawnThrusterParticles()
	{
		Quaternionf quaternion = getQuaternion();
		Vector3f craftVelocity = getTrackedVelocity();
		Vector3f craftAngularVelocity = getTrackedAngularVelocity();

		for(MovingCraftBlockData block : blocks)
		{
			if(block.getBlockState().getBlock() == StarflightBlocks.AIRSHIP_MOTOR)
			{
				Vector3f direction = block.getBlockState().get(SimpleFacingBlock.FACING).getUnitVector();
				Vector3f oppositeDirection = block.getBlockState().get(SimpleFacingBlock.FACING).getOpposite().getUnitVector();
				BlockPos blockPos = block.getPosition();
				Vector3f thrusterPos = new Vector3f(blockPos.getX(), blockPos.getY(), blockPos.getZ());
				Vector3f rotationVelocity = new Vector3f(craftAngularVelocity).cross(thrusterPos);

				if(checkThruster(thrusterPos, direction))
				{
					thrusterPos.rotate(quaternion);
					Vector3f globalDirection = direction.rotate(quaternion);
					Vec3d velocity = new Vec3d(globalDirection.x(), globalDirection.y(), globalDirection.z()).add(craftVelocity.x() + rotationVelocity.x(), craftVelocity.y() + rotationVelocity.y(), craftVelocity.z() + rotationVelocity.z());
					getWorld().addParticle(ParticleTypes.CLOUD, true, (float) getX() + thrusterPos.x(), (float) getY() + thrusterPos.y(), (float) getZ() + thrusterPos.z(), velocity.getX(), velocity.getY(), velocity.getZ());
				}
				else if(checkThruster(thrusterPos, oppositeDirection))
				{
					thrusterPos.rotate(quaternion);
					Vector3f globalDirection = oppositeDirection.rotate(quaternion);
					Vec3d velocity = new Vec3d(globalDirection.x(), globalDirection.y(), globalDirection.z()).add(craftVelocity.x() + rotationVelocity.x(), craftVelocity.y() + rotationVelocity.y(), craftVelocity.z() + rotationVelocity.z());
					getWorld().addParticle(ParticleTypes.CLOUD, true, (float) getX() + thrusterPos.x(), (float) getY() + thrusterPos.y(), (float) getZ() + thrusterPos.z(), velocity.getX(), velocity.getY(), velocity.getZ());
				}
			}
		}
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("thrusterCount", thrusters.size());

		for(int i = 0; i < thrusters.size(); i++)
		{
			NbtCompound thrusterNBT = new NbtCompound();
			Thruster thruster = thrusters.get(i);
			thruster.writeCustomDataToNbt(thrusterNBT);
			nbt.put("thruster" + i, thrusterNBT);
		}
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		int thrusterCount = nbt.getInt("thrusterCount");
		thrusters.clear();

		for(int i = 0; i < thrusterCount; i++)
			thrusters.add(new Thruster(nbt.getCompound("thruster" + i)));
	}
	
	public static void receiveInput(AirshipInputC2SPacket payload, ServerPlayNetworking.Context context)
	{
		int[] inputStates = payload.inputStates();
		int elevationState = inputStates[0];
		int forwardReverseState = inputStates[1];
		int lateralState = inputStates[2];
		int rotationState = inputStates[3];
		int stopState = inputStates[4];
		ServerPlayerEntity player = context.player();
		MinecraftServer server = player.getServer();

		server.execute(() -> {
			Entity entity = player.getVehicle();

			if(entity instanceof AirshipEntity)
			{
				AirshipEntity airshipEntity = (AirshipEntity) entity;
				int holdStop = airshipEntity.getHoldStop();

				if(stopState > 0)
					airshipEntity.setHoldStop(holdStop + 1);
				else if(holdStop > 0)
					airshipEntity.setHoldStop(holdStop - 1);
				
				Direction forward = airshipEntity.getForwardDirection();
				airshipEntity.elevationControl = elevationState;
				airshipEntity.forwardReverseControl = (forward == Direction.NORTH || forward == Direction.WEST) ? forwardReverseState : -forwardReverseState;
				airshipEntity.lateralControl = (forward == Direction.NORTH || forward == Direction.WEST) ? lateralState : -lateralState;
				airshipEntity.rotationControl = rotationState;
			}
		});
	}
	
	private static class Thruster
	{
		private Vector3f position;
		private Vector3f direction;
		private double thrust;

		public Thruster(Vector3f position, Vector3f direction, double thrust)
		{
			this.position = position;
			this.direction = direction;
			this.thrust = thrust;
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

		public void writeCustomDataToNbt(NbtCompound nbt)
		{
			nbt.putFloat("px", position.x());
			nbt.putFloat("py", position.y());
			nbt.putFloat("pz", position.z());
			nbt.putFloat("dx", direction.x());
			nbt.putFloat("dy", direction.y());
			nbt.putFloat("dz", direction.z());
			nbt.putDouble("thrust", thrust);
		}

		public void readCustomDataFromNbt(NbtCompound nbt)
		{
			position = new Vector3f(nbt.getFloat("px"), nbt.getFloat("py"), nbt.getFloat("pz"));
			direction = new Vector3f(nbt.getFloat("dx"), nbt.getFloat("dy"), nbt.getFloat("dz"));
			thrust = nbt.getDouble("thrust");
		}
	}
}