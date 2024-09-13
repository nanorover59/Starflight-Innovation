package space.client;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import space.StarflightMod;
import space.planet.Planet;
import space.planet.PlanetList;
import space.planet.PlanetResourceListener;

public class ClientPlanetResourceListener extends PlanetResourceListener
{
	@Override
	public Identifier getFabricId()
	{
		return Identifier.of(StarflightMod.MOD_ID, "client_planets");
	}
	
	@Override
	public CompletableFuture<Void> apply(ArrayList<Planet> data, ResourceManager manager, Profiler profiler, Executor executor)
	{
		return CompletableFuture.runAsync(() -> {
			PlanetList.getClient().loadPlanets(data);
		}, executor);
	}
}