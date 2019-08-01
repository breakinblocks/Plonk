package com.breakinblocks.plonk.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

public class ClientEvents {
    public static final KeyBinding KEY_PLACE = new KeyBinding("key.plonk.place.desc", Keyboard.KEY_P, "key.plonk.category");

    public ClientEvents() {
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiIngame ingameGUI = mc.ingameGUI;
        if (KEY_PLACE.isPressed()) {
            if (ingameGUI != null) {
                ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("PLONK!"));
                // TODO: Implement
            }
        }
    }

    public void registerKeyBindings() {
        ClientRegistry.registerKeyBinding(ClientEvents.KEY_PLACE);
    }
}
