package com.frontier.network;

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
	}
}