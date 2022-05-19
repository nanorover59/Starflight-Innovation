package space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin extends Entity
{
	@Shadow abstract boolean isNoClip();
	@Shadow protected boolean inGround;
	@Shadow int shake;
	@Shadow BlockState inBlockState;
	@Shadow abstract boolean shouldFall();
	@Shadow abstract void fall();
	@Shadow abstract protected void age();
	@Shadow protected int inGroundTime;
	@Shadow abstract protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition);
	@Shadow abstract byte getPierceLevel();
	@Shadow abstract boolean isCritical();
	@Shadow abstract protected float getDragInWater();
	
	public PersistentProjectileEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}
		
	/**
	 * Modified vanilla persistent projectile entity physics to account for different gravity and air resistance values.
	 */
	@Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
	public void tickInject(CallbackInfo info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());
			ProjectileEntity thisEntity = (ProjectileEntity) world.getEntityById(getId());

			if(currentPlanet != null && thisEntity != null)
			{
				Vec3d vec3d2;
				Object voxelShape;
				BlockPos d;
				BlockState blockState;
				super.tick();
				boolean bl = this.isNoClip();
				Vec3d vec3d = this.getVelocity();
				double airMultiplier = AirUtil.getAirResistanceMultiplier(world, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance;

				if(this.prevPitch == 0.0f && this.prevYaw == 0.0f)
				{
					double d2 = vec3d.horizontalLength();
					this.setYaw((float) (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875));
					this.setPitch((float) (MathHelper.atan2(vec3d.y, d2) * 57.2957763671875));
					this.prevYaw = this.getYaw();
					this.prevPitch = this.getPitch();
				}

				if(!((blockState = this.world.getBlockState(d = this.getBlockPos())).isAir() || bl || ((VoxelShape) (voxelShape = blockState.getCollisionShape(this.world, d))).isEmpty()))
				{
					vec3d2 = this.getPos();

					for(Box box : ((VoxelShape) voxelShape).getBoundingBoxes())
					{
						if(!box.offset(d).contains(vec3d2))
							continue;
						this.inGround = true;
						break;
					}
				}

				if(this.shake > 0)
					--this.shake;

				if(this.isTouchingWaterOrRain() || blockState.isOf(Blocks.POWDER_SNOW))
					this.extinguish();

				if(this.inGround && !bl)
				{
					if(this.inBlockState != blockState && this.shouldFall())
						this.fall();
					else if(!this.world.isClient)
						this.age();
					
					this.inGroundTime++;
					return;
				}
				
				this.inGroundTime = 0;
				voxelShape = this.getPos();
				HitResult hitResult = this.world.raycast(new RaycastContext((Vec3d) voxelShape, vec3d2 = ((Vec3d) voxelShape).add(vec3d), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));

				if(hitResult.getType() != HitResult.Type.MISS)
					vec3d2 = hitResult.getPos();

				while(!this.isRemoved())
				{
					EntityHitResult entityHitResult = this.getEntityCollision((Vec3d) voxelShape, vec3d2);

					if(entityHitResult != null)
						hitResult = entityHitResult;

					if(hitResult != null && hitResult.getType() == HitResult.Type.ENTITY)
					{
						Entity entity = ((EntityHitResult) hitResult).getEntity();
						Entity entity2 = thisEntity.getOwner();

						if(entity instanceof PlayerEntity && entity2 instanceof PlayerEntity && !((PlayerEntity) entity2).shouldDamagePlayer((PlayerEntity) entity))
							hitResult = null;
					}

					if(hitResult != null && !bl)
					{
						((ProjectileEntityMixin) this).callOnCollision(hitResult);
						this.velocityDirty = true;
					}
					
					if(this.getPierceLevel() <= 0)
						break;
					
					hitResult = null;
				}
				
				vec3d = this.getVelocity();
				double d2 = vec3d.x;
				double entity2 = vec3d.y;
				double e = vec3d.z;

				if(this.isCritical())
				{
					for(int i = 0; i < 4; ++i)
						this.world.addParticle(ParticleTypes.CRIT, this.getX() + d2 * i / 4.0, this.getY() + entity2 * i / 4.0, this.getZ() + e * i / 4.0, -d2, -entity2 + 0.2, -e);
				}
				
				double i = this.getX() + d2;
				double f = this.getY() + entity2;
				double g = this.getZ() + e;
				double h = vec3d.horizontalLength();

				if(bl)
					this.setYaw((float) (MathHelper.atan2(-d2, -e) * 57.2957763671875));
				else
					this.setYaw((float) (MathHelper.atan2(d2, e) * 57.2957763671875));
				
				this.setPitch((float) (MathHelper.atan2(entity2, h) * 57.2957763671875));
				this.setPitch(ProjectileEntityMixin.callUpdateRotation(this.prevPitch, this.getPitch()));
				this.setYaw(ProjectileEntityMixin.callUpdateRotation(this.prevYaw, this.getYaw()));
				float j = (float) (1.0 / (1.0 + (0.01 * airMultiplier)));

				if(this.isTouchingWater())
				{
					for(int l = 0; l < 4; ++l)
					{
						float m = 0.25f;
						this.world.addParticle(ParticleTypes.BUBBLE, i - d2 * m, f - entity2 * m, g - e * m, d2, entity2, e);
					}
					
					j = this.getDragInWater();
				}
				
				this.setVelocity(vec3d.multiply(j));

				if(!this.hasNoGravity() && !bl)
				{
					Vec3d l = this.getVelocity();
					float k = PlanetList.isOrbit(this.world.getRegistryKey()) ? 0.0f : (float) (0.04 * currentPlanet.getSurfaceGravity());
					this.setVelocity(l.x, l.y - k, l.z);
				}
				
				this.setPosition(i, f, g);
				this.checkBlockCollision();
				info.cancel();
			}
		}
	}
}
