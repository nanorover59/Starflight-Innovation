package space.network.s2c;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import space.StarflightMod;

public record BeamS2CPacket(int sourceID, int targetID, Vec3d sourcePos, Vec3d direction, int color, int duration) implements CustomPayload
{
	public static final CustomPayload.Id<BeamS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "beam"));
    public static final PacketCodec<PacketByteBuf, BeamS2CPacket> PACKET_CODEC = CustomPayload.codecOf(BeamS2CPacket::write, BeamS2CPacket::new);
    
    private BeamS2CPacket(PacketByteBuf buffer)
    {
    	this(buffer.readInt(), buffer.readInt(), buffer.readVec3d(), buffer.readVec3d(), buffer.readInt(), buffer.readInt());
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeInt(sourceID);
    	buffer.writeInt(targetID);
    	buffer.writeVec3d(sourcePos);
    	buffer.writeVec3d(direction);
    	buffer.writeInt(color);
    	buffer.writeInt(duration);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
    
    public static void sendBeam(Entity source, Entity target, Vec3d sourcePos, Vec3d direction, int color, int duration, int playerRange)
	{
		for(PlayerEntity player : source.getWorld().getPlayers())
		{
			if(source.distanceTo(player) < playerRange)
				ServerPlayNetworking.send((ServerPlayerEntity) player, new BeamS2CPacket(source.getId(), target == null ? -1 : target.getId(), sourcePos, direction, color, duration));
		}
	}
}