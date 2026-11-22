
package dev.kalan.client;

public final class KalanClickGuiEntry {

    private final String title;
    private final String description;
    private final KalanHudPanel panel;
    private final KalanVisualFeature visualFeature;

    public KalanClickGuiEntry(
            String title,
            String description,
            KalanHudPanel panel
    ) {
        this.title = title;
        this.description = description;
        this.panel = panel;
        this.visualFeature = null;
    }

    public KalanClickGuiEntry(KalanVisualFeature feature) {
        this.title = feature.getTitle();
        this.description = feature.getDescription();
        this.panel = null;
        this.visualFeature = feature;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public KalanHudPanel getPanel() {
        return panel;
    }

    public KalanVisualFeature getVisualFeature() {
        return visualFeature;
    }

    public boolean isEnabled() {
        if (visualFeature != null) return visualFeature.isEnabled();
        if (panel == null) {
            return false;
        }

        KalanHudElement element = KalanHudRegistry.find(panel);
        return element != null && element.isVisible();
    }

    public void toggle() {
        if (visualFeature != null) { visualFeature.toggle(); return; }
        if (panel == null) {
            return;
        }

        KalanHudElement element = KalanHudRegistry.find(panel);

        if (element != null) {
            element.setVisible(!element.isVisible());
            KalanHudLayoutStore.save();
        }
    }
}
