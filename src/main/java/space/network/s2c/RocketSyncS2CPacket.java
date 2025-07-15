package space.network.s2c;

import java.util.ArrayList;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.craft.MovingCraftBlock;
import space.craft.Thruster;

public record RocketSyncS2CPacket(int entityID, ArrayList<MovingCraftBlock> blockDataList, ArrayList<Thruster> mainThrusters, ArrayList<Thruster> rcsThrusters) implements CustomPayload
{
	public static final CustomPayload.Id<RocketSyncS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "rocket_sync"));
    public static final PacketCodec<PacketByteBuf, RocketSyncS2CPacket> PACKET_CODEC = CustomPayload.codecOf(RocketSyncS2CPacket::write, RocketSyncS2CPacket::new);
    
    private RocketSyncS2CPacket(PacketByteBuf buffer)
    {
    	this(buffer.readInt(), buffer.readCollection(ArrayList::new, MovingCraftBlock::new), buffer.readCollection(ArrayList::new, Thruster::new), buffer.readCollection(ArrayList::new, Thruster::new));
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeInt(entityID);
    	buffer.writeCollection(blockDataList, MovingCraftBlock::writeBlockData);
    	buffer.writeCollection(mainThrusters, Thruster::writeToBuffer);
    	buffer.writeCollection(rcsThrusters, Thruster::writeToBuffer);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
}