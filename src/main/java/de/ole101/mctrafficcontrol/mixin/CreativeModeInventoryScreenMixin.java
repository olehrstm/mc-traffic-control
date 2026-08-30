package de.ole101.mctrafficcontrol.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.ole101.mctrafficcontrol.McTrafficControl.COMPONENT_VIEWER_WIDGET;
import static de.ole101.mctrafficcontrol.McTrafficControl.configuration;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    protected CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu,
                                               Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void mtc$viewComponentsInCreativeInventory(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (COMPONENT_VIEWER_WIDGET.keyPressed(event)) {
            cir.setReturnValue(true);
            return;
        }

        if (configuration.componentViewing().getKeybind().matches(event)) {
            COMPONENT_VIEWER_WIDGET.setItemStack(this.hoveredSlot == null ? null : this.hoveredSlot.getItem().copy());
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mtc$clickViewerInCreativeInventory(MouseButtonEvent event, boolean doubleClick,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (COMPONENT_VIEWER_WIDGET.mouseClicked(event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void mtc$dragViewerInCreativeInventory(MouseButtonEvent event, double dx, double dy,
                                                   CallbackInfoReturnable<Boolean> cir) {
        if (COMPONENT_VIEWER_WIDGET.mouseDragged(event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void mtc$releaseViewerInCreativeInventory(MouseButtonEvent event,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (COMPONENT_VIEWER_WIDGET.mouseReleased(event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void mtc$renderViewerInCreativeInventory(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                                     float partialTick, CallbackInfo ci) {
        COMPONENT_VIEWER_WIDGET.extractRenderState(graphics, mouseX, mouseY);
    }
}
