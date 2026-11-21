
package dev.kalan.client;

public final class KalanClickGuiEntry {

    private final String title;
    private final String description;
    private final KalanHudPanel panel;

    public KalanClickGuiEntry(
            String title,
            String description,
            KalanHudPanel panel
    ) {
        this.title = title;
        this.description = description;
        this.panel = panel;
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

    public boolean isEnabled() {
        if (panel == null) {
            return false;
        }

        KalanHudElement element = KalanHudRegistry.find(panel);
        return element != null && element.isVisible();
    }

    public void toggle() {
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
