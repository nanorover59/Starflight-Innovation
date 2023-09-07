package space.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class SolarSpectreEntity extends ZeroGravityMobEntity
{
	private int targetCooldown;
	
	public SolarSpectreEntity(EntityType<? extends SolarSpectreEntity> entityType, World world)
	{
		super(entityType, world);
		targetCooldown = 0;
	}
	
	public static DefaultAttributeContainer.Builder createSolarSpectreAttributes()
	{
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
    }
	
	@Override
    protected void initGoals()
	{
        this.goalSelector.add(1, new CancelVelocityGoal(25.0));
        this.goalSelector.add(2, new EscapeFlightGoal(15.0));
        this.goalSelector.add(3, new TrackTargetGoal(15.0, 200, true));
        this.goalSelector.add(4, new RandomFlightGoal(15.0, 200, 16));
    }
	
	@Override public boolean isDisallowedInPeaceful()
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
		
		if(world.isClient && getTarget() != null)
		{
			/*Vec3f direction = Vec3f.POSITIVE_Z.copy();
			direction.rotate(clientQuaternion);

			for(int i = 0; i < 2; i++)
			{
				double x = getPos().getX() + random.nextDouble() - random.nextDouble();
				double y = getPos().getY() + random.nextDouble() - random.nextDouble();
				double z = getPos().getZ() + random.nextDouble() - random.nextDouble();
				double vx = direction.getX() * 8.0 + (random.nextDouble() - random.nextDouble()) * 0.1;
				double vy = direction.getY() * 8.0 + (random.nextDouble() - random.nextDouble()) * 0.1;
				double vz = direction.getZ() * 8.0 + (random.nextDouble() - random.nextDouble()) * 0.1;
				world.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, vx, vy, vz);
			}*/
			
			double d = distanceTo(getTarget());
			
			if(d < 16.0)
			{
				Vec3d direction = getTarget().getEyePos().subtract(getPos()).normalize();

				for(int i = 0; i < 2; i++)
				{
					double x = getPos().getX() + random.nextDouble() - random.nextDouble();
					double y = getPos().getY() + random.nextDouble() - random.nextDouble();
					double z = getPos().getZ() + random.nextDouble() - random.nextDouble();
					double vx = direction.getX() * 4.0 + (random.nextDouble() - random.nextDouble()) * 0.1;
					double vy = direction.getY() * 4.0 + (random.nextDouble() - random.nextDouble()) * 0.1;
					double vz = direction.getZ() * 4.0 + (random.nextDouble() - random.nextDouble()) * 0.1;
					world.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, vx, vy, vz);
				}
			}
		}
		
		if(targetCooldown > 0)
			targetCooldown--;
		
		if(targetCooldown == 0 && world.getDifficulty() != Difficulty.PEACEFUL)
		{
			PlayerEntity nearestPlayer = world.getClosestPlayer(this, 128.0);

			if(nearestPlayer != null)
				setTarget(nearestPlayer);
			else
				setTarget(null);
			
			if(getTarget() != null && distanceTo(getTarget()) < 3.0)
			{
				if(tryAttack(getTarget()))
				{
					playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
					targetCooldown = 50;
				}
			}
		}
		else
			setTarget(null);
	}
	
	class EscapeFlightGoal extends Goal
	{
		Vec3d targetDirection;
		double thrust;
		int ticks;
		
		public EscapeFlightGoal(double thrust)
		{
			this.thrust = thrust;
		}

		public boolean canStart()
		{
			return goalSelector.getRunningGoals().count() == 0 && age > 6000;
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
			return ticks < 600;
		}
	}
}