package space.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import space.block.StarflightBlocks;

public class DustEntity extends HostileEntity
{
	private static final TrackedData<Integer> STAMINA = DataTracker.registerData(DustEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final int INITIAL_STAMINA = 400;
	
	public DustEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
    protected void initGoals()
	{
        this.goalSelector.add(2, new AttackGoal(this));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        this.goalSelector.add(9, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<PlayerEntity>((MobEntity)this, PlayerEntity.class, true));
    }
	
	public static DefaultAttributeContainer.Builder createDustAttributes()
	{
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 50.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 4.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4f);
    }
	
	/*@Override
    public boolean isAttackable()
	{
        return false;
    }*/
	
	@Override
    protected void initDataTracker()
	{
        super.initDataTracker();
        this.dataTracker.startTracking(STAMINA, INITIAL_STAMINA);
    }
	
	public int getStamina()
	{
		return dataTracker.get(STAMINA);
	}
	
	public static boolean canSpawn(EntityType<DustEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) 
	{
		float spawnChance = 0.25f;
		
		if(world.toServerWorld().isRaining() || world.toServerWorld().isThundering())
			spawnChance = 0.75f;
		
        return random.nextFloat() < spawnChance && world.isSkyVisible(pos) && world.getBlockState(pos.down()).getBlock() == StarflightBlocks.FERRIC_SAND;
    }

    @Override
    public void tick()
    {
        super.tick();
        int stamina = getStamina();
        
        if(world.isClient)
        {
        	int maxParticles = stamina < 10 ? 8 : 4;
        	
        	for(int i = 0; i < random.nextBetween(2, maxParticles); i++)
        	{
        		double x = getPos().getX() + random.nextDouble() - random.nextDouble();
        		double y = getPos().getY() + random.nextDouble() * 2.8;
        		double z = getPos().getZ() + random.nextDouble() - random.nextDouble();
        		double vx = (random.nextDouble() - random.nextDouble()) * 0.1;
        		double vy = 1.0 + random.nextDouble() * 0.1;
        		double vz = (random.nextDouble() - random.nextDouble()) * 0.1;
        		world.addParticle(new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, StarflightBlocks.FERRIC_SAND.getDefaultState()), x, y, z, vx, vy, vz);
        	}
        	
        	return;
        }
        
        int decrease = 1;
        
        if(world.getBlockState(getBlockPos().down()).getBlock() != StarflightBlocks.FERRIC_SAND || !world.isSkyVisible(getBlockPos()))
        	decrease = 2;
        
        stamina -= decrease;
        dataTracker.set(STAMINA, stamina);
        
        if(stamina <= 0)
        	setRemoved(RemovalReason.DISCARDED);
    }
	
	@Override
	public boolean tryAttack(Entity target)
	{
		if(super.tryAttack(target))
		{
			if(target instanceof LivingEntity)
			{
				((LivingEntity) target).addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 120, 0), this);
				((LivingEntity) this).addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 120, 0), this);
				return true;
			}
		}
		
		return false;
	}
}