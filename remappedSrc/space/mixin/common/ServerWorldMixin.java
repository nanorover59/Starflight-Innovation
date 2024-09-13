package space.mixin.common;

import java.util.HashMap;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World
{
	@Shadow @Final List<ServerPlayerEntity> players;
	@Shadow @Final MinecraftServer server;
	
	boolean seedStored = false;
	long worldSeed;
	
	protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates)
	{
		super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
	}
	
	/**
	 * Inject into the getSeed() function to ensure a unique world generation seed for each dimension.
	 */
	@Inject(method = "getSeed()J", at = @At("HEAD"), cancellable = true)
	private void getSeedInject(CallbackInfoReturnable<Long> info)
	{
		if(!seedStored)
		{
			if(getRegistryKey() == OVERWORLD)
				worldSeed = server.getSaveProperties().getGeneratorOptions().getSeed();
			else
			{
				OptionalLong newSeedOptional = GeneratorOptions.parseSeed(getRegistryKey().getValue().getPath()); 
				worldSeed = BiomeAccess.hashSeed(server.getSaveProperties().getGeneratorOptions().getSeed() + newSeedOptional.getAsLong());
			}
			
			seedStored = true;
		}
		
		info.setReturnValue(worldSeed);
		info.cancel();
	}
	
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
			RegistryKey<World> worldKey = player.method_48926().getRegistryKey();
			
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
		
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(server.getWorld(selectedWorldKey));
		
		if(data != null)
			PlanetList.skipToMorning(data.getPlanet());
		else
			PlanetList.skipToMorning(PlanetList.getByName("earth"));
	}
}