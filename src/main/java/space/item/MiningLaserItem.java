package space.item;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import space.network.s2c.BeamS2CPacket;

public class MiningLaserItem extends EnergyStorageItem
{
	private static record BlockDamage(BlockPos pos, float progress) {}
	private static final Map<UUID, BlockDamage> LASER_PROGRESS = new ConcurrentHashMap<>();
	
	public MiningLaserItem(Settings settings)
	{
		super(4096, settings);
	}

	@Override
	public int getMaxUseTime(ItemStack stack, LivingEntity user)
	{
		return 72000;
	}

	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.BOW;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
	{
		player.setCurrentHand(hand);
		return TypedActionResult.pass(player.getStackInHand(hand));
	}
	
	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks)
	{
		if(world.isClient)
			return;
		
		if(EnergyStorageItem.removeEnergy(stack, 0, true) == 0)
		{
			double maxDistance = 256.0;
			Vec3d rayStart = user.getEyePos();
			Vec3d direction = user.getRotationVector();
			Vec3d end = rayStart.add(direction.multiply(maxDistance));
			BlockHitResult blockHit = world.raycast(new RaycastContext(rayStart, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, user));
			double blockDist = blockHit != null ? blockHit.getPos().squaredDistanceTo(rayStart) : Double.MAX_VALUE;
			Box box = user.getBoundingBox().stretch(direction.multiply(maxDistance)).expand(1.0);
			EntityHitResult entityHit = ProjectileUtil.getEntityCollision(world, user, rayStart, end, box, target -> !target.isSpectator() && target.isAlive());
			double entityDist = entityHit != null ? entityHit.getPos().squaredDistanceTo(rayStart) : Double.MAX_VALUE;

			if(entityDist < blockDist && entityHit.getEntity() instanceof LivingEntity)
			{
				Entity entity = entityHit.getEntity();
				entity.damage(user.getDamageSources().generic(), 4.0f);
				Vec3d knockback = direction.multiply(0.5);
				
				if(!entity.hasNoGravity())
					knockback = knockback.add(0.0, 0.15, 0.0);
				
				entity.addVelocity(knockback);
			}
			else if(blockDist < Double.MAX_VALUE)
			{
				//world.breakBlock(blockHit.getBlockPos(), true, user);
				damageBlock(world, user, blockHit.getBlockPos());
			}

			BeamS2CPacket.sendBeam(user, null, Vec3d.ZERO, Vec3d.ZERO, 0xFFFFFF, 5, 64);
		}
		
		if(stack.get(StarflightItems.ENERGY) <= 0)
			removeProgress((ServerWorld) world, user);
	}
	
	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks)
	{
		if(!world.isClient)
			removeProgress((ServerWorld) world, user);
	}
	
	public static void damageBlock(World world, LivingEntity entity, BlockPos pos)
	{
		UUID uuid = entity.getUuid();
		BlockDamage old = LASER_PROGRESS.get(uuid);
		float newProgress = (old != null && old.pos.equals(pos) ? old.progress : 0.0f) + (0.5f / world.getBlockState(pos).getBlock().getHardness());
		int breakStage = Math.min(9, (int) (newProgress * 10)); // 0–9
		LASER_PROGRESS.put(uuid, new BlockDamage(pos, newProgress));

		// send the breaking animation packet (visible to everyone around):
		sendBlockBreakStage((ServerWorld) world, entity, pos, breakStage);

		// once we reach 1.0f progress, break it for real:
		if(newProgress >= 1.0f)
		{
			world.breakBlock(pos, true, entity);
			LASER_PROGRESS.remove(uuid);
		}
	}
	
	public static void removeProgress(ServerWorld world, LivingEntity entity)
	{
		UUID uuid = entity.getUuid();
		
		if(LASER_PROGRESS.containsKey(uuid))
		{
			sendBlockBreakStage(world, entity, LASER_PROGRESS.get(uuid).pos(), -1);
			LASER_PROGRESS.remove(uuid);
		}
	}
	
	public static void sendBlockBreakStage(ServerWorld world, LivingEntity entity, BlockPos pos, int breakStage)
	{
		((ServerWorld) world).getServer().getPlayerManager().sendToAround(null, pos.getX(), pos.getY(), pos.getZ(), 32, world.getRegistryKey(), new BlockBreakingProgressS2CPacket(entity.getId(), pos, breakStage));
	}
}