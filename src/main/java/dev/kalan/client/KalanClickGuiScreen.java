package dev.kalan.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class KalanClickGuiScreen extends Screen {

    private static final int WIDTH = 540;
    private static final int HEIGHT = 350;
    private static final int BACKDROP = 0x9906030D;
    private static final int PANEL = 0xFF100D18;
    private static final int SIDEBAR = 0xFF0B0911;
    private static final int CARD = 0xFF1A1820;
    private static final int CARD_HEADER = 0xFF29272D;
    private static final int ACCENT = 0xFF9B62F5;
    private static final int ACCENT_DARK = 0xFF58358B;
    private static final int WHITE = 0xFFF5F1FA;
    private static final int MUTED = 0xFFAAA4B4;
    private static final int TRACK_OFF = 0xFF39363F;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private KalanClickGuiCategory category = KalanClickGuiCategory.HUD;
    private KalanClickGuiEntry selected;
    private String search = "";
    private int scrollOffset;

    public KalanClickGuiScreen() {
        super(Text.literal("Kalan Client"));
    }

    private int panelWidth() {
        return Math.min(WIDTH, Math.max(300, width - 16));
    }

    private int panelHeight() {
        return Math.min(HEIGHT, Math.max(220, height - 16));
    }

    private int left() {
        return (width - panelWidth()) / 2;
    }

    private int top() {
        return (height - panelHeight()) / 2;
    }

    private int sidebarWidth() {
        return Math.max(62, Math.min(78, panelWidth() / 6));
    }

    private void drawText(DrawContext context, String value, int x, int y, int color) {
        context.drawTextWithShadow(client.textRenderer, value, x, y, color);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, BACKDROP);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int x = left();
        int y = top();
        int w = panelWidth();
        int h = panelHeight();
        int side = sidebarWidth();

        rounded(context, x, y, w, h, 15, PANEL);
        rounded(context, x + 1, y + 1, w - 2, h - 2, 14, 0xFF100D18);

        // Brand and compact category rail.
        drawText(context, "K", x + 20, y + 16, ACCENT);
        drawText(context, "KALAN", x + 35, y + 16, WHITE);
        drawText(context, "CLIENT", x + 35, y + 29, MUTED);

        rounded(context, x + 10, y + 48, side - 16, h - 60, 12, SIDEBAR);
        renderCategories(context, x, y, side, h);

        int contentX = x + side + 12;
        int contentWidth = w - side - 24;

        // Search field.
        rounded(context, contentX, y + 12, contentWidth, 30, 15, 0xFF08070C);
        drawText(context, "⌕", contentX + 11, y + 20, MUTED);
        String searchLabel = search.isEmpty() ? "Search..." : search;
        drawText(context, searchLabel, contentX + 31, y + 21,
                search.isEmpty() ? 0xFF77717F : WHITE);
        drawText(context, "×", contentX + contentWidth - 17, y + 20, MUTED);

        if (selected != null) {
            renderSettings(context, contentX, y, contentWidth, h);
        } else {
            renderEntries(context, contentX, y, contentWidth, h);
        }

        drawText(context, "Right Shift  •  Fabric 1.21.4",
                x + 15, y + h - 17, 0xFF77717F);
    }

    private void renderCategories(DrawContext context, int x, int y, int side, int h) {
        int index = 0;
        for (KalanClickGuiCategory item : KalanClickGuiCategory.values()) {
            int buttonY = y + 60 + index * 43;
            boolean active = category == item;
            int buttonWidth = side - 24;
            rounded(context, x + 12, buttonY, buttonWidth, 34,
                    9, active ? 0xFF3A2855 : SIDEBAR);
            if (active) {
                rounded(context, x + 12, buttonY + 4, 3, 26, 2, ACCENT);
            }
            String symbol = switch (item) {
                case HUD -> "▦";
                case COMBAT -> "⚔";
                case SETTINGS -> "⚙";
            };
            int symbolWidth = client.textRenderer.getWidth(symbol);
            drawText(context, symbol, x + 12 + (buttonWidth - symbolWidth) / 2,
                    buttonY + 11, active ? ACCENT : MUTED);
            index++;
        }
    }

    private void renderEntries(DrawContext context, int x, int y, int contentWidth, int panelHeight) {
        List<KalanClickGuiEntry> entries = getVisibleEntries();
        if (entries.isEmpty()) {
            drawText(context, search.isEmpty()
                    ? "В этом разделе пока нет функций"
                    : "Ничего не найдено",
                    x + 12, y + 100, MUTED);
            return;
        }

        int gap = 8;
        int columns = contentWidth >= 350 ? 3 : contentWidth >= 230 ? 2 : 1;
        int cardWidth = (contentWidth - (columns - 1) * gap) / columns;
        int cardHeight = 58;
        int startY = y + 54;
        int visibleBottom = y + panelHeight - 27;

        for (int i = 0; i < entries.size(); i++) {
            KalanClickGuiEntry entry = entries.get(i);
            int column = i % columns;
            int row = i / columns;
            int cardX = x + column * (cardWidth + gap);
            int cardY = startY + row * (cardHeight + gap);
            if (cardY + cardHeight > visibleBottom) {
                continue;
            }

            boolean enabled = entry.isEnabled();
            rounded(context, cardX, cardY, cardWidth, cardHeight, 10,
                    enabled ? ACCENT_DARK : 0xFF26232C);
            rounded(context, cardX + 2, cardY + 2, cardWidth - 4, cardHeight - 4,
                    8, CARD);
            rounded(context, cardX + 8, cardY + 7, cardWidth - 16, 22,
                    7, CARD_HEADER);

            String title = trim(entry.getTitle(), Math.max(5, cardWidth - 28));
            int titleWidth = client.textRenderer.getWidth(title);
            drawText(context, title, cardX + (cardWidth - titleWidth) / 2,
                    cardY + 14, WHITE);

            // Toggle switch.
            int toggleX = cardX + 10;
            int toggleY = cardY + 38;
            rounded(context, toggleX, toggleY, 30, 13, 7,
                    enabled ? ACCENT : TRACK_OFF);
            rounded(context, toggleX + (enabled ? 17 : 2), toggleY + 2, 9, 9,
                    5, 0xFFFFFFFF);

            // Settings control.
            drawText(context, "☷", cardX + cardWidth - 21, cardY + 36, WHITE);
        }
    }

    private List<KalanClickGuiEntry> getVisibleEntries() {
        List<KalanClickGuiEntry> source = switch (category) {
            case HUD -> KalanClickGuiEntries.getHudEntries();
            case COMBAT -> KalanClickGuiEntries.getCombatEntries();
            case SETTINGS -> List.of();
        };
        if (search.isBlank()) {
            return source;
        }
        String query = search.toLowerCase(Locale.ROOT);
        List<KalanClickGuiEntry> filtered = new ArrayList<>();
        for (KalanClickGuiEntry entry : source) {
            if (entry.getTitle().toLowerCase(Locale.ROOT).contains(query)
                    || entry.getDescription().toLowerCase(Locale.ROOT).contains(query)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    private String trim(String value, int maxWidth) {
        if (client.textRenderer.getWidth(value) <= maxWidth) {
            return value;
        }
        String result = value;
        while (!result.isEmpty() && client.textRenderer.getWidth(result + "…") > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result + "…";
    }

    private void renderSettings(DrawContext context, int x, int y, int contentWidth, int panelHeight) {
        KalanHudElement element = KalanHudRegistry.find(selected.getPanel());
        if (element == null) {
            selected = null;
            return;
        }

        drawText(context, "‹", x + 8, y + 58, ACCENT);
        drawText(context, selected.getTitle(), x + 25, y + 58, WHITE);
        drawText(context, "Настройки HUD-элемента", x + 10, y + 83, MUTED);

        drawText(context, "Масштаб", x + 10, y + 116, WHITE);
        drawText(context, String.format(Locale.ROOT, "%.1f", element.getScale()),
                x + contentWidth - 34, y + 116, ACCENT);
        rounded(context, x + 10, y + 137, contentWidth - 20, 7, 4, TRACK_OFF);
        int scaleFill = Math.round((element.getScale() - 0.5f) / 1.5f * (contentWidth - 20));
        rounded(context, x + 10, y + 137, Math.max(4, scaleFill), 7, 4, ACCENT);

        drawText(context, "Прозрачность", x + 10, y + 166, WHITE);
        drawText(context, Math.round(element.getOpacity() * 100) + "%",
                x + contentWidth - 34, y + 166, ACCENT);
        rounded(context, x + 10, y + 187, contentWidth - 20, 7, 4, TRACK_OFF);
        rounded(context, x + 10, y + 187,
                Math.max(4, Math.round((contentWidth - 20) * element.getOpacity())),
                7, 4, ACCENT);

        drawText(context, "Нажми на левую или правую часть шкалы, чтобы изменить значение",
                x + 10, Math.min(y + panelHeight - 42, y + 220), MUTED);
        drawText(context, "ПКМ или Esc — назад", x + 10, y + panelHeight - 38, MUTED);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = left();
        int y = top();
        int w = panelWidth();
        int h = panelHeight();
        int side = sidebarWidth();
        int contentX = x + side + 12;
        int contentWidth = w - side - 24;

        if (button == 0 && mouseY >= y + 12 && mouseY <= y + 42
                && mouseX >= contentX && mouseX <= contentX + contentWidth) {
            if (mouseX >= contentX + contentWidth - 24) {
                search = "";
            }
            return true;
        }

        int categoryIndex = 0;
        for (KalanClickGuiCategory item : KalanClickGuiCategory.values()) {
            int buttonY = y + 60 + categoryIndex * 43;
            if (button == 0 && KalanClickGuiLayout.contains((int) mouseX, (int) mouseY,
                    x + 12, buttonY, side - 24, 34)) {
                category = item;
                selected = null;
                return true;
            }
            categoryIndex++;
        }

        if (selected != null) {
            if (button == 1 || (button == 0 && mouseX < contentX + 24
                    && mouseY < y + 75)) {
                selected = null;
                return true;
            }
            if (button == 0) {
                changeSetting(mouseX, mouseY, contentX, y, contentWidth);
                return true;
            }
            return true;
        }

        List<KalanClickGuiEntry> entries = getVisibleEntries();
        int columns = contentWidth >= 350 ? 3 : contentWidth >= 230 ? 2 : 1;
        int gap = 8;
        int cardWidth = (contentWidth - (columns - 1) * gap) / columns;
        int cardHeight = 58;
        int startY = y + 54;

        for (int i = 0; i < entries.size(); i++) {
            int column = i % columns;
            int row = i / columns;
            int cardX = contentX + column * (cardWidth + gap);
            int cardY = startY + row * (cardHeight + gap);
            if (!KalanClickGuiLayout.contains((int) mouseX, (int) mouseY,
                    cardX, cardY, cardWidth, cardHeight)) {
                continue;
            }

            KalanClickGuiEntry entry = entries.get(i);
            if (button == 0) {
                if (mouseX >= cardX + cardWidth - 32) {
                    selected = entry;
                } else {
                    entry.toggle();
                }
            } else if (button == 1) {
                selected = entry;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void changeSetting(double mouseX, double mouseY, int x, int y, int contentWidth) {
        KalanHudElement element = KalanHudRegistry.find(selected.getPanel());
        if (element == null) {
            return;
        }

        if (mouseY >= y + 128 && mouseY <= y + 151) {
            float value = mouseX < x + contentWidth / 2
                    ? element.getScale() - 0.1f : element.getScale() + 0.1f;
            element.setScale(value);
        } else if (mouseY >= y + 178 && mouseY <= y + 201) {
            float value = mouseX < x + contentWidth / 2
                    ? element.getOpacity() - 0.1f : element.getOpacity() + 0.1f;
            element.setOpacity(value);
        }
        KalanHudLayoutStore.save();
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (selected == null && !Character.isISOControl(chr) && search.length() < 32) {
            search += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (selected != null) {
                selected = null;
            } else {
                close();
            }
            return true;
        }
        if (selected == null && keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        selected = null;
        client.setScreen(null);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static void rounded(DrawContext context, int x, int y,
                                int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0) return;
        int r = Math.max(1, Math.min(radius, Math.min(width, height) / 2));
        context.fill(x + r, y, x + width - r, y + height, color);
        for (int row = 0; row < r; row++) {
            double d = r - row - 0.5;
            int inset = r - (int) Math.floor(Math.sqrt(r * r - d * d));
            context.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
            context.fill(x + inset, y + height - row - 1,
                    x + width - inset, y + height - row, color);
        }
    }
}
