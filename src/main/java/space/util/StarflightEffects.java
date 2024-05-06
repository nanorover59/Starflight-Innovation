package space.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import space.StarflightMod;
import space.particle.StarflightParticleTypes;

public class StarflightEffects
{
	public static SoundEvent CURRENT_SOUND_EVENT = SoundEvent.of(new Identifier(StarflightMod.MOD_ID, "current"));
	public static SoundEvent WRENCH_SOUND_EVENT = SoundEvent.of(new Identifier(StarflightMod.MOD_ID, "wrench"));
	public static SoundEvent STORAGE_CUBE_SOUND_EVENT = SoundEvent.of(new Identifier(StarflightMod.MOD_ID, "storage_cube"));
	public static SoundEvent THRUSTER_SOUND_EVENT = SoundEvent.of(new Identifier(StarflightMod.MOD_ID, "thruster"));
	public static SoundEvent LEAK_SOUND_EVENT = SoundEvent.of(new Identifier(StarflightMod.MOD_ID, "leak"));
	public static SoundEvent ELECTRIC_MOTOR_SOUND_EVENT = SoundEvent.of(new Identifier(StarflightMod.MOD_ID, "electric_motor"));
	public static SoundEvent MARS_WIND_SOUND_EVENT = SoundEvent.of(new Identifier(StarflightMod.MOD_ID, "mars_wind"));
	public static SoundEvent NOISE_SOUND_EVENT = SoundEvent.of(new Identifier(StarflightMod.MOD_ID, "noise"));
	
	public static void initializeSounds()
	{
		Registry.register(Registries.SOUND_EVENT, new Identifier(StarflightMod.MOD_ID, "current"), CURRENT_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, new Identifier(StarflightMod.MOD_ID, "wrench"), WRENCH_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, new Identifier(StarflightMod.MOD_ID, "storage_cube"), STORAGE_CUBE_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, new Identifier(StarflightMod.MOD_ID, "thruster"), THRUSTER_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, new Identifier(StarflightMod.MOD_ID, "leak"), LEAK_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, new Identifier(StarflightMod.MOD_ID, "electric_motor"), ELECTRIC_MOTOR_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, new Identifier(StarflightMod.MOD_ID, "mars_wind"), MARS_WIND_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, new Identifier(StarflightMod.MOD_ID, "noise"), NOISE_SOUND_EVENT);
	}
	
	public static void sendFizz(WorldAccess world, BlockPos pos)
	{
		for(ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList())
		{
			PacketByteBuf buffer = PacketByteBufs.create();
			buffer.writeBlockPos(pos);
			ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "fizz"), buffer);
		}
	}
	
	public static void receiveFizz(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
		BlockPos pos = buffer.readBlockPos();
		
		client.execute(() -> {
			if(client.world != null)
				client.world.playSound(client.player, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5f, 0.4f);
		});
	}
	
	public static void sendOutgas(WorldAccess world, BlockPos pos1, BlockPos pos2, boolean sound)
	{
		for(ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList())
		{
			PacketByteBuf buffer = PacketByteBufs.create();
			buffer.writeBlockPos(pos1);
			buffer.writeBlockPos(pos2);
			buffer.writeBoolean(sound);
			ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "outgas"), buffer);
		}
	}
	
	public static void receiveOutgas(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
		BlockPos pos1 = buffer.readBlockPos();
		BlockPos pos2 = buffer.readBlockPos();
		boolean sound = buffer.readBoolean();
		
		client.execute(() -> {
			Random random = Random.createLocal();
			Vec3i unitVector = pos2.subtract(pos1);
			int particleCount = 4 + random.nextInt(6);
			
			if(client.world == null)
				return;
			
			if(sound)
				client.world.playSound(client.player, pos1, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 1.0f, 0.5f);
			
			for(int i = 0; i < particleCount; i++)
			{
				Vec3d offset = new Vec3d(random.nextDouble(), random.nextDouble(), random.nextDouble());
				Vec3d velocity = new Vec3d(unitVector.getX(), unitVector.getY(), unitVector.getZ()).normalize().multiply(0.25 + random.nextDouble() * 0.25);
				client.world.addParticle(StarflightParticleTypes.AIR_FILL, pos1.getX() + offset.getX(), pos1.getY() + offset.getY(), pos1.getZ() + offset.getZ(), velocity.getX(), velocity.getY(), velocity.getZ());
			}
		});
	}
	
	public static void sendJet(WorldAccess world, Vec3d sourcePos, Vec3d velocity)
	{
		for(ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList())
		{
			PacketByteBuf buffer = PacketByteBufs.create();
			buffer.writeDouble(sourcePos.getX());
			buffer.writeDouble(sourcePos.getY());
			buffer.writeDouble(sourcePos.getZ());
			buffer.writeDouble(velocity.getX());
			buffer.writeDouble(velocity.getY());
			buffer.writeDouble(velocity.getZ());
			ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "jet"), buffer);
		}
	}
	
	public static void receiveJet(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
		double px = buffer.readDouble();
		double py = buffer.readDouble();
		double pz = buffer.readDouble();
		double vx = buffer.readDouble();
		double vy = buffer.readDouble();
		double vz = buffer.readDouble();
		
		client.execute(() -> {
			Vec3d sourcePos = new Vec3d(px, py, pz);
			Vec3d velocity = new Vec3d(vx, vy, vz);
			Random random = Random.createLocal();
			int particleCount = 2 + random.nextInt(2);
			
			for(int i = 0; i < particleCount; i++)
			{
				Vec3d offset = new Vec3d(random.nextDouble(), random.nextDouble(), random.nextDouble());
				offset.add(-random.nextDouble(), -random.nextDouble(), -random.nextDouble());
				offset = offset.multiply(0.25);
				client.world.addParticle(ParticleTypes.POOF, sourcePos.getX() + offset.getX(), sourcePos.getY() + offset.getY(), sourcePos.getZ() + offset.getZ(), velocity.getX(), velocity.getY(), velocity.getZ());
			}
		});
	}
}