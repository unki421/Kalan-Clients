
package dev.kalan.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class KalanClickGuiKeybind {

    private static boolean initialized;

    private static KeyBinding openMenu;

    private KalanClickGuiKeybind() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        openMenu = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.kalan_client.click_gui",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        "category.kalan_client"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                if (client.currentScreen instanceof KalanClickGuiScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new KalanClickGuiScreen());
                }
            }
        });

        initialized = true;
    }
}
