package space.network.s2c;

import java.util.ArrayList;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.craft.MovingCraftBlock;

public record RocketControllerDataS2CPacket(double[] stats, ArrayList<MovingCraftBlock> blockDataList) implements CustomPayload
{
	public static final CustomPayload.Id<RocketControllerDataS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "rocket_controller_data"));
    public static final PacketCodec<PacketByteBuf, RocketControllerDataS2CPacket> PACKET_CODEC = CustomPayload.codecOf(RocketControllerDataS2CPacket::write, RocketControllerDataS2CPacket::new);
    
    private RocketControllerDataS2CPacket(PacketByteBuf buffer)
    {
    	this(readDoubles(buffer), buffer.readCollection(ArrayList::new, MovingCraftBlock::new));
    }
    
    private void write(PacketByteBuf buffer)
    {
    	writeDoubles(buffer, stats);
    	buffer.writeCollection(blockDataList, MovingCraftBlock::writeBlockData);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
    
	public static double[] readDoubles(PacketByteBuf buffer)
	{
		int length = buffer.readInt();
		double[] values = new double[length];

		for(int i = 0; i < length; i++)
			values[i] = buffer.readDouble();

		return values;
	}
    
	private static void writeDoubles(PacketByteBuf buffer, double[] values)
	{
		buffer.writeInt(values.length);

		for(double value : values)
			buffer.writeDouble(value);
	}
}