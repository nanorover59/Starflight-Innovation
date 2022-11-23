package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.StarflightEffects;
import space.world.StarflightWorldGeneration;

@Mixin(FluidState.class)
public abstract class FluidStateMixin
{
	@Shadow abstract Fluid getFluid();
	
	/**
	 * Inject into the onScheduledTick() function for different behavior in a non-compatible atmosphere.
	 */
	@Inject(method = "onScheduledTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At("HEAD"), cancellable = true)
	public void onScheduledTickInject(World world, BlockPos pos, CallbackInfo info)
	{
		if(world.getRegistryKey() != World.OVERWORLD && world.getRegistryKey() != World.NETHER && world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(world.getRegistryKey());
			
			if(currentPlanet != null && this.getFluid() == Fluids.WATER && (world.getFluidState(pos).isStill() || world.getFluidState(pos).getLevel() == 0))
			{
				int temperature = currentPlanet.getTemperatureCategory(world.getSkyAngle(1.0f), PlanetList.isOrbit(world.getRegistryKey()));
				
		        if(temperature != Planet.TEMPERATE && !world.getBiome(pos).isIn(StarflightWorldGeneration.LIQUID_WATER))
		        {
		        	boolean air = false;
		        	float chance = temperature == Planet.EXTRA_COLD ? 0.5f : 0.05f;
		        	
		        	for(Direction d1 : Direction.values())
		        	{
		        		if(world.getBlockState(pos.offset(d1)).getBlock() == Blocks.AIR && (world.getFluidState(pos.offset(d1, -1)).getFluid() != Fluids.WATER || world.getBlockState(pos.up()).getBlock() == Blocks.AIR))
		        		{
		        			air = true;
		        			break;
		        		}
		        	}
		        	
		        	if(air)
		        	{
		        		BlockState blockState = world.getBlockState(pos);
		        		
						if(temperature < Planet.TEMPERATE && world.getRandom().nextFloat() < chance)
						{
							if(blockState.getBlock() instanceof Waterloggable)
								world.setBlockState(pos, blockState.with(Properties.WATERLOGGED, false));
							else
							{
								int i = 0;
								
								for(Direction d2 : Direction.values())
					        	{
									if(d2 != Direction.UP && world.getFluidState(pos.offset(d2)).getFluid() == Fluids.WATER)
										i++;
					        	}
								
								if(i < 5)
									world.setBlockState(pos, Blocks.ICE.getDefaultState());
								else
									world.setBlockState(pos.up(), Blocks.ICE.getDefaultState());
							}
						}
						else if(temperature >= Planet.TEMPERATE)
						{
							if(blockState.getBlock() instanceof Waterloggable)
								world.setBlockState(pos, blockState.with(Properties.WATERLOGGED, false));
							else
								world.setBlockState(pos, Blocks.AIR.getDefaultState());
							
							StarflightEffects.sendOutgas(world, pos, pos.up(), true);
						}
						else
							world.createAndScheduleFluidTick(pos, world.getFluidState(pos).getFluid(), 5);
		        	}
		        }
			}
		}
		
		if(PlanetList.isOrbit(world.getRegistryKey()))
			info.cancel();
	}
}
