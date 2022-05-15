package space.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;

@Mixin(FallingBlock.class)
public class FallingBlockMixin
{
	/**
	 * Prevent blocks from falling if the local gravity is sufficiently low.
	 */
	@Inject(method = "scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V", at = @At("HEAD"), cancellable = true)
	public void scheduledTickInject(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info)
	{
		if(world.getRegistryKey() != World.OVERWORLD && world.getRegistryKey() != World.NETHER && world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(world.getRegistryKey());
			
			if(currentPlanet != null && (currentPlanet.getSurfaceGravity() < 0.05 || PlanetList.isOrbit(world.getRegistryKey())))
				info.cancel();
		}
	}
}
