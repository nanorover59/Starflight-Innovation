package space.entity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PlasmaBallEntity extends AbstractFireballEntity
{
	public PlasmaBallEntity(EntityType<? extends PlasmaBallEntity> entityType, World world)
	{
		super((EntityType<? extends PlasmaBallEntity>) entityType, world);
	}

	public PlasmaBallEntity(World world, LivingEntity owner, Vec3d velocity)
	{
		super((EntityType<? extends PlasmaBallEntity>) StarflightEntities.PLASMA_BALL, owner, velocity, world);
	}

	public PlasmaBallEntity(World world, double x, double y, double z, Vec3d velocity)
	{
		super((EntityType<? extends PlasmaBallEntity>) StarflightEntities.PLASMA_BALL, x, y, z, velocity, world);
	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		super.onEntityHit(entityHitResult);
		World world = getWorld();

		if(world.isClient)
			return;

		Entity target = entityHitResult.getEntity();
		Entity owner = this.getOwner();

		if(!owner.equals(target) && owner instanceof LivingEntity)
		{
			target.damage(getDamageSources().mobProjectile(this, (LivingEntity) owner), 5.0f);
			discard();
		}
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		super.onCollision(hitResult);
		World world = getWorld();

		if(world.isClient)
			discard();
		else
			((ServerWorld) world).spawnParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1 + world.random.nextInt(2), 0.5, 0.5, 0.5, 0.1);
	}

	@Override
	protected boolean isBurning()
	{
		return false;
	}

	@Nullable
	@Override
	protected ParticleEffect getParticleType()
	{
		return null;
	}

	@Override
	protected float getDrag()
	{
		return 1.0f;
	}

	@Override
	protected float getDragInWater()
	{
		return 0.8f;
	}

	@Override
	public boolean canHit()
	{
		return false;
	}
}