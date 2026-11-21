
package dev.kalan.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class KalanClickGuiScreen extends Screen {

    private static final int WIDTH = 520;
    private static final int HEIGHT = 330;

    private static final int BACKGROUND = 0x6608070D;
    private static final int PANEL = 0xFF111019;
    private static final int CARD = 0xFF191622;
    private static final int ACCENT = 0xFFB46CFF;
    private static final int WHITE = 0xFFF4F0FA;
    private static final int MUTED = 0xFFAAA3B8;

    private KalanClickGuiCategory category = KalanClickGuiCategory.HUD;
    private KalanClickGuiEntry selected;

    private final MinecraftClient client = MinecraftClient.getInstance();

    public KalanClickGuiScreen() {
        super(Text.literal("Kalan Client"));
    }

    private int left() {
        return (width - WIDTH) / 2;
    }

    private int top() {
        return (height - HEIGHT) / 2;
    }

    private void text(DrawContext context, String value,
                      int x, int y, int color) {
        context.drawTextWithShadow(
                client.textRenderer,
                value,
                x,
                y,
                color
        );
    }

    @Override
    public void renderBackground(DrawContext context,
                                 int mouseX, int mouseY,
                                 float delta) {
        context.fill(0, 0, width, height, BACKGROUND);
    }

    @Override
    public void render(DrawContext context, int mouseX,
                       int mouseY, float delta) {

        // Сначала стандартная отрисовка экрана.
        // После неё рисуем интерфейс поверх неё.
        super.render(context, mouseX, mouseY, delta);

        // Полностью непрозрачный фон без размытия.
        context.fill(0, 0, width, height, BACKGROUND);

        int x = left();
        int y = top();

        // Основная панель.
        context.fill(x, y, x + WIDTH, y + HEIGHT, 0xFF0B0A10);
        context.fill(x, y, x + WIDTH, y + 3, ACCENT);

        // Боковая панель.
        context.fill(x, y + 3, x + 120, y + HEIGHT, PANEL);

        // Верхняя панель.
        context.fill(x + 120, y + 3, x + WIDTH, y + 44, 0xFF15131D);

        text(context, "KALAN", x + 18, y + 17, ACCENT);
        text(context, "CLIENT", x + 65, y + 17, WHITE);
        text(context, "Right Shift", x + 18, y + 36, MUTED);

        text(context, category.getTitle(),
                x + 140, y + 18, WHITE);

        text(context, "X", x + WIDTH - 25, y + 17, 0xFFFF8B9B);

        renderCategories(context, x, y);

        if (selected != null) {
            renderSettings(context, x, y);
        } else {
            renderEntries(context, x, y);
        }

        text(context, "Kalan Client | Fabric 1.21.4",
                x + 15, y + HEIGHT - 18, 0xFF777080);
    }

    private void renderCategories(DrawContext context, int x, int y) {
        int index = 0;

        for (KalanClickGuiCategory item : KalanClickGuiCategory.values()) {
            int buttonY = y + 65 + index * 38;
            boolean active = category == item;

            context.fill(
                    x + 10,
                    buttonY - 5,
                    x + 110,
                    buttonY + 23,
                    active ? 0xFF30203F : PANEL
            );

            if (active) {
                context.fill(
                        x + 10,
                        buttonY - 5,
                        x + 13,
                        buttonY + 23,
                        ACCENT
                );
            }

            text(
                    context,
                    item.getTitle(),
                    x + 20,
                    buttonY + 4,
                    active ? ACCENT : MUTED
            );

            index++;
        }
    }

    private void renderEntries(DrawContext context, int x, int y) {
        if (category != KalanClickGuiCategory.HUD && category != KalanClickGuiCategory.COMBAT) {
            text(
                    context,
                    "В этом разделе пока нет элементов",
                    x + 145,
                    y + 100,
                    MUTED
            );
            return;
        }

        int index = 0;

        List<KalanClickGuiEntry> entries = category == KalanClickGuiCategory.COMBAT
                ? KalanClickGuiEntries.getCombatEntries()
                : KalanClickGuiEntries.getHudEntries();

        for (KalanClickGuiEntry entry : entries) {
            int column = index % 2;
            int row = index / 2;

            int entryX = x + 140 + column * 175;
            int entryY = y + 65 + row * 65;

            boolean enabled = entry.isEnabled();

            context.fill(
                    entryX,
                    entryY,
                    entryX + 160,
                    entryY + 52,
                    CARD
            );

            context.fill(
                    entryX,
                    entryY,
                    entryX + 160,
                    entryY + 1,
                    enabled ? ACCENT : 0xFF30283F
            );

            text(
                    context,
                    entry.getTitle(),
                    entryX + 9,
                    entryY + 7,
                    WHITE
            );

            text(
                    context,
                    shorten(entry.getDescription(), 22),
                    entryX + 9,
                    entryY + 21,
                    MUTED
            );

            text(
                    context,
                    enabled ? "ВКЛ" : "ВЫКЛ",
                    entryX + 9,
                    entryY + 37,
                    enabled ? ACCENT : 0xFF777080
            );

            text(
                    context,
                    "ПКМ: настройки",
                    entryX + 75,
                    entryY + 37,
                    MUTED
            );

            index++;
        }
    }

    private String shorten(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private void renderSettings(DrawContext context, int x, int y) {
        KalanHudElement element =
                KalanHudRegistry.find(selected.getPanel());

        if (element == null) {
            selected = null;
            return;
        }

        text(context, selected.getTitle(),
                x + 145, y + 70, ACCENT);

        text(context, "Масштаб: " +
                String.format("%.1f", element.getScale()),
                x + 145, y + 105, WHITE);

        text(context, "[-] уменьшить       [+] увеличить",
                x + 145, y + 125, MUTED);

        text(context, "Прозрачность: " +
                String.format("%.1f", element.getOpacity()),
                x + 145, y + 165, WHITE);

        text(context, "[-] уменьшить       [+] увеличить",
                x + 145, y + 185, MUTED);

        text(context, "Нажми ПКМ, чтобы вернуться",
                x + 145, y + 230, MUTED);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = left();
        int y = top();

        if (button == 0 &&
                KalanClickGuiLayout.contains(
                        (int) mouseX, (int) mouseY,
                        x + WIDTH - 35, y + 5, 30, 30)) {
            close();
            return true;
        }

        int categoryIndex = 0;

        for (KalanClickGuiCategory item : KalanClickGuiCategory.values()) {
            int buttonY = y + 60 + categoryIndex * 38;

            if (KalanClickGuiLayout.contains(
                    (int) mouseX, (int) mouseY,
                    x + 10, buttonY, 100, 28)) {

                category = item;
                selected = null;
                return true;
            }

            categoryIndex++;
        }

        if (selected != null) {
            if (button == 1) {
                selected = null;
                return true;
            }

            if (button == 0) {
                changeSetting(mouseX, mouseY, x, y);
                return true;
            }

            return true;
        }

        if (category == KalanClickGuiCategory.HUD || category == KalanClickGuiCategory.COMBAT) {
            int index = 0;
            List<KalanClickGuiEntry> entries = category == KalanClickGuiCategory.COMBAT
                    ? KalanClickGuiEntries.getCombatEntries()
                    : KalanClickGuiEntries.getHudEntries();

            for (KalanClickGuiEntry entry : entries) {
                int column = index % 2;
                int row = index / 2;

                int entryX = x + 140 + column * 175;
                int entryY = y + 65 + row * 65;

                if (KalanClickGuiLayout.contains(
                        (int) mouseX, (int) mouseY,
                        entryX, entryY, 160, 52)) {

                    if (button == 0) {
                        entry.toggle();
                    } else if (button == 1) {
                        selected = entry;
                    }

                    return true;
                }

                index++;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void changeSetting(double mouseX, double mouseY,
                               int x, int y) {
        KalanHudElement element =
                KalanHudRegistry.find(selected.getPanel());

        if (element == null) {
            return;
        }

        if (mouseY >= y + 115 && mouseY <= y + 150) {
            if (mouseX < x + 260) {
                element.setScale(element.getScale() - 0.1f);
            } else {
                element.setScale(element.getScale() + 0.1f);
            }
        }

        if (mouseY >= y + 175 && mouseY <= y + 210) {
            if (mouseX < x + 260) {
                element.setOpacity(element.getOpacity() - 0.1f);
            } else {
                element.setOpacity(element.getOpacity() + 0.1f);
            }
        }

        KalanHudLayoutStore.save();
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
}
