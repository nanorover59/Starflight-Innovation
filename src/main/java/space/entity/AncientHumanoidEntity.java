package space.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class AncientHumanoidEntity extends ZombieEntity
{
	public AncientHumanoidEntity(EntityType<? extends AncientHumanoidEntity> entityType, World world)
	{
		super((EntityType<? extends ZombieEntity>) entityType, world);
	}

	public static boolean canSpawn(EntityType<HuskEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random)
	{
		return AncientHumanoidEntity.canSpawnInDark(type, world, spawnReason, pos, random) && (spawnReason == SpawnReason.SPAWNER || world.isSkyVisible(pos));
	}

	@Override
	protected boolean burnsInDaylight()
	{
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source)
	{
		return SoundEvents.ENTITY_HUSK_HURT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ENTITY_HUSK_DEATH;
	}

	@Override
	protected SoundEvent getStepSound()
	{
		return SoundEvents.BLOCK_STONE_STEP;
	}

	@Override
	public boolean tryAttack(Entity target)
	{
		boolean bl = super.tryAttack(target);
		
		/*if(bl && this.getMainHandStack().isEmpty() && target instanceof LivingEntity)
		{
			float f = this.world.getLocalDifficulty(this.getBlockPos()).getLocalDifficulty();
			((LivingEntity) target).addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 140 * (int) f), this);
		}*/
		
		return bl;
	}
	
	@Override
	protected void initEquipment(Random random, LocalDifficulty localDifficulty)
	{
		super.initEquipment(random, localDifficulty);
		float f = random.nextFloat();
		float f2 = this.world.getDifficulty() == Difficulty.HARD ? 0.05f : 0.01f;
		if(f < f2)
		{
			int i = random.nextInt(3);
			if(i == 0)
			{
				this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
			} else
			{
				this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
			}
		}
	}

	@Override
	protected ItemStack getSkull()
	{
		return ItemStack.EMPTY;
	}
}