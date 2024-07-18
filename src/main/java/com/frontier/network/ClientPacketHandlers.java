package com.frontier.network;

import com.frontier.entities.settler.SettlerEntity;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class ClientPacketHandlers
{
	public static void registerClientPacketHandlers()
	{
		ClientPlayNetworking.registerGlobalReceiver(FrontierPackets.SYNC_SETTLER_INVENTORY_ID,
				(client, handler, buf, responseSender) ->
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
	}
}