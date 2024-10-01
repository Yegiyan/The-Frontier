package com.frontier.blueprint;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.PlayerEntity;

public class BlueprintStateManager
{
	private static final Map<PlayerEntity, BlueprintState> playerBlueprintState = new HashMap<>();

	public static BlueprintState getOrCreateBlueprintState(PlayerEntity player)
	{
		return playerBlueprintState.computeIfAbsent(player, p -> new BlueprintState());
	}
}