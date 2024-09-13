package space.network.s2c;

import java.util.ArrayList;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.entity.MovingCraftEntity;

public record MovingCraftBlocksS2CPacket(int entityID, ArrayList<MovingCraftEntity.BlockData> blockDataList) implements CustomPayload
{
	public static final CustomPayload.Id<MovingCraftBlocksS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "moving_craft_render_data"));
    public static final PacketCodec<PacketByteBuf, MovingCraftBlocksS2CPacket> PACKET_CODEC = CustomPayload.codecOf(MovingCraftBlocksS2CPacket::write, MovingCraftBlocksS2CPacket::new);
    
    private MovingCraftBlocksS2CPacket(PacketByteBuf buffer)
    {
    	this(buffer.readInt(), buffer.readCollection(ArrayList::new, MovingCraftEntity.BlockData::new));
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeInt(entityID);
    	buffer.writeCollection(blockDataList, MovingCraftEntity.BlockData::writeBlockData);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
}