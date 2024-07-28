package com.frontier.items;

import com.frontier.Frontier;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FrontierItemGroup
{
	public static final RegistryKey<ItemGroup> FRONTIER_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(Frontier.MOD_ID, "item_group"));
	public static final ItemGroup FRONTIER_ITEM_GROUP = FabricItemGroup.builder().icon(() ->new ItemStack(Items.BELL)).displayName(Text.translatable("itemGroup." + Frontier.MOD_ID)).build();
	
	public static void register()
	{
		Registry.register(Registries.ITEM_GROUP, FRONTIER_ITEM_GROUP_KEY, FRONTIER_ITEM_GROUP);
		ItemGroupEvents.modifyEntriesEvent(FRONTIER_ITEM_GROUP_KEY).register(itemGroup ->
		{
			itemGroup.add(FrontierItems.BLUEPRINT_TOWNHALL);
			itemGroup.add(FrontierItems.BLUEPRINT_WAREHOUSE);
			itemGroup.add(FrontierItems.BLUEPRINT_HOUSE);
		});
	}
}