package space.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class CaveLampreyEntity extends WaterCreatureEntity
{
	protected CaveLampreyEntity(EntityType<? extends CaveLampreyEntity> entityType, World world)
	{
		super(entityType, world);
		this.moveControl = new CaveLampreyEntity.CaveLampreyMoveControl(this);
	}

	public static DefaultAttributeContainer.Builder createCaveLampreyAttributes()
	{
		return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 5.0);
	}
	
	@Override
	public boolean canImmediatelyDespawn(double distanceSquared)
	{
		return !this.hasCustomName();
	}

	@Override
	public int getLimitPerChunk()
	{
		return 4;
	}

	@Override
	protected void initGoals()
	{
		this.goalSelector.add(3, new AttackGoal(this));
		this.goalSelector.add(4, new CaveLampreyEntity.SwimToRandomPlaceGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
	}

	@Override
	protected EntityNavigation createNavigation(World world)
	{
		return new SwimNavigation(this, world);
	}

	@Override
	public void travel(Vec3d movementInput)
	{
		if(this.canMoveVoluntarily() && this.isTouchingWater())
		{
			this.updateVelocity(0.06f, movementInput);
			this.move(MovementType.SELF, this.getVelocity());
			this.setVelocity(this.getVelocity().multiply(0.9));
			
			if(this.getTarget() == null)
				this.setVelocity(this.getVelocity().add(0.0, -0.005, 0.0));
		}
		else
			super.travel(movementInput);
	}

	@Override
	public void tickMovement()
	{
		if(!this.isTouchingWater() && this.isOnGround() && this.verticalCollision)
		{
			this.setVelocity(this.getVelocity().add((double) ((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F), 0.4F, (double) ((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F)));
			this.setOnGround(false);
			this.velocityDirty = true;
			this.playSound(this.getFlopSound());
		}

		super.tickMovement();
	}

	protected boolean hasSelfControl()
	{
		return true;
	}
	
	@Override
	protected SoundEvent getSwimSound()
	{
		return SoundEvents.ENTITY_FISH_SWIM;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state)
	{
	}
	
	protected SoundEvent getFlopSound()
	{
		return SoundEvents.ENTITY_SALMON_FLOP;
	}
	
	public int getSwimTickOffset()
	{
        return this.getId() * 3;
    }
	
	@Override
	public boolean canSpawn(WorldAccess world, SpawnReason spawnReason)
	{
		return true;
	}
	
	public static boolean canCaveLampreySpawn(EntityType<CaveLampreyEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random)
	{
        return random.nextInt(32) == 0 && world.getFluidState(pos).isIn(FluidTags.WATER) && world.getBlockState(pos.up()).isOf(Blocks.WATER);
    }

	static class CaveLampreyMoveControl extends MoveControl
	{
		private final CaveLampreyEntity lamprey;

		CaveLampreyMoveControl(CaveLampreyEntity owner)
		{
			super(owner);
			this.lamprey = owner;
		}

		@Override
		public void tick()
		{
			if(this.lamprey.isSubmergedIn(FluidTags.WATER))
				this.lamprey.setVelocity(this.lamprey.getVelocity().add(0.0, 0.005, 0.0));

			if(this.state == MoveControl.State.MOVE_TO && !this.lamprey.getNavigation().isIdle())
			{
				float f = (float) (this.speed * this.lamprey.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
				this.lamprey.setMovementSpeed(MathHelper.lerp(0.125F, this.lamprey.getMovementSpeed(), f));
				double d = this.targetX - this.lamprey.getX();
				double e = this.targetY - this.lamprey.getY();
				double g = this.targetZ - this.lamprey.getZ();
				
				if(e != 0.0)
				{
					double h = Math.sqrt(d * d + e * e + g * g);
					this.lamprey.setVelocity(this.lamprey.getVelocity().add(0.0, (double) this.lamprey.getMovementSpeed() * (e / h) * 0.1, 0.0));
				}

				if(d != 0.0 || g != 0.0)
				{
					float i = (float) (MathHelper.atan2(g, d) * 180.0F / (float) Math.PI) - 90.0F;
					this.lamprey.setYaw(this.wrapDegrees(this.lamprey.getYaw(), i, 90.0F));
					this.lamprey.bodyYaw = this.lamprey.getYaw();
				}
			}
			else
				this.lamprey.setMovementSpeed(0.0F);
		}
	}

	static class SwimToRandomPlaceGoal extends SwimAroundGoal
	{
		private final CaveLampreyEntity lamprey;

		public SwimToRandomPlaceGoal(CaveLampreyEntity lamprey)
		{
			super(lamprey, 1.0, 40);
			this.lamprey = lamprey;
		}

		@Override
		public boolean canStart()
		{
			return this.lamprey.hasSelfControl() && super.canStart();
		}
	}
}