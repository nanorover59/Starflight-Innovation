package space.event;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.darkhax.ess.DataCompound;
import net.darkhax.ess.ESSHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.planet.PlanetResourceListener;
import space.world.persistent.StarflightPlayerState;

public class StarflightEvents
{
	private static int saveTimer;
	
	public static void registerEvents()
	{
		// Resource Reload Event
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PlanetResourceListener());
		
		// Server Started Event
		ServerLifecycleEvents.SERVER_STARTED.register((server) ->
	    {
	    	saveTimer = 0;
	    });
		
		// Server Stopping Event
		ServerLifecycleEvents.SERVER_STOPPING.register((server) ->
	    {
	    	// Save planet data when the server is stopping.
	    	saveData(server);
	    	PlanetList.reset();
	    });
		
		// Server Player Join Event
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
	    {
	    	PlanetList.get().sendStaticDataToClient(handler.getPlayer());
	    	StarflightPlayerState.syncPlayerState(handler.getPlayer());
	    });
		
		// Server Tick Event
		ServerTickEvents.END_SERVER_TICK.register((server) ->
	    {
			PlanetList.get().serverTick(server);
			
			saveTimer++;
			
			// Save planet data every 5 minutes.
			if(saveTimer >= 6000)
			{
				saveTimer = 0;
				saveData(server);
			}
	    });
		
		ServerTickEvents.START_WORLD_TICK.register((world) ->
	    {
	    	PlanetDimensionData dimensionData = PlanetList.getDimensionDataForWorld(world);
	    	
			if(dimensionData != null)
			{
				boolean dustStorm = world.getRainGradient(1.0f) > 0.0f && dimensionData.getPlanet().getName().equals("mars");
				
				if(dimensionData.isCloudy() || dustStorm)
				{
					Random random = world.getRandom();
					
					if(random.nextInt(256) == 0)
					{
						for(PlayerEntity player : world.getPlayers())
						{
							BlockPos playerPos = player.getBlockPos();
							int x = playerPos.getX() + random.nextInt(256) - 128;
							int z = playerPos.getZ() + random.nextInt(256) - 128;
							int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
							BlockPos lightningPos = new BlockPos(x, y, z);
							LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
							lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(lightningPos));
							world.spawnEntity(lightning);
						}
					}
				}
			}
	    });
	}
	
	private static void saveData(MinecraftServer server)
	{
		String directory = server.getSavePath(WorldSavePath.ROOT).toString() + "/space/";
    	File planetsFile = new File(directory + "planets.dat");
    	
    	try
		{
			Files.createDirectories(Paths.get(directory));
			DataCompound planetData = PlanetList.get().saveDynamicData();
			ESSHelper.writeCompound(planetData, planetsFile);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}