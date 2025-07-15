package space.network.s2c;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.world.persistent.StarflightPlayerData;

public record SyncPlayerStateS2CPacket(StarflightPlayerData data) implements CustomPayload
{
	public static final CustomPayload.Id<SyncPlayerStateS2CPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(StarflightMod.MOD_ID, "sync_player_state"));
    public static final PacketCodec<PacketByteBuf, SyncPlayerStateS2CPacket> PACKET_CODEC = CustomPayload.codecOf(SyncPlayerStateS2CPacket::write, SyncPlayerStateS2CPacket::new);
    
    private SyncPlayerStateS2CPacket(PacketByteBuf buffer)
    {
    	this(StarflightPlayerData.createFromNbt(buffer.readNbt()));
    }
    
    private void write(PacketByteBuf buffer)
    {
    	NbtCompound nbt = new NbtCompound();
    	data.writeNbt(nbt);
    	buffer.writeNbt(nbt);
    }
    
    @Override
	public Id<? extends CustomPayload> getId()
	{
		return PACKET_ID;
	}
}