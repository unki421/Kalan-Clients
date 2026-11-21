
package dev.kalan.client;

public final class KalanClickGuiState {

    private KalanClickGuiCategory category =
            KalanClickGuiCategory.HUD;

    private String search = "";

    private KalanClickGuiEntry selected;

    private boolean settingsOpen;

    public KalanClickGuiCategory getCategory() {
        return category;
    }

    public void setCategory(KalanClickGuiCategory category) {
        if (category != null) {
            this.category = category;
            selected = null;
            settingsOpen = false;
        }
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search == null ? "" : search;
    }

    public KalanClickGuiEntry getSelected() {
        return selected;
    }

    public void select(KalanClickGuiEntry entry) {
        selected = entry;
        settingsOpen = entry != null;
    }

    public boolean isSettingsOpen() {
        return settingsOpen;
    }

    public void closeSettings() {
        settingsOpen = false;
    }

    public void clearSelection() {
        selected = null;
        settingsOpen = false;
    }

    public boolean matchesSearch(KalanClickGuiEntry entry) {
        if (entry == null) {
            return false;
        }

        if (search.isBlank()) {
            return true;
        }

        String query = search.trim().toLowerCase();

        return entry.getTitle().toLowerCase().contains(query)
                || entry.getDescription().toLowerCase().contains(query);
    }
}
