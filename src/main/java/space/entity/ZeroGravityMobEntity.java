package space.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;
import space.util.VectorMathUtil;

public class ZeroGravityMobEntity extends MobEntity
{
	private static final TrackedData<Float> QX = DataTracker.registerData(ZeroGravityMobEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> QY = DataTracker.registerData(ZeroGravityMobEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> QZ = DataTracker.registerData(ZeroGravityMobEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> QW = DataTracker.registerData(ZeroGravityMobEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> ROLL_EXTRA = DataTracker.registerData(ZeroGravityMobEntity.class, TrackedDataHandlerRegistry.FLOAT);
	public Vec3d pointOfInterest;
	public Quaternion clientQuaternion;
	public Quaternion clientQuaternionPrevious;
	public float clientRollExtra;
	public float clientRollExtraPrevious;
	public int clientInterpolationSteps;

	protected ZeroGravityMobEntity(EntityType<? extends ZeroGravityMobEntity> entityType, World world)
	{
		super((EntityType<? extends MobEntity>) entityType, world);
		pointOfInterest = getPos().add(0.0, -1.0, 0.0);
	}

	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		this.dataTracker.startTracking(QX, Float.valueOf(0.0f));
		this.dataTracker.startTracking(QY, Float.valueOf(0.0f));
		this.dataTracker.startTracking(QZ, Float.valueOf(0.0f));
		this.dataTracker.startTracking(QW, Float.valueOf(1.0f));
		this.dataTracker.startTracking(ROLL_EXTRA, Float.valueOf(0.0f));
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
		double f = dimensions.width / 2.0;
        double g = dimensions.height / 2.0;
        return new Box(getX() - f, getY() - g, getZ() - f, getX() + f, getY() + g, getZ() + f);
	}

	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition)
	{
	}

	@Override
	public void tick()
	{
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
			double acc = 0.15;
			
			if(world.getBlockState(getBlockPos().east(i)).getMaterial().blocksMovement())
				addVelocity(-acc, 0.0, 0.0);
			
			if(world.getBlockState(getBlockPos().west(i)).getMaterial().blocksMovement())
				addVelocity(acc, 0.0, 0.0);
			
			if(world.getBlockState(getBlockPos().up(i)).getMaterial().blocksMovement())
				addVelocity(0.0, -acc, 0.0);
			
			if(world.getBlockState(getBlockPos().down(i)).getMaterial().blocksMovement())
				addVelocity(0.0, acc, 0.0);
			
			if(world.getBlockState(getBlockPos().south(i)).getMaterial().blocksMovement())
				addVelocity(0.0, 0.0, -acc);
			
			if(world.getBlockState(getBlockPos().north(i)).getMaterial().blocksMovement())
				addVelocity(0.0, 0.0, acc);
		}
		
		super.tick();
	}

	public void setQuaternion(Quaternion quaternion)
	{
		this.dataTracker.set(QX, quaternion.getX());
		this.dataTracker.set(QY, quaternion.getY());
		this.dataTracker.set(QZ, quaternion.getZ());
		this.dataTracker.set(QW, quaternion.getW());
	}

	public Quaternion getQuaternion()
	{
		return new Quaternion(this.dataTracker.get(QX).floatValue(), this.dataTracker.get(QY).floatValue(), this.dataTracker.get(QZ).floatValue(), this.dataTracker.get(QW).floatValue());
	}
	
	public void setRollExtra(float rollExtra)
	{
		this.dataTracker.set(ROLL_EXTRA, rollExtra);
	}
	
	public float getRollExtra()
	{
		return this.dataTracker.get(ROLL_EXTRA).floatValue();
	}
	
	public void updateMotion(Vec3d direction, double thrust, boolean ignoreDifference)
	{
		Vec3f directionF = new Vec3f(direction);
		directionF.normalize();
		Quaternion current = getQuaternion();
		Vec3f axis = Vec3f.NEGATIVE_Z.copy();
		axis.cross(directionF);
		axis.normalize();
		float angle = (float) Math.acos(-directionF.getZ());
		Quaternion target = axis.getRadialQuaternion(angle);
		target.normalize();
		float difference = VectorMathUtil.difference(current, target);
		Quaternion step = VectorMathUtil.interpolate(current, target, getTurningFactor());
		step.normalize();
		setQuaternion(step);
		
		if(ignoreDifference || difference < 0.2f)
			applyThrust(thrust);
	}

	private void applyThrust(double thrust)
	{
		double acc = thrust * 0.0025;
		Vec3f direction = Vec3f.NEGATIVE_Z.copy();
		direction.rotate(getQuaternion());
		this.addVelocity(direction.getX() * acc, direction.getY() * acc, direction.getZ() * acc);
		velocityModified = true;
	}
	
	public float getTurningFactor()
	{
		return 0.25f;
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
		setQuaternion(new Quaternion(nbt.getFloat("qx"), nbt.getFloat("qy"), nbt.getFloat("qz"), nbt.getFloat("qw")));
		pointOfInterest = new Vec3d(nbt.getDouble("px"), nbt.getDouble("py"), nbt.getDouble("pz"));
		setRollExtra(nbt.getFloat("re"));
	}
	
	class CancelVelocityGoal extends Goal
	{
		double thrust;
		
		public CancelVelocityGoal(double thrust)
		{
			this.thrust = thrust;
		}

		public boolean canStart()
		{
			return goalSelector.getRunningGoals().count() == 0 && getVelocity().length() > 0.05;
		}

		public void start()
		{
		}

		public void stop()
		{
		}

		public void tick()
		{
			updateMotion(getVelocityToCancel(), getVelocity().length() > 0.1 ? thrust : thrust * 0.25, false);
		}

		public boolean shouldContinue()
		{
			return getVelocity().length() > 0.05;
		}
		
		private Vec3d getVelocityToCancel()
		{
			return getVelocity().negate();
		}
	}
	
	class RandomFlightGoal extends Goal
	{
		Vec3d targetDirection;
		double thrust;
		double distance;
		double previousDistance;
		int maximumDuration;
		int range;
		int ticks;
		
		public RandomFlightGoal(double thrust, int maximumDuration, int range)
		{
			this.thrust = thrust;
			this.maximumDuration = maximumDuration;
			this.range = range;
		}

		public boolean canStart()
		{
			return goalSelector.getRunningGoals().count() == 0 && getVelocity().length() < 0.1;
		}

		public void start()
		{
			boolean upBias = getY() < world.getTopPosition(Type.WORLD_SURFACE, getBlockPos()).getY() + 8;
			int dx = random.nextInt(range) - random.nextInt(range);
			int dy = random.nextInt(range) - (upBias ? 0 : random.nextInt(range));
			int dz = random.nextInt(range) - random.nextInt(range);
			pointOfInterest = getPos().add(dx, dy, dz);
			targetDirection = pointOfInterest.subtract(getPos());
			ticks = 0;
		}

		public void stop()
		{
		}

		public void tick()
		{
			updateMotion(targetDirection, thrust, false);
			previousDistance = distance;
			distance = pointOfInterest.subtract(getPos()).length();
			ticks++;
		}

		public boolean shouldContinue()
		{
			return ticks < 40 || (ticks < maximumDuration && distance < previousDistance);
		}
	}
	
	class TrackTargetGoal extends Goal
	{
		Vec3d targetDirection;
		double thrust;
		double distance;
		double previousDistance;
		double initialDistance;
		int maximumDuration;
		boolean rolling;
		float rollSpeed;
		int ticks;
		
		public TrackTargetGoal(double thrust, int maximumDuration, boolean rolling)
		{
			this.thrust = thrust;
			this.maximumDuration = maximumDuration;
			this.rolling = rolling;
			this.rollSpeed = (0.05f + random.nextFloat() * 0.05f) * (random.nextBoolean() ? -1.0f : 1.0f);
			
			if(getTarget() != null)
				this.initialDistance = getTarget().getEyePos().subtract(getPos()).length();
		}

		public boolean canStart()
		{
			return goalSelector.getRunningGoals().count() == 0 && getTarget() != null && getVelocity().length() < 0.1 && getY() > world.getTopPosition(Type.WORLD_SURFACE, getBlockPos()).getY() + 8;
		}

		public void start()
		{
			trackTargetEntity();
			ticks = 0;
		}

		public void stop()
		{
		}

		public void tick()
		{
			if(getTarget() != null)
			{
				trackTargetEntity();
				updateMotion(targetDirection, thrust, true);
				
				if(distance < 16.0)
					setVelocity(getVelocity().multiply(0.98));
			}
			
			setRollExtra(getRollExtra() + rollSpeed);
			previousDistance = distance;
			distance = pointOfInterest.subtract(getPos()).length();
			ticks++;
		}

		public boolean shouldContinue()
		{
			return ticks < 40 || (ticks < maximumDuration && getTarget() != null);
		}
		
		private void trackTargetEntity()
		{
			pointOfInterest = getTarget().getEyePos();
			targetDirection = pointOfInterest.subtract(getPos());
		}
	}
}