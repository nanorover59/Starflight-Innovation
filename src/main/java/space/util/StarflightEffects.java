package space.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import space.StarflightMod;
import space.network.s2c.FizzS2CPacket;
import space.network.s2c.JetS2CPacket;
import space.network.s2c.OutgasS2CPacket;
import space.network.s2c.UnlockPlanetS2CPacket;
import space.particle.StarflightParticleTypes;

public class StarflightEffects
{
	public static SoundEvent CURRENT_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "current"));
	public static SoundEvent WRENCH_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "wrench"));
	public static SoundEvent STORAGE_CUBE_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "storage_cube"));
	public static SoundEvent THRUSTER_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "thruster"));
	public static SoundEvent LEAK_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "leak"));
	public static SoundEvent ELECTRIC_MOTOR_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "electric_motor"));
	public static SoundEvent MARS_WIND_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "mars_wind"));
	public static SoundEvent NOISE_SOUND_EVENT = SoundEvent.of(Identifier.of(StarflightMod.MOD_ID, "noise"));
	
	public static void initializeSounds()
	{
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "current"), CURRENT_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "wrench"), WRENCH_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "storage_cube"), STORAGE_CUBE_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "thruster"), THRUSTER_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "leak"), LEAK_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "electric_motor"), ELECTRIC_MOTOR_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "mars_wind"), MARS_WIND_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, Identifier.of(StarflightMod.MOD_ID, "noise"), NOISE_SOUND_EVENT);
	}
	
	public static void sendFizz(WorldAccess world, BlockPos pos)
	{
		for(ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList())
			ServerPlayNetworking.send(player, new FizzS2CPacket(pos));
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
	
	public static void sendOutgas(WorldAccess world, BlockPos pos1, BlockPos pos2, boolean sound)
	{
		for(ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList())
			ServerPlayNetworking.send(player, new OutgasS2CPacket(pos1, pos2, sound));
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
	
	public static void sendJet(WorldAccess world, Vec3d sourcePos, Vec3d velocity)
	{
		for(ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList())
			ServerPlayNetworking.send(player, new JetS2CPacket(sourcePos, velocity));
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
	
	public static void sendUnlockPlanet(WorldAccess world, String planetName, int color)
	{
		for(ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList())
			ServerPlayNetworking.send(player, new UnlockPlanetS2CPacket(planetName, color));
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