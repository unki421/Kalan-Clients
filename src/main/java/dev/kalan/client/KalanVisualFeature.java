package dev.kalan.client;

public enum KalanVisualFeature {
    CUSTOM_SKY("Custom Sky", "Локальная смена времени суток");

    private final String title;
    private final String description;

    KalanVisualFeature(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return KalanCustomSky.isEnabled();
    }

    public void toggle() {
        KalanCustomSky.toggle();
    }
}
