package space.network.s2c;

import java.util.ArrayList;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.craft.MovingCraftBlock;

public record MovingCraftSyncS2CPacket(int entityID, ArrayList<MovingCraftBlock> blockDataList) implements CustomPayload
{
	public static final CustomPayload.Id<MovingCraftSyncS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "moving_craft_sync"));
    public static final PacketCodec<PacketByteBuf, MovingCraftSyncS2CPacket> PACKET_CODEC = CustomPayload.codecOf(MovingCraftSyncS2CPacket::write, MovingCraftSyncS2CPacket::new);
    
    private MovingCraftSyncS2CPacket(PacketByteBuf buffer)
    {
    	this(buffer.readInt(), buffer.readCollection(ArrayList::new, MovingCraftBlock::new));
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeInt(entityID);
    	buffer.writeCollection(blockDataList, MovingCraftBlock::writeBlockData);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
}