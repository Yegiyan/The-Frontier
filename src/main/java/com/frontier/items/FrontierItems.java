package com.frontier.items;

import com.frontier.Frontier;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class FrontierItems
{
	public static final Item SETTLEMENT_CHARTER = register(new Item(new Item.Settings().maxCount(1)), "settlement_charter");
	public static final Item BLUEPRINT_TOWNHALL = register(new Item(new Item.Settings().maxCount(1)), "blueprint_townhall");
	public static final Item BLUEPRINT_WAREHOUSE = register(new Item(new Item.Settings().maxCount(1)), "blueprint_warehouse");
	public static final Item BLUEPRINT_HOUSE = register(new Item(new Item.Settings().maxCount(1)), "blueprint_house");
	
	public static void initialize() {}
	public static Item register(Item item, String id)
	{
		Identifier itemID = Identifier.of(Frontier.MOD_ID, id);
		Item registeredItem = Registry.register(Registries.ITEM, itemID, item);
		return registeredItem;
	}
	
	public static boolean isItemStackable(Item item)
	{
        return item.getMaxCount() > 1;
    }
}