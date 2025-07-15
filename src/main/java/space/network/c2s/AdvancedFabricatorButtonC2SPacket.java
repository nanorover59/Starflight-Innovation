package space.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public record AdvancedFabricatorButtonC2SPacket(int index) implements CustomPayload
{
	public static final CustomPayload.Id<AdvancedFabricatorButtonC2SPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "advanced_fabricator_button"));
    public static final PacketCodec<PacketByteBuf, AdvancedFabricatorButtonC2SPacket> PACKET_CODEC = CustomPayload.codecOf(AdvancedFabricatorButtonC2SPacket::write, AdvancedFabricatorButtonC2SPacket::new);
    
    private AdvancedFabricatorButtonC2SPacket(PacketByteBuf buffer)
    {
    	this(buffer.readInt());
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeInt(index);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
}