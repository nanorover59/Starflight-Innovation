package space.entity;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;
import space.util.QuaternionUtil;

public class ZeroGravityMobEntity extends PathAwareEntity
{
	private static final TrackedData<Float> QX = DataTracker.registerData(ZeroGravityMobEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> QY = DataTracker.registerData(ZeroGravityMobEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> QZ = DataTracker.registerData(ZeroGravityMobEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> QW = DataTracker.registerData(ZeroGravityMobEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> ROLL_EXTRA = DataTracker.registerData(ZeroGravityMobEntity.class, TrackedDataHandlerRegistry.FLOAT);
	public Vec3d pointOfInterest;
	public Quaternionf clientQuaternion;
	public Quaternionf clientQuaternionPrevious;
	public float clientRollExtra;
	public float clientRollExtraPrevious;
	public int clientInterpolationSteps;

	protected ZeroGravityMobEntity(EntityType<? extends ZeroGravityMobEntity> entityType, World world)
	{
		super((EntityType<? extends PathAwareEntity>) entityType, world);
		pointOfInterest = getPos().add(0.0, -1.0, 0.0);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder)
	{
		super.initDataTracker(builder);
		builder.add(QX, Float.valueOf(0.0f));
		builder.add(QY, Float.valueOf(0.0f));
		builder.add(QZ, Float.valueOf(0.0f));
		builder.add(QW, Float.valueOf(1.0f));
		builder.add(ROLL_EXTRA, Float.valueOf(0.0f));
	}

	@Override
	public boolean isClimbing()
	{
		return false;
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource)
	{
		return false;
	}

	@Override
	public boolean shouldRender(double distance)
	{
		return true;
	}
	
	@Override
	public boolean canImmediatelyDespawn(double d)
	{
		return false;
	}
	
	@Override
	public boolean hasNoGravity()
	{
		return true;
	}
	
	@Override
	public boolean hasNoDrag()
	{
		return true;
	}
	
	@Override
	public double getEyeY()
	{
		return getPos().getY();
	}

	@Override
	protected Box calculateBoundingBox()
	{
		EntityDimensions dimensions = getDimensions(getPose());
		double f = dimensions.width() / 2.0;
        double g = dimensions.height() / 2.0;
        return new Box(getX() - f, getY() - g, getZ() - f, getX() + f, getY() + g, getZ() + f);
	}

	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition)
	{
	}

	@Override
	public void tick()
	{
		World world = getWorld();
		
		if(world.isClient)
		{
			this.clientQuaternionPrevious = this.clientQuaternion;
			this.clientQuaternion = this.getQuaternion();
			this.clientRollExtraPrevious = this.clientRollExtra;
			this.clientRollExtra = this.getRollExtra();
		}
		else
		{
			int i = (int) (getBoundingBox().getAverageSideLength() * 2.0);
			double acc = 0.1;
			
			if(world.getBlockState(getBlockPos().east(i)).blocksMovement())
				addVelocity(-acc, 0.0, 0.0);
			
			if(world.getBlockState(getBlockPos().west(i)).blocksMovement())
				addVelocity(acc, 0.0, 0.0);
			
			if(world.getBlockState(getBlockPos().up(i)).blocksMovement())
				addVelocity(0.0, -acc, 0.0);
			
			if(world.getBlockState(getBlockPos().down(i)).blocksMovement())
				addVelocity(0.0, acc, 0.0);
			
			if(world.getBlockState(getBlockPos().south(i)).blocksMovement())
				addVelocity(0.0, 0.0, -acc);
			
			if(world.getBlockState(getBlockPos().north(i)).blocksMovement())
				addVelocity(0.0, 0.0, acc);
		}
		
		super.tick();
	}

	public void setQuaternion(Quaternionf quaternion)
	{
		this.dataTracker.set(QX, quaternion.x());
		this.dataTracker.set(QY, quaternion.y());
		this.dataTracker.set(QZ, quaternion.z());
		this.dataTracker.set(QW, quaternion.w());
	}

	public Quaternionf getQuaternion()
	{
		return new Quaternionf(this.dataTracker.get(QX).floatValue(), this.dataTracker.get(QY).floatValue(), this.dataTracker.get(QZ).floatValue(), this.dataTracker.get(QW).floatValue());
	}
	
	public void setRollExtra(float rollExtra)
	{
		if(rollExtra <= -180.0f)
			rollExtra += 360.0f;
		else if(rollExtra > 180.0f)
			rollExtra -= 360.0f;
		
		this.dataTracker.set(ROLL_EXTRA, rollExtra);
	}
	
	public float getRollExtra()
	{
		return this.dataTracker.get(ROLL_EXTRA).floatValue();
	}
	
	public void updateMotion(Vec3d direction, double thrust)
	{
		Vector3f directionF = direction.toVector3f().normalize();
		Quaternionf current = getQuaternion().normalize();
		Vector3f axis = new Vector3f(0.0f, 0.0f, -1.0f);
		axis.cross(directionF);
		float angle = (float) Math.acos(-directionF.z());
		Quaternionf target = new Quaternionf().rotateAxis(angle, axis);
		Quaternionf step = QuaternionUtil.interpolate(current, target, getTurningFactor());
		setQuaternion(step);
		applyThrust(thrust);
	}

	private void applyThrust(double thrust)
	{
		double acc = thrust * 0.0025;
		Vector3f direction = new Vector3f(0.0f, 0.0f, -1.0f).rotate(getQuaternion());
		this.addVelocity(direction.x() * acc, direction.y() * acc, direction.z() * acc);
		velocityModified = true;
	}
	
	public float getTurningFactor()
	{
		return 0.1f;
	}
	
	public boolean hasRunningGoals()
	{
		for(PrioritizedGoal goal : goalSelector.getGoals())
		{
			if(goal.isRunning())
				return true;
		}
		
		return false;
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat("qx", this.dataTracker.get(QX).floatValue());
		nbt.putFloat("qy", this.dataTracker.get(QY).floatValue());
		nbt.putFloat("qz", this.dataTracker.get(QZ).floatValue());
		nbt.putFloat("qw", this.dataTracker.get(QW).floatValue());
		nbt.putDouble("px", pointOfInterest.getX());
		nbt.putDouble("py", pointOfInterest.getY());
		nbt.putDouble("pz", pointOfInterest.getZ());
		nbt.putFloat("re", getRollExtra());
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		setQuaternion(new Quaternionf(nbt.getFloat("qx"), nbt.getFloat("qy"), nbt.getFloat("qz"), nbt.getFloat("qw")).normalize());
		pointOfInterest = new Vec3d(nbt.getDouble("px"), nbt.getDouble("py"), nbt.getDouble("pz"));
		setRollExtra(nbt.getFloat("re"));
	}
	
	class CancelVelocityGoal extends Goal
	{
		ZeroGravityMobEntity entity;
		double thrust;
		
		public CancelVelocityGoal(ZeroGravityMobEntity entity, double thrust)
		{
			this.entity = entity;
			this.thrust = thrust;
		}
		
		@Override
		public boolean canStart()
		{
			return !entity.hasRunningGoals() && getVelocity().length() > 0.1;
		}

		@Override
		public void tick()
		{
			updateMotion(getVelocityToCancel(), thrust * getVelocity().length());
		}

		@Override
		public boolean shouldContinue()
		{
			return !canStop();
		}
		
		@Override
		public boolean canStop()
		{
			return getVelocity().length() < 0.05;
		}
		
		private Vec3d getVelocityToCancel()
		{
			return getVelocity().negate();
		}
	}
	
	class RandomFlightGoal extends Goal
	{
		ZeroGravityMobEntity entity;
		Vec3d targetDirection;
		double thrust;
		double distance;
		double previousDistance;
		int maximumDuration;
		int range;
		int ticks;
		
		public RandomFlightGoal(ZeroGravityMobEntity entity, double thrust, int maximumDuration, int range)
		{
			this.entity = entity;
			this.thrust = thrust;
			this.maximumDuration = maximumDuration;
			this.range = range;
		}

		@Override
		public boolean canStart()
		{
			return !entity.hasRunningGoals();
		}

		@Override
		public void start()
		{
			setPointOfInterest();
			targetDirection = pointOfInterest.subtract(getPos());
			distance = targetDirection.length();
			previousDistance = distance;
			ticks = 0;
		}

		@Override
		public void tick()
		{
			int topY = getWorld().getTopPosition(Type.MOTION_BLOCKING_NO_LEAVES, getBlockPos()).getY();
			
			if(getPos().getY() < topY + 32.0 && getVelocity().getY() < 0.0)
				entity.addVelocity(0.0, thrust * 0.0025, 0.0);
			
			targetDirection = pointOfInterest.subtract(getPos());
			previousDistance = distance;
			distance = targetDirection.length();
			updateMotion(targetDirection, thrust);
			ticks++;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return !canStop();
		}

		@Override
		public boolean canStop()
		{
			return getTarget() != null || ticks >= maximumDuration || horizontalCollision || verticalCollision;
		}
		
		private void setPointOfInterest()
		{
			int topY = getWorld().getTopPosition(Type.MOTION_BLOCKING_NO_LEAVES, getBlockPos()).getY();
			int dx = random.nextInt(range) - random.nextInt(range);
			int dy = random.nextInt(range) - random.nextInt(range);
			int dz = random.nextInt(range) - random.nextInt(range);
			pointOfInterest = getPos().add(dx, dy, dz);
			
			if(pointOfInterest.getY() < topY + 32.0)
				pointOfInterest = new Vec3d(pointOfInterest.getX(), topY + 32.0, pointOfInterest.getZ());
		}
	}
	
	class TrackTargetGoal extends Goal
	{
		ZeroGravityMobEntity entity;
		Vec3d targetDirection;
		double thrust;
		double distance;
		double previousDistance;
		double initialDistance;
		int maximumDuration;
		boolean rolling;
		float rollSpeed;
		int ticks;
		
		public TrackTargetGoal(ZeroGravityMobEntity entity, double thrust, int maximumDuration, boolean rolling)
		{
			this.entity = entity;
			this.thrust = thrust;
			this.maximumDuration = maximumDuration;
			this.rolling = rolling;
			this.rollSpeed = (5.0f + random.nextFloat() * 5.0f) * (random.nextBoolean() ? -1.0f : 1.0f);
			
			if(getTarget() != null)
				this.initialDistance = getTarget().getEyePos().subtract(getPos()).length();
		}

		@Override
		public boolean canStart()
		{
			return !entity.hasRunningGoals() && getTarget() != null;
		}

		@Override
		public void start()
		{
			trackTargetEntity();
			ticks = 0;
		}
		
		@Override
		public void tick()
		{
			if(getTarget() != null)
			{
				trackTargetEntity();
				int topY = getWorld().getTopPosition(Type.MOTION_BLOCKING_NO_LEAVES, getBlockPos()).getY();
				
				if(getPos().getY() < topY + 16.0 && getVelocity().getY() < 0.0)
					entity.addVelocity(0.0, thrust * 0.0025, 0.0);
				
				if(distance < 16.0)
					updateMotion(targetDirection, -thrust);
				else
					updateMotion(targetDirection, thrust);
			}
			
			setRollExtra(getRollExtra() + rollSpeed);
			previousDistance = distance;
			distance = pointOfInterest.subtract(getPos()).length();
			ticks++;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return !canStop();
		}

		@Override
		public boolean canStop()
		{
			return getTarget() == null || ticks > maximumDuration || horizontalCollision || verticalCollision;
		}
		
		private void trackTargetEntity()
		{
			pointOfInterest = getTarget().getEyePos();
			targetDirection = pointOfInterest.subtract(getPos());
		}
	}
}