package space.entity;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class BlockShellEntity extends HostileEntity implements AlienMobEntity
{
	private static final TrackedData<Optional<BlockState>> WEARING_BLOCK = DataTracker.registerData(BlockShellEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_STATE);
	private static final TrackedData<Boolean> HIDING = DataTracker.registerData(BlockShellEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	public BlockShellEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}

	@Override
	protected void initGoals()
	{
		this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, false));
		this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
		this.targetSelector.add(2, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
	}

	public static DefaultAttributeContainer.Builder createBlockShellAttributes()
	{
		return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder)
	{
		super.initDataTracker(builder);
		builder.add(WEARING_BLOCK, Optional.empty());
		builder.add(HIDING, true);
	}

	@Override
	protected Entity.MoveEffect getMoveEffect()
	{
		return Entity.MoveEffect.ALL;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source)
	{
		return SoundEvents.ENTITY_SILVERFISH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ENTITY_SILVERFISH_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state)
	{
		this.playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.15F, 1.0F);
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
	public int getRadiationRange()
	{
		return 16;
	}

	@Override
	public float getRadiationStrength()
	{
		return isHiding() ? 0.0f : 0.5f;
	}

	public void setWearingBlock(@Nullable BlockState state)
	{
		this.dataTracker.set(WEARING_BLOCK, Optional.ofNullable(state));
	}

	@Nullable
	public BlockState getWearingBlock()
	{
		return (BlockState) this.dataTracker.get(WEARING_BLOCK).orElse(null);
	}

	public void setHiding(boolean hiding)
	{
		this.dataTracker.set(HIDING, hiding);
	}

	public boolean isHiding()
	{
		return this.dataTracker.get(HIDING);
	}

	@Override
	public void tick()
	{
		if(!getWorld().isClient())
		{
			setHiding(getTarget() == null);
			setInvisible(isHiding());

			if(isHiding() && isOnGround())
			{
				setVelocity(Vec3d.ZERO);
				setPos(getBlockX() + 0.5, getBlockY(), getBlockZ() + 0.5);
				setBodyYaw(Math.round(getBodyYaw()) / 90 * 90);
			}

			if(getWearingBlock() == null)
			{
				Mutable mutable = getBlockPos().down(2 + random.nextInt(16)).mutableCopy();
				BlockState rockState = null;

				while(rockState == null && mutable.getY() > getWorld().getBottomY())
				{
					BlockState blockState = getWorld().getBlockState(mutable);

					if(blockState.isOpaque() && blockState.isIn(BlockTags.PICKAXE_MINEABLE) && blockState.getBlock() != Blocks.MAGMA_BLOCK)
						rockState = blockState;

					mutable.setY(mutable.getY() - 1);
				}

				if(rockState != null)
					setWearingBlock(rockState);
			}
		}
		
		super.tick();
	}
	
	@Override
	public int getLimitPerChunk()
	{
		return 2;
	}
	
	@Override
	public boolean canSpawn(WorldAccess world, SpawnReason spawnReason)
	{
		return true;
	}
	
	public static boolean canBlockShellSpawn(EntityType<BlockShellEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random)
	{
        return random.nextInt(128) == 0 && world.isSkyVisible(pos);
    }

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		BlockState blockState = this.getWearingBlock();

		if(blockState != null)
			nbt.put("wearing", NbtHelper.fromBlockState(blockState));
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		BlockState blockState = null;

		if(nbt.contains("wearing", NbtElement.COMPOUND_TYPE))
		{
			blockState = NbtHelper.toBlockState(this.getWorld().createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt.getCompound("wearing"));

			if(blockState.isAir())
				blockState = null;
		}

		this.setWearingBlock(blockState);
	}
}