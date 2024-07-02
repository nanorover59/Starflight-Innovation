package space.entity;

import net.minecraft.entity.EntityType;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

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
		return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 128.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
	}

	@Override
	protected void initGoals()
	{
		this.goalSelector.add(3, new CancelVelocityGoal(this, 25.0));
		this.goalSelector.add(4, new EscapeFlightGoal(this, 15.0));
		this.goalSelector.add(5, new TrackTargetGoal(this, 15.0, 400, true));
		this.goalSelector.add(6, new RandomFlightGoal(this, 15.0, 400, 32));
		this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
		this.targetSelector.add(2, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
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
		return 64;
	}

	@Override
	public float getRadiationStrength()
	{
		return 1.0f;
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
			plasmaBallCooldown = 40 + getRandom().nextInt(60);
			plasmaBalls = 4 + getRandom().nextInt(8);
		}

		if(plasmaBalls > 0 && plasmaBallCooldown == 0 && plasmaBallFireCooldown == 0 && getTarget() != null && world.getDifficulty() != Difficulty.PEACEFUL)
		{
			playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
			double distance = distanceTo(getTarget());
			double dx = getTarget().getX() - getX();
			double dy = getTarget().getBodyY(0.5) - getPos().getY();
			double dz = getTarget().getZ() - getZ();
			double ds = Math.sqrt(Math.sqrt(distance)) * 0.5;
			PlasmaBallEntity plasmaBallEntity = new PlasmaBallEntity(world, this, new Vec3d(getRandom().nextTriangular(dx, 2.297 * ds), dy, getRandom().nextTriangular(dz, 2.297 * ds)));
			plasmaBallEntity.setPosition(plasmaBallEntity.getX(), getPos().getY(), plasmaBallEntity.getZ());
			world.spawnEntity(plasmaBallEntity);
			plasmaBallFireCooldown = 2 + getRandom().nextInt(3);
			plasmaBalls--;
		}
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

		public boolean canStart()
		{
			return age > 6000;
		}

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

		public void stop()
		{
			setRemoved(RemovalReason.DISCARDED);
		}

		public void tick()
		{
			updateMotion(targetDirection, thrust, false);
			ticks++;
		}

		public boolean shouldContinue()
		{
			return true;
		}
	}
}