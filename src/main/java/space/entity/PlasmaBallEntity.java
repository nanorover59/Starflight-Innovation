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
import space.particle.StarflightParticleTypes;

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
	public boolean shouldRender(double distance)
	{
		return true;
	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		super.onEntityHit(entityHitResult);
		World world = getWorld();

		if(!world.isClient)
		{
			Entity target = entityHitResult.getEntity();
			Entity owner = this.getOwner();
	
			if(getOwner() != null && owner.equals(target))
				return;
			
			target.damage(getDamageSources().mobProjectile(this, (LivingEntity) owner), 5.0f);
			discard();
		}
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		super.onCollision(hitResult);
		World world = getWorld();
		
		if(!world.isClient)
		{
			((ServerWorld) world).spawnParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1 + world.random.nextInt(2), 0.5, 0.5, 0.5, 0.1);
			discard();
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
		World world = getWorld();
		
		if(world.isClient)
		{
			if(this.random.nextBoolean())
			{
				int count = 1 + random.nextInt(1);
				
				for(int i = 0; i < count; i++)
				{
					double spread = 0.25;
					double x = getX() + (random.nextDouble() - random.nextDouble()) * spread;
					double y = getY() + (random.nextDouble() - random.nextDouble()) * spread;
					double z = getZ() + (random.nextDouble() - random.nextDouble()) * spread;
					world.addParticle(StarflightParticleTypes.EYE, true, x, y, z, 0.0, 0.0, 0.0);
				}
			}
			
			return;
		}
		else if(age > 320)
			discard();
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