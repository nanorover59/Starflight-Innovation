package space.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public record RocketTravelButtonC2SPacket(String planetName, double requiredDeltaV, boolean landing) implements CustomPayload
{
	public static final CustomPayload.Id<RocketTravelButtonC2SPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "rocket_travel_button"));
    public static final PacketCodec<PacketByteBuf, RocketTravelButtonC2SPacket> PACKET_CODEC = CustomPayload.codecOf(RocketTravelButtonC2SPacket::write, RocketTravelButtonC2SPacket::new);
    
    private RocketTravelButtonC2SPacket(PacketByteBuf buffer)
    {
    	this(buffer.readString(), buffer.readDouble(), buffer.readBoolean());
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeString(planetName);
    	buffer.writeDouble(requiredDeltaV);
    	buffer.writeBoolean(landing);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
}