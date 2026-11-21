
package dev.kalan.client;

public final class KalanClickGuiLayout {

    public static final int WIDTH = 460;
    public static final int HEIGHT = 300;

    public static final int SIDEBAR_WIDTH = 115;

    public static final int HEADER_HEIGHT = 42;

    public static final int ENTRY_WIDTH = 155;
    public static final int ENTRY_HEIGHT = 48;

    public static final int ENTRY_GAP = 8;

    private KalanClickGuiLayout() {
    }

    public static int getLeft(int screenWidth) {
        return (screenWidth - WIDTH) / 2;
    }

    public static int getTop(int screenHeight) {
        return (screenHeight - HEIGHT) / 2;
    }

    public static int getContentLeft(int screenWidth) {
        return getLeft(screenWidth) + SIDEBAR_WIDTH + 12;
    }

    public static int getContentTop(int screenHeight) {
        return getTop(screenHeight) + HEADER_HEIGHT + 12;
    }

    public static int getContentWidth() {
        return WIDTH - SIDEBAR_WIDTH - 24;
    }

    public static int getContentHeight() {
        return HEIGHT - HEADER_HEIGHT - 24;
    }

    public static int getEntryX(int screenWidth, int index) {
        int column = index % 2;
        return getContentLeft(screenWidth)
                + column * (ENTRY_WIDTH + ENTRY_GAP);
    }

    public static int getEntryY(int screenHeight, int index) {
        int row = index / 2;
        return getContentTop(screenHeight)
                + row * (ENTRY_HEIGHT + ENTRY_GAP);
    }

    public static boolean contains(
            int mouseX,
            int mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseY >= y
                && mouseX < x + width
                && mouseY < y + height;
    }
}
