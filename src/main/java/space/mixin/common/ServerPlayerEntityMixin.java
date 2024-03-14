package space.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.entity.RocketEntity;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin
{
	@Shadow @Nullable private BlockPos spawnPointPosition;
	
	/*
	 * Allow a player to respawn on another planet only if the spawn point is a habitable environment.
	 */
	@Inject(method = "getSpawnPointPosition()Lnet/minecraft/util/math/BlockPos;", at = @At("HEAD"), cancellable = true)
	private void getSpawnPointInject(CallbackInfoReturnable<BlockPos> info)
	{
		ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		World world = player.getWorld();
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);

		if(data != null && spawnPointPosition != null && !AirUtil.canEntityBreathe(player, spawnPointPosition, data))
		{
			BlockPos defaultSpawn = new BlockPos(world.getLevelProperties().getSpawnX(), world.getLevelProperties().getSpawnY(), world.getLevelProperties().getSpawnZ());
			player.setSpawnPoint(World.OVERWORLD, defaultSpawn, 0.0f, true, false);
			info.setReturnValue(defaultSpawn);
			info.cancel();
		}
	}
	
	/**
	 * Prevent the portal sound effect from playing.
	 */
	@Redirect(method = "moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
	private void cancelPortalSound(ServerPlayNetworkHandler networkHandler, Packet<?> packet)
	{
		// Check if the packet is the portal travel event.
		if(packet instanceof WorldEventS2CPacket)
		{
			ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
			Entity vehicle = player.getVehicle();
			
			if(vehicle instanceof RocketEntity || (player.getVelocity().getY() < 0.0 && player.getBlockY() >= player.getWorld().getTopY() - 8) || (player.getVelocity().getY() > 0.0 && player.getBlockY() <= player.getWorld().getBottomY() + 8))
				return;
		}
		
		// Otherwise, send the packet normally.
		networkHandler.sendPacket(packet);
	}
}