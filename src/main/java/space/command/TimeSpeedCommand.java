package space.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.ServerCommandSource;
import space.planet.PlanetList;

public class TimeSpeedCommand
{
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(literal("timespeed").requires(source -> source.hasPermissionLevel(2)).then(argument("multiplier", FloatArgumentType.floatArg(-10000.0f, 10000.0f)).executes(ctx -> timeSpeed(ctx, FloatArgumentType.getFloat(ctx, "multiplier")))));
	}
	
	public static int timeSpeed(CommandContext<ServerCommandSource> context, float floatArgument)
	{
		PlanetList.setTimeMultiplier(floatArgument);
		return 1;
	}
}