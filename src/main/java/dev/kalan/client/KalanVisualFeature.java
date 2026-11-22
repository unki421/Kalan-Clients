package dev.kalan.client;

public enum KalanVisualFeature {
    CUSTOM_SKY("Custom Sky", "Локальная смена времени суток"),
    CHINA_HAT("China Hat", "Косметическая 3D-шляпа над игроками");

    private final String title;
    private final String description;

    KalanVisualFeature(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }

    public boolean isEnabled() {
        return this == CUSTOM_SKY ? KalanCustomSky.isEnabled() : KalanChinaHat.isEnabled();
    }

    public void toggle() {
        if (this == CUSTOM_SKY) KalanCustomSky.toggle();
        else KalanChinaHat.toggle();
    }
}
