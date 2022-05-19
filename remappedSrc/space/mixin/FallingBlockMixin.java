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
public abstract class FallingBlockMixin
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
	
	/*@Inject(method = "onBlockAdded(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V", at = @At("HEAD"), cancellable = true)
	public void onBlockAddedInject(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo info)
	{
		if(world.getRegistryKey() != World.OVERWORLD && world.getRegistryKey() != World.NETHER && world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(world.getRegistryKey());
			
			if(currentPlanet != null && (currentPlanet.getSurfaceGravity() < 0.05 || PlanetList.isOrbit(world.getRegistryKey())))
				info.cancel();
		}
	}
	
	@Inject(method = "getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"), cancellable = true)
	public void getStateForNeighborUpdateInject(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> info)
	{
		RegistryKey<World> worldKey = world.getServer().getWorldRegistryKeys().
		
		if(world.getRegistryKey() != World.OVERWORLD && world.getRegistryKey() != World.NETHER && world.getRegistryKey() != World.END)
		{
			Planet currentPlanet = PlanetList.getPlanetForWorld(world.getRegistryKey());
			
			if(currentPlanet != null && (currentPlanet.getSurfaceGravity() < 0.05 || PlanetList.isOrbit(world.getRegistryKey())))
				info.cancel();
		}
	}*/
}
