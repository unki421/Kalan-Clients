package dev.kalan.client;

public enum KalanHudPanel {

    WATERMARK("Watermark", "Название клиента, ник, FPS, ping и время"),
    INFO("Info", "Дополнительная информация"),
    COORDINATES("Coordinates", "Координаты игрока"),
    KEYSTROKES("Keystrokes", "Клавиши WASD"),
    MOUSE("Mouse", "ЛКМ и ПКМ"),
    POTIONS("Potions", "Активные эффекты зелий");

    private final String title;
    private final String description;

    KalanHudPanel(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getId() {
        return name().toLowerCase();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
