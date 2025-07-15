package space.entity;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.QuaternionUtil;

public class SolarSpectreEntity extends PathAwareEntity implements AlienMobEntity
{
	private static final TrackedData<Float> QX = DataTracker.registerData(SolarSpectreEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> QY = DataTracker.registerData(SolarSpectreEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> QZ = DataTracker.registerData(SolarSpectreEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> QW = DataTracker.registerData(SolarSpectreEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> ROLL_EXTRA = DataTracker.registerData(SolarSpectreEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> LIMB_ANGLE = DataTracker.registerData(SolarSpectreEntity.class, TrackedDataHandlerRegistry.FLOAT);
	public Vec3d pointOfInterest;
	public Quaternionf clientQuaternion;
	public Quaternionf clientQuaternionPrevious;
	public float clientRollExtra;
	public float clientRollExtraPrevious;
	public float limbAngle;
	public float limbAnglePrevious;
	public int clientInterpolationSteps;
	
	private int plasmaBalls;
	private int plasmaBallCooldown;
	private int plasmaBallFireCooldown;
	private int movementTimer;

	public SolarSpectreEntity(EntityType<? extends SolarSpectreEntity> entityType, World world)
	{
		super(entityType, world);
		plasmaBalls = 0;
		plasmaBallCooldown = 0;
		plasmaBallFireCooldown = 0;
		movementTimer = 0;
	}

	public static DefaultAttributeContainer.Builder createSolarSpectreAttributes()
	{
		return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 512.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
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
		builder.add(LIMB_ANGLE, Float.valueOf(0.0f));
	}

	@Override
	protected void initGoals()
	{
		this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
		this.targetSelector.add(2, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
		this.targetSelector.add(3, new ActiveTargetGoal<SeleniteEntity>(this, SeleniteEntity.class, true));
	}
	
	@Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData)
	{
		if(spawnReason == SpawnReason.NATURAL || spawnReason == SpawnReason.CHUNK_GENERATION)
		{
			int topY = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, getBlockPos()).getY();
			
            if(topY > world.getBottomY())
            	this.setPosition(getPos().getX(), topY + random.nextBetween(64, 128), getPos().getZ());
            else
            	this.setPosition(getPos().getX(), random.nextBetween(0, 256), getPos().getZ());
            
            setQuaternion(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
            setPointOfInterest(64);
		}
		else
			this.setPosition(getPos().getX(), getPos().getY() + 2.5, getPos().getZ());
		
        return super.initialize(world, difficulty, spawnReason, entityData);
	}

	@Override
	public boolean isPressureSafe(double pressure)
	{
		return true;
	}

	@Override
	public boolean isTemperatureSafe(int temperature)
	{
		return true;
	}

	@Override
	public boolean requiresOxygen()
	{
		return false;
	}

	@Override
	public int getRadiationRange()
	{
		return 32;
	}

	@Override
	public float getRadiationStrength()
	{
		return 2.0f;
	}
	
	/*@Override
	public boolean shouldRender(double distance)
	{
		return true;
	}
	
	@Override
	public boolean canImmediatelyDespawn(double distance)
	{
		return false;
	}*/

	@Override
	public boolean isDisallowedInPeaceful()
	{
		return true;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source)
	{
		return SoundEvents.ENTITY_GENERIC_HURT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ENTITY_GENERIC_DEATH;
	}

	@Override
	protected float getSoundVolume()
	{
		return 2.0f;
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
		super.tick();
		World world = getWorld();

		if(world.isClient)
		{
			this.clientQuaternionPrevious = this.clientQuaternion;
			this.clientQuaternion = this.getQuaternion();
			this.clientRollExtraPrevious = this.clientRollExtra;
			this.clientRollExtra = this.getRollExtra();
			this.limbAnglePrevious = this.limbAngle;
			this.limbAngle = this.getLimbAngle();
			return;
		}
		
		if(plasmaBallCooldown > 0)
			plasmaBallCooldown--;

		if(plasmaBallFireCooldown > 0)
			plasmaBallFireCooldown--;
		
		if(movementTimer > 0)
			movementTimer--;
		
		if(plasmaBalls == 0)
		{
			plasmaBallCooldown = 60 + getRandom().nextInt(20);
			plasmaBalls = 4 + getRandom().nextInt(2);
		}

		if(plasmaBalls > 0 && plasmaBallCooldown == 0 && plasmaBallFireCooldown == 0 && getTarget() != null && getLimbAngle() < 0.1)
		{
			playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 10.0f, 1.0f);
			double distance = distanceTo(getTarget());
			double dx = getTarget().getX()- getX();
			double dy = getTarget().getEyeY() - getY();
			double dz = getTarget().getZ() - getZ();
			PlasmaBallEntity plasmaBallEntity = new PlasmaBallEntity(world, this, new Vec3d(getRandom().nextTriangular(dx, Math.sqrt(Math.sqrt(distance)) * 0.01), getRandom().nextTriangular(dy, Math.sqrt(Math.sqrt(distance)) * 0.01), getRandom().nextTriangular(dz, Math.sqrt(Math.sqrt(distance)) * 0.01)));
			plasmaBallEntity.setPosition(getX(), getY(), getZ());
			world.spawnEntity(plasmaBallEntity);
			plasmaBallFireCooldown = 10;
			plasmaBalls--;
			Vector3f recoil = new Vector3f(0.0f, 0.0f, 1.0f).rotate(getQuaternion()).mul(0.25f);
			addVelocity(recoil.x(), recoil.y(), recoil.z());
		}
		
		if(getTarget() != null)
			pointOfInterest = getTarget().getEyePos();
		
		if(movementTimer == 0 || (pointOfInterest != null && pointOfInterest.subtract(getPos()).length() < 4.0))
		{
			movementTimer = 60 + getRandom().nextInt(60);
			
			if(random.nextBoolean())
				setPointOfInterest(64);
			else
				pointOfInterest = null;
		}
		
		double drag = 0.98;
		
		if(pointOfInterest != null)
		{
			Quaternionf pointOfInterestRotation = new Quaternionf();
			new Vector3f(0.0f, 0.0f, -1.0f).rotationTo(pointOfInterest.subtract(getPos()).normalize().toVector3f(), pointOfInterestRotation);
			setQuaternion(QuaternionUtil.interpolate(getQuaternion().normalize(), pointOfInterestRotation, 0.25f));
			float distance = (float) pointOfInterest.subtract(getPos()).length();
			float acc = Math.clamp(distance * 0.001f, 0.0f, 0.1f);
			Vector3f accVector = new Vector3f(0.0f, 0.0f, -1.0f).rotate(getQuaternion()).mul(acc);
			addVelocity(new Vec3d(accVector.x(), accVector.y(), accVector.z()));
			
			if(getTarget() != null && distanceTo(getTarget()) < 32.0)
				drag = 0.9;
		}
		
		terrainBounce();
		setVelocity(getVelocity().multiply(drag));
		velocityModified = true;
		
		if(getVelocity().lengthSquared() > 0.05 && !(getTarget() != null && distanceTo(getTarget()) < 32.0))
			changeLimbAngle(0.01f);
		else
			changeLimbAngle(-0.01f);
		
		setRollExtra(getRollExtra() + 1.0f);
	}
	
	public static boolean canSolarSpectreSpawn(EntityType<SolarSpectreEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random)
	{
		PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 256, false);
		double solarMultiplier = 1.0;
		
		if(player != null)
		{
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(player.getWorld());
			
			if(data != null)
				solarMultiplier = data.getPlanet().getSolarMultiplier();
		}
		else
			return false;
		
		return random.nextDouble() / solarMultiplier < 0.0001;
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
	
	public void changeLimbAngle(float amount)
	{
		float angle = Math.clamp(getLimbAngle() + amount, 0.0f, 1.0f);
		this.dataTracker.set(LIMB_ANGLE, angle);
	}
	
	public float getLimbAngle()
	{
		return this.dataTracker.get(LIMB_ANGLE).floatValue();
	}
	
	/*public void updateMotion(Vec3d direction, double thrust)
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
	}*/
	
	public void setPointOfInterest(int range)
	{
		if(getTarget() != null)
		{
			pointOfInterest = getTarget().getEyePos();
			return;
		}
		
		int topY = getWorld().getTopPosition(Type.MOTION_BLOCKING_NO_LEAVES, getBlockPos()).getY();
		Vector3f vector = new Vector3f(0.0f, 0.0f, -1.0f).rotate(getQuaternion()).mul(random.nextBetween(range / 2, range));
		vector.rotateX((random.nextFloat() - random.nextFloat()) * (MathHelper.PI / 8.0f));
		vector.rotateY((random.nextFloat() - random.nextFloat()) * (MathHelper.PI / 8.0f));
		vector.rotateZ((random.nextFloat() - random.nextFloat()) * (MathHelper.PI / 8.0f));
		pointOfInterest = getPos().add(vector.x(), vector.y(), vector.z());
		
		while(pointOfInterest.getY() < topY + 32)
			pointOfInterest = pointOfInterest.add(0.0, 1.0, 0.0);
	}
	
	public void terrainBounce()
	{
		double acc = 0.1;
		Vec3d velocity = this.getVelocity();
		Box boundingBox = this.getBoundingBox();

		for(Direction direction : Direction.values())
		{
			Vec3d offset = Vec3d.of(direction.getVector());
			Box expandedBox = boundingBox.offset(offset);

			if(getWorld().isSpaceEmpty(expandedBox))
				continue;
			
			switch(direction)
			{
				case NORTH:
					velocity = velocity.add(0.0, 0.0, acc);
					break;
				case SOUTH:
					velocity = velocity.add(0.0, 0.0, -acc);
					break;
				case EAST:
					velocity = velocity.add(-acc, 0.0, 0.0);
					break;
				case WEST:
					velocity = velocity.add(acc, 0.0, 0.0);
					break;
				case UP:
					velocity = velocity.add(0.0, -acc, 0.0);
					break;
				case DOWN:
					velocity = velocity.add(0.0, acc, 0.0);
					break;
			}
		}
		
		setVelocity(velocity);
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat("qx", this.dataTracker.get(QX).floatValue());
		nbt.putFloat("qy", this.dataTracker.get(QY).floatValue());
		nbt.putFloat("qz", this.dataTracker.get(QZ).floatValue());
		nbt.putFloat("qw", this.dataTracker.get(QW).floatValue());
		nbt.putFloat("re", getRollExtra());
		
		if(pointOfInterest != null)
		{
			nbt.putDouble("px", pointOfInterest.getX());
			nbt.putDouble("py", pointOfInterest.getY());
			nbt.putDouble("pz", pointOfInterest.getZ());
		}
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		setQuaternion(new Quaternionf(nbt.getFloat("qx"), nbt.getFloat("qy"), nbt.getFloat("qz"), nbt.getFloat("qw")).normalize());
		setRollExtra(nbt.getFloat("re"));
		
		if(nbt.contains("px"))
			pointOfInterest = new Vec3d(nbt.getDouble("px"), nbt.getDouble("py"), nbt.getDouble("pz"));
	}
}