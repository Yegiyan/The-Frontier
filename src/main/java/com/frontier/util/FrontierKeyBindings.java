package com.frontier.util;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class FrontierKeyBindings 
{
	public static KeyBinding playerCardKey;
	public static KeyBinding toggleRegionsKey;

    public static void register() 
    {
        playerCardKey = new KeyBinding(
                "Toggle Player Card", // key description
                InputUtil.Type.KEYSYM, // KEYSYM = keyboard | MOUSE = mouse
                GLFW.GLFW_KEY_G, // default key
                "The Frontier" // category name
        );
        
        toggleRegionsKey = new KeyBinding(
                "Toggle Regions (On Maps)",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "The Frontier"
        );
    }
}