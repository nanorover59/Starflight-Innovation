package space.client;

import java.util.ArrayList;
import java.util.HashMap;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import space.StarflightMod;
import space.client.gui.RocketControllerScreen;
import space.client.gui.SpaceNavigationScreen;
import space.entity.MovingCraftEntity;
import space.network.s2c.FizzS2CPacket;
import space.network.s2c.JetS2CPacket;
import space.network.s2c.MovingCraftBlocksS2CPacket;
import space.network.s2c.MovingCraftEntityOffsetsS2CPacket;
import space.network.s2c.OpenNavigationScreenS2CPacket;
import space.network.s2c.OutgasS2CPacket;
import space.network.s2c.PlanetDynamicDataS2CPacket;
import space.network.s2c.PlanetStaticDataS2CPacket;
import space.network.s2c.RocketControllerDataS2CPacket;
import space.network.s2c.UnlockPlanetS2CPacket;
import space.particle.StarflightParticleTypes;
import space.planet.Planet;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

public class StarflightNetworkingS2C
{
	public static void initializePackets()
	{
		ClientPlayNetworking.registerGlobalReceiver(PlanetStaticDataS2CPacket.PACKET_ID, (payload, context) -> receiveStaticData((PlanetStaticDataS2CPacket) payload, context));
		ClientPlayNetworking.registerGlobalReceiver(PlanetDynamicDataS2CPacket.PACKET_ID, (payload, context) -> receiveDynamicData((PlanetDynamicDataS2CPacket) payload, context));
		ClientPlayNetworking.registerGlobalReceiver(MovingCraftBlocksS2CPacket.PACKET_ID, (payload, context) -> receiveBlockData((MovingCraftBlocksS2CPacket) payload, context));
		ClientPlayNetworking.registerGlobalReceiver(MovingCraftEntityOffsetsS2CPacket.PACKET_ID, (payload, context) -> receiveEntityOffsets((MovingCraftEntityOffsetsS2CPacket) payload, context));
		ClientPlayNetworking.registerGlobalReceiver(OpenNavigationScreenS2CPacket.PACKET_ID, (payload, context) -> SpaceNavigationScreen.receiveOpenNavigationScreen((OpenNavigationScreenS2CPacket) payload, context));
		ClientPlayNetworking.registerGlobalReceiver(RocketControllerDataS2CPacket.PACKET_ID, (payload, context) -> RocketControllerScreen.receiveDisplayDataUpdate((RocketControllerDataS2CPacket) payload, context));
		ClientPlayNetworking.registerGlobalReceiver(FizzS2CPacket.PACKET_ID, (payload, context) -> receiveFizz((FizzS2CPacket) payload, context));
		ClientPlayNetworking.registerGlobalReceiver(OutgasS2CPacket.PACKET_ID, (payload, context) -> receiveOutgas((OutgasS2CPacket) payload, context));
		ClientPlayNetworking.registerGlobalReceiver(JetS2CPacket.PACKET_ID, (payload, context) -> receiveJet((JetS2CPacket) payload, context));
		ClientPlayNetworking.registerGlobalReceiver(UnlockPlanetS2CPacket.PACKET_ID, (payload, context) -> receiveUnlockPlanet((UnlockPlanetS2CPacket) payload, context));
	}
	
	public static void receiveStaticData(PlanetStaticDataS2CPacket payload, ClientPlayNetworking.Context context)
	{
		HashMap<String, Planet.StaticData> staticDataMap = payload.staticDataMap();
		
		context.client().execute(() -> {
			ArrayList<Planet> planetList = new ArrayList<Planet>();
			StarflightMod.LOGGER.info("Receiving data pack planets from the server...");

			for(Planet.StaticData data : staticDataMap.values())
			{
				String name = data.name();
				String parentName = data.parentName();
				double mass = data.mass();
				double radius = data.radius();
				double lowOrbitAltitude = data.lowOrbitAltitude();
				double periapsis = data.periapsis();
				double apoapsis = data.apoapsis();
				double argumentOfPeriapsis = data.argumentOfPeriapsis();
				double trueAnomaly = data.trueAnomaly();
				double ascendingNode = data.ascendingNode();
				double inclination = data.inclination();
				boolean isTidallyLocked = data.isTidallyLocked();
				double obliquity = data.obliquity();
				double rotationRate = data.rotationRate();
				boolean simpleTexture = data.simpleTexture();
				boolean drawClouds = data.drawClouds();
				double cloudRotationRate = data.cloudRotationRate();
				HashMap<String, PlanetDimensionData> dimensionDataMap = data.dimensionDataMap();
				PlanetDimensionData orbit = null;
				PlanetDimensionData surface = null;
				PlanetDimensionData sky = null;
				Planet planet = new Planet(name, parentName, mass, radius, lowOrbitAltitude);
				planet.setOrbitParameters(periapsis, apoapsis, argumentOfPeriapsis, trueAnomaly, ascendingNode, inclination);
				planet.setRotationParameters(isTidallyLocked, obliquity, rotationRate, 0.0);
				planet.setDecorativeParameters(simpleTexture, drawClouds, cloudRotationRate);
				String dimensionNames = "";
				
				if(dimensionDataMap.containsKey("orbit"))
				{
					orbit = dimensionDataMap.get("orbit");
					planet.setOrbit(orbit);
					dimensionNames = dimensionNames.concat(" " + orbit.getWorldKey().getValue().toString());
				}
				
				if(dimensionDataMap.containsKey("surface"))
				{
					surface = dimensionDataMap.get("surface");
					planet.setSurface(surface);
					dimensionNames = dimensionNames.concat(" " + surface.getWorldKey().getValue().toString());
				}
				
				if(dimensionDataMap.containsKey("sky"))
				{
					sky = dimensionDataMap.get("sky");
					planet.setSky(sky);
					dimensionNames = dimensionNames.concat(" " + sky.getWorldKey().getValue().toString());
				}
				
				planetList.add(planet);
				StarflightMod.LOGGER.info(planet.getName() + " [" + dimensionNames + " ]");
			}
			
			for(Planet p : planetList)
				p.linkSatellites(planetList);
			
			for(Planet p1 : planetList)
			{
				int level = 0;
				Planet p2 = p1.getParent();
				
				while(p2 != null)
				{
					level++;
					p2 = p2.getParent();
				}
				
				p1.setSatelliteLevel(level);
			}
			
			PlanetList.getClient().loadPlanets(planetList);
		});
	}
	
