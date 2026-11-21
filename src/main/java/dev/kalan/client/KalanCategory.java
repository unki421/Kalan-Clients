package dev.kalan.client;

public enum KalanCategory {

    HUD("HUD"),
    VISUALS("Visuals"),
    COMBAT("Combat"),
    SETTINGS("Settings");

    private final String displayName;

    KalanCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
