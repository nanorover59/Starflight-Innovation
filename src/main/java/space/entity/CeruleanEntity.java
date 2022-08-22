package space.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class CeruleanEntity extends AnimalEntity
{
	public CeruleanEntity(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
    protected void initGoals()
	{
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        this.goalSelector.add(9, new LookAroundGoal(this));
    }
	
	public static DefaultAttributeContainer.Builder createCeruleanAttributes()
	{
		return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f).add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
	}

	@Override
	public PassiveEntity createChild(ServerWorld var1, PassiveEntity var2)
	{
		return null;
	}
}