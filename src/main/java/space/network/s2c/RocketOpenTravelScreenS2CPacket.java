package space.network.s2c;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public record RocketOpenTravelScreenS2CPacket(double deltaV) implements CustomPayload
{
	public static final CustomPayload.Id<RocketOpenTravelScreenS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "rocket_open_travel_screen"));
    public static final PacketCodec<PacketByteBuf, RocketOpenTravelScreenS2CPacket> PACKET_CODEC = CustomPayload.codecOf(RocketOpenTravelScreenS2CPacket::write, RocketOpenTravelScreenS2CPacket::new);
    
    private RocketOpenTravelScreenS2CPacket(PacketByteBuf buffer)
    {
    	this(buffer.readDouble());
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeDouble(deltaV);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
}