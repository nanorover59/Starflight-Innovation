package space.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.ServerCommandSource;
import space.planet.PlanetList;

public class TimeStepsCommand
{
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(literal("timesteps").requires(source -> source.hasPermissionLevel(2)).then(argument("steps", IntegerArgumentType.integer(0, 32000)).executes(ctx -> timeSteps(ctx, IntegerArgumentType.getInteger(ctx, "steps")))));
	}
	
	public static int timeSteps(CommandContext<ServerCommandSource> context, int integerArgument)
	{
		PlanetList.get().setTimeSteps(integerArgument);
		return 1;
	}
}