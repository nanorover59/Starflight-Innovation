package space.network.s2c;

import java.util.HashMap;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.planet.Planet;

public record PlanetStaticDataS2CPacket(HashMap<String, Planet.StaticData> staticDataMap) implements CustomPayload
{
	public static final CustomPayload.Id<PlanetStaticDataS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "planet_static_data"));
    public static final PacketCodec<PacketByteBuf, PlanetStaticDataS2CPacket> PACKET_CODEC = CustomPayload.codecOf(PlanetStaticDataS2CPacket::write, PlanetStaticDataS2CPacket::new);
    
    private PlanetStaticDataS2CPacket(PacketByteBuf buffer)
    {
    	this((HashMap<String, Planet.StaticData>) buffer.readMap(PacketByteBuf::readString, Planet.StaticData::new));
    }

	private void write(PacketByteBuf buffer)
    {
		buffer.writeMap(staticDataMap, PacketByteBuf::writeString, Planet::writeStaticData);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
}