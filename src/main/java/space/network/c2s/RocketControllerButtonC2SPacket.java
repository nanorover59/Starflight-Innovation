package space.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public record RocketControllerButtonC2SPacket(int action) implements CustomPayload
{
	public static final CustomPayload.Id<RocketControllerButtonC2SPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "rocket_controller_button"));
    public static final PacketCodec<PacketByteBuf, RocketControllerButtonC2SPacket> PACKET_CODEC = CustomPayload.codecOf(RocketControllerButtonC2SPacket::write, RocketControllerButtonC2SPacket::new);
    
    private RocketControllerButtonC2SPacket(PacketByteBuf buffer)
    {
    	this(buffer.readInt());
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeInt(action);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
}