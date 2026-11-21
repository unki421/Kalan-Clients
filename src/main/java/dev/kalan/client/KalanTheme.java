package dev.kalan.client;

public final class KalanTheme {

    public static final int BACKGROUND = 0xFF0B0A10;
    public static final int PANEL = 0xFF15131D;
    public static final int PANEL_LIGHT = 0xFF201C2B;
    public static final int BORDER = 0xFF30283F;

    public static final int ACCENT_DEFAULT = 0xFFB46CFF;
    public static final int ACCENT_DARK = 0xFF7845B8;
    public static final int ACCENT_SOFT = 0xFFD7B8FF;

    public static final int TEXT = 0xFFF4F0FA;
    public static final int TEXT_MUTED = 0xFFAAA3B8;

    public static final int SUCCESS = 0xFF8BE0B0;
    public static final int WARNING = 0xFFFFD18A;
    public static final int ERROR = 0xFFFF8B9B;

    private KalanTheme() {
    }

    public static int accent() {
        return KalanConfig.get().getAccentColor();
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }
}
