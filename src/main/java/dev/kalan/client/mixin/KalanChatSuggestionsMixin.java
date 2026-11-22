package dev.kalan.client.mixin;

import dev.kalan.client.KalanProfileManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class KalanChatSuggestionsMixin {
    @Shadow
    protected TextFieldWidget chatField;

    @Inject(method = "render", at = @At("TAIL"))
    private void kalan$renderCommandSuggestions(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (chatField == null) return;
        String input = chatField.getText();
        if (input == null || !input.startsWith(".")) return;

        String[] suggestions = KalanProfileManager.getSuggestions(input);
        if (suggestions.length == 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return;

        int lineHeight = 12;
        int boxWidth = 0;
        for (String suggestion : suggestions) {
            boxWidth = Math.max(boxWidth, client.textRenderer.getWidth(suggestion));
        }
        boxWidth = Math.min(300, boxWidth + 14);
        int boxX = chatField.getX();
        int boxBottom = chatField.getY() - 5;
        int boxY = boxBottom - suggestions.length * lineHeight - 8;

        context.fill(boxX, boxY, boxX + boxWidth, boxBottom, 0xE810101B);
        context.fill(boxX, boxY, boxX + 2, boxBottom, 0xFFB56BFF);

        for (int i = 0; i < suggestions.length; i++) {
            context.drawText(client.textRenderer, Text.literal(suggestions[i]),
                    boxX + 8, boxY + 5 + i * lineHeight, 0xFFF0E8FF, false);
        }
    }
}
