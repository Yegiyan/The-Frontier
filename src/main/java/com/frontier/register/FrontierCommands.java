package com.frontier.register;

import com.frontier.Frontier;
import com.frontier.PlayerData;
import com.frontier.settlements.SettlementManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public class FrontierCommands
{
	public static int getRenown(CommandContext<ServerCommandSource> context, String playerName) 
    {
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        PlayerData playerData = PlayerData.map.get(player.getUuid());
        if(playerData != null) 
        {
        	Frontier.sendMessage(player, playerData.getName() + " currently has " + playerData.getRenown() + " renown!", Formatting.WHITE);
        } 
        return 1;
    }

    public static int setRenown(CommandContext<ServerCommandSource> context, String playerName, int renown) 
    {
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        PlayerData playerData = PlayerData.map.get(player.getUuid());
        if(playerData != null) 
        {
            playerData.setRenown(renown);
            playerData.saveData();
            Frontier.sendMessage(player, playerData.getName() + "'s renown has been set to " + playerData.getRenown() + "!", Formatting.WHITE);
        }
        return 1;
    }
	
    public static int getFaction(CommandContext<ServerCommandSource> context, String playerName) 
    {
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        PlayerData playerData = PlayerData.map.get(player.getUuid());
        if(playerData != null) 
        {
            String faction = playerData.getFaction();
            if (faction.contentEquals("N/A"))
            	Frontier.sendMessage(player, playerData.getName() + " is not in a faction!", Formatting.WHITE);
            else
            	Frontier.sendMessage(player, playerData.getName() + " is aligned with " + faction + "!", Formatting.WHITE);
        } 
        return 1;
    }

    public static int setFaction(CommandContext<ServerCommandSource> context, String playerName, String faction)
    {
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        PlayerData playerData = PlayerData.map.get(player.getUuid());

        SettlementManager.switchPlayerSettlement(playerData.getUUID(), playerData.getFaction(), faction, context.getSource().getServer());
        
        if(playerData != null)
        {
            playerData.setFaction(faction);
            playerData.saveData();
            Frontier.sendMessage(player, playerData.getName() + "'s faction has been set to " + faction + "!", Formatting.WHITE);
        }
        
        return 1;
    }

    public static int getProfession(CommandContext<ServerCommandSource> context, String playerName) 
    {
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        PlayerData playerData = PlayerData.map.get(player.getUuid());
        if(playerData != null) 
        {
            String profession = playerData.getProfession();
            Frontier.sendMessage(player, playerData.getName() + " is a " + profession + "!", Formatting.WHITE);
        } 
        return 1;
    }

    public static int setProfession(CommandContext<ServerCommandSource> context, String playerName, String profession) 
    {
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        PlayerData playerData = PlayerData.map.get(player.getUuid());
        if(playerData != null) 
        {
            playerData.setProfession(profession);
            playerData.saveData();
            Frontier.sendMessage(player, playerData.getName() + " is now a " + profession + "!", Formatting.WHITE);
        }
        return 1;
    }
    
    public static void register()
	{
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
		{
	        LiteralArgumentBuilder<ServerCommandSource> getRenownCommand = CommandManager.literal("getFrontierRenown")
	                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
	                .executes(context -> getRenown(context, GameProfileArgumentType.getProfileArgument(context, "player").iterator().next().getName())));
	        dispatcher.register(getRenownCommand);
	    });

	    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
	    {
	        LiteralArgumentBuilder<ServerCommandSource> setRenownCommand = CommandManager.literal("setFrontierRenown")
	                .requires(source -> source.hasPermissionLevel(2))
	                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
	                .then(CommandManager.argument("renown", IntegerArgumentType.integer())
	                        .executes(context -> setRenown(context, GameProfileArgumentType.getProfileArgument(context, "player").iterator().next().getName(), 
	                                IntegerArgumentType.getInteger(context, "renown")))));
	        dispatcher.register(setRenownCommand);
	    });
	    
	    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
	    {
	        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("getFrontierFaction")
	                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
	                .executes(context -> getFaction(context, GameProfileArgumentType.getProfileArgument(context, "player").iterator().next().getName())));
	        dispatcher.register(command);
	    });
	    
	    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
	    {
	        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("setFrontierFaction")
	                .requires(source -> source.hasPermissionLevel(2))
	                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
	                .then(CommandManager.argument("faction", StringArgumentType.word())
	                        .executes(context -> setFaction(context, GameProfileArgumentType.getProfileArgument(context, "player").iterator().next().getName(), 
	                        		StringArgumentType.getString(context, "faction")))));
	        dispatcher.register(command);
	    });
	    
	    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
	    {
	        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("getFrontierProfession")
	                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
	                .executes(context -> getProfession(context, GameProfileArgumentType.getProfileArgument(context, "player").iterator().next().getName())));
	        dispatcher.register(command);
	    });
	    
	    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
	    {
	        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("setFrontierProfession")
	                .requires(source -> source.hasPermissionLevel(2))
	                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
	                .then(CommandManager.argument("profession", StringArgumentType.word())
	                        .executes(context -> setProfession(context, GameProfileArgumentType.getProfileArgument(context, "player").iterator().next().getName(), 
	                        		StringArgumentType.getString(context, "profession")))));
	        dispatcher.register(command);
	    });
	}
}