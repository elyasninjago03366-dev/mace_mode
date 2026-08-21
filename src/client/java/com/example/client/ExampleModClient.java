package com.example.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {

    public static boolean enabled = true;
    public static boolean singleHitMode = true;
    public static double reachDistance = 3.0;

    private static boolean hasAttackedInCurrentFall = false;
    private static KeyMapping openGuiKey;

    @Override
    public void onInitializeClient() {
        // ثبت کلید Right Shift با سیستم استاندارد فبریک برای پوجاو لانچر
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.ninjago.open_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.ninjago.title"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null || client.gameMode == null) return;

            // ۱. باز کردن GUI با کلید ثبت‌شده
            while (openGuiKey.consumeClick()) {
                client.setScreen(new NinjagoScreen());
            }

            // ۲. ریست کردن تک‌ضربه وقتی روی زمینه
            if (client.player.onGround()) {
                hasAttackedInCurrentFall = false;
                return;
            }

            if (!enabled) return;

            // ۳. منطق ضربه با میس
            boolean isHoldingMace = client.player.getMainHandItem().is(Items.MACE);
            boolean isFalling = !client.player.onGround() && client.player.getDeltaMovement().y < 0;

            if (!isHoldingMace || !isFalling) return;
            if (singleHitMode && hasAttackedInCurrentFall) return;

            // ۴. نشانه روی انتیتی
            HitResult hitResult = client.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hitResult;

                if (entityHit.getEntity() instanceof LivingEntity targetEntity) {
                    double distance = client.player.distanceTo(targetEntity);

                    if (distance <= reachDistance) {
                        client.gameMode.attack(client.player, targetEntity);
                        client.player.swing(InteractionHand.MAIN_HAND);
                        hasAttackedInCurrentFall = true;
                    }
                }
            }
        });
    }

    // کلاس منوی گرافیکی Ninjago Client
    public static class NinjagoScreen extends Screen {

        private boolean draggingSlider = false;

        public NinjagoScreen() {
            super(Component.literal("Ninjago Client"));
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // پس‌زمینه نیمه‌شفاف
            guiGraphics.fill(0, 0, this.width, this.height, 0x80000000);

            int cardWidth = 280;
            int cardHeight = 210;
            int x = (this.width - cardWidth) / 2;
            int y = (this.height - cardHeight) / 2;

            // Glass Container
            guiGraphics.fill(x - 1, y - 1, x + cardWidth + 1, y + cardHeight + 1, 0xFF00F2FE);
            guiGraphics.fill(x, y, x + cardWidth, y + cardHeight, 0xEE0B0E14);

            // Title
            guiGraphics.drawCenteredString(this.font, "NINJAGO CLIENT", x + cardWidth / 2, y + 15, 0xFF00F2FE);
            guiGraphics.fill(x + 20, y + 32, x + cardWidth - 20, y + 33, 0x44FFFFFF);

            // 1. Mace Assist Toggle
            String toggleText = ExampleModClient.enabled ? "ENABLED" : "DISABLED";
            int toggleColor = ExampleModClient.enabled ? 0xFF00FF88 : 0xFFFF4444;
            guiGraphics.drawString(this.font, "Mace Assist", x + 25, y + 50, 0xFFFFFFFF);
            guiGraphics.fill(x + 180, y + 46, x + 255, y + 64, ExampleModClient.enabled ? 0x3300FF88 : 0x33FF4444);
            guiGraphics.drawCenteredString(this.font, toggleText, x + 217, y + 51, toggleColor);

            // 2. Attack Mode Toggle
            String modeText = ExampleModClient.singleHitMode ? "SINGLE HIT" : "SPAM MODE";
            int modeColor = ExampleModClient.singleHitMode ? 0xFF00F2FE : 0xFFFFAA00;
            guiGraphics.drawString(this.font, "Attack Mode", x + 25, y + 85, 0xFFFFFFFF);
            guiGraphics.fill(x + 170, y + 81, x + 255, y + 99, 0x3300F2FE);
            guiGraphics.drawCenteredString(this.font, modeText, x + 212, y + 86, modeColor);

            // 3. Reach Distance Slider
            String reachText = String.format("%.1f Blocks", ExampleModClient.reachDistance);
            guiGraphics.drawString(this.font, "Reach Distance", x + 25, y + 120, 0xFFFFFFFF);
            guiGraphics.drawString(this.font, reachText, x + 195, y + 120, 0xFF00F2FE);

            int sliderX = x + 25;
            int sliderY = y + 138;
            int sliderWidth = 230;
            guiGraphics.fill(sliderX, sliderY, sliderX + sliderWidth, sliderY + 6, 0x55FFFFFF);

            double progress = (ExampleModClient.reachDistance - 1.0) / 4.0;
            int knobX = sliderX + (int) (progress * sliderWidth);
            guiGraphics.fill(knobX - 4, sliderY - 3, knobX + 4, sliderY + 9, 0xFF00F2FE);

            if (this.draggingSlider) {
                double relativeX = Math.max(0, Math.min(sliderWidth, mouseX - sliderX));
                ExampleModClient.reachDistance = Math.round((1.0 + (relativeX / sliderWidth) * 4.0) * 10.0) / 10.0;
            }

            // Footer
            guiGraphics.fill(x + 20, y + 180, x + cardWidth - 20, y + 181, 0x22FFFFFF);
            guiGraphics.drawCenteredString(this.font, "made by elyasninjago", x + cardWidth / 2, y + 190, 0x88AAAAAA);

            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                int cardWidth = 280;
                int cardHeight = 210;
                int x = (this.width - cardWidth) / 2;
                int y = (this.height - cardHeight) / 2;

                if (mouseX >= x + 180 && mouseX <= x + 255 && mouseY >= y + 46 && mouseY <= y + 64) {
                    ExampleModClient.enabled = !ExampleModClient.enabled;
                    return true;
                }

                if (mouseX >= x + 170 && mouseX <= x + 255 && mouseY >= y + 81 && mouseY <= y + 99) {
                    ExampleModClient.singleHitMode = !ExampleModClient.singleHitMode;
                    return true;
                }

                int sliderX = x + 25;
                int sliderY = y + 138;
                if (mouseX >= sliderX && mouseX <= sliderX + 230 && mouseY >= sliderY - 5 && mouseY <= sliderY + 11) {
                    this.draggingSlider = true;
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (button == 0) {
                this.draggingSlider = false;
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
