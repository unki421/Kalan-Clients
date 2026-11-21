package dev.kalan.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class KalanHudDragHandler {

    private static final double[] CURSOR_X = new double[1];
    private static final double[] CURSOR_Y = new double[1];

    private static boolean initialized;
    private static boolean leftWasDown;
    private static KalanHudElement dragging;
    private static int dragOffsetX;
    private static int dragOffsetY;

    private KalanHudDragHandler() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        ClientTickEvents.END_CLIENT_TICK.register(KalanHudDragHandler::tick);
        initialized = true;
    }

    private static void tick(MinecraftClient client) {
        if (!(client.currentScreen instanceof ChatScreen)
                || client.player == null
                || client.world == null) {
            stopDragging();
            return;
        }

        Window window = client.getWindow();
        long handle = window.getHandle();

        boolean leftDown = GLFW.glfwGetMouseButton(
                handle,
                GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) == GLFW.GLFW_PRESS;

        GLFW.glfwGetCursorPos(handle, CURSOR_X, CURSOR_Y);

        int mouseX = (int) Math.round(
                CURSOR_X[0] * window.getScaledWidth() / Math.max(1.0, window.getWidth())
        );
        int mouseY = (int) Math.round(
                CURSOR_Y[0] * window.getScaledHeight() / Math.max(1.0, window.getHeight())
        );

        if (leftDown && !leftWasDown) {
            beginDragging(mouseX, mouseY);
        }

        if (leftDown && dragging != null) {
            dragging.setPosition(
                    mouseX - dragOffsetX,
                    mouseY - dragOffsetY
            );
            dragging.clampToScreen(
                    window.getScaledWidth(),
                    window.getScaledHeight()
            );
        }

        if (!leftDown && leftWasDown && dragging != null) {
            KalanHudLayoutStore.save();
            dragging = null;
        }

        leftWasDown = leftDown;
    }

    private static void beginDragging(int mouseX, int mouseY) {
        List<KalanHudElement> elements = KalanHudRegistry.getAll();

        // Последний отрисованный элемент проверяется первым.
        for (int i = elements.size() - 1; i >= 0; i--) {
            KalanHudElement element = elements.get(i);

            if (!element.isVisible() || !element.contains(mouseX, mouseY)) {
                continue;
            }

            dragging = element;
            dragOffsetX = mouseX - element.getX();
            dragOffsetY = mouseY - element.getY();
            return;
        }
    }

    private static void stopDragging() {
        if (dragging != null) {
            KalanHudLayoutStore.save();
        }

        dragging = null;
        leftWasDown = false;
    }
}
