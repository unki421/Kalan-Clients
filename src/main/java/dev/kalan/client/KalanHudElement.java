package dev.kalan.client;

public final class KalanHudElement {

    private final String id;
    private final KalanHudSettings settings;

    private int x;
    private int y;

    public KalanHudElement(String id, int x, int y) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("HUD element id cannot be empty");
        }

        this.id = id;
        this.x = x;
        this.y = y;
        this.settings = new KalanHudSettings();
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

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
