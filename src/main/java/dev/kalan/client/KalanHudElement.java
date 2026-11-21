package dev.kalan.client;

public final class KalanHudElement {

    private final String id;
    private final KalanHudSettings settings;

    private int x;
    private int y;
    private int width = 100;
    private int height = 16;

    public KalanHudElement(String id, int x, int y) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("HUD element id cannot be empty");
        }

        this.id = id;
        this.x = Math.max(0, x);
        this.y = Math.max(0, y);
        this.settings = new KalanHudSettings();
    }

    public KalanHudElement(KalanHudPanel panel, int x, int y) {
        this(panel.getId(), x, y);
    }

    public String getId() {
        return id;
    }

    public KalanHudSettings getSettings() {
        return settings;
    }

    public boolean isVisible() {
        return settings.isVisible();
    }

    public void setVisible(boolean visible) {
        settings.setVisible(visible);
    }

    public float getScale() {
        return settings.getScale();
    }

    public void setScale(float scale) {
        settings.setScale(scale);
    }

    public float getOpacity() {
        return settings.getOpacity();
    }

    public void setOpacity(float opacity) {
        settings.setOpacity(opacity);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setPosition(int x, int y) {
        this.x = Math.max(0, x);
        this.y = Math.max(0, y);
    }

    public void setX(int x) {
        this.x = Math.max(0, x);
    }

    public void setY(int y) {
        this.y = Math.max(0, y);
    }

    public void setSize(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
    }

    public boolean contains(int mouseX, int mouseY) {
        int scaledWidth = Math.max(1, Math.round(width * getScale()));
        int scaledHeight = Math.max(1, Math.round(height * getScale()));

        return mouseX >= x
                && mouseY >= y
                && mouseX < x + scaledWidth
                && mouseY < y + scaledHeight;
    }

    public void clampToScreen(int screenWidth, int screenHeight) {
        int scaledWidth = Math.max(1, Math.round(width * getScale()));
        int scaledHeight = Math.max(1, Math.round(height * getScale()));

        x = Math.max(0, Math.min(x, Math.max(0, screenWidth - scaledWidth)));
        y = Math.max(0, Math.min(y, Math.max(0, screenHeight - scaledHeight)));
    }
}
