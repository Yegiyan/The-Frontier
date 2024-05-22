package com.frontier.util;

import com.frontier.Frontier;
import com.frontier.PlayerData;
import com.frontier.settlements.SettlementManager;
import com.mojang.brigadier.context.CommandContext;

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
}