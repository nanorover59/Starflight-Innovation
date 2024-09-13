package space.network.s2c;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;
import space.StarflightMod;

public record JetS2CPacket(Vec3d sourcePos, Vec3d velocity) implements CustomPayload
{
	public static final CustomPayload.Id<JetS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "jet"));
    public static final PacketCodec<PacketByteBuf, JetS2CPacket> PACKET_CODEC = CustomPayload.codecOf(JetS2CPacket::write, JetS2CPacket::new);
    
    private JetS2CPacket(PacketByteBuf buffer)
    {
    	this(buffer.readVec3d(), buffer.readVec3d());
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeVec3d(sourcePos);
    	buffer.writeVec3d(velocity);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
    
    public static void sendJet(WorldAccess world, Vec3d sourcePos, Vec3d velocity)
	{
		for(ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList())
			ServerPlayNetworking.send(player, new JetS2CPacket(sourcePos, velocity));
	}
}