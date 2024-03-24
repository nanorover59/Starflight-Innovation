package space.entity;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class StratofishEntity extends FlyingEntity
{
	Vec3d targetPosition = Vec3d.ZERO;
    BlockPos circlingCenter = BlockPos.ORIGIN;
	
	public StratofishEntity(EntityType<? extends StratofishEntity> entityType, World world)
	{
		super(entityType, world);
		this.moveControl = new StratofishMoveControl(this);
	}
	
	public static DefaultAttributeContainer.Builder createStratofishAttributes()
	{
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 5.0);
    }
	
	@Override
    protected void initGoals()
	{
        this.goalSelector.add(1, new CircleMovementGoal());
    }
	
	@Override
    public boolean shouldRender(double distance)
	{
        return true;
    }

	public int getWingFlapTickOffset()
	{
        return this.getId() * 3;
    }
	
	@Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt)
	{
        this.circlingCenter = this.getBlockPos().up(5);
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
	}
	
	@Override
    public void readCustomDataFromNbt(NbtCompound nbt)
	{
        super.readCustomDataFromNbt(nbt);
        
        if(nbt.contains("x"))
            this.circlingCenter = new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt)
    {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("x", this.circlingCenter.getX());
        nbt.putInt("y", this.circlingCenter.getY());
        nbt.putInt("z", this.circlingCenter.getZ());
    }
	
	class StratofishMoveControl extends MoveControl
	{
		private float targetSpeed;

		public StratofishMoveControl(MobEntity owner)
		{
			super(owner);
			this.targetSpeed = 0.1f;
		}

		@Override
		public void tick()
		{
			if(StratofishEntity.this.horizontalCollision)
			{
				StratofishEntity.this.setYaw(StratofishEntity.this.getYaw() + 180.0f);
				this.targetSpeed = 0.1f;
			}
			
			double d = StratofishEntity.this.targetPosition.x - StratofishEntity.this.getX();
			double e = StratofishEntity.this.targetPosition.y - StratofishEntity.this.getY();
			double f = StratofishEntity.this.targetPosition.z - StratofishEntity.this.getZ();
			double g = Math.sqrt(d * d + f * f);
			
			if(Math.abs(g) > (double) 1.0E-5f)
			{
				double h = 1.0 - Math.abs(e * (double) 0.7f) / g;
				g = Math.sqrt((d *= h) * d + (f *= h) * f);
				double i = Math.sqrt(d * d + f * f + e * e);
				float j = StratofishEntity.this.getYaw();
				float k = (float) MathHelper.atan2(f, d);
				float l = MathHelper.wrapDegrees(StratofishEntity.this.getYaw() + 90.0f);
				float m = MathHelper.wrapDegrees(k * 57.295776f);
				StratofishEntity.this.setYaw(MathHelper.stepUnwrappedAngleTowards(l, m, 4.0f) - 90.0f);
				StratofishEntity.this.bodyYaw = StratofishEntity.this.getYaw();
				this.targetSpeed = MathHelper.angleBetween(j, StratofishEntity.this.getYaw()) < 3.0f ? MathHelper.stepTowards(this.targetSpeed, 1.8f, 0.005f * (1.8f / this.targetSpeed)) : MathHelper.stepTowards(this.targetSpeed, 0.2f, 0.025f);
				float n = (float) (-(MathHelper.atan2(-e, g) * 57.2957763671875));
				StratofishEntity.this.setPitch(n);
				float o = StratofishEntity.this.getYaw() + 90.0f;
				double p = (double) (this.targetSpeed * MathHelper.cos(o * ((float) Math.PI / 180))) * Math.abs(d / i);
				double q = (double) (this.targetSpeed * MathHelper.sin(o * ((float) Math.PI / 180))) * Math.abs(f / i);
				double r = (double) (this.targetSpeed * MathHelper.sin(n * ((float) Math.PI / 180))) * Math.abs(e / i);
				Vec3d vec3d = StratofishEntity.this.getVelocity();
				StratofishEntity.this.setVelocity(vec3d.add(new Vec3d(p, r, q).subtract(vec3d).multiply(0.2)));
			}
		}
	}
	
	abstract class MovementGoal extends Goal
	{
		public MovementGoal()
		{
			this.setControls(EnumSet.of(Goal.Control.MOVE));
		}

		protected boolean isNearTarget()
		{
			return StratofishEntity.this.targetPosition.squaredDistanceTo(StratofishEntity.this.getX(), StratofishEntity.this.getY(), StratofishEntity.this.getZ()) < 4.0;
		}
	}
	
	class CircleMovementGoal extends MovementGoal
	{
		private float angle;
		private float radius;
		private float yOffset;
		private float circlingDirection;

		CircleMovementGoal()
		{
		}

		@Override
		public boolean canStart()
		{
			return StratofishEntity.this.getTarget() == null;
		}

		@Override
		public void start()
		{
			this.radius = 10.0f + StratofishEntity.this.random.nextFloat() * 10.0f;
			this.yOffset = -4.0f + StratofishEntity.this.random.nextFloat() * 9.0f;
			this.circlingDirection = StratofishEntity.this.random.nextBoolean() ? 1.0f : -1.0f;
			this.adjustDirection();
		}

		@Override
		public void tick()
		{
			if(StratofishEntity.this.random.nextInt(this.getTickCount(350)) == 0)
				this.yOffset = -4.0f + StratofishEntity.this.random.nextFloat() * 9.0f;
			
			if(StratofishEntity.this.random.nextInt(this.getTickCount(250)) == 0)
			{
				this.radius += 1.0f;
				
				if(this.radius > 15.0f)
				{
					this.radius = 5.0f;
					this.circlingDirection = -this.circlingDirection;
				}
			}
			
			if(this.isNearTarget() || StratofishEntity.this.horizontalCollision)
			{
				this.angle += (float) Math.PI;
				this.adjustDirection();
			}
			
			if(StratofishEntity.this.random.nextInt(this.getTickCount(450)) == 0)
			{
				this.angle = StratofishEntity.this.random.nextFloat() * 2.0f * (float) Math.PI;
				this.adjustDirection();
			}
			
			if(StratofishEntity.this.targetPosition.y < StratofishEntity.this.getY() && !StratofishEntity.this.getWorld().isAir(StratofishEntity.this.getBlockPos().down(1)))
			{
				this.yOffset = Math.max(1.0f, this.yOffset);
				this.adjustDirection();
			}
			
			if(StratofishEntity.this.targetPosition.y > StratofishEntity.this.getY() && !StratofishEntity.this.getWorld().isAir(StratofishEntity.this.getBlockPos().up(1)))
			{
				this.yOffset = Math.min(-1.0f, this.yOffset);
				this.adjustDirection();
			}
		}

		private void adjustDirection()
		{
			if(BlockPos.ORIGIN.equals(StratofishEntity.this.circlingCenter))
				StratofishEntity.this.circlingCenter = StratofishEntity.this.getBlockPos();
			
			this.angle += this.circlingDirection * 15.0f * ((float) Math.PI / 180.0f);
			StratofishEntity.this.targetPosition = Vec3d.of(StratofishEntity.this.circlingCenter).add(this.radius * MathHelper.cos(this.angle), -4.0f + this.yOffset, this.radius * MathHelper.sin(this.angle));
		}
	}
}