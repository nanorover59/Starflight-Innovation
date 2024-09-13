package space.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.entity.AlienMobEntity;
import space.item.StarflightItems;
import space.network.s2c.JetS2CPacket;
import space.planet.Planet;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity
{
	private double gravity = 1.0;
	private float airMultiplier = 1.0f;
	
	@Shadow @Nullable abstract StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> jumpBoost);
	
	protected LivingEntityMixin(EntityType<? extends LivingEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	/**
	 * Inject into the baseTick() function.
	 */
	@Inject(method = "baseTick()V", at = @At("TAIL"), cancellable = true)
	public void baseTickInject(CallbackInfo info)
	{
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(this.getWorld());
		
		// Update the gravity and air resistance multiplier variables.
		if(data != null && data.overridePhysics())
		{
			// Ignore custom physics for creative mode flight.
			if((Entity) this instanceof PlayerEntity && ((PlayerEntity) ((Entity) this)).getAbilities().flying)
			{
				gravity = 1.0;
				airMultiplier = 1.0f;
			}
			else
			{
				gravity = data.isOrbit() || this.hasNoGravity() ? 0.0 : data.getGravity();
				airMultiplier = (float) AirUtil.getAirResistanceMultiplier(this.getWorld(), data, this.getBlockPos());
			}
		}
		else
		{
			gravity = 1.0;
			airMultiplier = 1.0f;
		}
		
		// Allow climbing any wall in sufficiently low gravity;
		if(gravity < 0.01)
		{
			boolean wall = false;
			
			for(Direction direction : Direction.values())
			{
				BlockPos offset = this.getBlockPos().offset(direction, direction == Direction.UP ? Math.round(this.getHeight()) : 1);
				
				if(!this.getWorld().getBlockState(offset).isAir())
				{
					wall = true;
					break;
				}
			}
			
			if(wall)
				airMultiplier = 1.0f;
			
			if(this.horizontalCollision)
			{
				double xVelocity = this.getVelocity().getX();
				double yVelocity = 0.0;
				double zVelocity = this.getVelocity().getZ();
	
				if(this.getPitch() < -15.0f)
					yVelocity = 0.05;
				else if(this.getPitch() > 15.0f)
					yVelocity = -0.05;
	
				this.setVelocity(xVelocity, yVelocity, zVelocity);
			}
		}
		
		// Assume an air resistance multiplier of one for motion in water.
		if(this.isSubmergedInWater())
			airMultiplier = 1.0f;
		
		// Run oxygen supply mechanics on the server side.
		if(!this.getWorld().isClient())
		{
			LivingEntity thisEntity = (LivingEntity) this.getWorld().getEntityById(getId());
			
			if(thisEntity != null)
			{
				int spaceSuitCheck = 0;
				boolean habitableAir = AirUtil.canEntityBreathe(thisEntity, data);
				boolean survivalPlayer = thisEntity instanceof PlayerEntity && !((PlayerEntity) thisEntity).isCreative() && !((PlayerEntity) thisEntity).isSpectator();
				boolean creativeFlying = thisEntity instanceof PlayerEntity && ((PlayerEntity) thisEntity).getAbilities().flying;
				ItemStack chestplate = null;
				float oxygen = 0.0f;
				
				if(thisEntity instanceof AlienMobEntity)
				{
					AlienMobEntity mob = (AlienMobEntity) thisEntity;
					int temperatureCategory = data.getTemperatureCategory(thisEntity.getWorld().getSkyAngle(1.0f));
					double pressure = data.getPressure();
					
					if(habitableAir)
					{
						temperatureCategory = Planet.TEMPERATE;
						pressure = 1.0;
						
						if(!mob.oxygenCompatible())
							thisEntity.setOnFire(true);
					}
					
					if(!mob.isTemperatureSafe(temperatureCategory) || !mob.isPressureSafe(pressure) || (mob.requiresOxygen() && !habitableAir))
						thisEntity.damage(thisEntity.getDamageSources().generic(), 1.0f);
					
					return;
				}
				
				for(ItemStack stack : thisEntity.getArmorItems())
				{
					if(stack.getItem() == StarflightItems.SPACE_SUIT_HELMET || stack.getItem() == StarflightItems.SPACE_SUIT_LEGGINGS || stack.getItem() == StarflightItems.SPACE_SUIT_BOOTS)
						spaceSuitCheck++;
					else if(stack.getItem() == StarflightItems.SPACE_SUIT_CHESTPLATE)
					{
						spaceSuitCheck++;
						chestplate = stack;
					}
				}
				
				if(chestplate != null && chestplate.contains(StarflightItems.OXYGEN))
					oxygen = chestplate.get(StarflightItems.OXYGEN);
				
				if((spaceSuitCheck < 4 || oxygen == 0.0f) && !(thisEntity.isSubmergedInWater() || habitableAir))
				{
					if(!thisEntity.isInLava())
						thisEntity.setFireTicks(0);
					
					thisEntity.damage(thisEntity.getDamageSources().generic(), 1.0f);
				}
				else if(spaceSuitCheck == 4)
				{
					float oxygenUsed = 0.0f;
					
					if(survivalPlayer && (thisEntity.isSubmergedInWater() || !habitableAir))
						oxygenUsed += 4.0f / 24000.0f; // 4kg of oxygen should last for 20 minutes (24000 ticks) without using maneuvering jets.
					
					// Use space suit thrust jets when sneaking.
					if(thisEntity.isSneaking() && !creativeFlying && data.isOrbit())
					{
						oxygenUsed += 0.05f * 0.05f;
						Vec3d deltaV = thisEntity.getRotationVector().multiply(0.1 * 0.05);
						thisEntity.addVelocity(deltaV.getX(), deltaV.getY(), deltaV.getZ());
						thisEntity.velocityModified = true;
						JetS2CPacket.sendJet(this.getWorld(), thisEntity.getPos().add(0.0, 0.65, 0.0), thisEntity.getVelocity().add(thisEntity.getRotationVector().multiply(-2.0)));
					}
					
					thisEntity.setAir(thisEntity.getMaxAir());
					chestplate.set(StarflightItems.OXYGEN, oxygen - oxygenUsed < 0.0f ? 0.0f : oxygen - oxygenUsed);
				}
			}
		}
	}
	
	/*@Inject(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
	public void travelInject(Vec3d movementInput, CallbackInfo info)
	{
		//System.out.println(this.verticalCollision + "   " + this.horizontalCollision + "   " + airMultiplier);
		
		if(!this.verticalCollision && !this.horizontalCollision && airMultiplier == 0.0f)
		{
			this.move(MovementType.SELF, this.getVelocity());
			info.cancel();
		}
	}*/
	
	/**
	 * Modify the variable for living entity gravity.
	 */
	/*@ModifyVariable(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At("STORE"), ordinal = 0)
	public double gravityInject(double d)
	{
		return d * gravity;
	}*/
	
	/**
	 * Modify the constants for living entity air resistance.
	 */
	/*@ModifyConstant(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", constant = @Constant(floatValue = 0.99f))
	public float frictionInject1(float f)
	{
		return 1.0f - (0.01f * airMultiplier);
	}
	
	@ModifyConstant(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", constant = @Constant(floatValue = 0.98f))
	public float frictionInject2(float f)
	{
		return 1.0f - (0.02f * airMultiplier);
	}
	
	@ModifyConstant(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", constant = @Constant(floatValue = 0.91f))
	public float frictionInject3(float f)
	{
		return 1.0f - (0.09f * airMultiplier);
	}
	
	/**
	 * Modify the lift for living entity fall flight.
	 */
	@ModifyArg(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(DD)D"), index = 0)
	private double minInject1(double d)
	{
		return d * airMultiplier;
	}
	
	@ModifyArg(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(DD)D"), index = 1)
	private double minInject2(double d)
	{
		return d * airMultiplier;
	}
	
	/**
	 * Modify movement input according to air resistance.
	 */
	@ModifyVariable(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"))
	public Vec3d movementInputInject(Vec3d movementInput)
	{
		if(gravity < 0.01)
			return movementInput.multiply(airMultiplier);
		else
			return movementInput;
	}
	
	/**
	 * Properly recognize a zero drag environment.
	 */
	@Inject(method = "hasNoDrag()Z", at = @At("HEAD"), cancellable = true)
	public void hasNoDragInject(CallbackInfoReturnable<Boolean> info)
	{
		if(gravity < 0.01 && airMultiplier == 0.0f)
			info.setReturnValue(true);
	}
	
	/**
	 * Reduce fall damage by the appropriate amount.
	 */
	@Inject(method = "computeFallDamage(FF)I", at = @At("HEAD"), cancellable = true)
	public void computeFallDamageInject(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> info)
	{
		if(this.getWorld().getRegistryKey() != World.OVERWORLD && this.getWorld().getRegistryKey() != World.NETHER && this.getWorld().getRegistryKey() != World.END)
		{
			fallDistance *= gravity; // The distance for fall damage is changed to match local gravity.
			StatusEffectInstance statusEffectInstance = this.getStatusEffect(StatusEffects.JUMP_BOOST);
			float f = statusEffectInstance == null ? 0.0f : (float) (statusEffectInstance.getAmplifier() + 1);
			info.setReturnValue(MathHelper.ceil((fallDistance - 3.0f - f) * damageMultiplier));
			info.cancel();
		}
	}
}