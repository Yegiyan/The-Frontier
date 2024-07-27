package com.frontier.network;

import java.util.List;
import java.util.UUID;

import com.frontier.Frontier;
import com.frontier.PlayerData;
import com.frontier.entities.settler.ArchitectEntity;
import com.frontier.entities.settler.NomadEntity;
import com.frontier.entities.settler.SettlerEntity;
import com.frontier.entities.util.HireSettler;
import com.frontier.register.FrontierEntities;
import com.frontier.settlements.SettlementManager;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FrontierPacketsServer
{
	public static final Identifier CREATE_SETTLEMENT_ID = new Identifier(Frontier.MOD_ID, "create_settlement");
	public static final Identifier ABANDON_SETTLEMENT_ID = new Identifier(Frontier.MOD_ID, "abandon_settlement");
	
	public static final Identifier SETTLEMENT_RESOURCES_REQUEST_ID = new Identifier(Frontier.MOD_ID, "settlement_resources_request");
	public static final Identifier BUILD_STRUCTURE_ID = new Identifier(Frontier.MOD_ID, "build_structure");
	public static final Identifier UPGRADE_STRUCTURE_ID = new Identifier(Frontier.MOD_ID, "upgrade_structure");
	
	public static final Identifier SYNC_SETTLER_INVENTORY_ID = new Identifier(Frontier.MOD_ID, "sync_settler_inventory");
	public static final Identifier HIRE_SETTLER_ID = new Identifier(Frontier.MOD_ID, "hire_settler");
	
	public static void registerServerPacketHandlers()
	{
		ServerPlayNetworking.registerGlobalReceiver(CREATE_SETTLEMENT_ID, (server, player, handler, buf, responseSender) ->
	    {
	        String factionName = buf.readString(32767);
	        server.execute(() ->
	        {
	            SettlementManager.create(player.getUuid(), factionName, server);
	            if (SettlementManager.getSettlement(factionName) != null)
	            	SettlementManager.getSettlement(factionName).updateStatistic("Players", 1);
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
		
		ServerPlayNetworking.registerGlobalReceiver(SETTLEMENT_RESOURCES_REQUEST_ID, (server, player, handler, buf, responseSender) ->
		{
			UUID settlerUUID = buf.readUuid();
            String settlerFaction = buf.readString(32767);
            
			server.execute(() ->
			{
				PlayerData playerData = PlayerData.players.get(player.getUuid());
				if (playerData != null)
				{
					List<ItemStack> structureInventory = SettlementManager.getSettlement(playerData.getFaction()).getStructureByName("townhall").getStructureInventory(player.getServer().getOverworld());
					
					PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
					buffer.writeInt(structureInventory.size());
					buffer.writeUuid(settlerUUID);
					buffer.writeString(settlerFaction);
					
					for (ItemStack itemStack : structureInventory)
					{
						buffer.writeItemStack(itemStack);
						buffer.writeInt(itemStack.getCount()); // write item count explicitly
					}

					ServerPlayNetworking.send(player, FrontierPacketsClient.SETTLEMENT_RESOURCES_RESPONSE_ID, buffer);
				}
			});
		});
		
		ServerPlayNetworking.registerGlobalReceiver(BUILD_STRUCTURE_ID, (server, player, handler, buf, responseSender) ->
		{
		    //int emeraldCost = buf.readInt();
		    //World world = player.getServerWorld();
		    
		    server.execute(() ->
		    {
		    	// do building shit here
		    });
		});
		
		ServerPlayNetworking.registerGlobalReceiver(UPGRADE_STRUCTURE_ID, (server, player, handler, buf, responseSender) ->
		{
		    //int emeraldCost = buf.readInt();
		    //World world = player.getServerWorld();
		    
		    server.execute(() ->
		    {
		    	// do building shit here
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
		
		ServerPlayNetworking.registerGlobalReceiver(HIRE_SETTLER_ID, (server, player, handler, buf, responseSender) ->
		{
		    String settlerProfession = buf.readString(32767);
		    String settlerFaction = buf.readString(32767);
		    String settlerFirstName = buf.readString(32767);
		    String settlerLastName = buf.readString(32767);
		    String settlerName = buf.readString(32767);
		    String settlerGender = buf.readString(32767);
		    String settlerExpertise = buf.readString(32767);
		    int settlerHunger = buf.readInt();
		    int settlerMorale = buf.readInt();
		    int settlerSkill = buf.readInt();
		    UUID settlerUUID = buf.readUuid();
		    BlockPos settlerPos = buf.readBlockPos();
		    int emeraldCost = buf.readInt();
		    World world = player.getServerWorld();
		    
		    server.execute(() ->
		    {
		    	SettlerEntity nomad = NomadEntity.findSettlerEntityInRadius(world, settlerPos, settlerName);
	            if (nomad != null)
	            {
	            	if (HireSettler.removeEmeraldsFromPlayer(player, emeraldCost))
			        {
	            		switch (settlerProfession)
		                {
		                    case "ARCHITECT":
		                        nomad.remove(RemovalReason.DISCARDED);
		                        ArchitectEntity architect = FrontierEntities.ARCHITECT_ENTITY.create(world);
		                        architect.refreshPositionAndAngles(settlerPos, 0, 0);
		                        HireSettler.architect(architect, settlerFirstName, settlerLastName, settlerName, settlerFaction, "Architect", settlerExpertise, settlerHunger, settlerMorale, settlerSkill, settlerUUID, settlerGender, world);
		                        break;
		                    default:
		                    	Frontier.LOGGER.error("FrontierPackets() - No settler profession found!");
		                        HireSettler.refundEmeraldsToPlayer(player, emeraldCost);
		                        break;
		                }
			        }
			        else
			        	Frontier.LOGGER.info(player.getDisplayName() + " does not have enough emeralds to hire " + settlerName + "!");
	            }
	            else
	            	Frontier.LOGGER.error("FrontierPackets() - Nomad entity not found!");
		    });
		});
	}
}