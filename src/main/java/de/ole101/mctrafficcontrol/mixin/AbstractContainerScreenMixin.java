package de.ole101.mctrafficcontrol.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.ole101.mctrafficcontrol.McTrafficControl.COMPONENT_VIEWER_WIDGET;
import static de.ole101.mctrafficcontrol.McTrafficControl.configuration;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Inject(method = "isHovering(IIIIDD)Z", at = @At("HEAD"), cancellable = true)
    private void mtc$ignoreSlotsBehindViewer(int left, int top, int w, int h,
                                             double xm, double ym,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (COMPONENT_VIEWER_WIDGET.isHovering(xm, ym)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void mtc$viewComponents(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (COMPONENT_VIEWER_WIDGET.keyPressed(event)) {
            cir.setReturnValue(true);
            return;
        }

        if (configuration.componentViewing().getKeybind().matches(event)) {
            COMPONENT_VIEWER_WIDGET.setItemStack(hoveredSlot == null ? null : hoveredSlot.getItem().copy());
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mtc$clickViewer(MouseButtonEvent event, boolean doubleClick,
                                 CallbackInfoReturnable<Boolean> cir) {
        if (COMPONENT_VIEWER_WIDGET.mouseClicked(event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void mtc$dragViewer(MouseButtonEvent event, double dx, double dy,
                                CallbackInfoReturnable<Boolean> cir) {
        if (COMPONENT_VIEWER_WIDGET.mouseDragged(event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void mtc$releaseViewer(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (COMPONENT_VIEWER_WIDGET.mouseReleased(event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void mtc$scrollViewer(double x, double y, double scrollX, double scrollY,
                                  CallbackInfoReturnable<Boolean> cir) {
        if (COMPONENT_VIEWER_WIDGET.mouseScrolled(x, y, scrollY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void mtc$renderViewer(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                  float a, CallbackInfo ci) {
        if ((Object) this instanceof AbstractRecipeBookScreen<?>
                || (Object) this instanceof CreativeModeInventoryScreen) {
            return;
        }

        COMPONENT_VIEWER_WIDGET.extractRenderState(graphics, mouseX, mouseY);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void mtc$closeViewer(CallbackInfo ci) {
        COMPONENT_VIEWER_WIDGET.close();
    }
}
