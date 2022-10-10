package space.mixin.common;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.item.SpaceSuitItem;
import space.item.StarflightItems;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.AirUtil;
import space.util.StarflightEffects;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity
{
	private double gravity = 1.0;
	private float airMultiplier = 1.0f;
	
	@Shadow @Nullable abstract StatusEffectInstance getStatusEffect(StatusEffect effect);
	
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
		Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());
		boolean inOrbit = PlanetList.isOrbit(this.world.getRegistryKey());
		
		// Update the gravity and air resistance multiplier variables.
		if(currentPlanet != null)
		{
			// Ignore custom physics for creative mode flight.
			if((Entity) this instanceof PlayerEntity && ((PlayerEntity) ((Entity) this)).getAbilities().flying)
			{
				gravity = 1.0;
				airMultiplier = 1.0f;
			}
			else
			{
				gravity = inOrbit || this.hasNoGravity() ? 0.0 : currentPlanet.getSurfaceGravity();
				airMultiplier = (float) AirUtil.getAirResistanceMultiplier(this.world, currentPlanet, this.getBlockPos());
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
				
				if(world.getBlockState(offset).getMaterial() != Material.AIR)
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
		if(!this.world.isClient)
		{
			LivingEntity thisEntity = (LivingEntity) world.getEntityById(getId());
			
			if(thisEntity != null)
			{
				int spaceSuitCheck = 0;
				boolean habitableAir = AirUtil.canEntityBreathe(thisEntity, currentPlanet);
				boolean creativePlayer = (thisEntity instanceof PlayerEntity && ((PlayerEntity) thisEntity).isCreative());
				boolean creativeFlying = creativePlayer && ((PlayerEntity) thisEntity).getAbilities().flying;
				ItemStack chestplate = null;
				
				for(ItemStack stack : thisEntity.getArmorItems())
				{
					if(stack.getItem() == StarflightItems.SPACE_SUIT_HELMET || stack.getItem() == StarflightItems.SPACE_SUIT_LEGGINGS || stack.getItem() == StarflightItems.SPACE_SUIT_BOOTS)
						spaceSuitCheck++;
					else if(stack.getItem() == StarflightItems.SPACE_SUIT_CHESTPLATE && stack.getNbt() != null && stack.getNbt().getDouble("oxygen") > 0.0)
					{
						spaceSuitCheck++;
						chestplate = stack;
					}
				}
				
				double oxygen = spaceSuitCheck == 4 ? chestplate.getNbt().getDouble("oxygen") : 0.0;
				double oxygenUsed = 0.0;
				
				if(spaceSuitCheck < 4 && !(thisEntity.isSubmergedInWater() || habitableAir) && !creativePlayer)
					thisEntity.damage(DamageSource.GENERIC, 0.5f);
				else if(spaceSuitCheck == 4 && (thisEntity.isSubmergedInWater() || !habitableAir) && !creativePlayer)
					oxygenUsed += 4.0 / 24000.0; // 4kg of oxygen should last for 20 minutes (24000 ticks) without using maneuvering jets.
				
				if(spaceSuitCheck == 4)
				{
					NbtCompound nbt = chestplate.getNbt();
					
					// Use space suit thrust jets when sneaking.
					if(thisEntity.isSneaking() && !creativeFlying && inOrbit)
					{
						oxygenUsed += 0.05 * 0.05;
						Vec3d deltaV = thisEntity.getRotationVector().multiply(0.1 * 0.05);
						thisEntity.addVelocity(deltaV.getX(), deltaV.getY(), deltaV.getZ());
						thisEntity.velocityModified = true;
						StarflightEffects.sendJet(world, thisEntity.getPos().add(0.0, 0.65, 0.0), thisEntity.getVelocity().add(thisEntity.getRotationVector().multiply(-2.0)));
					}
					
					thisEntity.setAir(thisEntity.getMaxAir());
					nbt.putDouble("oxygen", oxygen - oxygenUsed < 0.0 ? 0.0 : oxygen - oxygenUsed);
					
					// Oxygen level warning messages.
					if(thisEntity instanceof PlayerEntity)
					{
						PlayerEntity player = (PlayerEntity) thisEntity;
						int percent = (int) Math.ceil((oxygen / ((SpaceSuitItem) chestplate.getItem()).getMaxOxygen()) * 100.0);
						MutableText text = Text.translatable("item.space.space_suit.oxygen_supply");
						
						if(!nbt.contains("message1"))
							nbt.putBoolean("message1", false);
						
						if(!nbt.contains("message2"))
							nbt.putBoolean("message2", false);
						
						if(!nbt.contains("message3"))
							nbt.putBoolean("message3", false);
						
						if(percent <= 50 && !nbt.getBoolean("message1"))
						{
							text.append(percent + "%").formatted(Formatting.BOLD, Formatting.YELLOW);
							player.sendMessage(text, false);
							nbt.putBoolean("message1", true);
						}
						else if(percent > 50)
							nbt.putBoolean("message1", false);
						
						if(percent <= 25 && !nbt.getBoolean("message2"))
						{
							text.append(percent + "%").formatted(Formatting.BOLD, Formatting.YELLOW);
							player.sendMessage(text, false);
							nbt.putBoolean("message2", true);
						}
						else if(percent > 25)
							nbt.putBoolean("message2", false);
						
						if(percent <= 10 && !nbt.getBoolean("message3"))
						{
							text.append(percent + "%").formatted(Formatting.BOLD, Formatting.RED);
							player.sendMessage(text, false);
							nbt.putBoolean("message3", true);
						}
						else if(percent > 10)
							nbt.putBoolean("message3", false);
					}
				}
			}
		}
	}
	
	/**
	 * Modify the variable for living entity gravity.
	 */
	@ModifyVariable(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At("STORE"), ordinal = 0)
	public double gravityInject(double d)
	{
		return d * gravity;
	}
	
	/**
	 * Modify the constants for living entity air resistance.
	 */
	@ModifyConstant(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", constant = @Constant(floatValue = 0.99f))
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
	 * Modify movement input according to air resistance.
	 */
	@ModifyVariable(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"))
	public Vec3d movementInputInject(Vec3d movementInput)
	{
		if(this.verticalCollision || this.horizontalCollision || (gravity > 0.0 && this.getVelocity().getY() > 0.0 && this.horizontalSpeed < 0.25f))
			return movementInput;
		else
			return movementInput.multiply(airMultiplier);
	}
	
	/**
	 * Properly recognize a zero gravity environment.
	 */
	@Inject(method = "hasNoDrag()Z", at = @At("HEAD"), cancellable = true)
	public void hasNoGravityInject(CallbackInfoReturnable<Boolean> info)
	{
		if(gravity == 0.0 && airMultiplier == 0.0f)
			info.setReturnValue(true);
	}
	
	/**
	 * Reduce fall damage by the appropriate amount.
	 */
	@Inject(method = "computeFallDamage(FF)I", at = @At("HEAD"), cancellable = true)
	public void computeFallDamageInject(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			fallDistance *= gravity; // The distance for fall damage is changed to match local gravity.
			StatusEffectInstance statusEffectInstance = this.getStatusEffect(StatusEffects.JUMP_BOOST);
			float f = statusEffectInstance == null ? 0.0f : (float) (statusEffectInstance.getAmplifier() + 1);
			info.setReturnValue(MathHelper.ceil((fallDistance - 3.0f - f) * damageMultiplier));
			info.cancel();
		}
	}
}