package space.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public record AdvancedFabricatorUnlockC2SPacket(String recipeID, int science) implements CustomPayload
{
	public static final CustomPayload.Id<AdvancedFabricatorUnlockC2SPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "advanced_fabricator_unlock"));
    public static final PacketCodec<PacketByteBuf, AdvancedFabricatorUnlockC2SPacket> PACKET_CODEC = CustomPayload.codecOf(AdvancedFabricatorUnlockC2SPacket::write, AdvancedFabricatorUnlockC2SPacket::new);
    
    private AdvancedFabricatorUnlockC2SPacket(PacketByteBuf buffer)
    {
    	this(buffer.readString(), buffer.readInt());
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeString(recipeID);
    	buffer.writeInt(science);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
}