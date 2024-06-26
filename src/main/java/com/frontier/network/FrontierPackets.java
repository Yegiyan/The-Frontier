package com.frontier.network;

import com.frontier.Frontier;
import com.frontier.entities.ArchitectEntity;
import com.frontier.entities.HireSettler;
import com.frontier.entities.SettlerEntity;
import com.frontier.settlements.SettlementManager;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FrontierPackets
{
	public static final Identifier CREATE_SETTLEMENT_ID = new Identifier(Frontier.MOD_ID, "create_settlement");
	public static final Identifier ABANDON_SETTLEMENT_ID = new Identifier(Frontier.MOD_ID, "abandon_settlement");
	
	public static final Identifier HIRE_ARCHITECT_ID = new Identifier(Frontier.MOD_ID, "hire_architect");
	
	public static void apply()
	{
		ServerPlayNetworking.registerGlobalReceiver(CREATE_SETTLEMENT_ID, (server, player, handler, buf, responseSender) ->
	    {
	        String factionName = buf.readString(32767);
	        server.execute(() ->
	        {
	            SettlementManager.create(player.getUuid(), factionName, server);
	        });
	    });
		
		ServerPlayNetworking.registerGlobalReceiver(ABANDON_SETTLEMENT_ID, (server, player, handler, buf, responseSender) ->
	    {
	        String factionName = buf.readString(32767);
	        server.execute(() ->
	        {
	        	SettlementManager.abandon(player.getUuid(), factionName, server);
	        });
	    });
		
		ServerPlayNetworking.registerGlobalReceiver(HIRE_ARCHITECT_ID, (server, player, handler, buf, responseSender) ->
	    {
	        String settlerFaction = buf.readString(32767);
	        String settlerName = buf.readString(32767);
	        String settlerGender = buf.readString(32767);
	        String settlerExpertise = buf.readString(32767);
	        BlockPos settlerPos = buf.readBlockPos();
	        World world = player.getServerWorld();
	        
	        server.execute(() ->
	        {
	        	SettlerEntity settler = SettlerEntity.findSettlerEntity(world, settlerPos, settlerName);
	        	if (settler != null)
	        	{
	        		settler.remove(RemovalReason.DISCARDED);
	                
	                ArchitectEntity architect = Frontier.ARCHITECT_ENTITY.create(world);
		            architect.refreshPositionAndAngles(settlerPos, 0, 0);
		            HireSettler.architect((ArchitectEntity) architect, settlerName, settlerFaction, "Architect", settlerExpertise, settlerGender, world);
	        	}
	        	else
	                System.err.println("Nomad entity not found!");
	        });
	    });
	}
}