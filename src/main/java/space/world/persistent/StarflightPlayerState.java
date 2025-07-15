package space.world.persistent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import space.network.c2s.AdvancedFabricatorUnlockC2SPacket;
import space.network.s2c.SyncPlayerStateS2CPacket;
import space.screen.AdvancedFabricatorScreenHandler;

public class StarflightPlayerState extends PersistentState
{
	private static Type<StarflightPlayerState> type = new Type<StarflightPlayerState>(StarflightPlayerState::new, StarflightPlayerState::createFromNbt, null);
	private final Map<UUID, StarflightPlayerData> playerData = new HashMap<>();
	

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		for(Map.Entry<UUID, StarflightPlayerData> entry : playerData.entrySet())
		{
			NbtCompound playerDataNbt = new NbtCompound();
			entry.getValue().writeNbt(playerDataNbt);
			nbt.put(entry.getKey().toString(), playerDataNbt);
		}
		
		return nbt;
	}

	public static StarflightPlayerState createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
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
	
	public static StarflightPlayerState getServerState(MinecraftServer server)
	{
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();
        StarflightPlayerState state = persistentStateManager.getOrCreate(type, "starflight_player");
        state.markDirty();
        return state;
    }
	
	public static StarflightPlayerData getPlayerData(LivingEntity player)
	{
		StarflightPlayerState serverState = getServerState(player.getWorld().getServer());
		StarflightPlayerData playerState = serverState.playerData.computeIfAbsent(player.getUuid(), uuid -> new StarflightPlayerData());
		return playerState;
	}
	
	public static void syncPlayerState(ServerPlayerEntity player)
	{
    	StarflightPlayerData playerData = StarflightPlayerState.getPlayerData(player);
		ServerPlayNetworking.send(player, new SyncPlayerStateS2CPacket(playerData));
	}
	
	public static void receiveUnlockRecipe(AdvancedFabricatorUnlockC2SPacket payload, ServerPlayNetworking.Context context)
	{
		ServerPlayerEntity player = context.player();
		String recipeID = payload.recipeID();
		int science = payload.science();
		
		if(player.currentScreenHandler != null && player.currentScreenHandler instanceof AdvancedFabricatorScreenHandler)
		{
			player.getServer().execute(() -> {
				StarflightPlayerData data = getPlayerData(player);
				data.science -= science;
				data.unlockedRecipes.add(recipeID);
				syncPlayerState(player);
			});
		}
	}
}