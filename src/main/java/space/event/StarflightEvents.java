package space.event;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.darkhax.ess.DataCompound;
import net.darkhax.ess.ESSHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import space.planet.PlanetList;
import space.planet.PlanetResourceListener;
import space.util.MobSpawningUtil;

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
	    	// Save planet and vessel data when the server is stopping.
	    	saveData(server);
	    });
		
		// Server Tick Event
		ServerTickEvents.END_SERVER_TICK.register((server) ->
	    {
			PlanetList.get().serverTick(server);
			MobSpawningUtil.doCustomMobSpawning(server);
			
			saveTimer++;
			
			// Save planet and vessel data every 5 minutes.
			if(saveTimer >= 6000)
			{
				saveTimer = 0;
				saveData(server);
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