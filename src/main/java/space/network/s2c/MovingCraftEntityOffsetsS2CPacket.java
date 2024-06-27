package space.network.s2c;

import java.util.HashMap;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import space.StarflightMod;

public record MovingCraftEntityOffsetsS2CPacket(int entityID, HashMap<Integer, BlockPos> passengerMap) implements CustomPayload
{
	public static final CustomPayload.Id<MovingCraftEntityOffsetsS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "moving_craft_entity_offsets"));
    public static final PacketCodec<PacketByteBuf, MovingCraftEntityOffsetsS2CPacket> PACKET_CODEC = CustomPayload.codecOf(MovingCraftEntityOffsetsS2CPacket::write, MovingCraftEntityOffsetsS2CPacket::new);
    
    private MovingCraftEntityOffsetsS2CPacket(PacketByteBuf buffer)
    {
    	this(buffer.readInt(), (HashMap<Integer, BlockPos>) buffer.readMap(PacketByteBuf::readInt, MovingCraftEntityOffsetsS2CPacket::readBlockPos));
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeInt(entityID);
    	buffer.writeMap(passengerMap, PacketByteBuf::writeInt, MovingCraftEntityOffsetsS2CPacket::writeBlockPos);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
    
    private static BlockPos readBlockPos(ByteBuf buffer)
    {
        return BlockPos.fromLong(buffer.readLong());
    }
    
	private static void writeBlockPos(ByteBuf buffer, BlockPos pos)
	{
		buffer.writeLong(pos.asLong());
	}
}