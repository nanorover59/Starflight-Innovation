package space.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin
{
	@Shadow @Nullable private BlockPos spawnPointPosition;
	
	@Inject(method = "getSpawnPointPosition()Lnet/minecraft/util/math/BlockPos;", at = @At("HEAD"), cancellable = true)
	private void getSpawnPointInject(CallbackInfoReturnable<BlockPos> info)
	{
		ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		World world = player.method_48926();
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);

		if(data != null && spawnPointPosition != null && !AirUtil.canEntityBreathe(player, spawnPointPosition, data))
		{
			BlockPos defaultSpawn = new BlockPos(world.getLevelProperties().getSpawnX(), world.getLevelProperties().getSpawnY(), world.getLevelProperties().getSpawnZ());
			player.setSpawnPoint(World.OVERWORLD, defaultSpawn, 0.0f, true, false);
			info.setReturnValue(defaultSpawn);
			info.cancel();
		}
	}
}