package space.vessel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class MovingCraftRenderList
{
	private static HashMap<UUID, ArrayList<MovingCraftBlockRenderData>> craftList = new HashMap<UUID, ArrayList<MovingCraftBlockRenderData>>();
	
	public static void addCraft(UUID entityUUID, ArrayList<MovingCraftBlockRenderData> blockList)
	{
		craftList.put(entityUUID, blockList);
	}
	
	public static void removeCraft(UUID entityUUID)
	{
		craftList.remove(entityUUID);
	}
	
	public static boolean hasBlocksForEntity(UUID entityUUID)
	{
		return craftList.containsKey(entityUUID);
	}
	
	public static ArrayList<MovingCraftBlockRenderData> getBlocksForEntity(UUID entityUUID)
	{
		return craftList.get(entityUUID);
	}
	
	public static void receiveCraftListUpdate(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
			boolean b = buffer.readBoolean(); // Load render data if the boolean value is true or remove render data if it is false.
			UUID entityUUID = buffer.readUuid();
			
			if(b)
			{
				int blockCount = buffer.readInt();
				ArrayList<MovingCraftBlockRenderData> blockList = new ArrayList<MovingCraftBlockRenderData>();
				
				for(int i = 0; i < blockCount; i++)
				{
					BlockState blockState = NbtHelper.toBlockState(buffer.readNbt());
					BlockPos blockPos = buffer.readBlockPos();
					boolean[] sidesShowing = new boolean[6];
					
					for(int j = 0; j < 6; j++)
						sidesShowing[j] = buffer.readBoolean();
					
					blockList.add(new MovingCraftBlockRenderData(blockState, blockPos, sidesShowing));
				}
				
				client.execute(() -> craftList.put(entityUUID, blockList));
			}
			else
				client.execute(() -> craftList.remove(entityUUID));
	}
}