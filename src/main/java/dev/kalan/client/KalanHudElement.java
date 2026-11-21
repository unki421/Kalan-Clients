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
}
