package space.util;

import java.util.ArrayList;

import com.google.common.base.Predicates;

import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Difficulty;
import space.StarflightMod;
import space.block.StarflightBlocks;
import space.entity.DustEntity;
import space.entity.StarflightEntities;

public class MobSpawningUtil
{
	private static int dustTimer = 0;
	
	public static void doCustomMobSpawning(MinecraftServer server)
	{
		for(ServerWorld world : server.getWorlds())
		{
			if(world.getDifficulty() != Difficulty.PEACEFUL && world.getDimensionEntry().matchesId(new Identifier(StarflightMod.MOD_ID, "mars")))
				doDustSpwning(world);
		}
	}
	
	private static void doDustSpwning(ServerWorld world)
	{
		if(dustTimer > 0)
		{
			dustTimer--;
			return;
		}
		
		dustTimer = world.random.nextBetween(20, 60);
		ArrayList<ChunkPos> chunkPosList = new ArrayList<ChunkPos>();
		boolean weather = world.toServerWorld().isRaining() || world.toServerWorld().isThundering();
		
		for(ServerPlayerEntity player : world.getPlayers())
		{
			ChunkPos playerChunkPos = player.getChunkPos();
			
			for(int i = -8; i < 8; i++)
			{
				for(int j = -8; j < 8; j++)
				{
					ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + i, playerChunkPos.z + j);
					int distance = playerChunkPos.getChebyshevDistance(chunkPos);
					
					if(!chunkPosList.contains(chunkPos) && world.isChunkLoaded(chunkPos.x, chunkPos.z) && distance > 1 && distance < 8)
						chunkPosList.add(chunkPos);
				}
			}
		}
		
		for(ChunkPos chunkPos : chunkPosList)
		{
			float chance = weather ? 0.025f : 0.001f;
			
			if(world.random.nextFloat() > chance)
				continue;
			
			BlockPos pos = chunkPos.getStartPos().add(world.random.nextInt(16), 256, world.random.nextInt(16));
			Box checkBox = new Box(new BlockPos(pos.getX() - 32, world.getBottomY(), pos.getZ() - 32), new BlockPos(pos.getX() + 32, world.getTopY(), pos.getZ() + 32));
			
			if(world.getEntitiesByType(TypeFilter.instanceOf(DustEntity.class), checkBox, Predicates.alwaysTrue()).size() > 1)
				continue;
			
			// Find the ground Y level.
			while(world.getBlockState(pos.down()).getBlock() == Blocks.AIR && !world.isOutOfHeightLimit(pos))
				pos = pos.down();
			
			// Only spawn on ferric sand.
			if(world.getBlockState(pos.down()).getBlock() != StarflightBlocks.FERRIC_SAND)
				continue;
			
			DustEntity entity = new DustEntity(StarflightEntities.DUST, world);
			entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			entity.setYaw(world.random.nextFloat() * (float) Math.PI * 2.0f);
			world.spawnEntity(entity);
		}
	}
}