package space.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class QuicksilverEntity extends SlimeEntity implements AlienMobEntity
{

	public QuicksilverEntity(EntityType<? extends QuicksilverEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	public static DefaultAttributeContainer.Builder createQuicksilverAttributes()
	{
		return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3F).add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, 8.0F);
	}

	public static boolean canQuicksilverSpawn(EntityType<QuicksilverEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random)
	{
		return world.getDifficulty() != Difficulty.PEACEFUL;
	}

	@Override
	public void setSize(int size, boolean heal)
	{
		super.setSize(size, heal);
		this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue((double) (size * 3));
	}

	@Override
	public float getBrightnessAtEyes()
	{
		return 1.0F;
	}

	@Override
	protected ParticleEffect getParticles()
	{
		return ParticleTypes.SOUL;
	}

	@Override
	public boolean isOnFire()
	{
		return false;
	}

	@Override
	protected int getTicksUntilNextJump()
	{
		return super.getTicksUntilNextJump();
	}

	@Override
	protected void updateStretch()
	{
		this.targetStretch *= 0.75f;
	}

	@Override
	public void jump()
	{
		Vec3d vec3d = this.getVelocity();
		float f = (float) this.getSize() * 0.1F;
		this.setVelocity(vec3d.x, (double) (this.getJumpVelocity() + f), vec3d.z);
		this.velocityDirty = true;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		World world = getWorld();
		
		if(!world.isClient && age > 80)
		{
			List<Entity> entities = world.getOtherEntities(this, getBoundingBox().expand(getSize()), entity -> entity.isAlive() && entity instanceof QuicksilverEntity && ((QuicksilverEntity) entity).getSize() <= getSize());
			int count = entities.size();
			
			if(count > 0 && random.nextInt((64 * getSize()) / count) == 0)
			{
				for(Entity entity : entities)
				{
					setSize(getSize() + ((QuicksilverEntity) entity).getSize(), true);
					entity.setRemoved(RemovalReason.DISCARDED);
				}
			}
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source)
	{
		return this.isSmall() ? SoundEvents.ENTITY_MAGMA_CUBE_HURT_SMALL : SoundEvents.ENTITY_MAGMA_CUBE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return this.isSmall() ? SoundEvents.ENTITY_MAGMA_CUBE_DEATH_SMALL : SoundEvents.ENTITY_MAGMA_CUBE_DEATH;
	}

	@Override
	protected SoundEvent getSquishSound()
	{
		return this.isSmall() ? SoundEvents.ENTITY_MAGMA_CUBE_SQUISH_SMALL : SoundEvents.ENTITY_MAGMA_CUBE_SQUISH;
	}

	@Override
	protected SoundEvent getJumpSound()
	{
		return SoundEvents.BLOCK_METAL_HIT;
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
}
