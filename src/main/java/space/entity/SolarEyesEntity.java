package space.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.particle.StarflightParticleTypes;

public class SolarEyesEntity extends MobEntity implements AlienMobEntity
{
	private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(SolarEyesEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private LivingEntity entityCollided = null;
	
	public SolarEyesEntity(EntityType<? extends MobEntity> entityType, World world)
	{
        super(entityType, world);
    }
	
	@Override
    protected void initGoals()
	{
        this.targetSelector.add(1, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
    }
	
	public static DefaultAttributeContainer.Builder createSolarEyesAttributes()
	{
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 2.0);
    }
	
	@Override
	protected void initDataTracker(DataTracker.Builder builder)
	{
		super.initDataTracker(builder);
		builder.add(ANGER_TIME, 0);
	}
	
	public int getAngerTime()
	{
		return this.dataTracker.get(ANGER_TIME);
	}

	public void increaseAngerTime()
	{
		int angerTime = getAngerTime();
		
		if(angerTime < 63)
			this.dataTracker.set(ANGER_TIME, angerTime + 1);
	}
	
	public void decreaseAngerTime()
	{
		int angerTime = getAngerTime();
		
		if(angerTime > 0)
			this.dataTracker.set(ANGER_TIME, angerTime - 1);
	}
	
	@Override
	public boolean isPressureSafe(double pressure)
	{
		return pressure < 0.01;
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
		return 16;
	}
	
	@Override
	public float getRadiationStrength()
	{
		return getAngerTime() * 0.04f;
	}
	
	@Override
	public boolean isInvisible()
	{
		return true;
	}
	
	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition)
	{
	}
	
	@Override
    public boolean collidesWith(Entity other)
	{
		World world = getWorld();
		
		if(!world.isClient() && other instanceof LivingEntity && !(other instanceof AlienMobEntity && ((AlienMobEntity) other).getRadiationRange() > 0))
		{
			entityCollided = (LivingEntity) other;
			return false;
		}
		
        return super.collidesWith(other);
    }

	@Override
	public void tick()
	{
		super.tick();
		World world = getWorld();
		
		if(world.isClient)
		{
			if(this.random.nextInt(8) == 0)
			{
				int count = 1 + random.nextInt(1);
				
				for(int i = 0; i < count; i++)
				{
					double spread = 1.0;
					double x = getX() + (random.nextDouble() - random.nextDouble()) * spread;
					double y = getY() + (random.nextDouble() - random.nextDouble()) * spread;
					double z = getZ() + (random.nextDouble() - random.nextDouble()) * spread;
					world.addParticle(StarflightParticleTypes.EYE, true, x, y, z, 0.0, 0.0, 0.0);
				}
			}
			
			return;
		}
		
		this.setNoGravity(true);
		this.setVelocity(this.getVelocity().multiply(0.98));
		
		if(this.random.nextInt(400) == 0 || horizontalCollision || verticalCollision)
			this.setVelocity((this.random.nextDouble() - 0.5) * 0.25, (this.random.nextDouble() - 0.5) * 0.25, (this.random.nextDouble() - 0.5) * 0.25);
		
		if(getTarget() != null)
		{
			Vec3d difference = getTarget().getEyePos().subtract(getPos());
			double distance = difference.length();
			addVelocity(difference.normalize().multiply(getAngerTime() > 48 ? Math.max(0.02, 0.25 / (distance * distance)) : 0.001));
			increaseAngerTime();
		}
		else
			decreaseAngerTime();
		
		if(entityCollided != null || world.getLightLevel(LightType.SKY, getBlockPos()) > 4)
		{
			if(entityCollided != null)
				tryAttack(entityCollided);
			
			((ServerWorld) world).spawnParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1 + world.random.nextInt(3), 0.5, 0.5, 0.5, 0.01);
			remove(RemovalReason.DISCARDED);
		}
	}
	
	public static boolean canSolarEyesSpawn(EntityType<SolarEyesEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random)
	{
        return random.nextInt(10) == 0 && !world.isSkyVisible(pos) && world.isAir(pos);
    }
}