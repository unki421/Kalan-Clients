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
    // Logical dimensions are derived from the supplied 1800x1013 reference:
    // the window occupies about 50% of screen width and 54% of screen height.
    private static final int W = 448;
    private static final int H = 273;
    private static final int OVERLAY = 0x9906030D;
    private static final int PANEL = 0xF50B0814;
    private static final int SIDEBAR = 0xFF100B1B;
    private static final int CARD = 0xFF171320;
    private static final int CARD_TOP = 0xFF211B2B;
    private static final int PURPLE = 0xFF963CFF;
    private static final int PURPLE_LIGHT = 0xFFB77BFF;
    private static final int WHITE = 0xFFF5F0FF;
    private static final int MUTED = 0xFFAAA0B9;
    private static final int LINE = 0xFF39264E;

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private KalanClickGuiCategory category = KalanClickGuiCategory.HUD;
    private KalanClickGuiEntry selected;
    private String search = "";
    private int scroll;
    private boolean searchFocused;

    public KalanClickGuiScreen() {
        super(Text.literal("Kalan Client"));
    }

    private int px() {
        return Math.min(W, Math.max(320, width - 16));
    }

    private int py() {
        return Math.min(H, Math.max(240, height - 16));
    }

    private int left() { return (width - px()) / 2; }
    private int top() { return (height - py()) / 2; }
    private int side() { return Math.max(52, Math.min(60, px() / 8)); }

    private void txt(DrawContext c, String s, int x, int y, int color) {
        c.drawTextWithShadow(mc.textRenderer, s, x, y, color);
    }

    private int textW(String s) {
        return mc.textRenderer.getWidth(s);
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, OVERLAY);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);

        int x = left();
        int y = top();
        int w = px();
        int h = py();
        int sw = side();
        int cx = x + sw + 17;
        int cw = x + w - 12 - cx;

        rounded(c, x, y, w, h, 13, 0xFF6D36B1);
        rounded(c, x + 1, y + 1, w - 2, h - 2, 12, PANEL);

        // Compact brand block and top search row mirror the reference proportions.
        rounded(c, x + 7, y + 7, sw + 31, 39, 10, 0xFF21142F);
        txt(c, "◢", x + 15, y + 15, PURPLE_LIGHT);
        txt(c, "Kalan", x + 34, y + 10, WHITE);
        txt(c, "Client", x + 34, y + 23, PURPLE_LIGHT);

        int searchW = Math.max(100, cw - 43);
        rounded(c, cx, y + 9, searchW, 32, 16, 0xFF09070F);
        rounded(c, cx + 1, y + 10, searchW - 2, 30, 15,
                searchFocused ? 0xFF21132F : 0xFF0B0912);
        txt(c, "⌕", cx + 10, y + 17, PURPLE_LIGHT);
        String shown = search.isEmpty() ? "Search..." : search;
        txt(c, shown, cx + 31, y + 18, search.isEmpty() ? 0xFF777080 : WHITE);
        if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
            int caretX = cx + 31 + textW(search);
            c.fill(caretX, y + 16, caretX + 1, y + 31, PURPLE_LIGHT);
        }

        rounded(c, x + w - 39, y + 9, 29, 32, 9, 0xFF21152E);
        txt(c, "×", x + w - 30, y + 17, PURPLE_LIGHT);

        // Left icon rail.
        rounded(c, x + 7, y + 51, sw + 1, h - 59, 11, SIDEBAR);
        int idx = 0;
        for (KalanClickGuiCategory cat : KalanClickGuiCategory.values()) {
            int by = y + 59 + idx * 44;
            boolean active = category == cat;
            if (active) {
                rounded(c, x + 12, by, sw - 8, 37, 9, 0xFF43216D);
            }
            String symbol = switch (cat) {
                case HUD -> "◇";
                case COMBAT -> "⚒";
                case SETTINGS -> "♟";
            };
            int tw = textW(symbol);
            txt(c, symbol, x + 12 + (sw - 8 - tw) / 2, by + 10,
                    active ? PURPLE_LIGHT : 0xFF9870C8);
            idx++;
        }

        c.fill(x + sw + 10, y + 48, x + w - 9, y + 49, LINE);

        if (selected != null) {
            renderSettings(c, cx, y, cw, h);
        } else {
            renderCards(c, cx, y, cw, h, mx, my);
        }
    }

    private void renderCards(DrawContext c, int x, int y, int cw, int h, int mouseX, int mouseY) {
        List<KalanClickGuiEntry> entries = visibleEntries();
        if (entries.isEmpty()) {
            txt(c, "Ничего не найдено", x + 12, y + 66, MUTED);
            return;
        }

        int gap = 8;
        int cols = cw >= 330 ? 3 : cw >= 220 ? 2 : 1;
        int cardW = (cw - gap * (cols - 1)) / cols;
        int cardH = 48;
        int start = y + 52;
        int bottom = y + h - 5;

        for (int i = 0; i < entries.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int ax = x + col * (cardW + gap);
            int ay = start + row * (cardH + gap) - scroll;
            if (ay + cardH < y + 49 || ay > bottom) {
                continue;
            }

            KalanClickGuiEntry entry = entries.get(i);
            boolean enabled = entry.isEnabled();
            boolean hover = KalanClickGuiLayout.contains(mouseX, mouseY, ax, ay, cardW, cardH);

            rounded(c, ax, ay, cardW, cardH, 10,
                    hover ? PURPLE_LIGHT : 0xFF7545A8);
            rounded(c, ax + 1, ay + 1, cardW - 2, cardH - 2, 9, CARD);
            rounded(c, ax + 4, ay + 4, cardW - 8, 20, 7, CARD_TOP);

            String title = trim(entry.getTitle(), cardW - 18);
            txt(c, title, ax + (cardW - textW(title)) / 2, ay + 9, WHITE);

            // Toggle on the lower-left and settings affordance on the lower-right.
            rounded(c, ax + 8, ay + 29, 29, 12, 7,
                    enabled ? PURPLE : 0xFF393342);
            rounded(c, ax + (enabled ? 25 : 10), ay + 31, 8, 8, 5, 0xFFFFFFFF);
            drawSettingsIcon(c, ax + cardW - 25, ay + 29, PURPLE_LIGHT);
        }

        int rows = (entries.size() + cols - 1) / cols;
        int visibleHeight = bottom - start;
        int maxScroll = Math.max(0, rows * (cardH + gap) - visibleHeight);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
    }

    private void drawSettingsIcon(DrawContext c, int x, int y, int color) {
        // Three real slider tracks, matching the reference's settings glyph.
        c.fill(x, y + 2, x + 14, y + 4, color);
        c.fill(x, y + 7, x + 14, y + 9, color);
        c.fill(x, y + 12, x + 14, y + 14, color);
        rounded(c, x + 3, y, 4, 6, 2, WHITE);
        rounded(c, x + 9, y + 5, 4, 6, 2, WHITE);
        rounded(c, x + 5, y + 10, 4, 6, 2, WHITE);
    }

    private void renderSettings(DrawContext c, int x, int y, int cw, int h) {
        KalanHudElement element = KalanHudRegistry.find(selected.getPanel());
        if (element == null) {
            selected = null;
            return;
        }

        txt(c, "‹", x + 8, y + 59, PURPLE_LIGHT);
        txt(c, selected.getTitle(), x + 28, y + 59, WHITE);
        txt(c, "НАСТРОЙКИ ЭЛЕМЕНТА", x + 8, y + 83, MUTED);
        drawSlider(c, x, y + 111, cw, "Масштаб", element.getScale(),
                0.5f, 2.0f, String.format(Locale.ROOT, "%.1f", element.getScale()));
        drawSlider(c, x, y + 174, cw, "Прозрачность", element.getOpacity(),
                0.1f, 1.0f, Math.round(element.getOpacity() * 100) + "%");
        txt(c, "Клик по шкале меняет значение", x + 8, y + 228, MUTED);
        txt(c, "ПКМ / ESC — назад", x + 8, y + h - 18, PURPLE_LIGHT);
    }

    private void drawSlider(DrawContext c, int x, int y, int cw, String label,
                            float value, float min, float max, String valueText) {
        txt(c, label, x + 8, y, WHITE);
        txt(c, valueText, x + cw - 38, y, PURPLE_LIGHT);
        rounded(c, x + 8, y + 20, cw - 16, 7, 4, 0xFF393141);
        int fill = Math.round((cw - 16) * (value - min) / (max - min));
        rounded(c, x + 8, y + 20, Math.max(4, fill), 7, 4, PURPLE);
        rounded(c, x + 4 + fill, y + 16, 15, 15, 8, WHITE);
    }

    private List<KalanClickGuiEntry> visibleEntries() {
        List<KalanClickGuiEntry> source = switch (category) {
            case HUD -> KalanClickGuiEntries.getHudEntries();
            case COMBAT -> KalanClickGuiEntries.getCombatEntries();
            case SETTINGS -> KalanClickGuiEntries.getAll();
        };

        if (search.isBlank()) {
            return source;
        }

        List<KalanClickGuiEntry> result = new ArrayList<>();
        String query = search.toLowerCase(Locale.ROOT);
        for (KalanClickGuiEntry entry : source) {
            if (entry.getTitle().toLowerCase(Locale.ROOT).contains(query)
                    || entry.getDescription().toLowerCase(Locale.ROOT).contains(query)) {
                result.add(entry);
            }
        }
        return result;
    }

    private String trim(String value, int maxWidth) {
        if (textW(value) <= maxWidth) {
            return value;
        }
        while (!value.isEmpty() && textW(value + "…") > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "…";
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x = left();
        int y = top();
        int w = px();
        int h = py();
        int sw = side();
        int cx = x + sw + 17;
        int cw = x + w - 12 - cx;

        if (button == 0 && mx >= x + w - 42 && my >= y + 6 && my <= y + 44) {
            close();
            return true;
        }

        int searchW = Math.max(100, cw - 43);
        if (button == 0 && mx >= cx && mx <= cx + searchW && my >= y + 8 && my <= y + 43) {
            searchFocused = true;
            selected = null;
            return true;
        }
        if (button == 0) {
            searchFocused = false;
        }

        int index = 0;
        for (KalanClickGuiCategory cat : KalanClickGuiCategory.values()) {
            int by = y + 59 + index * 44;
            if (button == 0 && KalanClickGuiLayout.contains((int) mx, (int) my,
                    x + 12, by, sw - 8, 37)) {
                category = cat;
                selected = null;
                scroll = 0;
                return true;
            }
            index++;
        }

        if (selected != null) {
            if (button == 1 || (button == 0 && mx < cx + 25 && my < y + 76)) {
                selected = null;
                return true;
            }
            if (button == 0) {
                KalanHudElement element = KalanHudRegistry.find(selected.getPanel());
                if (element != null) {
                    if (my >= y + 127 && my <= y + 151) {
                        element.setScale(element.getScale()
                                + (mx < cx + cw / 2 ? -0.1f : 0.1f));
                    } else if (my >= y + 190 && my <= y + 214) {
                        element.setOpacity(element.getOpacity()
                                + (mx < cx + cw / 2 ? -0.1f : 0.1f));
                    }
                    KalanHudLayoutStore.save();
                }
                return true;
            }
            return true;
        }

        List<KalanClickGuiEntry> entries = visibleEntries();
        int gap = 8;
        int cols = cw >= 330 ? 3 : cw >= 220 ? 2 : 1;
        int cardW = (cw - gap * (cols - 1)) / cols;
        int cardH = 48;
        int start = y + 52;

        for (int n = 0; n < entries.size(); n++) {
            int ax = cx + (n % cols) * (cardW + gap);
            int ay = start + (n / cols) * (cardH + gap) - scroll;
            if (!KalanClickGuiLayout.contains((int) mx, (int) my, ax, ay, cardW, cardH)) {
                continue;
            }

            KalanClickGuiEntry entry = entries.get(n);
            if (button == 1 || (button == 0 && mx >= ax + cardW - 23 && my >= ay + 25)) {
                selected = entry;
                return true;
            }
            if (button == 0) {
                entry.toggle();
                return true;
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        if (mx < left() + side() + 12) {
            return super.mouseScrolled(mx, my, horizontal, vertical);
        }
        scroll = Math.max(0, Math.min(2000, scroll - (int) (vertical * 28)));
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused && !Character.isISOControl(chr) && search.length() < 32) {
            search += chr;
            scroll = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (selected != null) {
                selected = null;
            } else if (searchFocused) {
                searchFocused = false;
            } else {
                close();
            }
            return true;
        }
        if (searchFocused && key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            scroll = 0;
            return true;
        }
        if (searchFocused && key == GLFW.GLFW_KEY_ENTER) {
            searchFocused = false;
            return true;
        }
        return super.keyPressed(key, scan, modifiers);
    }

    @Override
    public void close() {
        selected = null;
        mc.setScreen(null);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static void rounded(DrawContext c, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        int radius = Math.max(1, Math.min(r, Math.min(w, h) / 2));
        c.fill(x + radius, y, x + w - radius, y + h, color);
        for (int row = 0; row < radius; row++) {
            double distance = radius - row - 0.5;
            int inset = radius - (int) Math.floor(
                    Math.sqrt(radius * radius - distance * distance));
            c.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            c.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, color);
        }
    }
}
