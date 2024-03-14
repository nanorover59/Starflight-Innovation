package space.util;

import java.util.ArrayList;
import java.util.function.Predicate;

import com.google.common.base.Predicates;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import space.StarflightMod;
import space.block.StarflightBlocks;
import space.entity.DustEntity;
import space.entity.SolarSpectreEntity;
import space.entity.StarflightEntities;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

public class MobSpawningUtil
{
	private static int solarSpectreTimer = 0;
	private static int dustTimer = 0;
	
	public static void doCustomMobSpawning(MinecraftServer server)
	{
		for(ServerWorld world : server.getWorlds())
		{
			if(world.getDifficulty() != Difficulty.PEACEFUL)
			{
				PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
				
				if(data != null && (data.isOrbit() || data.getPressure() == 0.0))
					doSolarSpectreSpawning(world, data);
				
				if(world.getDimensionEntry().matchesId(new Identifier(StarflightMod.MOD_ID, "mars")))
					doDustSpawning(world);	
			}
		}
	}
	
	private static ArrayList<ChunkPos> getChunkPosList(ServerWorld world, int minChunkDistance, int maxChunkDistance, Predicate<PlayerEntity> includePlayer)
	{
		ArrayList<ChunkPos> chunkPosList = new ArrayList<ChunkPos>();
		
		for(ServerPlayerEntity player : world.getPlayers())
		{
			if(!includePlayer.test(player))
				continue;
			
			ChunkPos playerChunkPos = player.getChunkPos();
			
			for(int i = -maxChunkDistance; i < maxChunkDistance; i++)
			{
				for(int j = -maxChunkDistance; j < maxChunkDistance; j++)
				{
					ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + i, playerChunkPos.z + j);
					int distance = (int) MathHelper.hypot(playerChunkPos.x - chunkPos.x, playerChunkPos.z - chunkPos.z);
					
					if(!chunkPosList.contains(chunkPos) && world.isChunkLoaded(chunkPos.x, chunkPos.z) && distance >= minChunkDistance && distance < maxChunkDistance)
						chunkPosList.add(chunkPos);
				}
			}
		}
		
		return chunkPosList;
	}
	
	private static void doSolarSpectreSpawning(ServerWorld world, PlanetDimensionData data)
	{
		if(solarSpectreTimer > 0)
		{
			solarSpectreTimer--;
			return;
		}
		
		Predicate<PlayerEntity> include = (p) -> {
			return true; //!p.isCreative() && !p.isSpectator();
		};
		
		solarSpectreTimer = world.random.nextBetween(20, 60);
		ArrayList<ChunkPos> chunkPosList = getChunkPosList(world, 2, 4, include);
		double d = data.getPlanet().getPosition().lengthSquared();
		d /= 2.238016e22; // Convert the distance from meters to astronomical units.
		float chance = (float) (1.0e-5f * (1.0 / d));
		
		for(ChunkPos chunkPos : chunkPosList)
		{
			if(world.random.nextFloat() > chance)
				continue;
			
			BlockPos pos = chunkPos.getStartPos().add(world.random.nextInt(16), 64, world.random.nextInt(16));
			Box checkBox = new Box(pos.getX() - 128, world.getBottomY(), pos.getZ() - 128, pos.getX() + 128, world.getTopY(), pos.getZ() + 128);
			
			if(world.getEntitiesByType(TypeFilter.instanceOf(SolarSpectreEntity.class), checkBox, Predicates.alwaysTrue()).size() > 4)
				continue;
			
			// Find a Y level above everything.
			while(!world.isSkyVisible(pos) && !world.isOutOfHeightLimit(pos.up(16)))
				pos = pos.up();
			
			pos = pos.up(64);
			
			SolarSpectreEntity entity = new SolarSpectreEntity(StarflightEntities.SOLAR_SPECTRE, world);
			entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			world.spawnEntity(entity);
			
			//System.out.println(entity.getBlockPos());
		}
	}
	
	private static void doDustSpawning(ServerWorld world)
	{
		if(dustTimer > 0)
		{
			dustTimer--;
			return;
		}
		
		dustTimer = world.random.nextBetween(20, 60);
		boolean weather = world.toServerWorld().isRaining() || world.toServerWorld().isThundering();
		ArrayList<ChunkPos> chunkPosList = getChunkPosList(world, 2, 6, Predicates.alwaysTrue());
		
		for(ChunkPos chunkPos : chunkPosList)
		{
			float chance = weather ? 1.0e-2f : 5.0e-4f;
			
			if(world.random.nextFloat() > chance)
				continue;
			
			BlockPos pos = chunkPos.getStartPos().add(world.random.nextInt(16), 256, world.random.nextInt(16));
			Box checkBox = new Box(pos.getX() - 32, world.getBottomY(), pos.getZ() - 32, pos.getX() + 32, world.getTopY(), pos.getZ() + 32);
			
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