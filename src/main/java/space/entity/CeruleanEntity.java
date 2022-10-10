package space.entity;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import space.item.StarflightItems;

public class CeruleanEntity extends TameableEntity implements Angerable
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
		this.goalSelector.add(6, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f, false));
		this.goalSelector.add(7, new AnimalMateGoal(this, 1.0));
		this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));
		this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
		this.goalSelector.add(10, new LookAroundGoal(this));
		this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
		this.targetSelector.add(3, new RevengeGoal(this, new Class[0]).setGroupRevenge(new Class[0]));
		this.targetSelector.add(4, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
		this.targetSelector.add(8, new UniversalAngerGoal<CeruleanEntity>(this, true));
	}

	public static DefaultAttributeContainer.Builder createCeruleanAttributes()
	{
		return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f).add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
	}

	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		this.dataTracker.startTracking(ANGER_TIME, 0);
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
		this.readAngerFromNbt(this.world, nbt);
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
	public boolean canSpawn(WorldAccess world, SpawnReason spawnReason)
	{
        return (!world.isSkyVisible(getBlockPos()) && this.random.nextFloat() < 0.15f) || this.random.nextFloat() < 0.005f;
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
        	ceruleanEntity.setTamed(true);
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
		ItemStack itemStack = player.getStackInHand(hand);
        
        if (this.world.isClient)
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
			if(this.random.nextInt(4) == 0)
			{
				this.setOwner(player);
				this.navigation.stop();
				this.setTarget(null);
				this.setSitting(true);
				this.world.sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
				return ActionResult.SUCCESS;
			}
			else
				this.world.sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
		}
		
		return ActionResult.SUCCESS;
	}

	@Override
	protected void mobTick()
	{
		if(this.world.isDay() && this.world.isSkyVisible(this.getBlockPos()) && this.random.nextFloat() < 0.005f)
		{
			this.setTarget(null);
			this.teleportRandomly();
		}

		super.mobTick();
	}

	protected boolean teleportRandomly()
	{
		if(this.world.isClient || this.isTamed())
			return false;

		double x = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
		double y = this.getY() + (double) (this.random.nextInt(128) - 64);
		double z = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
		return this.teleportTo(x, y, z);
	}

	private boolean teleportTo(double x, double y, double z)
	{
		BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);

		while(mutable.getY() > this.world.getBottomY() && this.world.getBlockState(mutable).getMaterial().blocksMovement())
			mutable.move(Direction.DOWN);

		BlockState blockState = this.world.getBlockState(mutable);

		if(blockState.getMaterial().blocksMovement())
			return false;

		Vec3d vec3d = this.getPos();
		boolean bl = this.teleport(x, y, z, true);

		if(bl)
		{
			this.world.emitGameEvent(GameEvent.TELEPORT, vec3d, GameEvent.Emitter.of(this));

			if(!this.isSilent())
			{
				this.world.playSound(null, this.prevX, this.prevY, this.prevZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0f, 1.0f);
				this.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
			}
		}

		return bl;
	}
}