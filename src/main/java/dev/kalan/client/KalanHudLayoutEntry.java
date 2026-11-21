package dev.kalan.client;

public final class KalanHudLayoutEntry {

    public String id;
    public int x;
    public int y;
    public boolean visible;
    public float scale;
    public float opacity;

    public KalanHudLayoutEntry() {
    }

    public KalanHudLayoutEntry(KalanHudElement element) {
        this.id = element.getId();
        this.x = element.getX();
        this.y = element.getY();
        this.visible = element.isVisible();
        this.scale = element.getScale();
        this.opacity = element.getOpacity();
    }

    public void applyTo(KalanHudElement element) {
        if (element == null) {
            return;
        }

        element.setPosition(x, y);
        element.setVisible(visible);
        element.setScale(scale);
        element.setOpacity(opacity);
    }
}
