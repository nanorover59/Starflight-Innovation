package space.network.s2c;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.WorldAccess;
import space.StarflightMod;

public record UnlockPlanetS2CPacket(String planetName, int color) implements CustomPayload
{
	public static final CustomPayload.Id<UnlockPlanetS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "unlock_planet"));
    public static final PacketCodec<PacketByteBuf, UnlockPlanetS2CPacket> PACKET_CODEC = CustomPayload.codecOf(UnlockPlanetS2CPacket::write, UnlockPlanetS2CPacket::new);
    
    private UnlockPlanetS2CPacket(PacketByteBuf buffer)
    {
    	this(buffer.readString(), buffer.readInt());
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeString(planetName);
    	buffer.writeInt(color);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
    
    public static void sendUnlockPlanet(WorldAccess world, String planetName, int color)
	{
		for(ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList())
			ServerPlayNetworking.send(player, new UnlockPlanetS2CPacket(planetName, color));
	}
}