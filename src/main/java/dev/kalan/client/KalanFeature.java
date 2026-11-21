package dev.kalan.client;

import java.util.Objects;

public final class KalanFeature {

    private final String name;
    private final String description;
    private final KalanCategory category;

    private boolean enabled;

    public KalanFeature(String name, String description, KalanCategory category) {
        this.name = Objects.requireNonNull(name, "name");
        this.description = Objects.requireNonNull(description, "description");
        this.category = Objects.requireNonNull(category, "category");
        this.enabled = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public KalanCategory getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }
}