	public static void receiveDynamicData(PlanetDynamicDataS2CPacket payload, ClientPlayNetworking.Context context)
	{
		PlanetList.getClient().setDynamicDataBuffer(payload);
	}
	
	public static void receiveBlockData(MovingCraftBlocksS2CPacket payload, ClientPlayNetworking.Context context)
	{
		int entityID = payload.entityID();
		ArrayList<MovingCraftEntity.BlockData> blockList = payload.blockDataList();
		MinecraftClient client = context.client();
		ClientWorld clientWorld = client.world;
		
		client.execute(() -> {
			if(clientWorld == null)
				return;
			
			Entity entity = clientWorld.getEntityById(entityID);

			if(entity == null || !(entity instanceof MovingCraftEntity))
				return;

			((MovingCraftEntity) entity).getBlocks().clear();
			((MovingCraftEntity) entity).getBlocks().addAll(blockList);
			((MovingCraftEntity) entity).refreshExposedBlocks();
		});
	}
	
	public static void receiveEntityOffsets(MovingCraftEntityOffsetsS2CPacket payload, ClientPlayNetworking.Context context)
	{
		int entityID = payload.entityID();
		HashMap<Integer, BlockPos> passengerMap = payload.passengerMap();
		MinecraftClient client = context.client();
		ClientWorld clientWorld = client.world;
		
		client.execute(() -> {
			if(clientWorld == null)
				return;

			Entity entity = clientWorld.getEntityById(entityID);

			if(entity == null || !(entity instanceof MovingCraftEntity))
				return;

			((MovingCraftEntity) entity).getEntityOffsets().clear();
			
			for(int id : passengerMap.keySet())
				((MovingCraftEntity) entity).getEntityOffsets().put(clientWorld.getEntityById(id).getUuid(), passengerMap.get(id));
		});
	}
	
	public static void receiveFizz(FizzS2CPacket payload, ClientPlayNetworking.Context context)
	{
		BlockPos pos = payload.blockPos();
		MinecraftClient client = context.client();
		
		client.execute(() -> {
			ClientWorld clientWorld = client.world;
			
			if(clientWorld != null)
				clientWorld.playSound(client.player, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5f, 0.4f);
		});
	}
	
	public static void receiveOutgas(OutgasS2CPacket payload, ClientPlayNetworking.Context context)
	{
		BlockPos pos1 = payload.blockPos1();
		BlockPos pos2 = payload.blockPos2();
		boolean sound = payload.sound();
		MinecraftClient client = context.client();
		
		client.execute(() -> {
			ClientWorld clientWorld = client.world;
			Random random = Random.createLocal();
			Vec3i unitVector = pos2.subtract(pos1);
			int particleCount = 4 + random.nextInt(6);
			
			if(clientWorld == null)
				return;
			
			if(sound)
				clientWorld.playSound(client.player, pos1, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 1.0f, 0.5f);
			
			for(int i = 0; i < particleCount; i++)
			{
				Vec3d offset = new Vec3d(random.nextDouble(), random.nextDouble(), random.nextDouble());
				Vec3d velocity = new Vec3d(unitVector.getX(), unitVector.getY(), unitVector.getZ()).normalize().multiply(0.25 + random.nextDouble() * 0.25);
				clientWorld.addParticle(StarflightParticleTypes.AIR_FILL, pos1.getX() + offset.getX(), pos1.getY() + offset.getY(), pos1.getZ() + offset.getZ(), velocity.getX(), velocity.getY(), velocity.getZ());
			}
		});
	}
	
	public static void receiveJet(JetS2CPacket payload, ClientPlayNetworking.Context context)
	{
		Vec3d sourcePos = payload.sourcePos();
		Vec3d velocity = payload.velocity();
		MinecraftClient client = context.client();
		
		client.execute(() -> {
			ClientWorld clientWorld = client.world;
			Random random = Random.createLocal();
			int particleCount = 2 + random.nextInt(2);
			
			for(int i = 0; i < particleCount; i++)
			{
				Vec3d offset = new Vec3d(random.nextDouble(), random.nextDouble(), random.nextDouble());
				offset.add(-random.nextDouble(), -random.nextDouble(), -random.nextDouble());
				offset = offset.multiply(0.25);
				clientWorld.addParticle(ParticleTypes.POOF, sourcePos.getX() + offset.getX(), sourcePos.getY() + offset.getY(), sourcePos.getZ() + offset.getZ(), velocity.getX(), velocity.getY(), velocity.getZ());
			}
		});
	}
	
	public static void receiveUnlockPlanet(UnlockPlanetS2CPacket payload, ClientPlayNetworking.Context context)
	{
		String planetName = payload.planetName();
		int color = payload.color();
		MinecraftClient client = context.client();
		
		client.execute(() -> {
			ClientWorld clientWorld = client.world;
			
			if(clientWorld != null)
				client.player.sendMessage(Text.translatable("block.space.planetarium.unlocked").append(Text.translatable("planet.space." + planetName).withColor(color)));
				clientWorld.playSound(client.player, client.player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1.0f, 1.0f);
		});
	}
}