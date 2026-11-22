package dev.kalan.client;

public enum KalanVisualFeature {
    CUSTOM_SKY("Custom Sky", "Локальная смена времени суток"),
    CHINA_HAT("China Hat", "Косметическая 3D-шляпа над игроками"),
    BLOCK_HIGHLIGHT("Block Highlight", "Подсветка блока под прицелом"),
    PLAYER_TRAILS("Player Trails", "След из частиц только за тобой"),
    JUMP_CIRCLE("Jump Circle", "Круг появляется под тобой при прыжке"),
    SNOW("Snow", "Снегопад и снежный покров на блоках"),
    CUSTOM_CROSSHAIR("Custom Crosshair", "Настраиваемый прицел для PvP");

    private final String title;
    private final String description;

    KalanVisualFeature(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }

    public boolean isEnabled() {
        return switch (this) {
            case CUSTOM_SKY -> KalanCustomSky.isEnabled();
            case CHINA_HAT -> KalanChinaHat.isEnabled();
            case BLOCK_HIGHLIGHT -> KalanBlockHighlight.isEnabled();
            case PLAYER_TRAILS -> KalanPlayerTrails.isEnabled();
            case JUMP_CIRCLE -> KalanJumpCircle.isEnabled();
            case SNOW -> KalanSnow.isEnabled();
            case CUSTOM_CROSSHAIR -> KalanCustomCrosshair.isEnabled();
        };
    }

    public void toggle() {
        switch (this) {
            case CUSTOM_SKY -> KalanCustomSky.toggle();
            case CHINA_HAT -> KalanChinaHat.toggle();
            case BLOCK_HIGHLIGHT -> KalanBlockHighlight.toggle();
            case PLAYER_TRAILS -> KalanPlayerTrails.toggle();
            case JUMP_CIRCLE -> KalanJumpCircle.toggle();
            case SNOW -> KalanSnow.toggle();
            case CUSTOM_CROSSHAIR -> KalanCustomCrosshair.toggle();
        }
    }
}
