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
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import space.energy.EnergyNet;
import space.planet.PlanetList;
import space.planet.PlanetResourceListener;
import space.util.MobSpawningUtil;

public class StarflightEvents
{
	private static int saveTimer;
	
	public static void registerEvents()
	{
		// Server Started Event
		ServerLifecycleEvents.SERVER_STARTED.register((server) ->
	    {
	    	
	    	
	    	saveTimer = 0;
	    	File energyFile = new File(server.getSavePath(WorldSavePath.ROOT).toString() + "/space/energy.dat");
	    	DataCompound energyData = null;
	    	
	    	if(energyFile.exists())
	    		energyData = ESSHelper.readCompound(energyFile);
	    	
	    	EnergyNet.loadData(energyData);
	    });
		
		// Server Stopping Event
		ServerLifecycleEvents.SERVER_STOPPING.register((server) ->
	    {
	    	// Save planet and vessel data when the server is stopping.
	    	saveData(server);
	    	PlanetList.clear();
	    });
		
		// Server Tick Event
		ServerTickEvents.END_SERVER_TICK.register((server) ->
	    {
			PlanetList.serverTick(server);
			EnergyNet.doEnergyFlow(server);
			MobSpawningUtil.doCustomMobSpawning(server);
			
			saveTimer++;
			
			// Save planet and vessel data every 5 minutes.
			if(saveTimer >= 6000)
			{
				saveTimer = 0;
				saveData(server);
			}
	    });
		
		// Server Disconnect Event
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
		{
			PlanetList.deactivateClient(handler.getPlayer());
        });
		
		// Resource Reload Event
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PlanetResourceListener());
	}
	
	private static void saveData(MinecraftServer server)
	{
		String directory = server.getSavePath(WorldSavePath.ROOT).toString() + "/space/";
    	File planetsFile = new File(directory + "planets.dat");
    	File energyFile = new File(directory + "energy.dat");
    	
    	try
		{
			Files.createDirectories(Paths.get(directory));
			DataCompound planetData = PlanetList.saveDynamicData();
			ESSHelper.writeCompound(planetData, planetsFile);
			DataCompound energyData = EnergyNet.saveData();
			ESSHelper.writeCompound(energyData, energyFile);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}