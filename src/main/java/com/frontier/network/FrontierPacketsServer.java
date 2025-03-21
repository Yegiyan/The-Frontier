package com.frontier.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.frontier.Frontier;
import com.frontier.PlayerData;
import com.frontier.entities.settler.ArchitectEntity;
import com.frontier.entities.settler.NomadEntity;
import com.frontier.entities.settler.SettlerEntity;
import com.frontier.entities.util.HireSettler;
import com.frontier.items.FrontierItems;
import com.frontier.register.FrontierEntities;
import com.frontier.settlements.Blueprint;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;
import com.frontier.structures.House;
import com.frontier.structures.Structure;
import com.frontier.structures.StructureInventoryManager;
import com.frontier.structures.StructureType;
import com.frontier.structures.TownHall;
import com.frontier.structures.Warehouse;
import com.frontier.util.FrontierUtil;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FrontierPacketsServer
{
	public static final Identifier CREATE_SETTLEMENT_ID = new Identifier(Frontier.MOD_ID, "create_settlement");
	public static final Identifier ABANDON_SETTLEMENT_ID = new Identifier(Frontier.MOD_ID, "abandon_settlement");
	
	public static final Identifier SETTLEMENT_RESOURCES_REQUEST_ARCHITECT_ID = new Identifier(Frontier.MOD_ID, "settlement_resources_request_architect");
	public static final Identifier SETTLEMENT_RESOURCES_REQUEST_PLAYER_ID = new Identifier(Frontier.MOD_ID, "settlement_resources_request_player");
	
	public static final Identifier BUY_BLUEPRINT_ID = new Identifier(Frontier.MOD_ID, "build_structure");
	public static final Identifier UPGRADE_STRUCTURE_ID = new Identifier(Frontier.MOD_ID, "upgrade_structure");
	public static final Identifier REPAIR_STRUCTURE_ID = new Identifier(Frontier.MOD_ID, "repair_structure");
	
	public static final Identifier SYNC_SETTLER_INVENTORY_ID = new Identifier(Frontier.MOD_ID, "sync_settler_inventory");
	public static final Identifier HIRE_SETTLER_ID = new Identifier(Frontier.MOD_ID, "hire_settler");
	
	public static final Identifier BLUEPRINT_PLACEMENT_ID = new Identifier(Frontier.MOD_ID, "blueprint_placement");
	
	public static void registerHandlers()
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
		
		ServerPlayNetworking.registerGlobalReceiver(SETTLEMENT_RESOURCES_REQUEST_ARCHITECT_ID, (server, player, handler, buf, responseSender) ->
		{
			UUID settlerUUID = buf.readUuid();
			String settlerFaction = buf.readString(32767);

			server.execute(() ->
			{
				PlayerData playerData = PlayerData.players.get(player.getUuid());
				if (playerData != null)
				{
					List<ItemStack> structureInventory = new ArrayList<>();
					Settlement settlement = SettlementManager.getSettlement(playerData.getFaction());

					if (settlement != null)
					{
						// collect from TOWNHALL + WAREHOUSE
						for (Structure structure : settlement.getStructures())
							if (structure.getType() == StructureType.TOWNHALL || structure.getType() == StructureType.WAREHOUSE)
								structureInventory.addAll(structure.getStructureInventory(player.getServer().getOverworld()));
					}

					// merge identical stacks
					structureInventory = StructureInventoryManager.mergeStacks(structureInventory);

					// send results back to client
					PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
					buffer.writeInt(structureInventory.size()); // how many stacks we have
					buffer.writeUuid(settlerUUID);
					buffer.writeString(settlerFaction);

					// write each stack
					for (ItemStack itemStack : structureInventory)
					{
						buffer.writeItemStack(itemStack);
						buffer.writeInt(itemStack.getCount());
					}

					ServerPlayNetworking.send(player, FrontierPacketsClient.SETTLEMENT_RESOURCES_RESPONSE_ARCHITECT_ID, buffer);
				}
			});
		});
		
		ServerPlayNetworking.registerGlobalReceiver(SETTLEMENT_RESOURCES_REQUEST_PLAYER_ID, (server, player, handler, buf, responseSender) ->
		{
			server.execute(() ->
			{
				PlayerData playerData = PlayerData.players.get(player.getUuid());
				if (playerData != null)
				{
					PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
					List<ItemStack> structureInventory = new ArrayList<>();
					Settlement settlement = SettlementManager.getSettlement(playerData.getFaction());

					if (settlement != null)
					{
						// collect from TOWNHALL + WAREHOUSE
						for (Structure structure : settlement.getStructures())
							if (structure.getType() == StructureType.TOWNHALL || structure.getType() == StructureType.WAREHOUSE)
								structureInventory.addAll(structure.getStructureInventory(player.getServer().getOverworld()));
					}

					// merge identical stacks
					structureInventory = StructureInventoryManager.mergeStacks(structureInventory);

					// write merged results
					buffer.writeInt(structureInventory.size());
					for (ItemStack itemStack : structureInventory)
					{
						buffer.writeItemStack(itemStack);
						buffer.writeInt(itemStack.getCount());
					}

					ServerPlayNetworking.send(player, FrontierPacketsClient.SETTLEMENT_RESOURCES_RESPONSE_PLAYER_ID, buffer);
				}
			});
		});
		
		ServerPlayNetworking.registerGlobalReceiver(BUY_BLUEPRINT_ID, (server, player, handler, buf, responseSender) ->
		{
			int cost = buf.readInt();
			Blueprint blueprint = buf.readEnumConstant(Blueprint.class);
		    
		    server.execute(() ->
		    {
		    	if (FrontierUtil.hasEnoughEmeralds(player, cost))
		        {
		    		ItemStack item;
		    		switch (blueprint)
	                {
	                    case TOWNHALL:
	                    	item = new ItemStack(FrontierItems.BLUEPRINT_TOWNHALL);
	                    	FrontierUtil.addItemToPlayerInventory(player.getInventory(), item);
	                        break;
	                    case WAREHOUSE:
	                    	item = new ItemStack(FrontierItems.BLUEPRINT_WAREHOUSE);
	                    	FrontierUtil.addItemToPlayerInventory(player.getInventory(), item);
	                        break;
	                    case HOUSE:
	                    	item = new ItemStack(FrontierItems.BLUEPRINT_HOUSE);
	                    	FrontierUtil.addItemToPlayerInventory(player.getInventory(), item);
	                        break;
	                    default:
	                    	item = null;
	                    	Frontier.LOGGER.error("FrontierPacketsServer() - No blueprint found!");
	                    	FrontierUtil.refundEmeralds(player, cost);
	                        break;
	                }
		        }
		    	else
		        	Frontier.LOGGER.info(player.getDisplayName() + " does not have enough emeralds to buy a " + blueprint + " blueprint!");
		    });
		});
		
		// structure upgrades triggered by UI
		ServerPlayNetworking.registerGlobalReceiver(UPGRADE_STRUCTURE_ID, (server, player, handler, buf, responseSender) ->
		{
			UUID structureUUID = buf.readUuid();
			
			server.execute(() ->
			{
				PlayerData playerData = PlayerData.players.get(player.getUuid());
				if (playerData != null)
				{
					Settlement settlement = SettlementManager.getSettlement(playerData.getFaction());
					if (settlement != null)
					{
						for (Structure structure : settlement.getStructures())
						{
							if (structure.getUUID().equals(structureUUID))
							{
								// check if upgrade is available and player has permission
								if (structure.upgradeAvailable() && structure.getLeader().equals(player.getUuid()))
								{
									structure.upgrade(server.getOverworld());
									player.sendMessage(Text.literal("Upgrading " + structure.getName()), false);
								}
								break;
							}
						}
					}
				}
			});
		});
		
		ServerPlayNetworking.registerGlobalReceiver(REPAIR_STRUCTURE_ID, (server, player, handler, buf, responseSender) ->
		{
			UUID structureUUID = buf.readUuid();

			server.execute(() -> {
				PlayerData playerData = PlayerData.players.get(player.getUuid());
				if (playerData != null)
				{
					Settlement settlement = SettlementManager.getSettlement(playerData.getFaction());
					if (settlement != null)
					{
						Structure structure = settlement.getStructureByUUID(structureUUID);
						if (structure != null && structure.requiresRepair())
						{
							structure.getRepairManager().repairStructure(server.getOverworld());
							player.sendMessage(Text.literal("Repairing " + structure.getName()), false);
						}
					}
				}
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
		    int cost = buf.readInt();
		    World world = player.getServerWorld();
		    
		    server.execute(() ->
		    {
		    	SettlerEntity nomad = NomadEntity.findSettlerEntityInRadius(world, settlerPos, settlerName);
	            if (nomad != null)
	            {
	            	if (FrontierUtil.hasEnoughEmeralds(player, cost))
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
		                    	Frontier.LOGGER.error("FrontierPacketsServer() - No settler profession found!");
		                    	FrontierUtil.refundEmeralds(player, cost);
		                        break;
		                }
			        }
			        else
			        	Frontier.LOGGER.info(player.getDisplayName() + " does not have enough emeralds to hire " + settlerName + "!");
	            }
	            else
	            	Frontier.LOGGER.error("FrontierPacketsServer() - Nomad entity not found!");
		    });
		});
		
		ServerPlayNetworking.registerGlobalReceiver(BLUEPRINT_PLACEMENT_ID, (server, player, handler, buf, responseSender) ->
		{
		    String faction = buf.readString(32767);
		    String blueprintName = buf.readString(32767);
		    BlockPos placementPos = buf.readBlockPos();
		    Direction facing = buf.readEnumConstant(Direction.class);
		    
		    server.execute(() ->
		    {
		        PlayerData playerData = PlayerData.players.get(player.getUuid());
		        if (playerData != null && playerData.isLeader())
		        {
		            Settlement settlement = SettlementManager.getSettlement(faction);
		            if (settlement != null)
		            {
		                Structure structure = null;
		                switch (blueprintName)
		                {
		                    case "TOWNHALL":
		                        structure = new TownHall(blueprintName, faction, placementPos, facing);
		                        break;
		                    case "WAREHOUSE":
		                        structure = new Warehouse(blueprintName, faction, placementPos, facing);
		                        break;
		                    case "HOUSE":
		                        structure = new House(blueprintName, faction, placementPos, facing);
		                        break;
		                    default:
		                        Frontier.LOGGER.error("Unknown blueprint type: " + blueprintName);
		                        break;
		                }
		                
		                if (structure != null)
		                {
		                	if (!structure.getPosition().equals(placementPos))
		                        structure.setPosition(placementPos);
		                	
		                    settlement.addStructure(structure);
		                    structure.constructStructure(server.getOverworld());
		                    Frontier.sendMessage(player, "Started construction of " + structure.getName() + " at " + placementPos.toShortString(), Formatting.GREEN);
		                }
		            }
		            else
		                Frontier.sendMessage(player, "Settlement not found!", Formatting.RED);
		        }
		        else
		            Frontier.sendMessage(player, "Only settlement leaders can confirm blueprint placement!", Formatting.RED);
		    });
		});
	}
}