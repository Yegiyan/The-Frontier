package com.frontier.network;

import java.util.HashMap;
import java.util.Map;

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
	
	public static final Identifier HIRE_SETTLER_ID = new Identifier(Frontier.MOD_ID, "hire_settler");
	
	private static final Map<String, String> SKILL_TRANSFERS = createSkillTransfers();
	
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
		
		ServerPlayNetworking.registerGlobalReceiver(HIRE_SETTLER_ID, (server, player, handler, buf, responseSender) ->
		{
            String settlerProfession = buf.readString(32767);
            String settlerFaction = buf.readString(32767);
            String settlerName = buf.readString(32767);
            String settlerGender = buf.readString(32767);
            String settlerExpertise = buf.readString(32767);
            int settlerMorale = buf.readInt();
            int initialSettlerSkill = buf.readInt();
            BlockPos settlerPos = buf.readBlockPos();
            World world = player.getServerWorld();

            final int settlerSkill = settlerExpertise.equals(SKILL_TRANSFERS.get(settlerProfession)) ? initialSettlerSkill : 0;

            server.execute(() ->
            {
                SettlerEntity settler = SettlerEntity.findSettlerEntity(world, settlerPos, settlerName);
                if (settler != null)
                {
                    settler.remove(RemovalReason.DISCARDED);

                    switch (settlerProfession)
                    {
                        case "ARCHITECT":
                            ArchitectEntity architect = Frontier.ARCHITECT_ENTITY.create(world);
                            architect.refreshPositionAndAngles(settlerPos, 0, 0);
                            HireSettler.architect((ArchitectEntity) architect, settlerName, settlerFaction, "Architect", settlerExpertise, settlerMorale, settlerSkill, settlerGender, world);
                            break;
                        default:
                            System.err.println("apply() - No settler profession found!");
                            break;
                    }
                }
                else
                    System.err.println("Nomad entity not found!");
            });
        });
	}
	
    private static Map<String, String> createSkillTransfers()
    {
        Map<String, String> skillTransfers = new HashMap<>();
        skillTransfers.put("ARCHITECT", "GOVERNING");
        skillTransfers.put("DELIVERER", "GOVERNING");
        skillTransfers.put("COURIER", "GOVERNING");
        skillTransfers.put("PRIEST", "GOVERNING");
        skillTransfers.put("INNKEEPER", "GOVERNING");

        skillTransfers.put("ARCHER", "MILITARY");
        skillTransfers.put("KNIGHT", "MILITARY");
        skillTransfers.put("CLERIC", "MILITARY");

        skillTransfers.put("FARMER", "HARVESTING");
        skillTransfers.put("MINER", "HARVESTING");
        skillTransfers.put("LUMBERJACK", "HARVESTING");
        skillTransfers.put("FISHERMAN", "HARVESTING");

        skillTransfers.put("ALCHEMIST", "CRAFTING");
        skillTransfers.put("BLACKSMITH", "CRAFTING");
        skillTransfers.put("FLETCHER", "CRAFTING");
        skillTransfers.put("MASON", "CRAFTING");
        skillTransfers.put("CARPENTER", "CRAFTING");
        skillTransfers.put("CARTOGRAPHER", "CRAFTING");

        skillTransfers.put("BEEKEEPER", "RANCHING");
        skillTransfers.put("POULTRYMAN", "RANCHING");
        skillTransfers.put("COWHAND", "RANCHING");
        skillTransfers.put("SWINEHERD", "RANCHING");
        skillTransfers.put("SHEPHERD", "RANCHING");
        skillTransfers.put("STABLEHAND", "RANCHING");

        skillTransfers.put("BAKER", "TRADING");
        skillTransfers.put("COOK", "TRADING");
        skillTransfers.put("ARCANIST", "TRADING");
        skillTransfers.put("TANNER", "TRADING");
        skillTransfers.put("MERCHANT", "TRADING");

        return skillTransfers;
    }
}