package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity
{
	@Shadow abstract ItemStack getStack();
	@Shadow int pickupDelay;
	@Shadow abstract void applyWaterBuoyancy();
	@Shadow abstract void applyLavaBuoyancy();
	@Shadow abstract boolean canMerge();
	@Shadow abstract void tryMerge();
	@Shadow int itemAge;
	
	protected ItemEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}

	/**
	 * Modified vanilla item entity physics to account for different gravity and air resistance values.
	 */
	@Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
	public void tickInject(CallbackInfo info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());

			if(currentPlanet != null)
			{
				int vec3d2;
				double airMultiplier = AirUtil.getAirResistanceMultiplier(world, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance.

				if(this.getStack().isEmpty())
				{
					this.discard();
					return;
				}
				
				super.tick();

				if(this.pickupDelay > 0 && this.pickupDelay != Short.MAX_VALUE)
					this.pickupDelay--;
				
				this.prevX = this.getX();
				this.prevY = this.getY();
				this.prevZ = this.getZ();
				Vec3d vec3d = this.getVelocity();
				float f = this.getStandingEyeHeight() - 0.11111111f;

				if(this.isTouchingWater() && this.getFluidHeight(FluidTags.WATER) > f)
					this.applyWaterBuoyancy();
				else if(this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > f)
					this.applyLavaBuoyancy();
				else if(!(this.hasNoGravity() || PlanetList.isOrbit(this.world.getRegistryKey())))
					this.setVelocity(this.getVelocity().add(0.0, -0.04 * currentPlanet.getSurfaceGravity(), 0.0));

				if(this.world.isClient)
					this.noClip = false;
				else if(this.noClip)
					this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());

				if(!this.onGround || this.getVelocity().horizontalLengthSquared() > 1.0E-5f || (this.age + this.getId()) % 4 == 0)
				{
					this.move(MovementType.SELF, this.getVelocity());
					float g = (float) (1.0 / (1.0 + (0.02 * airMultiplier)));

					if(this.onGround)
						g = this.world.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getSlipperiness() * 0.98F;
					
					this.setVelocity(this.getVelocity().multiply(g));

					if(this.onGround)
					{
						Vec3d vec3d22 = this.getVelocity();

						if(vec3d22.y < 0.0)
							this.setVelocity(vec3d22.multiply(1.0, -0.5, 1.0));
					}
				}
				
				boolean g = MathHelper.floor(this.prevX) != MathHelper.floor(this.getX()) || MathHelper.floor(this.prevY) != MathHelper.floor(this.getY()) || MathHelper.floor(this.prevZ) != MathHelper.floor(this.getZ());
				vec3d2 = g ? 2 : 40;

				if(this.age % vec3d2 == 0 && !this.world.isClient && this.canMerge())
					this.tryMerge();

				if(this.itemAge != Short.MIN_VALUE)
					this.itemAge++;
				this.velocityDirty |= this.updateWaterState();

				if(!this.world.isClient && (this.getVelocity().subtract(vec3d).lengthSquared()) > 0.01)
					this.velocityDirty = true;

				if(!this.world.isClient && this.itemAge >= 6000)
					this.discard();
				
				info.cancel();
			}
		}
	}
}