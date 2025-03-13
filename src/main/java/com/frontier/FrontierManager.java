package com.frontier;

import java.util.UUID;

import com.frontier.entities.settler.SettlerEntity;
import com.frontier.network.FrontierPacketsServer;
import com.frontier.regions.RegionManager;
import com.frontier.register.FrontierKeyBindings;
import com.frontier.renderers.RegionMapRenderer;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;
import com.frontier.structures.Structure;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class FrontierManager
{

	public static void registerEvents()
	{
		registerEntityData();
		registerPlayerData();
		registerSettlementData();
		registerRegionData();
		registerKeyBindings();
	}

	private static void registerEntityData()
	{
		ServerLifecycleEvents.SERVER_STARTED.register(server ->
		{
			ServerWorld world = server.getWorld(World.OVERWORLD);
			if (world != null && !world.isClient)
			{
				SettlerEntity.loadEntityData(world);
			}
		});
	}

	private static void registerPlayerData()
	{
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
		{
			ServerPlayerEntity player = handler.player;
			UUID playerUUID = player.getUuid();
			PlayerData playerData = PlayerData.players.getOrDefault(playerUUID, new PlayerData(playerUUID, player.getEntityName(), "N/A", "Adventurer", 0, server));
			if (!PlayerData.players.containsKey(playerUUID))
			{
				PlayerData.players.put(playerUUID, playerData);
				playerData.saveData();
			}
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server ->
		{
			PlayerData.loadData(server);
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server ->
		{
			PlayerData.reset();
		});
	}

	private static void registerSettlementData()
	{
		ServerLifecycleEvents.SERVER_STARTED.register(server ->
		{
			SettlementManager.loadSettlements(server);

			for (Settlement settlement : SettlementManager.getSettlements().values())
			{
				for (Structure structure : settlement.getStructures())
				{
					structure.resume(server.getOverworld());
				}
			}
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server ->
		{
			SettlementManager.saveSettlements(server);
			SettlementManager.reset();
		});

		// world save event to save periodically
		ServerTickEvents.END_SERVER_TICK.register(server ->
		{
			// save settlements every 5 minutes (6000 ticks)
			if (server.getTicks() % 6000 == 0)
			{
				Frontier.LOGGER.info("Auto saving settlement data...");
				SettlementManager.saveSettlements(server);
			}
		});
	}

	private static void registerRegionData()
	{
		ServerLifecycleEvents.SERVER_STARTED.register(server ->
		{
			RegionManager.setWorldSeed(server.getOverworld().getSeed());
			ServerWorld world = server.getWorld(World.OVERWORLD);
			if (world != null && !world.isClient)
			{
				RegionManager.loadRegionData(world);
			}
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server ->
		{
			ServerWorld world = server.getWorld(World.OVERWORLD);
			if (world != null && !world.isClient)
			{
				RegionManager.saveRegionData(world);
			}
			RegionManager.reset();
		});
	}

	private static void registerKeyBindings()
	{
		FrontierKeyBindings.register();
		KeyBindingHelper.registerKeyBinding(FrontierKeyBindings.keyCharacterSheet);
		KeyBindingHelper.registerKeyBinding(FrontierKeyBindings.keyToggleRegions);

		ClientTickEvents.END_CLIENT_TICK.register(client ->
		{
			if (FrontierKeyBindings.keyCharacterSheet.wasPressed())
				ClientPlayNetworking.send(FrontierPacketsServer.SETTLEMENT_RESOURCES_REQUEST_PLAYER_ID, new PacketByteBuf(Unpooled.buffer()));

			if (FrontierKeyBindings.keyToggleRegions.wasPressed())
				RegionMapRenderer.isRenderingEnabled = !RegionMapRenderer.isRenderingEnabled;
		});
	}
}
