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

public record FizzS2CPacket(BlockPos blockPos) implements CustomPayload
{
	public static final CustomPayload.Id<FizzS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "fizz"));
    public static final PacketCodec<PacketByteBuf, FizzS2CPacket> PACKET_CODEC = CustomPayload.codecOf(FizzS2CPacket::write, FizzS2CPacket::new);
    
    private FizzS2CPacket(PacketByteBuf buffer)
    {
    	this(buffer.readBlockPos());
    }
    
    private void write(PacketByteBuf buffer)
    {
    	buffer.writeBlockPos(blockPos);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
    
    public static void sendFizz(WorldAccess world, BlockPos pos)
	{
		for(ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList())
			ServerPlayNetworking.send(player, new FizzS2CPacket(pos));
	}
}