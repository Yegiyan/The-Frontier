package com.frontier.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class FrontierUtil
{
	public static boolean hasEnoughEmeralds(PlayerEntity player, int amount)
	{
		int emeralds = 0;

		// count number of emeralds
		for (int i = 0; i < player.getInventory().size(); i++)
		{
			ItemStack stack = player.getInventory().getStack(i);
			if (stack.getItem() == Items.EMERALD)
			{
				emeralds += stack.getCount();
				if (emeralds >= amount)
					break;
			}
		}

		// not enough emeralds
		if (emeralds < amount)
			return false;

		// remove emeralds
		int remaining = amount;
		for (int i = 0; i < player.getInventory().size(); i++)
		{
			ItemStack stack = player.getInventory().getStack(i);
			if (stack.getItem() == Items.EMERALD)
			{
				if (stack.getCount() > remaining)
				{
					stack.decrement(remaining);
					return true;
				}
				else
				{
					remaining -= stack.getCount();
					stack.setCount(0);
				}
			}
			if (remaining <= 0)
				return true;
		}

		return false;
	}

	public static void refundEmeralds(PlayerEntity player, int amount)
	{
		while (amount > 0)
		{
			int stackSize = Math.min(amount, Items.EMERALD.getMaxCount());
			ItemStack emeraldStack = new ItemStack(Items.EMERALD, stackSize);
			if (!player.getInventory().insertStack(emeraldStack))
				player.dropItem(emeraldStack, false);
			amount -= stackSize;
		}
	}

	public static boolean addItemToPlayerInventory(PlayerInventory inventory, ItemStack itemStack)
	{
        return inventory.insertStack(itemStack);
    }
	
	public static boolean hasEnoughFreeSlots(PlayerInventory inventory, int slots)
	{
		return getEmptySlots(inventory) >= slots;
	}
	
	private static int getEmptySlots(PlayerInventory inventory)
	{
        int emptySlots = 0;
        for (int i = 0; i < 36; i++)
        {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty())
                emptySlots++;
        }
        return emptySlots;
    }
	
	public static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}