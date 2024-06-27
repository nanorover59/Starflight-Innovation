package space.vessel;

import java.util.ArrayList;
import java.util.HashMap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import space.entity.MovingCraftEntity;
import space.network.s2c.MovingCraftRenderDataS2CPacket;

@Environment(value=EnvType.CLIENT)
public class MovingCraftRenderList
{
	private static HashMap<Integer, ArrayList<MovingCraftBlockRenderData>> craftList = new HashMap<Integer, ArrayList<MovingCraftBlockRenderData>>();
	
	public static void addCraft(int entityID, ArrayList<MovingCraftBlockRenderData> blockList)
	{
		craftList.put(entityID, blockList);
	}
	
	public static void removeCraft(int entityID)
	{
		craftList.remove(entityID);
	}
	
	public static boolean hasBlocksForEntity(int entityID)
	{
		return craftList.containsKey(entityID);
	}
	
	public static ArrayList<MovingCraftBlockRenderData> getBlocksForEntity(int entityID)
	{
		return craftList.get(entityID);
	}
	
	public static void receiveCraftListUpdate(MovingCraftRenderDataS2CPacket payload, ClientPlayNetworking.Context context)
	{
		int entityID = payload.entityID();
		ArrayList<MovingCraftEntity.BlockRenderData> blockRenderDataList = payload.blockDataList();
		MinecraftClient client = context.client();
		
		if(client.world != null && !blockRenderDataList.isEmpty())
		{
			ArrayList<MovingCraftBlockRenderData> blockList = new ArrayList<MovingCraftBlockRenderData>();
			
			for(MovingCraftEntity.BlockRenderData blockRenderData : blockRenderDataList)
			{
				BlockState blockState = blockRenderData.blockState();
				BlockPos blockPos = blockRenderData.position();
				boolean redstone = (blockRenderData.flags() & (1 << 0)) != 0;
				boolean[] sidesShowing = new boolean[6];
				
				for (int i = 0; i < 6; i++)
					sidesShowing[i] = (blockRenderData.sidesShowing() & (1 << i)) != 0;
				
				blockList.add(new MovingCraftBlockRenderData(blockState, blockPos, redstone, sidesShowing));
			}
			
			client.execute(() -> craftList.put(entityID, blockList));
		}
		else
			client.execute(() -> craftList.remove(entityID));
	}
}