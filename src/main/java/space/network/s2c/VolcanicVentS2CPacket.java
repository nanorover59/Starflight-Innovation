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

public record VolcanicVentS2CPacket(BlockPos blockPos1, BlockPos blockPos2) implements CustomPayload
{
	public static final CustomPayload.Id<VolcanicVentS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "volcanic_vent"));
    public static final PacketCodec<PacketByteBuf, VolcanicVentS2CPacket> PACKET_CODEC = CustomPayload.codecOf(VolcanicVentS2CPacket::write, VolcanicVentS2CPacket::new);
    
    private VolcanicVentS2CPacket(PacketByteBuf buffer)
    {
    	this(buffer.readBlockPos(), buffer.readBlockPos());
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeBlockPos(blockPos1);
    	buffer.writeBlockPos(blockPos2);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
    
    public static void sendVolcanicVent(WorldAccess world, BlockPos pos1, BlockPos pos2)
	{
		for(ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList())
			ServerPlayNetworking.send(player, new VolcanicVentS2CPacket(pos1, pos2));
	}
}