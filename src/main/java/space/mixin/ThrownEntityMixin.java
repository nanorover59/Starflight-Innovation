package space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(ThrownEntity.class)
public abstract class ThrownEntityMixin extends Entity
{
	public ThrownEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	/**
	 * Modified vanilla thrown entity physics to account for different gravity and air resistance values.
	 */
	@Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
	public void travelInject(CallbackInfo info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());
			
			if(currentPlanet != null)
			{
				float g;
				Object blockPos;
				super.tick();
				HitResult hitResult = ProjectileUtil.getCollision(this, ((ProjectileEntityMixin) this)::callCanHit);
				boolean bl = false;
				double airMultiplier = AirUtil.getAirResistanceMultiplier(world, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance;

				if(hitResult.getType() == HitResult.Type.BLOCK)
				{
					blockPos = ((BlockHitResult) hitResult).getBlockPos();
					BlockState blockState = this.world.getBlockState((BlockPos) blockPos);

					if(blockState.isOf(Blocks.NETHER_PORTAL))
					{
						this.setInNetherPortal((BlockPos) blockPos);
						bl = true;
					}
					else if(blockState.isOf(Blocks.END_GATEWAY))
					{
						BlockEntity blockEntity = this.world.getBlockEntity((BlockPos) blockPos);

						if(blockEntity instanceof EndGatewayBlockEntity && EndGatewayBlockEntity.canTeleport(this))
							EndGatewayBlockEntity.tryTeleportingEntity(this.world, (BlockPos) blockPos, blockState, this, (EndGatewayBlockEntity) blockEntity);
						bl = true;
					}
				}

				if(hitResult.getType() != HitResult.Type.MISS && !bl)
					((ProjectileEntityMixin) this).callOnCollision(hitResult);
				
				this.checkBlockCollision();
				blockPos = this.getVelocity();
				double c = this.getX() + ((Vec3d) blockPos).x;
				double d = this.getY() + ((Vec3d) blockPos).y;
				double e = this.getZ() + ((Vec3d) blockPos).z;
				Vec3d vec3d = this.getVelocity();
				this.setPitch(ProjectileEntityMixin.callUpdateRotation(this.prevPitch, (float) (MathHelper.atan2(vec3d.y, vec3d.horizontalLength()) * 57.2957763671875)));
				this.setYaw(ProjectileEntityMixin.callUpdateRotation(this.prevYaw, (float) (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875)));

				if(this.isTouchingWater())
				{
					for(int i = 0; i < 4; ++i)
					{
						float f = 0.25f;
						this.world.addParticle(ParticleTypes.BUBBLE, c - ((Vec3d) blockPos).x * f, d - ((Vec3d) blockPos).y * f, e - ((Vec3d) blockPos).z * f, ((Vec3d) blockPos).x, ((Vec3d) blockPos).y, ((Vec3d) blockPos).z);
					}
					
					g = 0.8f;
				}
				else
					g = (float) (1.0 / (1.0 + (0.01 * airMultiplier)));
				
				this.setVelocity(((Vec3d) blockPos).multiply(g));

				if(!this.hasNoGravity())
				{
					Vec3d i = this.getVelocity();
					float h = PlanetList.isOrbit(this.world.getRegistryKey()) ? 0.0f : (float) (0.04 * currentPlanet.getSurfaceGravity());
					this.setVelocity(i.x, i.y - h, i.z);
				}
				
				this.setPosition(c, d, e);
				info.cancel();
			}
		}
	}
	
	
}