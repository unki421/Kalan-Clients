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
    private static final int OUTER_GLOW = 0x428B5CF6;
    private static final int WINDOW_BORDER = 0xFF7D4CE6;
    private static final int WINDOW_BG = 0xEC0D0918;
    private static final int HEADER_BG = 0xF2140E24;
    private static final int SIDEBAR_BG = 0xEE110C20;
    private static final int SEARCH_BG = 0xFF090611;
    private static final int CARD_BG = 0xE8140F24;
    private static final int CARD_HOVER = 0xF21C1530;
    private static final int CARD_BORDER = 0xC27446DC;
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

        // Фиолетовое свечение вокруг окна и основная панель
        rounded(c, x - 3, y - 3, w + 6, h + 6, 16, OUTER_GLOW);
        rounded(c, x, y, w, h, 13, WINDOW_BORDER);
        rounded(c, x + 1, y + 1, w - 2, h - 2, 12, WINDOW_BG);

        // Шапка окна
        rounded(c, x + 1, y + 1, w - 2, headerH, 12, HEADER_BG);
        c.fill(x + 1, y + headerH - 6, x + w - 1, y + headerH + 1, HEADER_BG);
        c.fill(x + 1, y + headerH, x + w - 1, y + headerH + 1, LINE);

        // Левый сайдбар категорий
        rounded(c, x + 1, y + headerH + 1, sw, h - headerH - 2, 11, SIDEBAR_BG);
        c.fill(x + sw - 6, y + headerH + 1, x + sw + 1, y + h - 1, SIDEBAR_BG);
        c.fill(x + sw, y + headerH + 1, x + sw + 1, y + h - 1, LINE);

        // Логотип + Kalan Client
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

        rounded(c, searchX, searchY, searchW, searchH, 12,
                searchFocused ? ACCENT_LIGHT : 0x775E3BA6);
        rounded(c, searchX + 1, searchY + 1, searchW - 2, searchH - 2, 11, SEARCH_BG);
        txt(c, "⌕", searchX + 10, searchY + 8, ACCENT_LIGHT);

        String shown = search.isEmpty() ? "Search..." : search;
        txt(c, shown, searchX + 24, searchY + 8, search.isEmpty() ? 0xFF796D96 : WHITE);
        if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
            int caretX = searchX + 24 + textW(search);
            c.fill(caretX, searchY + 6, caretX + 1, searchY + 18, ACCENT_LIGHT);
        }

        // Кнопка закрытия [×]
        boolean closeHover = KalanClickGuiLayout.contains(mx, my, closeX, closeY, closeSize, closeSize);
        rounded(c, closeX, closeY, closeSize, closeSize, 7,
                closeHover ? ACCENT_LIGHT : 0x665E3BA6);
        rounded(c, closeX + 1, closeY + 1, closeSize - 2, closeSize - 2, 6,
                closeHover ? 0xFF2A1A4A : 0xFF181129);
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

            rounded(c, ax, ay, cardW, CARD_H, 10,
                    hover || enabled ? CARD_BORDER_ACTIVE : CARD_BORDER);
            rounded(c, ax + 1, ay + 1, cardW - 2, CARD_H - 2, 9,
                    hover ? CARD_HOVER : CARD_BG);

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
        rounded(c, x + 3, y, 3, 5, 1, WHITE);
        rounded(c, x + 9, y + 5, 3, 5, 1, WHITE);
        rounded(c, x + 5, y + 10, 3, 5, 1, WHITE);
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
```[cite: 3, 4]

### 2. `KalanHudRenderer.java`

```java
package dev.kalan.client;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class KalanHudRenderer {

    private static final Identifier LAYER_ID =
            Identifier.of(KalanClient.MOD_ID, "watermark_hud");

    private static final DateTimeFormatter CLOCK_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final int GLOW = 0x3A8B5CF6;
    private static final int BACKGROUND = 0xE8100B1E;
    private static final int OUTLINE = 0xCC7C4CE6;
    private static final int ACCENT = 0xFF9E6CFF;
    private static final int ACCENT_FILL = 0xFF8338EC;
    private static final int WHITE = 0xFFF7F4FF;
    private static final int MUTED = 0xFFA498C2;
    private static final int DIVIDER = 0x556441A5;
    private static final int KEY_BG = 0xEC171129;
    private static final int KEY_BORDER = 0x99693DC4;
    private static final int KEY_ACTIVE = 0xFF532A94;

    private static boolean initialized;

    private KalanHudRenderer() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        HudLayerRegistrationCallback.EVENT.register(layeredDrawer ->
                layeredDrawer.attachLayerBefore(
                        IdentifiedLayer.CHAT,
                        LAYER_ID,
                        KalanHudRenderer::render
                )
        );

        initialized = true;
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null
                || client.options.hudHidden
                || client.player == null
                || client.world == null) {
            return;
        }

        renderWatermark(client, context);
        renderInfo(client, context);
        renderCoordinates(client, context);
        renderKeystrokes(client, context);
        renderMouse(client, context);
        renderPotions(client, context);
        renderArmor(client, context);
        renderTargetHud(client, context);
        renderArrayList(client, context);
    }

    private static void renderWatermark(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.WATERMARK);
        if (element == null || !element.isVisible()) {
            return;
        }

        String username = client.getSession().getUsername();
        if (username == null || username.isBlank()) {
            username = client.player.getName().getString();
        }

        String fpsText = "FPS: " + client.getCurrentFps();
        String pingText = "Ping: " + getPingText(client);
        String timeText = "Time: " + LocalTime.now().format(CLOCK_FORMAT);

        int fpsW = client.textRenderer.getWidth(fpsText) + 14;
        int pingW = client.textRenderer.getWidth(pingText) + 14;
        int timeW = client.textRenderer.getWidth(timeText) + 14;
        int pillsTotalW = fpsW + pingW + timeW + 8;

        int topCardW = Math.max(
                pillsTotalW,
                Math.max(
                        client.textRenderer.getWidth("Kalan Client") + 40,
                        client.textRenderer.getWidth("Player: " + username) + 40
                )
        );

        int width = Math.max(topCardW, pillsTotalW);
        int height = 55;

        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, topCardW, 33, 9, opacity);
        drawLogoMark(context, 10, 10, opacity);
        context.drawTextWithShadow(client.textRenderer, "Kalan Client", 30, 6,
                withOpacity(WHITE, opacity));
        context.drawTextWithShadow(client.textRenderer, "Player: ", 30, 18,
                withOpacity(MUTED, opacity));
        int labelW = client.textRenderer.getWidth("Player: ");
        context.drawTextWithShadow(client.textRenderer, username, 30 + labelW, 18,
                withOpacity(WHITE, opacity));

        int pillY = 37;
        int pillH = 18;
        drawCard(context, 0, pillY, fpsW, pillH, 6, opacity);
        context.drawTextWithShadow(client.textRenderer, fpsText, 7, pillY + 5,
                withOpacity(WHITE, opacity));

        int pingX = fpsW + 4;
        drawCard(context, pingX, pillY, pingW, pillH, 6, opacity);
        context.drawTextWithShadow(client.textRenderer, pingText, pingX + 7, pillY + 5,
                withOpacity(WHITE, opacity));

        int timeX = pingX + pingW + 4;
        drawCard(context, timeX, pillY, timeW, pillH, 6, opacity);
        context.drawTextWithShadow(client.textRenderer, timeText, timeX + 7, pillY + 5,
                withOpacity(WHITE, opacity));

        context.getMatrices().pop();
    }

    private static void drawLogoMark(DrawContext context, int x, int y, float opacity) {
        context.fill(x, y + 1, x + 14, y + 3, withOpacity(WHITE, opacity));
        context.fill(x + 2, y + 4, x + 13, y + 6, withOpacity(ACCENT, opacity));
        context.fill(x + 4, y + 7, x + 11, y + 9, withOpacity(WHITE, opacity));
        context.fill(x + 6, y + 10, x + 9, y + 13, withOpacity(ACCENT, opacity));
    }

    private static void renderInfo(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.INFO);
        if (element == null || !element.isVisible()) {
            return;
        }

        String line1 = "HP: " + Math.round(client.player.getHealth())
                + "/" + Math.round(client.player.getMaxHealth());
        String line2 = "Hunger: " + client.player.getHungerManager().getFoodLevel();

        int width = Math.max(106, Math.max(
                client.textRenderer.getWidth(line1),
                client.textRenderer.getWidth(line2)
        ) + 20);
        int height = 34;

        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 8, opacity);
        context.drawTextWithShadow(client.textRenderer, line1, 9, 6, withOpacity(WHITE, opacity));
        context.drawTextWithShadow(client.textRenderer, line2, 9, 19, withOpacity(MUTED, opacity));

        context.getMatrices().pop();
    }

    private static void renderCoordinates(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.COORDINATES);
        if (element == null || !element.isVisible()) {
            return;
        }

        String coordsVal = client.player.getBlockX()
                + " " + client.player.getBlockY()
                + " " + client.player.getBlockZ();
        String facingVal = client.player.getHorizontalFacing().getName().toUpperCase();

        int width = Math.max(
                134,
                Math.max(
                        client.textRenderer.getWidth("XYZ: " + coordsVal),
                        client.textRenderer.getWidth("Facing: " + facingVal)
                ) + 28
        );
        int height = 35;

        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 8, opacity);

        context.drawTextWithShadow(client.textRenderer, "⌖", 9, 6, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "XYZ: ", 21, 6, withOpacity(MUTED, opacity));
        int xyzLabelW = client.textRenderer.getWidth("XYZ: ");
        context.drawTextWithShadow(client.textRenderer, coordsVal, 21 + xyzLabelW, 6,
                withOpacity(WHITE, opacity));

        context.drawTextWithShadow(client.textRenderer, "➤", 9, 19, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "Facing: ", 21, 19, withOpacity(MUTED, opacity));
        int faceLabelW = client.textRenderer.getWidth("Facing: ");
        context.drawTextWithShadow(client.textRenderer, facingVal, 21 + faceLabelW, 19,
                withOpacity(WHITE, opacity));

        context.getMatrices().pop();
    }

    private static void renderKeystrokes(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.KEYSTROKES);
        if (element == null || !element.isVisible()) {
            return;
        }

        int width = 92;
        int height = 92;

        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 9, opacity);
        context.drawTextWithShadow(client.textRenderer, "⌨", 8, 5, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "Keystrokes", 20, 5, withOpacity(WHITE, opacity));

        drawKeyTile(context, client, "W", 37, 21, 18, 17,
                client.options.forwardKey.isPressed(), opacity);
        drawKeyTile(context, client, "A", 16, 41, 18, 17,
                client.options.leftKey.isPressed(), opacity);
        drawKeyTile(context, client, "S", 37, 41, 18, 17,
                client.options.backKey.isPressed(), opacity);
        drawKeyTile(context, client, "D", 58, 41, 18, 17,
                client.options.rightKey.isPressed(), opacity);

        drawKeyTile(context, client, "LMB", 8, 62, 36, 18,
                client.options.attackKey.isPressed(), opacity);
        drawKeyTile(context, client, "RMB", 48, 62, 36, 18,
                client.options.useKey.isPressed(), opacity);

        context.getMatrices().pop();
    }

    private static void drawKeyTile(
            DrawContext context,
            MinecraftClient client,
            String label,
            int x,
            int y,
            int w,
            int h,
            boolean pressed,
            float opacity
    ) {
        drawRoundedPanel(context, x, y, w, h, 4,
                withOpacity(pressed ? ACCENT : KEY_BORDER, opacity));
        drawRoundedPanel(context, x + 1, y + 1, w - 2, h - 2, 3,
                withOpacity(pressed ? KEY_ACTIVE : KEY_BG, opacity));

        int textW = client.textRenderer.getWidth(label);
        context.drawTextWithShadow(
                client.textRenderer,
                label,
                x + (w - textW) / 2,
                y + (h - client.textRenderer.fontHeight) / 2 + 1,
                withOpacity(pressed ? WHITE : MUTED, opacity)
        );
    }

    private static void renderMouse(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.MOUSE);
        if (element == null || !element.isVisible()) {
            return;
        }

        int width = 92;
        int height = 30;

        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 8, opacity);
        drawKeyTile(context, client, "LMB", 7, 6, 37, 18,
                client.options.attackKey.isPressed(), opacity);
        drawKeyTile(context, client, "RMB", 48, 6, 37, 18,
                client.options.useKey.isPressed(), opacity);

        context.getMatrices().pop();
    }

    private static void renderPotions(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.POTIONS);
        if (element == null || !element.isVisible()) {
            return;
        }

        List<String[]> rows = new ArrayList<>();
        for (StatusEffectInstance effect : client.player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString()
                    + getAmplifierText(effect.getAmplifier());
            String duration = formatDuration(effect.getDuration());
            rows.add(new String[]{name, duration});
        }

        if (rows.isEmpty()) {
            rows.add(new String[]{"Нет эффектов", ""});
        }

        int visible = Math.min(rows.size(), 6);
        int width = 148;
        for (int i = 0; i < visible; i++) {
            int rowW = client.textRenderer.getWidth(rows.get(i)[0])
                    + client.textRenderer.getWidth(rows.get(i)[1]) + 34;
            width = Math.max(width, Math.min(210, rowW));
        }

        int height = 24 + visible * 14;
        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 9, opacity);
        context.drawTextWithShadow(client.textRenderer, "⚗", 9, 5, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "Potions", 20, 5, withOpacity(WHITE, opacity));
        context.fill(8, 16, width - 8, 17, withOpacity(DIVIDER, opacity));

        for (int i = 0; i < visible; i++) {
            int rowY = 20 + i * 14;
            String name = rows.get(i)[0];
            String time = rows.get(i)[1];

            context.drawTextWithShadow(client.textRenderer, "•", 9, rowY, withOpacity(ACCENT, opacity));
            context.drawTextWithShadow(client.textRenderer, name, 18, rowY, withOpacity(WHITE, opacity));
            if (!time.isEmpty()) {
                int timeW = client.textRenderer.getWidth(time);
                context.drawTextWithShadow(client.textRenderer, time,
                        width - timeW - 9, rowY, withOpacity(MUTED, opacity));
            }
        }

        context.getMatrices().pop();
    }

    private static void renderArmor(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.ARMOR);
        if (element == null || !element.isVisible()) {
            return;
        }

        int width = 108;
        int height = 43;
        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 8, opacity);
        context.drawTextWithShadow(client.textRenderer, "🛡", 8, 5, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "Armor", 20, 5, withOpacity(WHITE, opacity));

        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = client.player.getEquippedStack(slots[i]);
            int slotX = 8 + i * 24;
            drawRoundedPanel(context, slotX, 17, 20, 20, 4, withOpacity(KEY_BG, opacity));

            if (!stack.isEmpty()) {
                context.drawItem(stack, slotX + 2, 19);

                if (stack.isDamageable()) {
                    float remaining = 1.0f - (float) stack.getDamage() / stack.getMaxDamage();
                    int barColor = remaining > 0.5f ? 0xFF7CE3A0
                            : remaining > 0.25f ? 0xFFFFD166 : 0xFFFF6B8B;
                    context.fill(slotX + 2, 36, slotX + 18, 38, withOpacity(DIVIDER, opacity));
                    context.fill(slotX + 2, 36,
                            slotX + 2 + Math.round(16 * remaining), 38,
                            withOpacity(barColor, opacity));
                }
            }
        }

        context.getMatrices().pop();
    }

    private static void renderTargetHud(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.TARGET_HUD);
        if (element == null || !element.isVisible()) {
            return;
        }

        LivingEntity target = null;
        if (client.targetedEntity instanceof LivingEntity living) {
            target = living;
        } else if (client.player.getAttacking() instanceof LivingEntity attacking) {
            target = attacking;
        }

        if (target == null) {
            return;
        }

        String name = target.getName().getString();
        float hp = Math.max(0.0f, target.getHealth());
        String hpText = String.format("%.1f HP", hp);

        int width = Math.max(124, client.textRenderer.getWidth(name) + 52);
        int height = 50;

        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 9, opacity);
        context.drawTextWithShadow(client.textRenderer, "⚔", 8, 5, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "Target HUD", 19, 5, withOpacity(WHITE, opacity));

        drawRoundedPanel(context, 8, 17, 25, 25, 4, withOpacity(KEY_BG, opacity));
        if (target instanceof AbstractClientPlayerEntity playerTarget) {
            PlayerSkinDrawer.draw(context, playerTarget.getSkinTextures(), 9, 18, 23);
        } else {
            String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
            int iw = client.textRenderer.getWidth(initial);
            context.drawTextWithShadow(client.textRenderer, initial, 8 + (25 - iw) / 2, 26,
                    withOpacity(ACCENT, opacity));
        }

        context.drawTextWithShadow(client.textRenderer, name, 38, 18, withOpacity(WHITE, opacity));
        context.drawTextWithShadow(client.textRenderer, hpText, 38, 29, withOpacity(MUTED, opacity));

        float maxHp = Math.max(1.0f, target.getMaxHealth());
        float ratio = Math.max(0.0f, Math.min(1.0f, hp / maxHp));
        int barW = width - 46;
        roundedBar(context, 38, 39, barW, 4, withOpacity(DIVIDER, opacity));
        roundedBar(context, 38, 39, Math.max(4, Math.round(barW * ratio)), 4,
                withOpacity(ACCENT_FILL, opacity));

        context.getMatrices().pop();
    }

    private static void renderArrayList(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.ARRAY_LIST);
        if (element == null || !element.isVisible()) {
            return;
        }

        List<String> enabled = new ArrayList<>();
        for (KalanHudPanel panel : KalanHudPanel.values()) {
            if (panel == KalanHudPanel.ARRAY_LIST) {
                continue;
            }

            KalanHudElement panelElement = KalanHudRegistry.find(panel);
            if (panelElement != null && panelElement.isVisible()) {
                enabled.add(panel.getTitle());
            }
        }

        enabled.sort((a, b) -> Integer.compare(
                client.textRenderer.getWidth(b),
                client.textRenderer.getWidth(a)
        ));

        int maxShown = 5;
        int shownCount = Math.min(enabled.size(), maxShown);
        int extraCount = Math.max(0, enabled.size() - maxShown);
        String moreText = extraCount > 0 ? "+ " + extraCount + " more..." : null;

        int width = 108;
        for (int i = 0; i < shownCount; i++) {
            width = Math.max(width, client.textRenderer.getWidth(enabled.get(i)) + 28);
        }
        if (moreText != null) {
            width = Math.max(width, client.textRenderer.getWidth(moreText) + 18);
        }

        int height = 24 + shownCount * 14 + (moreText != null ? 13 : 0);
        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 9, opacity);
        context.drawTextWithShadow(client.textRenderer, "≣", 9, 5, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "ArrayList", 20, 5, withOpacity(WHITE, opacity));
        context.fill(8, 16, width - 8, 17, withOpacity(DIVIDER, opacity));

        for (int i = 0; i < shownCount; i++) {
            String name = enabled.get(i);
            int rowY = 20 + i * 14;
            context.drawTextWithShadow(client.textRenderer, "◆", 9, rowY,
                    withOpacity(ACCENT, opacity));
            context.drawTextWithShadow(client.textRenderer, name, 20, rowY,
                    withOpacity(WHITE, opacity));
        }

        if (moreText != null) {
            int moreY = 20 + shownCount * 14;
            int moreW = client.textRenderer.getWidth(moreText);
            context.drawTextWithShadow(client.textRenderer, moreText,
                    width - moreW - 8, moreY, withOpacity(MUTED, opacity));
        }

        context.getMatrices().pop();
    }

    private static String getAmplifierText(int amplifier) {
        String[] levels = {"", " II", " III", " IV", " V", " VI"};
        if (amplifier >= 0 && amplifier < levels.length) {
            return amplifier == 0 ? " I" : levels[amplifier];
        }
        return " " + (amplifier + 1);
    }

    private static String formatDuration(int ticks) {
        if (ticks < 0) {
            return "∞";
        }

        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private static String getPingText(MinecraftClient client) {
        if (client.getNetworkHandler() == null || client.player == null) {
            return "—";
        }

        PlayerListEntry entry = client.getNetworkHandler()
                .getPlayerListEntry(client.player.getUuid());

        if (entry == null) {
            return client.isInSingleplayer() ? "LOCAL" : "—";
        }

        return entry.getLatency() + " ms";
    }

    private static void prepareElement(
            KalanHudElement element,
            DrawContext context,
            int width,
            int height
    ) {
        element.setSize(width, height);
        element.clampToScreen(
                context.getScaledWindowWidth(),
                context.getScaledWindowHeight()
        );
    }

    private static void drawCard(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int radius,
            float opacity
    ) {
        drawRoundedPanel(context, x - 1, y - 1, width + 2, height + 2,
                radius + 1, withOpacity(GLOW, opacity));
        drawRoundedPanel(context, x, y, width, height,
                radius, withOpacity(OUTLINE, opacity));
        drawRoundedPanel(context, x + 1, y + 1, width - 2, height - 2,
                Math.max(1, radius - 1), withOpacity(BACKGROUND, opacity));
    }

    private static void roundedBar(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        drawRoundedPanel(context, x, y, width, height, height / 2, color);
    }

    private static void drawRoundedPanel(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int radius,
            int color
    ) {
        if (width <= 0 || height <= 0) {
            return;
        }

        int safeRadius = Math.max(1, Math.min(radius, Math.min(width, height) / 2));

        context.fill(
                x + safeRadius,
                y,
                x + width - safeRadius,
                y + height,
                color
        );

        for (int row = 0; row < safeRadius; row++) {
            double distance = safeRadius - row - 0.5;
            int inset = safeRadius - (int) Math.floor(
                    Math.sqrt(safeRadius * safeRadius - distance * distance)
            );

            context.fill(
                    x + inset,
                    y + row,
                    x + width - inset,
                    y + row + 1,
                    color
            );

            context.fill(
                    x + inset,
                    y + height - row - 1,
                    x + width - inset,
                    y + height - row,
                    color
            );
        }
    }

    private static int withOpacity(int color, float opacity) {
        int originalAlpha = (color >>> 24) & 0xFF;
        int alpha = Math.max(0, Math.min(255, Math.round(originalAlpha * opacity)));
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
