package space.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import space.particle.StarflightParticleTypes;
import space.util.AirUtil;
import space.util.IWorldMixin;

public class SolarEyesEntity extends MobEntity implements AlienMobEntity
{
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
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 2.0);
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
		return 0.5f;
	}
	
	@Override
	public boolean isInvisible()
	{
		return true;
	}
	
	@Override
	public boolean canSpawn(WorldView world)
	{
		double p = AirUtil.getAirResistanceMultiplier(getWorld(), ((IWorldMixin) getWorld()).getPlanetDimensionData(), getBlockPos());
        return !world.isSkyVisible(getBlockPos()) && isPressureSafe(p);
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
				double spread = 4.0;
				double x = getX() + (random.nextDouble() - random.nextDouble()) * spread;
				double y = getY() + (random.nextDouble() - random.nextDouble()) * spread;
				double z = getZ() + (random.nextDouble() - random.nextDouble()) * spread;
				world.addParticle(StarflightParticleTypes.EYE, true, x, y, z, 0.0, 0.0, 0.0);
			}
			
			return;
		}
		
		this.setNoGravity(true);
		this.setVelocity(this.getVelocity().multiply(0.98));
		
		if(getTarget() == null)
		{
			if(this.random.nextInt(400) == 0 || horizontalCollision || verticalCollision)
				this.setVelocity(this.random.nextDouble() - 0.5, this.random.nextDouble() - 0.5, this.random.nextDouble() - 0.5);
		}
		else
		{
			Vec3d difference = getTarget().getPos().subtract(getPos());
			double distance = difference.length();
			addVelocity(difference.normalize().multiply(1.0 / (distance * distance)));
		}
		
		if(entityCollided != null || world.getLightLevel(LightType.SKY, getBlockPos()) > 4)
		{
			if(entityCollided != null)
				entityCollided.damage(getDamageSources().mobAttack(this), 2.0f);
			
			((ServerWorld) world).spawnParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1 + world.random.nextInt(3), 0.5, 0.5, 0.5, 0.01);
			remove(RemovalReason.DISCARDED);
		}
	}
}