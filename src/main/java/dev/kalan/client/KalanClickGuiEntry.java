
package dev.kalan.client;

import java.util.List;

public final class KalanClickGuiEntry {

    private final String title;
    private final String description;
    private final KalanHudPanel panel;
    private final KalanVisualFeature visualFeature;
    private final List<KalanHudPanel> hudPanels;

    public KalanClickGuiEntry(
            String title,
            String description,
            KalanHudPanel panel
    ) {
        this.title = title;
        this.description = description;
        this.panel = panel;
        this.visualFeature = null;
        this.hudPanels = List.of();
    }

    public KalanClickGuiEntry(String title, String description, List<KalanHudPanel> hudPanels) {
        this.title = title;
        this.description = description;
        this.panel = null;
        this.visualFeature = null;
        this.hudPanels = List.copyOf(hudPanels);
    }

    public KalanClickGuiEntry(KalanVisualFeature feature) {
        this.title = feature.getTitle();
        this.description = feature.getDescription();
        this.panel = null;
        this.visualFeature = feature;
        this.hudPanels = List.of();
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

    public boolean isHudGroup() {
        return !hudPanels.isEmpty();
    }

    public List<KalanHudPanel> getHudPanels() {
        return hudPanels;
    }

    public boolean isEnabled() {
        if (visualFeature != null) return visualFeature.isEnabled();
        if (isHudGroup()) {
            for (KalanHudPanel hudPanel : hudPanels) {
                KalanHudElement element = KalanHudRegistry.find(hudPanel);
                if (element != null && element.isVisible()) return true;
            }
            return false;
        }
        if (panel == null) {
            return false;
        }

        KalanHudElement element = KalanHudRegistry.find(panel);
        return element != null && element.isVisible();
    }

    public void toggle() {
        if (visualFeature != null) { visualFeature.toggle(); return; }
        if (isHudGroup()) {
            boolean anyEnabled = isEnabled();
            for (KalanHudPanel hudPanel : hudPanels) {
                KalanHudElement element = KalanHudRegistry.find(hudPanel);
                if (element != null) element.setVisible(!anyEnabled);
            }
            KalanHudLayoutStore.save();
            return;
        }
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
