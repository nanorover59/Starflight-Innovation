package space.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public record RocketInputC2SPacket(int[] inputStates) implements CustomPayload
{
	public static final CustomPayload.Id<RocketInputC2SPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "rocket_input"));
    public static final PacketCodec<PacketByteBuf, RocketInputC2SPacket> PACKET_CODEC = CustomPayload.codecOf(RocketInputC2SPacket::write, RocketInputC2SPacket::new);
    
    private RocketInputC2SPacket(PacketByteBuf buffer)
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