package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
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
import space.block.StarflightBlocks;
import space.network.s2c.OutgasS2CPacket;
import space.planet.Planet;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
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
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
			
			if(data != null && data.overridePhysics() && getFluid() == Fluids.WATER)
			{
				int temperature = data.getTemperatureCategory(world.getSkyAngle(1.0f));
	
				if(temperature != Planet.TEMPERATE && !world.getBiome(pos).isIn(StarflightWorldGeneration.LIQUID_WATER))
				{
					boolean air = false;
					float chance = temperature == Planet.EXTRA_COLD ? 0.05f : 0.005f;
					
					for(Direction d1 : Direction.values())
					{
						if(world.getBlockState(pos.offset(d1)).getBlock() == StarflightBlocks.HABITABLE_AIR)
							break;
						else if(world.getBlockState(pos.offset(d1)).isAir())
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
								world.setBlockState(pos, blockState.with(Properties.WATERLOGGED, false), Block.NOTIFY_ALL);
							else
								world.setBlockState(pos, Blocks.ICE.getDefaultState(), Block.NOTIFY_ALL);
						}
						else if(temperature >= Planet.TEMPERATE)
						{
							if(blockState.getBlock() instanceof Waterloggable)
								world.setBlockState(pos, blockState.with(Properties.WATERLOGGED, false), Block.NOTIFY_ALL);
							else
								world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
	
							OutgasS2CPacket.sendOutgas(world, pos, pos.up(), true);
						}
						else
							world.scheduleFluidTick(pos, getFluid(), 5);
					}
				}
			}
	
			// Require gravity for fluid flow.
			if(data != null && data.overridePhysics() && data.getGravity() < 0.01)
				info.cancel();
		}
	}
}