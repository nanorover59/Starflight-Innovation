package space.world.persistent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class StarflightPlayerState extends PersistentState
{
	private static Type<StarflightPlayerState> type = new Type<StarflightPlayerState>(StarflightPlayerState::new, StarflightPlayerState::createFromNbt, null);
	private final Map<UUID, StarflightPlayerData> playerData = new HashMap<>();
	
	public StarflightPlayerData getPlayerData(ServerPlayerEntity player)
	{
		StarflightPlayerData data;
		
		if(playerData.containsKey(player.getUuid()))
			data = playerData.get(player.getUuid());
		else
		{
			data = new StarflightPlayerData();
			data.unlockPlanet("earth");
			data.unlockPlanet("moon");
			data.unlockPlanet("sol");
			playerData.put(player.getUuid(), data);
		}
		
		return data;
	}
	
	public void unlockPlanet(ServerPlayerEntity player, String name)
	{
		StarflightPlayerData data = getPlayerData(player);
		data.unlockPlanet(name);
		markDirty();
	}
	
	public boolean isPlanetUnlocked(ServerPlayerEntity player, String name)
	{
		StarflightPlayerData data = getPlayerData(player);
		return data.getUnlockedPlanetNames().contains(name);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt)
	{
		for(Map.Entry<UUID, StarflightPlayerData> entry : playerData.entrySet())
		{
			NbtCompound playerDataNbt = new NbtCompound();
			entry.getValue().writeNbt(playerDataNbt);
			nbt.put(entry.getKey().toString(), playerDataNbt);
		}
		
		return nbt;
	}

	public static StarflightPlayerState createFromNbt(NbtCompound nbt)
	{
		StarflightPlayerState state = new StarflightPlayerState();
		
		for(String uuidKey : nbt.getKeys())
		{
			UUID playerUUID = UUID.fromString(uuidKey);
			NbtCompound playerDataNbt = nbt.getCompound(uuidKey);
			state.playerData.put(playerUUID, StarflightPlayerData.createFromNbt(playerDataNbt));
		}
		
		return state;
	}
	
	public static StarflightPlayerState get(MinecraftServer server)
	{
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();
        StarflightPlayerState state = persistentStateManager.getOrCreate(type, "starflight_player");
        state.markDirty();
        return state;
    }
}