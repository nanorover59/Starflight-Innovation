package space.entity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

public class SolarSpectreEntity extends ZeroGravityMobEntity implements AlienMobEntity
{
	private int plasmaBalls;
	private int plasmaBallCooldown;
	private int plasmaBallFireCooldown;

	public SolarSpectreEntity(EntityType<? extends SolarSpectreEntity> entityType, World world)
	{
		super(entityType, world);
		plasmaBalls = 0;
		plasmaBallCooldown = 0;
		plasmaBallFireCooldown = 0;
	}

	public static DefaultAttributeContainer.Builder createSolarSpectreAttributes()
	{
		return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 512.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
	}

	@Override
	protected void initGoals()
	{
		this.goalSelector.add(3, new CancelVelocityGoal(this, 15.0));
		this.goalSelector.add(4, new EscapeFlightGoal(this, 15.0));
		this.goalSelector.add(5, new TrackTargetGoal(this, 15.0, 8000, true));
		this.goalSelector.add(6, new RandomFlightGoal(this, 15.0, 120, 64));
		this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
		this.targetSelector.add(2, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, false));
	}
	
	@Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData)
	{
		if(spawnReason == SpawnReason.NATURAL || spawnReason == SpawnReason.CHUNK_GENERATION)
		{
			int topY = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, getBlockPos()).getY();
			
            if(topY > world.getBottomY())
            	this.setPosition(getPos().getX(), topY + random.nextBetween(128, 256), getPos().getZ());
            else
            	this.setPosition(getPos().getX(), random.nextBetween(0, 256), getPos().getZ());
            
            setQuaternion(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
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
	public void tick()
	{
		super.tick();
		World world = getWorld();

		if(world.isClient)
			return;
		
		if(plasmaBallCooldown > 0)
			plasmaBallCooldown--;

		if(plasmaBallFireCooldown > 0)
			plasmaBallFireCooldown--;
		
		if(plasmaBalls == 0)
		{
			plasmaBallCooldown = 60 + getRandom().nextInt(60);
			plasmaBalls = 4 + getRandom().nextInt(4);
		}

		if(plasmaBalls > 0 && plasmaBallCooldown == 0 && plasmaBallFireCooldown == 0 && pointOfInterest.subtract(getPos()).length() < 64.0 && getTarget() != null && world.getDifficulty() != Difficulty.PEACEFUL)
		{
			playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
			double distance = distanceTo(getTarget());
			double dx = (getTarget().getX() + getTarget().getVelocity().getX()) - getX();
			double dy = (getTarget().getBodyY(0.5) + getTarget().getVelocity().getY()) - getBodyY(0.5);
			double dz = (getTarget().getZ() + getTarget().getVelocity().getZ()) - getZ();
			double ds = Math.sqrt(Math.sqrt(distance)) * 0.5;
			PlasmaBallEntity plasmaBallEntity = new PlasmaBallEntity(world, this, new Vec3d(getRandom().nextTriangular(dx, 2.297 * ds), dy, getRandom().nextTriangular(dz, 2.297 * ds)));
			plasmaBallEntity.setPosition(plasmaBallEntity.getX(), getPos().getY(), plasmaBallEntity.getZ());
			world.spawnEntity(plasmaBallEntity);
			plasmaBallFireCooldown = 5;
			plasmaBalls--;
		}
	}
	
	@Override
	public boolean canSpawn(WorldAccess world, SpawnReason spawnReason)
	{
		return true;
	}
	
	public static boolean canSolarSpectreSpawn(EntityType<SolarSpectreEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random)
	{
		PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 1024, false);
		double solarMultiplier = 1.0;
		
		if(player != null)
		{
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(player.getWorld());
			
			if(data != null)
				solarMultiplier = data.getPlanet().getSolarMultiplier();
		}
		else
			return false;
		
		return random.nextDouble() / solarMultiplier < 0.00005;
    }

	class EscapeFlightGoal extends Goal
	{
		SolarSpectreEntity entity;
		Vec3d targetDirection;
		double thrust;
		int ticks;

		public EscapeFlightGoal(SolarSpectreEntity entity, double thrust)
		{
			this.entity = entity;
			this.thrust = thrust;
		}

		@Override
		public boolean canStart()
		{
			return age > 4000 && entity.getTarget() == null;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return entity.getPos().getY() < entity.getWorld().getTopY();
		}

		@Override
		public void start()
		{
			World world = getWorld();
			int dx = random.nextInt(32) - random.nextInt(32);
			int dy = world.getTopY();
			int dz = random.nextInt(32) - random.nextInt(32);
			pointOfInterest = getPos().add(dx, dy, dz);
			targetDirection = pointOfInterest.subtract(getPos());
			ticks = 0;
		}

		@Override
		public void stop()
		{
			setRemoved(RemovalReason.DISCARDED);
		}

		@Override
		public void tick()
		{
			updateMotion(targetDirection, thrust);
			ticks++;
		}
	}
}