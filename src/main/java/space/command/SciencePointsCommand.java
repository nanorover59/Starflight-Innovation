package space.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import space.world.persistent.StarflightPlayerData;
import space.world.persistent.StarflightPlayerState;

public class SciencePointsCommand
{
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(literal("science")
                // /science set <players> <amount>
                .then(literal("set")
                    .then(argument("players", EntityArgumentType.players())
                    .then(argument("amount", IntegerArgumentType.integer(0))
                        .executes(context -> science(context, EntityArgumentType.getPlayers(context, "players"), IntegerArgumentType.getInteger(context, "amount"), false)))))
                // /science add <players> <amount>
                .then(literal("add")
                    .then(argument("players", EntityArgumentType.players())
                    .then(argument("amount", IntegerArgumentType.integer())
                        .executes(context -> science(context, EntityArgumentType.getPlayers(context, "players"), IntegerArgumentType.getInteger(context, "amount"), true)))))
            );
	}
	
	public static int science(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> players, int integerArgument, boolean add)
	{
		for(ServerPlayerEntity player : players)
		{
			StarflightPlayerData data = StarflightPlayerState.getPlayerData(player);
			
			if(add)
				data.science += integerArgument;
			else
				data.science = integerArgument;
			
			StarflightPlayerState.syncPlayerState(player);
			context.getSource().sendFeedback(() -> Text.translatable("science." + (add ? "add" : "set"), player.getName(), integerArgument), true);
		}
		
		return players.size();
	}
}