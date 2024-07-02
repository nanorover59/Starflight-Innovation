package space.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import space.item.StarflightItems;
import space.util.StarflightEffects;

public class AncientHumanoidEntity extends ZombieEntity implements AlienMobEntity
{
	private static final Item[] HELD_ITEMS = {StarflightItems.TITANIUM_PICKAXE, StarflightItems.TITANIUM_SWORD, StarflightItems.WRENCH};
	
	public AncientHumanoidEntity(EntityType<? extends AncientHumanoidEntity> entityType, World world)
	{
		super((EntityType<? extends ZombieEntity>) entityType, world);
	}
	
	public static DefaultAttributeContainer.Builder createAncientHumanoidAttributes()
	{
        return ZombieEntity.createZombieAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 60.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0);
    }

	public static boolean canSpawn(EntityType<HuskEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random)
	{
		return AncientHumanoidEntity.canSpawnInDark(type, world, spawnReason, pos, random) && (spawnReason == SpawnReason.SPAWNER || world.isSkyVisible(pos));
	}
	
	@Override
	public boolean cannotDespawn()
	{
        return true;
    }

	@Override
	protected boolean burnsInDaylight()
	{
		return false;
	}
	
	@Override
	public float getMovementSpeed()
	{
		return super.getMovementSpeed() * (getTarget() == null ? 1.0f : 2.0f);
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return StarflightEffects.NOISE_SOUND_EVENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source)
	{
		return SoundEvents.ENTITY_GENERIC_HURT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ENTITY_GENERIC_DEATH;
	}

	@Override
	protected SoundEvent getStepSound()
	{
		return SoundEvents.BLOCK_STONE_STEP;
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
		return 0.5f;
	}
	
	@Override
	public void initEquipment(Random random, LocalDifficulty localDifficulty)
	{
		this.equipStack(EquipmentSlot.HEAD, new ItemStack(StarflightItems.SPACE_SUIT_HELMET));
		this.equipStack(EquipmentSlot.CHEST, new ItemStack(StarflightItems.SPACE_SUIT_CHESTPLATE));
		this.equipStack(EquipmentSlot.LEGS, new ItemStack(StarflightItems.SPACE_SUIT_LEGGINGS));
		this.equipStack(EquipmentSlot.FEET, new ItemStack(StarflightItems.SPACE_SUIT_BOOTS));
		
		if(random.nextInt(4) == 0)
			this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(HELD_ITEMS[random.nextInt(HELD_ITEMS.length)]));
	}

	@Override
	protected ItemStack getSkull()
	{
		return ItemStack.EMPTY;
	}
}