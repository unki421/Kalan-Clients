
package dev.kalan.client;

public enum KalanClickGuiCategory {

    HUD("HUD", "Информационный интерфейс"),
    INTERFACE("Interface", "Настройки интерфейса"),
    SETTINGS("Settings", "Общие настройки");

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
