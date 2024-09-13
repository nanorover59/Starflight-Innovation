package space.entity;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.item.StarflightItems;

public class CeruleanEntity extends TameableEntity implements Angerable, AlienMobEntity
{
	private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(CeruleanEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(30, 60);
	@Nullable
	private UUID angryAt;

	public CeruleanEntity(EntityType<? extends TameableEntity> entityType, World world)
	{
		super(entityType, world);
	}

	@Override
	protected void initGoals()
	{
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, new SitGoal(this));
		this.goalSelector.add(4, new PounceAtTargetGoal(this, 0.1f));
		this.goalSelector.add(5, new MeleeAttackGoal(this, 1.0, true));
		this.goalSelector.add(6, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f));
		this.goalSelector.add(7, new AnimalMateGoal(this, 1.0));
		this.goalSelector.add(8, new TemptGoal(this, 1.0, Ingredient.ofItems(StarflightItems.CHEESE), false));
		this.goalSelector.add(9, new WanderAroundFarGoal(this, 1.0));
		this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
		this.goalSelector.add(11, new LookAroundGoal(this));
		this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
		this.targetSelector.add(3, new RevengeGoal(this, new Class[0]));
		this.targetSelector.add(4, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
		this.targetSelector.add(5, new UniversalAngerGoal<CeruleanEntity>(this, true));
	}

	public static DefaultAttributeContainer.Builder createCeruleanAttributes()
	{
		return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f).add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder)
	{
		super.initDataTracker(builder);
		builder.add(ANGER_TIME, 0);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		this.writeAngerToNbt(nbt);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		this.readAngerFromNbt(this.getWorld(), nbt);
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source)
	{
		return SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK;
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
	public boolean canImmediatelyDespawn(double distanceSquared)
	{
        return !this.isTamed();
    }

	@Override
    public boolean cannotDespawn()
    {
        return this.hasVehicle() || this.isTamed();
    }
	
	@Override
    public boolean isBreedingItem(ItemStack stack)
	{
        return stack.isOf(StarflightItems.CHEESE);
    }

	@Override
	public PassiveEntity createChild(ServerWorld serverWorld, PassiveEntity entity)
	{
		CeruleanEntity ceruleanEntity = StarflightEntities.CERULEAN.create(serverWorld);
		UUID uuid = this.getOwnerUuid();
		
        if(uuid != null)
        {
        	ceruleanEntity.setOwnerUuid(uuid);
        	ceruleanEntity.setTamed(true, false);
        }
        
		return ceruleanEntity;
	}
	
	@Override
	public boolean canBreedWith(AnimalEntity other)
	{
		if(other == this || !this.isTamed() || !(other instanceof CeruleanEntity))
			return false;
		
		CeruleanEntity ceruleanEntity = (CeruleanEntity) other;
		
		if(!ceruleanEntity.isTamed() || ceruleanEntity.isInSittingPose())
			return false;
		
		return this.isInLove() && ceruleanEntity.isInLove();
	}

	@Override
	public int getLimitPerChunk()
	{
		return 1;
	}
	
	@Override
	public boolean canSpawn(WorldAccess world, SpawnReason spawnReason)
	{
		return true;
	}

	@Override
	public int getAngerTime()
	{
		return this.dataTracker.get(ANGER_TIME);
	}

	@Override
	public void setAngerTime(int angerTime)
	{
		this.dataTracker.set(ANGER_TIME, angerTime);
	}

	@Override
	public void chooseRandomAngerTime()
	{
		this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
	}

	@Override
	@Nullable
	public UUID getAngryAt()
	{
		return this.angryAt;
	}

	@Override
	public void setAngryAt(@Nullable UUID angryAt)
	{
		this.angryAt = angryAt;
	}
	
	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand)
	{
		World world = getWorld();
		ItemStack itemStack = player.getStackInHand(hand);
        
        if (world.isClient)
            return this.isOwner(player) || this.isTamed() || this.isBreedingItem(itemStack) && !this.isTamed() && !this.hasAngerTime() ? ActionResult.CONSUME : ActionResult.PASS;
		
		if(!player.getAbilities().creativeMode)
			itemStack.decrement(1);
		
		if(this.isTamed() && this.isOwner(player))
		{
			if(this.isBreedingItem(itemStack))
				return super.interactMob(player, hand);
			else
			{
				this.setInSittingPose(!this.isSitting());
				this.setSitting(!this.isSitting());
				this.jumping = false;
				this.navigation.stop();
				this.setTarget(null);
			}
		}
		else if(!this.isTamed() && this.isBreedingItem(itemStack))
		{
			if(this.random.nextBoolean())
			{
				this.setOwner(player);
				this.navigation.stop();
				this.setTarget(null);
				this.setSitting(true);
				this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
				return ActionResult.SUCCESS;
			}
			else
				this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
		}
		
		return ActionResult.SUCCESS;
	}
	
	public static boolean canCeruleanSpawn(EntityType<CeruleanEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random)
	{
        return random.nextInt(30) == 0 && !world.isSkyVisible(pos);
    }
}