package space.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class CloudSharkEntity extends StratofishEntity
{
	public CloudSharkEntity(EntityType<? extends CloudSharkEntity> entityType, World world)
	{
		super(entityType, world);
	}

	public static DefaultAttributeContainer.Builder createCloudSharkAttributes()
	{
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0);
    }
	
	@Override
    protected void initGoals()
	{
		this.goalSelector.add(2, new AttackGoal());
        this.goalSelector.add(3, new CircleMovementGoal());
        this.targetSelector.add(1, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, true, (entity) -> !(entity.getVehicle() instanceof CloudSharkEntity)));
    }
	
	@Override
	public boolean isPressureSafe(double pressure)
	{
		return pressure > 0.5;
	}

	@Override
	public boolean isTemperatureSafe(int temperature)
	{
		return temperature == 2;
	}

	@Override
	public boolean requiresOxygen()
	{
		return false;
	}
	
	@Override
	public boolean tryAttack(Entity target)
	{
		if(super.tryAttack(target))
		{
			if(target instanceof PlayerEntity)
			{
				((PlayerEntity) target).startRiding(this);
				setTarget(null);
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean canCloudSharkSpawn(EntityType<CloudSharkEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random)
	{
        return random.nextInt(2) == 0;
    }
	
	class AttackGoal extends Goal
	{
		private int cooldown;

		AttackGoal()
		{
		}

		@Override
		public boolean canStart()
		{
			return CloudSharkEntity.this.getTarget() != null;
		}

		@Override
		public void stop()
		{
			CloudSharkEntity.this.circlingCenter = CloudSharkEntity.this.circlingCenter.up(4 + CloudSharkEntity.this.random.nextInt(8));
		}

		@Override
		public void tick()
		{
			CloudSharkEntity.this.targetPosition = CloudSharkEntity.this.getTarget().getEyePos();
			double d = CloudSharkEntity.this.getWidth() * 2.0f * (CloudSharkEntity.this.getWidth() * 2.0f);
	        double e = CloudSharkEntity.this.squaredDistanceTo(CloudSharkEntity.this.getTarget().getX(), CloudSharkEntity.this.getTarget().getY(), CloudSharkEntity.this.getTarget().getZ());

			if(e > d)
				return;

			if(this.cooldown > 0)
			{
				this.cooldown--;
				return;
			}

			this.cooldown = 40;
			CloudSharkEntity.this.tryAttack(CloudSharkEntity.this.getTarget());
		}
	}
}