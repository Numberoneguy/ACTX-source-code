package net.minecraft.actx.selector;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
public class searchwidget {
    private final FontRenderer fontrenderer;
    private final GuiTextField searchfield;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private String label = "";
    public interface SearchCallback {
        void onSearchChanged(String query);
    }
    public searchwidget(int id, FontRenderer fontrenderer, int x, int y, int width, int height) {
        this.fontrenderer = fontrenderer;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.searchfield = new GuiTextField(id, fontrenderer, x, y, width, height);
        this.searchfield.setMaxStringLength(64);
        this.searchfield.setEnableBackgroundDrawing(true);
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public String getText() {
        return this.searchfield.getText();
    }
    public void setText(String text) {
        this.searchfield.setText(text);
        this.searchfield.setCursorPositionEnd();
    }
    public boolean isFocused() {
        return this.searchfield.isFocused();
    }
    public void setFocused(boolean focused) {
        this.searchfield.setFocused(focused);
    }
    public void updateScreen() {
        this.searchfield.updateCursorCounter();
    }
    public void drawWidget(int mousex, int mousey) {
        if (label != null && !label.isEmpty()) {
            this.fontrenderer.drawStringWithShadow(label, this.x, this.y + (this.height - 8) / 2, 0xFFFFFF);
        }
        this.searchfield.drawTextBox();
    }
    public boolean keyTyped(char typedchar, int keycode, SearchCallback callback) {
        if (this.searchfield.isFocused()) {
            String oldtext = this.searchfield.getText();
            boolean handled = this.searchfield.textboxKeyTyped(typedchar, keycode);
            String newtext = this.searchfield.getText();
            if (handled && !oldtext.equals(newtext) && callback != null) {
                callback.onSearchChanged(newtext);
            }
            return handled;
        }
        return false;
    }
    public void mouseClicked(int mousex, int mousey, int mousebutton) {
        this.searchfield.mouseClicked(mousex, mousey, mousebutton);
    }
}
