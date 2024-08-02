package space.network.c2s;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import space.block.entity.RocketControllerBlockEntity;
import space.entity.AirshipEntity;
import space.entity.RocketEntity;
import space.network.s2c.FizzS2CPacket;
import space.network.s2c.JetS2CPacket;
import space.network.s2c.MovingCraftBlocksS2CPacket;
import space.network.s2c.MovingCraftEntityOffsetsS2CPacket;
import space.network.s2c.OutgasS2CPacket;
import space.network.s2c.PlanetDataS2CPacket;
import space.network.s2c.RocketControllerDataS2CPacket;
import space.network.s2c.RocketOpenTravelScreenS2CPacket;
import space.network.s2c.UnlockPlanetS2CPacket;

public class StarflightNetworkingC2S
{
	public static void initializePackets()
	{
		PayloadTypeRegistry.playS2C().register(PlanetDataS2CPacket.PACKET_ID, PlanetDataS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(MovingCraftBlocksS2CPacket.PACKET_ID, MovingCraftBlocksS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(MovingCraftEntityOffsetsS2CPacket.PACKET_ID, MovingCraftEntityOffsetsS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(RocketOpenTravelScreenS2CPacket.PACKET_ID, RocketOpenTravelScreenS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(RocketControllerDataS2CPacket.PACKET_ID, RocketControllerDataS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(FizzS2CPacket.PACKET_ID, FizzS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(OutgasS2CPacket.PACKET_ID, OutgasS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(JetS2CPacket.PACKET_ID, JetS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(UnlockPlanetS2CPacket.PACKET_ID, UnlockPlanetS2CPacket.PACKET_CODEC);
		
		PayloadTypeRegistry.playC2S().register(RocketControllerButtonC2SPacket.PACKET_ID, RocketControllerButtonC2SPacket.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(RocketInputC2SPacket.PACKET_ID, RocketInputC2SPacket.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(RocketTravelButtonC2SPacket.PACKET_ID, RocketTravelButtonC2SPacket.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(AirshipInputC2SPacket.PACKET_ID, AirshipInputC2SPacket.PACKET_CODEC);
		
		ServerPlayNetworking.registerGlobalReceiver(RocketControllerButtonC2SPacket.PACKET_ID, (payload, context) -> RocketControllerBlockEntity.receiveButtonPress((RocketControllerButtonC2SPacket) payload, context));
		ServerPlayNetworking.registerGlobalReceiver(RocketInputC2SPacket.PACKET_ID, (payload, context) -> RocketEntity.receiveInput((RocketInputC2SPacket) payload, context));
		ServerPlayNetworking.registerGlobalReceiver(RocketTravelButtonC2SPacket.PACKET_ID, (payload, context) -> RocketEntity.receiveTravelInput((RocketTravelButtonC2SPacket) payload, context));
		ServerPlayNetworking.registerGlobalReceiver(AirshipInputC2SPacket.PACKET_ID, (payload, context) -> AirshipEntity.receiveInput((AirshipInputC2SPacket) payload, context));
	}
}