package space.network.c2s;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import space.block.entity.AdvancedFabricatorBlockEntity;
import space.block.entity.RocketControllerBlockEntity;
import space.entity.AirshipEntity;
import space.entity.RocketEntity;
import space.network.s2c.FizzS2CPacket;
import space.network.s2c.JetS2CPacket;
import space.network.s2c.MovingCraftEntityOffsetsS2CPacket;
import space.network.s2c.MovingCraftSyncS2CPacket;
import space.network.s2c.OpenNavigationScreenS2CPacket;
import space.network.s2c.OutgasS2CPacket;
import space.network.s2c.PlanetDynamicDataS2CPacket;
import space.network.s2c.PlanetStaticDataS2CPacket;
import space.network.s2c.RocketControllerDataS2CPacket;
import space.network.s2c.RocketSyncS2CPacket;
import space.network.s2c.SyncPlayerStateS2CPacket;
import space.network.s2c.VolcanicVentS2CPacket;
import space.world.persistent.StarflightPlayerState;

public class StarflightNetworkingC2S
{
	public static void initializePackets()
	{
		PayloadTypeRegistry.playS2C().register(PlanetStaticDataS2CPacket.PACKET_ID, PlanetStaticDataS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(PlanetDynamicDataS2CPacket.PACKET_ID, PlanetDynamicDataS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(MovingCraftSyncS2CPacket.PACKET_ID, MovingCraftSyncS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(RocketSyncS2CPacket.PACKET_ID, RocketSyncS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(MovingCraftEntityOffsetsS2CPacket.PACKET_ID, MovingCraftEntityOffsetsS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(OpenNavigationScreenS2CPacket.PACKET_ID, OpenNavigationScreenS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(RocketControllerDataS2CPacket.PACKET_ID, RocketControllerDataS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(FizzS2CPacket.PACKET_ID, FizzS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(OutgasS2CPacket.PACKET_ID, OutgasS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(VolcanicVentS2CPacket.PACKET_ID, VolcanicVentS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(JetS2CPacket.PACKET_ID, JetS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(SyncPlayerStateS2CPacket.PACKET_ID, SyncPlayerStateS2CPacket.PACKET_CODEC);
		
		PayloadTypeRegistry.playC2S().register(RocketControllerButtonC2SPacket.PACKET_ID, RocketControllerButtonC2SPacket.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(RocketInputC2SPacket.PACKET_ID, RocketInputC2SPacket.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(RocketTravelButtonC2SPacket.PACKET_ID, RocketTravelButtonC2SPacket.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(AirshipInputC2SPacket.PACKET_ID, AirshipInputC2SPacket.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(AdvancedFabricatorButtonC2SPacket.PACKET_ID, AdvancedFabricatorButtonC2SPacket.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(AdvancedFabricatorUnlockC2SPacket.PACKET_ID, AdvancedFabricatorUnlockC2SPacket.PACKET_CODEC);
		
		ServerPlayNetworking.registerGlobalReceiver(RocketControllerButtonC2SPacket.PACKET_ID, (payload, context) -> RocketControllerBlockEntity.receiveButtonPress((RocketControllerButtonC2SPacket) payload, context));
		ServerPlayNetworking.registerGlobalReceiver(RocketInputC2SPacket.PACKET_ID, (payload, context) -> RocketEntity.receiveInput((RocketInputC2SPacket) payload, context));
		ServerPlayNetworking.registerGlobalReceiver(RocketTravelButtonC2SPacket.PACKET_ID, (payload, context) -> RocketEntity.receiveTravelInput((RocketTravelButtonC2SPacket) payload, context));
		ServerPlayNetworking.registerGlobalReceiver(AirshipInputC2SPacket.PACKET_ID, (payload, context) -> AirshipEntity.receiveInput((AirshipInputC2SPacket) payload, context));
		ServerPlayNetworking.registerGlobalReceiver(AdvancedFabricatorButtonC2SPacket.PACKET_ID, (payload, context) -> AdvancedFabricatorBlockEntity.receiveRecipe((AdvancedFabricatorButtonC2SPacket) payload, context));
		ServerPlayNetworking.registerGlobalReceiver(AdvancedFabricatorUnlockC2SPacket.PACKET_ID, (payload, context) -> StarflightPlayerState.receiveUnlockRecipe((AdvancedFabricatorUnlockC2SPacket) payload, context));
	}
}