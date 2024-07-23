package com.frontier.network;

import java.util.ArrayList;
import java.util.List;

import com.frontier.Frontier;
import com.frontier.entities.settler.SettlerEntity;
import com.frontier.gui.StructureScreen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class FrontierPacketsClient
{
	public static final Identifier SETTLEMENT_RESOURCES_RESPONSE_ID = new Identifier(Frontier.MOD_ID, "settlement_resources_response");
	
	public static void registerClientPacketHandlers()
	{
		ClientPlayNetworking.registerGlobalReceiver(FrontierPacketsServer.SYNC_SETTLER_INVENTORY_ID, (client, handler, buf, responseSender) ->
		{
			int entityId = buf.readInt();
			DefaultedList<ItemStack> inventory = DefaultedList.ofSize(buf.readInt(), ItemStack.EMPTY);
			for (int i = 0; i < inventory.size(); i++)
				inventory.set(i, buf.readItemStack());
			client.execute(() ->
			{
				Entity entity = client.world.getEntityById(entityId);
				if (entity instanceof SettlerEntity)
					((SettlerEntity) entity).setClientInventory(inventory);
			});
		});
		
		ClientPlayNetworking.registerGlobalReceiver(SETTLEMENT_RESOURCES_RESPONSE_ID, (client, handler, buf, responseSender) ->
		{
			int size = buf.readInt();
			List<ItemStack> structureInventory = new ArrayList<>();
			for (int i = 0; i < size; i++)
			{
			    ItemStack itemStack = buf.readItemStack();
			    int count = buf.readInt(); // read item count explicitly
			    itemStack.setCount(count);
			    structureInventory.add(itemStack);
			}
		    
		    client.execute(() ->
		    {
		        MinecraftClient.getInstance().setScreen(new StructureScreen(structureInventory));
		    });
		});
	}
}