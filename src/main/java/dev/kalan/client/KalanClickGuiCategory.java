package dev.kalan.client;

public enum KalanClickGuiCategory {

    HUD("HUD", "Информационный интерфейс"),
    COMBAT("PvP", "Прицел и элементы для PvP"),
    VISUALS("Visuals", "Косметические визуальные эффекты");

    private final String title;
    private final String description;

    KalanClickGuiCategory(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
