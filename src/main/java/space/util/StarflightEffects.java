package space.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldAccess;
import space.StarflightMod;

public class StarflightEffects
{
	public static SoundEvent THRUSTER_SOUND_EVENT = new SoundEvent(new Identifier(StarflightMod.MOD_ID, "thruster"));
	
	public static void initializeSounds()
	{
		Registry.register(Registry.SOUND_EVENT, new Identifier(StarflightMod.MOD_ID, "thruster"), THRUSTER_SOUND_EVENT);
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
		client.world.playSound(pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5f, 0.4f, false);
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
		Vec3i unitVector = pos2.subtract(pos1);
		
		if(sound)
			client.world.playSound(pos1, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 1.0f, 0.5f, false);
		
		for(int i = 0; i < 10 + client.world.getRandom().nextInt(6); i++)
		{
			Vec3d offset = new Vec3d(client.world.getRandom().nextDouble(), client.world.getRandom().nextDouble(), client.world.getRandom().nextDouble());
			Vec3d velocity = new Vec3d(unitVector.getX(), unitVector.getY(), unitVector.getZ()).normalize().multiply(0.25 + client.world.getRandom().nextDouble() * 0.25);
			client.world.addParticle(ParticleTypes.POOF, pos1.getX() + offset.getX(), pos1.getY() + offset.getY(), pos1.getZ() + offset.getZ(), velocity.getX(), velocity.getY(), velocity.getZ());
		}
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
		Vec3d sourcePos = new Vec3d(px, py, pz);
		double vx = buffer.readDouble();
		double vy = buffer.readDouble();
		double vz = buffer.readDouble();
		Vec3d velocity = new Vec3d(vx, vy, vz);
		
		for(int i = 0; i < 2 + client.world.getRandom().nextInt(2); i++)
		{
			Vec3d offset = new Vec3d(client.world.getRandom().nextDouble(), client.world.getRandom().nextDouble(), client.world.getRandom().nextDouble());
			offset.add(-client.world.getRandom().nextDouble(), -client.world.getRandom().nextDouble(), -client.world.getRandom().nextDouble());
			offset = offset.multiply(0.25);
			client.world.addParticle(ParticleTypes.POOF, sourcePos.getX() + offset.getX(), sourcePos.getY() + offset.getY(), sourcePos.getZ() + offset.getZ(), velocity.getX(), velocity.getY(), velocity.getZ());
		}
	}
}