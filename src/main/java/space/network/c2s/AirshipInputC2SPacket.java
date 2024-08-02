package space.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public record AirshipInputC2SPacket(int[] inputStates) implements CustomPayload
{
	public static final CustomPayload.Id<AirshipInputC2SPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "airship_input"));
    public static final PacketCodec<PacketByteBuf, AirshipInputC2SPacket> PACKET_CODEC = CustomPayload.codecOf(AirshipInputC2SPacket::write, AirshipInputC2SPacket::new);
    
    private AirshipInputC2SPacket(PacketByteBuf buffer)
    {
    	this(buffer.readIntArray());
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeIntArray(inputStates);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
}