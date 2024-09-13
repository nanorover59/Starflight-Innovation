package space.network.s2c;

import java.util.HashMap;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import space.StarflightMod;
import space.planet.Planet;

public record PlanetDynamicDataS2CPacket(HashMap<String, Planet.DynamicData> dynamicDataMap, RegistryKey<World> viewpoint, int timeSteps) implements CustomPayload
{
	public static final CustomPayload.Id<PlanetDynamicDataS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "planet_dynamic_data"));
    public static final PacketCodec<PacketByteBuf, PlanetDynamicDataS2CPacket> PACKET_CODEC = CustomPayload.codecOf(PlanetDynamicDataS2CPacket::write, PlanetDynamicDataS2CPacket::new);
    
    private PlanetDynamicDataS2CPacket(PacketByteBuf buffer)
    {
    	this((HashMap<String, Planet.DynamicData>) buffer.readMap(PacketByteBuf::readString, Planet.DynamicData::new), buffer.readRegistryKey(RegistryKeys.WORLD), buffer.readInt());
    }

	private void write(PacketByteBuf buffer)
    {
		buffer.writeMap(dynamicDataMap, PacketByteBuf::writeString, Planet::writeDynamicData);
    	buffer.writeRegistryKey(viewpoint);
    	buffer.writeInt(timeSteps);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
}