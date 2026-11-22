package dev.kalan.client;

public enum KalanSkyPreset {
    DAWN("Рассвет", 23000L),
    DAY("День", 6000L),
    EVENING("Вечер", 12000L),
    NIGHT("Ночь", 18000L);

    private final String title;
    private final long time;

    KalanSkyPreset(String title, long time) {
        this.title = title;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public long getTime() {
        return time;
    }

    public static boolean isValid(String value) {
        if (value == null) return false;
        try {
            valueOf(value);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
