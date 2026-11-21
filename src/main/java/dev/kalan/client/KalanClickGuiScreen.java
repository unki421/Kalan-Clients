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
    private static final int W = 540;
    private static final int H = 330;

    private static final int OVERLAY = 0x7A04020A;
    private static final int WINDOW_BORDER = 0xFF7D4CE6;
    private static final int WINDOW_BG = 0xF20D0918;
    private static final int HEADER_BG = 0xF5140E24;
    private static final int SIDEBAR_BG = 0xF2110C20;
    private static final int SEARCH_BG = 0xFF090611;
    private static final int CARD_BG = 0xF0140F24;
    private static final int CARD_HOVER = 0xF51C1530;
    private static final int CARD_BORDER = 0xCC7446DC;
    private static final int CARD_BORDER_ACTIVE = 0xFFA276FF;
    private static final int ACCENT = 0xFF8338EC;
    private static final int ACCENT_LIGHT = 0xFFBB96FF;
    private static final int TAB_ACTIVE = 0xFF47257A;
    private static final int SWITCH_OFF = 0xFF2B233D;
    private static final int WHITE = 0xFFF7F4FF;
    private static final int MUTED = 0xFF9F93BC;
    private static final int LINE = 0x66613DA3;

    private static final int CARD_GAP = 9;
    private static final int CARD_H = 54;

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
        return Math.min(W, Math.max(340, width - 24));
    }

    private int py() {
        return Math.min(H, Math.max(240, height - 24));
    }

    private int left() {
        return (width - px()) / 2;
    }

    private int top() {
        return (height - py()) / 2;
    }

    private int side() {
        return 52;
    }

    private int columns(int cw) {
        if (cw >= 330) {
            return 3;
        }
        return cw >= 210 ? 2 : 1;
    }

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
        int headerH = 44;
        int cx = x + sw + 12;
        int cw = x + w - 12 - cx;

        // Основное окно без внешнего ореола, дающего угловые артефакты
        box(c, x, y, w, h, 12, WINDOW_BG, WINDOW_BORDER);

        // Шапка и левый сайдбар внутри окна
        c.fill(x + 2, y + 2, x + w - 2, y + headerH, HEADER_BG);
        c.fill(x + 2, y + headerH, x + w - 2, y + headerH + 1, LINE);

        c.fill(x + 2, y + headerH + 1, x + sw, y + h - 2, SIDEBAR_BG);
        c.fill(x + sw, y + headerH + 1, x + sw + 1, y + h - 2, LINE);

        // Логотип Kalan Client
        drawClientMark(c, x + 11, y + 12);
        txt(c, "Kalan", x + 32, y + 11, WHITE);
        txt(c, "Client", x + 32, y + 22, ACCENT_LIGHT);

        // Поле поиска Search...
        int closeSize = 24;
        int closeX = x + w - 12 - closeSize;
        int closeY = y + (headerH - closeSize) / 2;

        int searchX = cx + 6;
        int searchW = Math.max(100, closeX - searchX - 10);
        int searchY = y + 10;
        int searchH = 24;

        box(c, searchX, searchY, searchW, searchH, 11,
                SEARCH_BG, searchFocused ? ACCENT_LIGHT : 0x775E3BA6);
        txt(c, "⌕", searchX + 10, searchY + 8, ACCENT_LIGHT);

        String shown = search.isEmpty() ? "Search..." : search;
        txt(c, shown, searchX + 24, searchY + 8, search.isEmpty() ? 0xFF796D96 : WHITE);
        if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
            int caretX = searchX + 24 + textW(search);
            c.fill(caretX, searchY + 6, caretX + 1, searchY + 18, ACCENT_LIGHT);
        }

        // Кнопка закрытия [×]
        boolean closeHover = KalanClickGuiLayout.contains(mx, my, closeX, closeY, closeSize, closeSize);
        box(c, closeX, closeY, closeSize, closeSize, 6,
                closeHover ? 0xFF2A1A4A : 0xFF181129,
                closeHover ? ACCENT_LIGHT : 0x665E3BA6);
        txt(c, "×", closeX + (closeSize - textW("×")) / 2, closeY + 8, WHITE);

        // Иконки категорий в левой колонке
        int idx = 0;
        for (KalanClickGuiCategory cat : KalanClickGuiCategory.values()) {
            int btnSize = 30;
            int bx = x + (sw - btnSize) / 2;
            int by = y + headerH + 12 + idx * 40;
            boolean active = category == cat;
            boolean hover = KalanClickGuiLayout.contains(mx, my, bx, by, btnSize, btnSize);

            if (active || hover) {
                rounded(c, bx, by, btnSize, btnSize, 8,
                        active ? TAB_ACTIVE : 0x662B1A4A);
            }

            String symbol = switch (cat) {
                case HUD -> "◇";
                case COMBAT -> "⚒";
                case SETTINGS -> "♟";
            };
            int tw = textW(symbol);
            txt(c, symbol, bx + (btnSize - tw) / 2, by + 11,
                    active ? WHITE : 0xFF8C72B8);
            idx++;
        }

        if (selected != null) {
            renderSettings(c, cx, y + headerH, cw, h - headerH);
        } else {
            renderCards(c, cx, y + headerH, cw, h - headerH, mx, my);
        }
    }

    private void drawClientMark(DrawContext c, int x, int y) {
        c.fill(x, y + 2, x + 14, y + 4, ACCENT_LIGHT);
        c.fill(x + 2, y + 5, x + 13, y + 7, ACCENT);
        c.fill(x + 4, y + 8, x + 11, y + 10, ACCENT_LIGHT);
        c.fill(x + 6, y + 11, x + 9, y + 14, WHITE);
    }

    private void renderCards(DrawContext c, int x, int bodyY, int cw, int bodyH, int mouseX, int mouseY) {
        List<KalanClickGuiEntry> entries = visibleEntries();
        if (entries.isEmpty()) {
            txt(c, "Ничего не найдено", x + 12, bodyY + 18, MUTED);
            return;
        }

        int cols = columns(cw);
        int cardW = (cw - CARD_GAP * (cols - 1)) / cols;
        int start = bodyY + 10;
        int bottom = bodyY + bodyH - 8;

        int rows = (entries.size() + cols - 1) / cols;
        int visibleHeight = Math.max(1, bottom - start);
        int maxScroll = Math.max(0, rows * (CARD_H + CARD_GAP) - CARD_GAP - visibleHeight);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        c.enableScissor(x - 2, start, x + cw + 2, bottom);
        for (int i = 0; i < entries.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int ax = x + col * (cardW + CARD_GAP);
            int ay = start + row * (CARD_H + CARD_GAP) - scroll;
            if (ay + CARD_H < start || ay > bottom) {
                continue;
            }

            KalanClickGuiEntry entry = entries.get(i);
            boolean enabled = entry.isEnabled();
            boolean hover = KalanClickGuiLayout.contains(mouseX, mouseY, ax, ay, cardW, CARD_H);

            box(c, ax, ay, cardW, CARD_H, 9,
                    hover ? CARD_HOVER : CARD_BG,
                    hover || enabled ? CARD_BORDER_ACTIVE : CARD_BORDER);

            String title = trim(entry.getTitle(), cardW - 16);
            txt(c, title, ax + (cardW - textW(title)) / 2, ay + 10, WHITE);

            c.fill(ax + 10, ay + 24, ax + cardW - 10, ay + 25, 0x33754AC9);

            int swX = ax + 10;
            int swY = ay + 31;
            int swW = 28;
            int swH = 14;
            rounded(c, swX, swY, swW, swH, 7, enabled ? ACCENT : SWITCH_OFF);
            int knobX = enabled ? (swX + swW - 12) : (swX + 2);
            rounded(c, knobX, swY + 2, 10, 10, 5, WHITE);

            drawSettingsIcon(c, ax + cardW - 24, ay + 31, hover ? WHITE : ACCENT_LIGHT);
        }
        c.disableScissor();
    }

    private void drawSettingsIcon(DrawContext c, int x, int y, int color) {
        c.fill(x, y + 2, x + 14, y + 3, color);
        c.fill(x, y + 7, x + 14, y + 8, color);
        c.fill(x, y + 12, x + 14, y + 13, color);
        c.fill(x + 3, y + 1, x + 6, y + 5, WHITE);
        c.fill(x + 9, y + 6, x + 12, y + 10, WHITE);
        c.fill(x + 5, y + 11, x + 8, y + 15, WHITE);
    }

    private void renderSettings(DrawContext c, int x, int bodyY, int cw, int bodyH) {
        KalanHudElement element = KalanHudRegistry.find(selected.getPanel());
        if (element == null) {
            selected = null;
            return;
        }

        txt(c, "‹", x + 8, bodyY + 14, ACCENT_LIGHT);
        txt(c, selected.getTitle(), x + 24, bodyY + 14, WHITE);
        txt(c, "НАСТРОЙКИ ЭЛЕМЕНТА", x + 8, bodyY + 38, MUTED);

        drawSlider(c, x, bodyY + 64, cw, "Масштаб", element.getScale(),
                0.5f, 2.0f, String.format(Locale.ROOT, "%.1f", element.getScale()));
        drawSlider(c, x, bodyY + 124, cw, "Прозрачность", element.getOpacity(),
                0.1f, 1.0f, Math.round(element.getOpacity() * 100) + "%");

        txt(c, "Клик по шкале меняет значение", x + 8, bodyY + 178, MUTED);
        txt(c, "ПКМ / ESC — назад", x + 8, bodyY + bodyH - 18, ACCENT_LIGHT);
    }

    private void drawSlider(DrawContext c, int x, int y, int cw, String label,
                            float value, float min, float max, String valueText) {
        txt(c, label, x + 8, y, WHITE);
        txt(c, valueText, x + cw - 38, y, ACCENT_LIGHT);
        int trackW = cw - 16;
        rounded(c, x + 8, y + 19, trackW, 6, 3, SWITCH_OFF);
        float ratio = Math.max(0.0f, Math.min(1.0f, (value - min) / (max - min)));
        int fill = Math.round(trackW * ratio);
        rounded(c, x + 8, y + 19, Math.max(4, fill), 6, 3, ACCENT);
        rounded(c, x + 4 + fill, y + 15, 14, 14, 7, WHITE);
    }

    private List<KalanClickGuiEntry> visibleEntries() {
        List<KalanClickGuiEntry> source = switch (category) {
            case HUD -> KalanClickGuiEntries.getHudEntries();
            case COMBAT -> KalanClickGuiEntries.getCombatEntries();
            case SETTINGS -> List.of();
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
        int headerH = 44;
        int cx = x + sw + 12;
        int cw = x + w - 12 - cx;

        int closeSize = 24;
        int closeX = x + w - 12 - closeSize;
        int closeY = y + (headerH - closeSize) / 2;
        if (button == 0 && KalanClickGuiLayout.contains((int) mx, (int) my,
                closeX, closeY, closeSize, closeSize)) {
            close();
            return true;
        }

        int searchX = cx + 6;
        int searchW = Math.max(100, closeX - searchX - 10);
        if (button == 0 && KalanClickGuiLayout.contains((int) mx, (int) my,
                searchX, y + 10, searchW, 24)) {
            searchFocused = true;
            selected = null;
            return true;
        }
        if (button == 0) {
            searchFocused = false;
        }

        int index = 0;
        for (KalanClickGuiCategory cat : KalanClickGuiCategory.values()) {
            int btnSize = 30;
            int bx = x + (sw - btnSize) / 2;
            int by = y + headerH + 12 + index * 40;
            if (button == 0 && KalanClickGuiLayout.contains((int) mx, (int) my,
                    bx, by, btnSize, btnSize)) {
                category = cat;
                selected = null;
                scroll = 0;
                return true;
            }
            index++;
        }

        int bodyY = y + headerH;
        if (selected != null) {
            if (button == 1 || (button == 0 && mx < cx + 28 && my >= bodyY && my <= bodyY + 30)) {
                selected = null;
                return true;
            }
            if (button == 0) {
                KalanHudElement element = KalanHudRegistry.find(selected.getPanel());
                if (element != null) {
                    if (my >= bodyY + 78 && my <= bodyY + 100) {
                        element.setScale(element.getScale()
                                + (mx < cx + cw / 2.0 ? -0.1f : 0.1f));
                    } else if (my >= bodyY + 138 && my <= bodyY + 160) {
                        element.setOpacity(element.getOpacity()
                                + (mx < cx + cw / 2.0 ? -0.1f : 0.1f));
                    }
                    KalanHudLayoutStore.save();
                }
                return true;
            }
            return true;
        }

        List<KalanClickGuiEntry> entries = visibleEntries();
        int cols = columns(cw);
        int cardW = (cw - CARD_GAP * (cols - 1)) / cols;
        int start = bodyY + 10;
        int bottom = y + h - 8;

        if (my < start || my > bottom) {
            return super.mouseClicked(mx, my, button);
        }

        for (int n = 0; n < entries.size(); n++) {
            int ax = cx + (n % cols) * (cardW + CARD_GAP);
            int ay = start + (n / cols) * (CARD_H + CARD_GAP) - scroll;
            if (!KalanClickGuiLayout.contains((int) mx, (int) my, ax, ay, cardW, CARD_H)) {
                continue;
            }

            KalanClickGuiEntry entry = entries.get(n);
            if (button == 1 || (button == 0 && mx >= ax + cardW - 28 && my >= ay + 26)) {
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
        if (mx < left() + side() + 8 || selected != null) {
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

    private static void box(DrawContext c, int x, int y, int w, int h, int r,
                            int fillColor, int borderColor) {
        if (w <= 0 || h <= 0) {
            return;
        }
        rounded(c, x, y, w, h, r, borderColor);
        if (w > 2 && h > 2) {
            rounded(c, x + 1, y + 1, w - 2, h - 2, Math.max(1, r - 1), fillColor);
        }
    }

    private static void rounded(DrawContext c, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        int radius = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (radius == 0) {
            c.fill(x, y, x + w, y + h, color);
            return;
        }

        c.fill(x, y + radius, x + w, y + h - radius, color);
        for (int row = 0; row < radius; row++) {
            double dy = radius - row - 0.5;
            int inset = radius - (int) Math.floor(Math.sqrt(radius * radius - dy * dy));
            int leftX = x + inset;
            int rightX = x + w - inset;
            if (rightX > leftX) {
                c.fill(leftX, y + row, rightX, y + row + 1, color);
                c.fill(leftX, y + h - row - 1, rightX, y + h - row, color);
            }
        }
    }
}
