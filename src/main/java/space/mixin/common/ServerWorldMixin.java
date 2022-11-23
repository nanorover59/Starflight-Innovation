package space.mixin.common;

import java.util.HashMap;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import space.planet.Planet;
import space.planet.PlanetList;

@Mixin(ServerWorld.class)
public class ServerWorldMixin
{
	@Shadow @Final List<ServerPlayerEntity> players;
	
	/**
	 * Inject into the tick() function immediately before setTimeOfDay() is called.
	 */
	@Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
	private void tickInject(CallbackInfo info)
	{
		// Determine the planet occupied by the majority of players on the server.
		HashMap<RegistryKey<World>, Integer> map = new HashMap<RegistryKey<World>, Integer>();
		
		for(ServerPlayerEntity player : players)
		{
			RegistryKey<World> worldKey = player.getWorld().getRegistryKey();
			
			if(map.containsKey(worldKey))
				map.put(worldKey, map.get(worldKey) + 1);
			else
				map.put(worldKey, 1);
		}
		
		int maximumPlayers = 0;
		RegistryKey<World> selectedWorldKey = null;
		
		for(RegistryKey<World> worldKey : map.keySet())
		{
			int i = map.get(worldKey);
			
			if(i > maximumPlayers)
			{
				maximumPlayers = i;
				selectedWorldKey = worldKey;
			}
		}
		
		Planet planet = PlanetList.getPlanetForWorld(selectedWorldKey);
		
		if(planet != null)
			PlanetList.skipToMorning(PlanetList.getPlanetForWorld(selectedWorldKey));
		else
			PlanetList.skipToMorning(PlanetList.getByName("earth"));
	}
}