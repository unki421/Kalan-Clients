package dev.kalan.client;

public final class KalanHudSettings {

    private boolean visible = true;
    private float scale = 1.0f;
    private float opacity = 1.0f;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = clamp(scale, 0.5f, 2.0f);
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = clamp(opacity, 0.1f, 1.0f);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
