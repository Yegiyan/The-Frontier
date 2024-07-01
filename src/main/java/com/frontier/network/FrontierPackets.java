package com.frontier.network;

import com.frontier.Frontier;
import com.frontier.entities.ArchitectEntity;
import com.frontier.entities.HireSettler;
import com.frontier.entities.SettlerEntity;
import com.frontier.settlements.SettlementManager;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FrontierPackets
{
	public static final Identifier CREATE_SETTLEMENT_ID = new Identifier(Frontier.MOD_ID, "create_settlement");
	public static final Identifier ABANDON_SETTLEMENT_ID = new Identifier(Frontier.MOD_ID, "abandon_settlement");
	
	public static final Identifier SYNC_SETTLER_INVENTORY_ID = new Identifier(Frontier.MOD_ID, "sync_settler_inventory");
	
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
		
		ServerPlayNetworking.registerGlobalReceiver(SYNC_SETTLER_INVENTORY_ID, (server, player, handler, buf, responseSender) ->
		{
	        int entityId = buf.readInt();
	        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(buf.readInt(), ItemStack.EMPTY);
	        for (int i = 0; i < inventory.size(); i++)
	            inventory.set(i, buf.readItemStack());
	        server.execute(() ->
	        {
	            Entity entity = player.getWorld().getEntityById(entityId);
	            if (entity instanceof SettlerEntity)
	                ((SettlerEntity) entity).setClientInventory(inventory);
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