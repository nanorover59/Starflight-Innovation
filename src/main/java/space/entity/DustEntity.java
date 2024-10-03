package space.entity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.StarflightBlocks;

public class DustEntity extends HostileEntity implements AlienMobEntity
{
	private static final TrackedData<Integer> STAMINA = DataTracker.registerData(DustEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final int INITIAL_STAMINA = 1600;
	
	public DustEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
    protected void initGoals()
	{
        this.goalSelector.add(3, new AttackGoal(this));
        this.goalSelector.add(4, new DustWanderAroundGoal(this, 1.0));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(2, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
    }
	
	public static DefaultAttributeContainer.Builder createDustAttributes()
	{
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25).add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4f);
    }
	
	@Override
	public boolean isPressureSafe(double pressure)
	{
		return pressure > 0;
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
    public boolean isCollidable()
	{
        return false;
    }
	
	@Override
    public boolean collidesWith(Entity other)
	{
        return false;
    }
	
	@Override
    protected void initDataTracker(DataTracker.Builder builder)
	{
        super.initDataTracker(builder);
        builder.add(STAMINA, INITIAL_STAMINA);
    }
	
	public int getStamina()
	{
		return dataTracker.get(STAMINA);
	}

    @Override
    public void tick()
    {
        super.tick();
        World world = getWorld();
        int stamina = getStamina();
        
        if(world.isClient)
        {
        	int maxParticles = stamina < 10 ? 4 : 2;
        	
        	for(int i = 0; i < random.nextBetween(1, maxParticles); i++)
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
        	decrease = 4;
        
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
				((LivingEntity) target).addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 80, 0), this);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("stamina", dataTracker.get(STAMINA));
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		dataTracker.set(STAMINA, nbt.getInt("stamina"));
	}
	
	@Override
	public boolean canSpawn(WorldAccess world, SpawnReason spawnReason)
	{
		return true;
	}
	
	public static boolean canDustSpawn(EntityType<DustEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random)
	{
		int chance = world.getServer().getOverworld().isRaining() ? 128 : 256;
        return random.nextInt(chance) == 0 && world.isSkyVisible(pos) && world.getBlockState(pos.down()).getBlock() == StarflightBlocks.FERRIC_SAND;
    }
	
	static public class DustWanderAroundGoal extends WanderAroundGoal
	{
		public DustWanderAroundGoal(PathAwareEntity mob, double speed)
		{
			super(mob, speed);
		}

		@Override
		@Nullable
		protected Vec3d getWanderTarget()
		{
			return FuzzyTargeting.find(this.mob, 24, 6);
		}
	}
}