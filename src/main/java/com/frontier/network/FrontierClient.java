package com.frontier.network;

import com.frontier.items.BlueprintItem;
import com.frontier.renderers.TerritoryRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FrontierClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		FrontierPacketsClient.registerClientPacketHandlers();
		TerritoryRenderer.register();
		BlueprintItem.register();
	}
}