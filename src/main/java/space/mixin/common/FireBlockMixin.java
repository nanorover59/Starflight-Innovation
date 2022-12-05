package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import space.block.StarflightBlocks;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin
{
	@Inject(method = "scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V", at = @At("HEAD"), cancellable = true)
	public void scheduledTickInject(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info)
	{
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
		
		if(data != null && data.overridePhysics() && world.getRegistryKey() != World.OVERWORLD && world.getRegistryKey() != World.NETHER && world.getRegistryKey() != World.END)
		{
			boolean b = false;
			
			for(Direction direction : Direction.values())
			{
				if(world.getBlockState(pos.offset(direction)).getBlock() == StarflightBlocks.HABITABLE_AIR)
				{
					b = true;
					break;
				}
			}
			
			if((data.isOrbit() || !data.hasOxygen()) && !b)
			{
				world.removeBlock(pos, false);
				info.cancel();
			}
		}
	}
}