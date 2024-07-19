package com.frontier.register;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class FrontierKeyBindings 
{
	public static KeyBinding keyCharacterSheet;
	public static KeyBinding keyToggleRegions;
	public static KeyBinding keyToggleConstructionGUI;

    public static void register() 
    {
        keyCharacterSheet = new KeyBinding(
                "Toggle Character Sheet", // key description
                InputUtil.Type.KEYSYM,    // KEYSYM = keyboard | MOUSE = mouse
                GLFW.GLFW_KEY_G,          // default key
                "The Frontier"            // category name
        );
        
        keyToggleConstructionGUI = new KeyBinding(
                "Toggle Construction GUI",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "The Frontier"
        );
        
        keyToggleRegions = new KeyBinding(
                "Toggle Regions (On Maps)",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "The Frontier"
        );
    }
}