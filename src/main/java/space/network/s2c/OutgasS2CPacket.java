package space.network.s2c;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import space.StarflightMod;

public record OutgasS2CPacket(BlockPos blockPos1, BlockPos blockPos2, boolean sound) implements CustomPayload
{
	public static final CustomPayload.Id<OutgasS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "outgas"));
    public static final PacketCodec<PacketByteBuf, OutgasS2CPacket> PACKET_CODEC = CustomPayload.codecOf(OutgasS2CPacket::write, OutgasS2CPacket::new);
    
    private OutgasS2CPacket(PacketByteBuf buffer)
    {
    	this(buffer.readBlockPos(), buffer.readBlockPos(), buffer.readBoolean());
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeBlockPos(blockPos1);
    	buffer.writeBlockPos(blockPos2);
    	buffer.writeBoolean(sound);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
    
    public static void sendOutgas(WorldAccess world, BlockPos pos1, BlockPos pos2, boolean sound)
	{
		for(ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList())
			ServerPlayNetworking.send(player, new OutgasS2CPacket(pos1, pos2, sound));
	}
}