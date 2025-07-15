package space.entity;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.util.StarflightSoundEvents;

public class LunarMechEntity extends HostileEntity implements AlienMobEntity
{
	private static final TrackedData<Integer> FLIP_TIMER = DataTracker.registerData(LunarMechEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> ARMING_STATE = DataTracker.registerData(LunarMechEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> ARMING_TIMER = DataTracker.registerData(LunarMechEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> BEAM_TARGET_ID = DataTracker.registerData(LunarMechEntity.class, TrackedDataHandlerRegistry.INTEGER);
	
	public AnimationState walkingAnimationState = new AnimationState();
	public AnimationState flipAnimationState = new AnimationState();
	public AnimationState armAnimationState = new AnimationState();
	public AnimationState disarmAnimationState = new AnimationState();
	
	private LivingEntity cachedBeamTarget;
	private int beamTicks;
	private boolean beamComplete;
	
	public LunarMechEntity(EntityType<? extends LunarMechEntity> entityType, World world)
	{
		super(entityType, world);
	}

	public static DefaultAttributeContainer.Builder createLunarMechAttributes()
	{
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 128.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.8).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 8.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0);
    }
	
	@Override
    protected void initGoals()
	{
		this.goalSelector.add(1, new FlipAttackGoal(this, 1.0));
		this.goalSelector.add(2, new BeamAttackGoal(this, 1.0, 20, 15.0f));
		this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0));
		this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
	}
	
	@Override
    protected void initDataTracker(DataTracker.Builder builder)
	{
        super.initDataTracker(builder);
        builder.add(FLIP_TIMER, 0);
        builder.add(ARMING_STATE, 0);
        builder.add(ARMING_TIMER, 0);
        builder.add(BEAM_TARGET_ID, 0);
    }
	
	@Override
	protected SoundEvent getAmbientSound()
	{
		return StarflightSoundEvents.NOISE_SOUND_EVENT;
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
	protected void playStepSound(BlockPos pos, BlockState state)
	{
		this.playSound(SoundEvents.BLOCK_COPPER_STEP, 0.75f, 1.0f);
	}
	
	@Override
	public boolean canImmediatelyDespawn(double distanceSquared)
	{
		return false;
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
		return 16;
	}
	
	@Override
	public float getRadiationStrength()
	{
		return 0.5f;
	}
	
	public void setFlipTimer(int timer)
	{
		dataTracker.set(FLIP_TIMER, timer);
	}
	
	public int getFlipTimer()
	{
		return dataTracker.get(FLIP_TIMER);
	}
	
	public void setArmingState(int state)
	{
		dataTracker.set(ARMING_STATE, state);
	}
	
	public int getArmingState()
	{
		return dataTracker.get(ARMING_STATE);
	}
	
	public void setArmingTimer(int timer)
	{
		dataTracker.set(ARMING_TIMER, timer);
	}
	
	public int getArmingTimer()
	{
		return dataTracker.get(ARMING_TIMER);
	}

	void setBeamTarget(int entityId)
	{
		this.dataTracker.set(BEAM_TARGET_ID, entityId);
	}

	public boolean hasBeamTarget()
	{
		return this.dataTracker.get(BEAM_TARGET_ID) != 0;
	}

	@Nullable
	public LivingEntity getBeamTarget()
	{
		if(!this.hasBeamTarget())
			return null;
		else if(this.getWorld().isClient())
		{
			if(this.cachedBeamTarget != null)
				return this.cachedBeamTarget;
			else
			{
				Entity entity = this.getWorld().getEntityById(this.dataTracker.get(BEAM_TARGET_ID));
				
				if(entity instanceof LivingEntity)
				{
					this.cachedBeamTarget = (LivingEntity) entity;
					return this.cachedBeamTarget;
				}
				else
					return null;
			}
		}
		else
			return this.getTarget();
	}
	
	public float getBeamProgress(float tickDelta)
	{
		return ((float) this.beamTicks + tickDelta) / 80.0f;
	}

	public float getBeamTicks()
	{
		return (float) this.beamTicks;
	}

	@Override
	public void tick()
	{
		super.tick();
		
		if(getWorld().isClient())
		{
			if(this.getVelocity().horizontalLengthSquared() > 0.0 && this.isOnGround())
				walkingAnimationState.startIfNotRunning(age);
			else
				walkingAnimationState.stop();
			
			if(getFlipTimer() > 0)
				flipAnimationState.startIfNotRunning(age);
			else
				flipAnimationState.stop();

			if(getArmingState() == 0)
			{
				armAnimationState.stop();
				disarmAnimationState.stop();
			}
			else if(getArmingState() == 1)
			{
				walkingAnimationState.stop();
				armAnimationState.startIfNotRunning(age);
			}
			else if(getArmingState() == 3)
			{
				walkingAnimationState.stop();
				armAnimationState.stop();
				disarmAnimationState.startIfNotRunning(age);
			}
		}
		else
		{
			int armingTimer = getArmingTimer();
			
			if(armingTimer > 0)
				setArmingTimer(armingTimer - 1);
			
			if(armingTimer == 1)
			{
				int armingState = getArmingState();
				
				if(armingState == 1)
					setArmingState(2);
				else if(armingState == 3)
					setArmingState(0);
			}
			
			if(this.getTarget() == null)
			{
				this.setBeamTarget(0);
				this.beamTicks = 0;
				
				if(this.getArmingState() > 0 && this.getArmingState() < 3)
				{
					this.setArmingState(3);
					this.setArmingTimer(25);
				}
			}
		}
	}
	
	private class FlipAttackGoal extends Goal
	{
		private final LunarMechEntity actor;
		private final double speed;
		
		public FlipAttackGoal(LunarMechEntity actor, double speed)
		{
			this.actor = actor;
			this.speed = speed;
			this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
		}

		@Override
		public boolean canStart()
		{
			return this.actor.getTarget() != null && (distanceTo(getTarget()) < 4.0 || this.actor.beamComplete);
		}
		
		@Override
		public boolean shouldContinue()
		{
			return !canStop();
		}

		@Override
		public boolean canStop()
		{
			return getTarget() == null || distanceTo(getTarget()) > 16.0 || this.actor.getFlipTimer() == 0;
		}
		
		@Override
		public void start()
		{
			super.start();
			this.actor.setAttacking(true);
			this.actor.setFlipTimer(-1);
		}
		
		@Override
		public void stop()
		{
			super.stop();
			this.actor.beamComplete = false;
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}

		@Override
		public void tick()
		{
			LivingEntity livingEntity = this.actor.getTarget();
			this.actor.getLookControl().lookAt(livingEntity, 30.0F, 30.0F);
			double d = (double) (this.actor.getWidth() * 2.0F * this.actor.getWidth() * 2.0F);
			double e = this.actor.squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());

			if(this.actor.getFlipTimer() < 0)
				this.actor.getNavigation().startMovingTo(livingEntity, speed);
			else if(this.actor.getFlipTimer() > 0)
				this.actor.setFlipTimer(this.actor.getFlipTimer() - 1);

			if(e <= d && this.actor.getFlipTimer() == -1)
			{
				this.actor.setFlipTimer(20);
				this.actor.tryAttack(livingEntity);
				livingEntity.addVelocity(0.0, 0.25, 0.0);
			}
		}
	}
	
	private class BeamAttackGoal extends Goal
	{
		private final LunarMechEntity actor;
		private final double speed;
		private int attackInterval;
		private final float squaredRange;
		private int cooldown = -1;
		private int targetSeeingTicker;

		public BeamAttackGoal(LunarMechEntity actor, double speed, int attackInterval, float range)
		{
			this.actor = actor;
			this.speed = speed;
			this.attackInterval = attackInterval;
			this.squaredRange = range * range;
			this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
		}

		@Override
		public boolean canStart()
		{
			return this.actor.getTarget() != null && !this.actor.beamComplete;
		}

		@Override
		public boolean shouldContinue()
		{
			return !canStop();
		}

		@Override
		public boolean canStop()
		{
			return this.actor.getArmingState() == 0 && (this.actor.beamComplete || getTarget() == null || (getTarget() != null && distanceTo(getTarget()) < 4.0));
		}

		@Override
		public void start()
		{
			super.start();
			this.actor.setAttacking(true);
		}

		@Override
		public void stop()
		{
			super.stop();
			this.actor.setAttacking(false);
			this.targetSeeingTicker = 0;
			this.cooldown = -1;
		}

		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}

		@Override
		public void tick()
		{
			LivingEntity livingEntity = this.actor.getTarget();
			
			if(livingEntity != null)
			{
				double d = this.actor.squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
				boolean bl = this.actor.getVisibilityCache().canSee(livingEntity);
				boolean bl2 = this.targetSeeingTicker > 0;
				
				if(bl != bl2)
					this.targetSeeingTicker = 0;

				if(bl)
					this.targetSeeingTicker++;
				else
					this.targetSeeingTicker--;
				
				boolean bl3 = !(d > (double) this.squaredRange) && this.targetSeeingTicker >= 20 && d > 100.0 && !this.actor.beamComplete;
				
				if(this.actor.getArmingState() == 0 && bl3)
				{
					this.actor.setArmingState(1);
					this.actor.setArmingTimer(15);
					this.cooldown = attackInterval;
				}
				else if(this.actor.getArmingState() == 2 && !bl3)
				{
					this.actor.setArmingState(3);
					this.actor.setArmingTimer(15);
				}
				
				if(this.actor.getArmingState() == 0 && !bl3)
					this.actor.getNavigation().startMovingTo(livingEntity, this.speed);
				else
					this.actor.getNavigation().stop();
				
				this.actor.getLookControl().lookAt(livingEntity, 30.0F, 30.0F);
				
				if(this.actor.getArmingState() == 2 && this.actor.getArmingTimer() == 0 && bl)
				{
					this.actor.setBeamTarget(livingEntity.getId());
					
					if(this.actor.beamTicks < 80)
						this.actor.beamTicks++;
					else
						this.actor.beamComplete = true;
					
					if(this.cooldown == 0)
					{
						actor.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 4.0F, 1.0F / (actor.getRandom().nextFloat() * 0.4F + 0.8F));
						this.cooldown = this.attackInterval;
					}
					else
						this.cooldown--;
				}
				else
				{
					this.actor.setBeamTarget(0);
					this.actor.beamTicks = 0;
				}
			}
		}
	}
}