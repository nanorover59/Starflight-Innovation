package space.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.entity.RocketEntity;
import space.item.SpaceSuitItem;
import space.item.StarflightItems;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.AirUtil;
import space.util.StarflightEffects;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity
{
	@Shadow abstract boolean canMoveVoluntarily();
	@Shadow abstract SoundEvent getFallSound(int distance);
	@Shadow abstract boolean hasStatusEffect(StatusEffect effect);
	@Shadow abstract protected boolean shouldSwimInFluids();
	@Shadow abstract boolean canWalkOnFluid(FluidState fluidState);
	@Shadow abstract protected float getBaseMovementSpeedMultiplier();
	@Shadow abstract float getMovementSpeed();
	@Shadow abstract boolean isClimbing();
	@Shadow abstract Vec3d method_26317(double d, boolean bl, Vec3d vec3d);
	@Shadow abstract boolean isFallFlying();
	@Shadow abstract Vec3d applyMovementInput(Vec3d movementInput, float slipperiness);
	@Shadow @Nullable abstract StatusEffectInstance getStatusEffect(StatusEffect effect);
	@Shadow abstract boolean hasNoDrag();
	@Shadow abstract void updateLimbs(LivingEntity entity, boolean flutter);

	protected LivingEntityMixin(EntityType<? extends LivingEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	/**
	 * Inject into the baseTick() function for dealing damage in a non-compatible atmosphere.
	 */
	@Inject(method = "baseTick()V", at = @At("TAIL"), cancellable = true)
	public void baseTickInject(CallbackInfo info)
	{
		LivingEntity thisEntity = (LivingEntity) world.getEntityById(getId());
		
		if(thisEntity != null && !thisEntity.getWorld().isClient() && !(thisEntity.getVehicle() instanceof RocketEntity))
		{
			int spaceSuitCheck = 0;
			boolean habitableAir = AirUtil.canEntityBreathe(thisEntity);
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
				if(thisEntity.isSneaking() && !creativeFlying && PlanetList.isOrbit(world.getRegistryKey()))
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
	
	/**
	 * Modified vanilla living entity physics to account for different gravity and air resistance values.
	 */
	@Inject(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
	public void travelInject(Vec3d movementInput, CallbackInfo info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());
			LivingEntity thisEntity = (LivingEntity) world.getEntityById(getId());
			boolean b = true;
			
			if(thisEntity instanceof PlayerEntity)
				b = !((PlayerEntity) thisEntity).getAbilities().flying;
			
			if(currentPlanet != null && thisEntity != null && b)
			{
				if(this.canMoveVoluntarily() || this.isLogicalSideForUpdatingMovement())
				{
					double d = 0.08 * currentPlanet.getSurfaceGravity(); // Adjust the vertical acceleration due to gravity.
					boolean bl = this.getVelocity().y <= 0.0;
					double airMultiplier = AirUtil.getAirResistanceMultiplier(world, this.getBlockPos()); // Atmospheric pressure multiplier for air resistance.
					
					if(thisEntity.isTouchingWater())
						airMultiplier = 1.0;
					
					if(PlanetList.isOrbit(this.world.getRegistryKey()))
					{
						thisEntity.fallDistance = 0.0f;
						boolean wall = false;
						
						for(Direction direction : Direction.values())
						{
							BlockPos offset = thisEntity.getBlockPos().offset(direction, direction == Direction.UP ? Math.round(thisEntity.getHeight()) : 1);
							
							if(world.getBlockState(offset).getBlock() != Blocks.AIR)
							{
								wall = true;
								break;
							}
						}
						
						if(!wall)
							movementInput = Vec3d.ZERO;
						else
							airMultiplier = 1.0;
					}
					
					if(bl && hasStatusEffect(StatusEffects.SLOW_FALLING))
					{
						d = 0.01 * currentPlanet.getSurfaceGravity();
						this.onLanding();
					}

					FluidState fluidState = this.world.getFluidState(this.getBlockPos());

					if(this.isTouchingWater() && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState))
					{
						double e = this.getY();
						float f = this.isSprinting() ? 0.9f : this.getBaseMovementSpeedMultiplier();
						float g = 0.02f;
						float h = EnchantmentHelper.getDepthStrider(thisEntity);

						if(h > 3.0f)
							h = 3.0f;

						if(!this.onGround)
							h *= 0.5f;

						if(h > 0.0f)
						{
							f += (0.54600006f - f) * h / 3.0f;
							g += (this.getMovementSpeed() - g) * h / 3.0f;
						}

						if(this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE))
							f = 0.96f;

						this.updateVelocity(g, movementInput);
						this.move(MovementType.SELF, this.getVelocity());
						Vec3d vec3d = this.getVelocity();

						if(this.horizontalCollision && this.isClimbing())
							vec3d = new Vec3d(vec3d.x, 0.2, vec3d.z);

						this.setVelocity(vec3d.multiply(f, 0.8f, f));
						Vec3d vec3d2 = this.method_26317(d, bl, this.getVelocity());
						this.setVelocity(vec3d2);

						if(this.horizontalCollision && this.doesNotCollide(vec3d2.x, vec3d2.y + 0.6f - this.getY() + e, vec3d2.z))
							this.setVelocity(vec3d2.x, 0.3f, vec3d2.z);
					}
					else if(this.isInLava() && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState))
					{
						Vec3d f;
						double e = this.getY();
						this.updateVelocity(0.02f, movementInput);
						this.move(MovementType.SELF, this.getVelocity());

						if(this.getFluidHeight(FluidTags.LAVA) <= this.getSwimHeight())
						{
							this.setVelocity(this.getVelocity().multiply(0.5, 0.8f, 0.5));
							f = this.method_26317(d, bl, this.getVelocity());
							this.setVelocity(f);
						}
						else
							this.setVelocity(this.getVelocity().multiply(0.5));

						if(!this.hasNoGravity())
							this.setVelocity(this.getVelocity().add(0.0, -d / 4.0, 0.0));

						f = this.getVelocity();

						if(this.horizontalCollision && this.doesNotCollide(f.x, f.y + 0.6f - this.getY() + e, f.z))
							this.setVelocity(f.x, 0.3f, f.z);
					}
					else if(this.isFallFlying())
					{
						float drag1 = (float) (1.0 / (1.0 + (0.01 * airMultiplier)));
						float drag2 = (float) (1.0 / (1.0 + (0.02 * airMultiplier)));
						float m;
						double k;
						Vec3d e = this.getVelocity();

						if(e.y > -0.5)
							this.fallDistance = 1.0f;

						Vec3d vec3d3 = this.getRotationVector();
						float f = this.getPitch() * ((float) Math.PI / 180);
						double g = Math.sqrt(vec3d3.x * vec3d3.x + vec3d3.z * vec3d3.z);
						double h = e.horizontalLength();
						double i = vec3d3.length();
						float j = MathHelper.cos(f);
						j = (float) (j * (j * Math.min(1.0, i / 0.4)) * airMultiplier); // Lift is now dependent on atmospheric pressure.
						e = this.getVelocity().add(0.0, d * (-1.0 + j * 0.75), 0.0);

						if(e.y < 0.0 && g > 0.0)
						{
							k = e.y * -0.1 * j;
							e = e.add(vec3d3.x * k / g, k, vec3d3.z * k / g);
						}

						// Horizontal control is now dependent on atmospheric pressure.
						if(f < 0.0f && g > 0.0)
						{
							k = h * (-MathHelper.sin(f)) * 0.04 * airMultiplier;
							Vec3d n = new Vec3d(-vec3d3.x * k / g, k * 3.2, -vec3d3.z * k / g);
							e = e.add(n.multiply(airMultiplier));
						}

						if(g > 0.0)
						{
							Vec3d n = new Vec3d((vec3d3.x / g * h - e.x) * 0.1, 0.0, (vec3d3.z / g * h - e.z) * 0.1);
							e = e.add(n.multiply(airMultiplier));
						}
						
						this.setVelocity(e.multiply(drag1, drag2, drag1));
						this.move(MovementType.SELF, this.getVelocity());

						if(this.horizontalCollision && !this.world.isClient && (m = (float) ((h - (k = this.getVelocity().horizontalLength())) * 10.0 - 3.0)) > 0.0f)
						{
							this.playSound(this.getFallSound((int) m), 1.0f, 1.0f);
							this.damage(DamageSource.FLY_INTO_WALL, m);
						}

						if(this.onGround && !this.world.isClient)
							this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);
					}
					else
					{
						float drag1 = (float) (1.0 / (1.0 + (0.09 * airMultiplier)));
						float drag2 = (float) (1.0 / (1.0 + (0.02 * airMultiplier)));
						BlockPos e = this.getVelocityAffectingPos();
						float i = this.world.getBlockState(e).getBlock().getSlipperiness();
						float f = this.onGround ? i * 0.91f : drag1;
						Vec3d g = this.applyMovementInput(this.onGround ? movementInput : movementInput.multiply(airMultiplier), i);
						double h = g.y;
						ChunkPos chunkPos = new ChunkPos(e);
						
						if(PlanetList.isOrbit(this.world.getRegistryKey()))
						{
							if(thisEntity.horizontalCollision)
							{
								if(thisEntity.getPitch() < -15.0f)
									h = 0.05;
								else if(thisEntity.getPitch() > 15.0f)
									h = -0.05;
							}
						}
						else if(this.hasStatusEffect(StatusEffects.LEVITATION))
						{
							h += (0.05 * (this.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - g.y) * 0.2;
							this.onLanding();
						}
						else if(!this.world.isClient || this.world.isChunkLoaded(chunkPos.x, chunkPos.z))
						{
							if(!this.hasNoGravity())
								h -= d;
						}
						else
							h = this.getY() > this.world.getBottomY() ? -0.1 : 0.0;

						if(this.hasNoDrag())
							this.setVelocity(g.x, h, g.z);
						else
							this.setVelocity(g.x * f, h * drag2, g.z * f);
					}
				}
				
				if(thisEntity != null)
					this.updateLimbs(thisEntity, thisEntity instanceof Flutterer);

				info.cancel();
			}
		}
	}
	
	/**
	 * Reduce fall damage by the appropriate amount.
	 */
	@Inject(method = "computeFallDamage(FF)I", at = @At("HEAD"), cancellable = true)
	public void computeFallDamageInject(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> info)
	{
		if(this.world.getRegistryKey() != World.OVERWORLD && this.world.getRegistryKey() != World.NETHER && this.world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(this.world.getRegistryKey());
			
			if(currentPlanet != null)
			{
				fallDistance *= currentPlanet.getSurfaceGravity(); // The distance for fall damage is changed to match local gravity.
				StatusEffectInstance statusEffectInstance = this.getStatusEffect(StatusEffects.JUMP_BOOST);
				float f = statusEffectInstance == null ? 0.0f : (float)(statusEffectInstance.getAmplifier() + 1);
				info.setReturnValue(MathHelper.ceil((fallDistance - 3.0f - f) * damageMultiplier));
				info.cancel();
			}
		}
	}
}
