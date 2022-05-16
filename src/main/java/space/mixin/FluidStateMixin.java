package space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.StarflightEffects;

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
			
			if(currentPlanet != null && this.getFluid() == Fluids.WATER && world.getFluidState(pos).isStill())
			{
				int temperature = currentPlanet.getTemperatureCategory(world.getSkyAngle(1.0f), PlanetList.isOrbit(world.getRegistryKey()));
				
		        if(temperature != Planet.TEMPERATE)
		        {
		        	boolean air = false;
		        	float chance = 1.0f;
					
					if(temperature < Planet.TEMPERATE && world.getLightLevel(LightType.BLOCK, pos) > 11 - world.getBlockState(pos).getOpacity(world, pos))
						chance /= world.getLightLevel(LightType.BLOCK, pos) * 16.0f;
		        	
		        	for(Direction d1 : Direction.values())
		        	{
		        		if(world.getBlockState(pos.offset(d1)).getBlock() == Blocks.AIR && (world.getFluidState(pos.offset(d1, -1)).getFluid() != Fluids.WATER || world.getBlockState(pos.up()).getBlock() == Blocks.AIR))
		        		{
		        			air = true;
		        			break;
		        		}
		        	}
		        	
		        	if(air && world.getRandom().nextFloat() < chance)
		        	{
						if(temperature < Planet.TEMPERATE)
							world.setBlockState(pos, Blocks.ICE.getDefaultState());
						else
						{
							world.setBlockState(pos, Blocks.AIR.getDefaultState());
							StarflightEffects.sendOutgas(world, pos, pos.up(), true);
						}
						
						for(Direction d2 : Direction.values())
			        	{
							if(world.getFluidState(pos.offset(d2)).getFluid() == Fluids.WATER)
								world.createAndScheduleFluidTick(pos.offset(d2), world.getFluidState(pos.offset(d2)).getFluid(), 2);
			        	}
						
						info.cancel();
		        	}
		        	else if(air)
			        	world.createAndScheduleFluidTick(pos, world.getFluidState(pos).getFluid(), 2);
		        }
			}
		}
		
		if(PlanetList.isOrbit(world.getRegistryKey()))
			info.cancel();
	}
}
