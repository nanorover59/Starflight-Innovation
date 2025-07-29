package space.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import space.item.StarflightItems;

public class SpaceSuitCommand
{
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(literal("spacesuit").then(argument("entities", EntityArgumentType.entities()).executes(context -> spaceSuit(context, EntityArgumentType.getEntities(context, "entities")))));
	}
	
	public static int spaceSuit(CommandContext<ServerCommandSource> context, Collection<? extends Entity> entities)
	{
		int count = 0;
		
		for(Entity entity : entities)
		{
			if(entity instanceof LivingEntity)
			{
				LivingEntity livingEntity = (LivingEntity) entity;
				ItemStack chestplate = StarflightItems.SPACE_SUIT_CHESTPLATE.getDefaultStack();
				chestplate.set(StarflightItems.OXYGEN, StarflightItems.SPACE_SUIT_CHESTPLATE.getComponents().get(StarflightItems.MAX_OXYGEN));
				livingEntity.equipStack(EquipmentSlot.HEAD, StarflightItems.SPACE_SUIT_HELMET.getDefaultStack());
				livingEntity.equipStack(EquipmentSlot.CHEST, chestplate);
				livingEntity.equipStack(EquipmentSlot.LEGS, StarflightItems.SPACE_SUIT_LEGGINGS.getDefaultStack());
				livingEntity.equipStack(EquipmentSlot.FEET, StarflightItems.SPACE_SUIT_BOOTS.getDefaultStack());
				count++;
			}
		}
		
		return count;
	}
}