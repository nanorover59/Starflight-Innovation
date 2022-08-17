package space.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class StarflightCommands
{
	public static void initializeCommands()
	{
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			TimeSpeedCommand.register(dispatcher);
		});
	}
}