package space.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.particle.StarflightParticleTypes;

public class SolarEyesEntity extends MobEntity implements AlienMobEntity
{
	public SolarEyesEntity(EntityType<? extends MobEntity> entityType, World world)
	{
        super(entityType, world);
    }
	
	public static DefaultAttributeContainer.Builder createSolarEyesAttributes()
	{
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 2.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
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
	protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition)
	{
	}

	@Override
	public void tick()
	{
		super.tick();
		World world = getWorld();
		
		if(world.isClient)
		{
			if(this.random.nextInt(10) == 0)
			{
				double x = getX() + random.nextDouble() - random.nextDouble();
				double y = getY() + random.nextDouble() - random.nextDouble();
				double z = getZ() + random.nextDouble() - random.nextDouble();
				world.addParticle(StarflightParticleTypes.EYE, true, x, y, z, 0.0, 0.0, 0.0);
			}
			
			return;
		}
		
		this.setNoGravity(true);
		this.setVelocity(this.getVelocity().multiply(0.98));
		
		if(this.random.nextInt(200) == 0 || horizontalCollision || verticalCollision)
			this.setVelocity(this.random.nextDouble() - 0.5, this.random.nextDouble() - 0.5, this.random.nextDouble() - 0.5);
	}
}