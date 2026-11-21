package dev.kalan.client;

public final class KalanHudEditController {

    private boolean editing;
    private KalanHudElement selected;

    private int dragOffsetX;
    private int dragOffsetY;

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;

        if (!editing) {
            stopDragging();
        }
    }

    public void toggleEditing() {
        setEditing(!editing);
    }

    public KalanHudElement getSelected() {
        return selected;
    }

    public boolean isDragging() {
        return selected != null && editing;
    }

    public boolean beginDrag(int mouseX, int mouseY) {
        if (!editing) {
            return false;
        }

        selected = null;

        for (KalanHudElement element : KalanHudRegistry.getAll()) {
            if (element.isVisible() && element.contains(mouseX, mouseY)) {
                selected = element;
                dragOffsetX = mouseX - element.getX();
                dragOffsetY = mouseY - element.getY();
            }
        }

        return selected != null;
    }

    public void dragTo(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (!editing || selected == null) {
            return;
        }

        selected.setPosition(
                mouseX - dragOffsetX,
                mouseY - dragOffsetY
        );

        selected.clampToScreen(screenWidth, screenHeight);
    }

    public void stopDragging() {
        selected = null;
        dragOffsetX = 0;
        dragOffsetY = 0;
    }

    public void deselect() {
        stopDragging();
    }
}
