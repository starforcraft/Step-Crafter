package com.ultramega.stepcrafter.common.mixin;

import com.ultramega.stepcrafter.common.support.MaintainableClientTooltipComponent;
import com.ultramega.stepcrafter.common.support.MaintainableResourceHint;
import com.ultramega.stepcrafter.common.support.MaintainingResource;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;

import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.AutocraftableResourceHint;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import com.refinedmods.refinedstorage.common.support.stretching.AbstractStretchingScreen;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslationKey;

@Mixin(AbstractGridScreen.class)
public abstract class MixinAbstractGridScreen<T extends AbstractGridContainerMenu> extends AbstractStretchingScreen<T> {
    protected MixinAbstractGridScreen(final T menu,
                                      final Inventory playerInventory,
                                      final TextMarquee title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "renderResourceWithAmount", at = @At("HEAD"), cancellable = true)
    private void renderResourceWithAmount(final GuiGraphics graphics,
                                          final int slotX,
                                          final int slotY,
                                          final GridResource resource,
                                          final CallbackInfo ci) {
        if (!this.stepcrafter$getMaintainingResources(this.getMenu().getRepository(), resource).isEmpty()) {
            if (!resource.isAutocraftable(this.getMenu().getRepository())) {
                AbstractGridScreen.renderSlotBackground(
                    graphics,
                    slotX,
                    slotY,
                    false,
                    MaintainableResourceHint.MAINTAINABLE.getColor()
                );
            } else {
                stepcrafter$renderHalfSlotBackground(
                    graphics,
                    slotX,
                    slotY,
                    true,
                    AutocraftableResourceHint.AUTOCRAFTABLE.getColor()
                );
                stepcrafter$renderHalfSlotBackground(
                    graphics,
                    slotX,
                    slotY,
                    false,
                    MaintainableResourceHint.MAINTAINABLE.getColor()
                );
            }

            resource.render(graphics, slotX, slotY);
            this.renderAmount(graphics, slotX, slotY, resource);
            ci.cancel();
        }
    }

    @Inject(method = "getAmountText", at = @At("HEAD"), remap = false, cancellable = true)
    private void getAmountText(final GridResource resource, final long amount, final CallbackInfoReturnable<String> cir) {
        // If the resource is also Autocraftable the "Craft" text will be currently replaced
        if (amount == 0 && !this.stepcrafter$getMaintainingResources(this.getMenu().getRepository(), resource).isEmpty()) {
            cir.setReturnValue(I18n.get(createStepCrafterTranslationKey("gui", "grid.stock")));
        }
    }

    @Redirect(method = "getAmountColor", at = @At(value = "INVOKE", target = "Lcom/refinedmods/refinedstorage/common/api/grid/view/GridResource;"
        + "isAutocraftable(Lcom/refinedmods/refinedstorage/api/resource/repository/ResourceRepository;)Z"), remap = false)
    private boolean getAmountColor(final GridResource resource, final ResourceRepository<GridResource> repository) {
        return resource.isAutocraftable(repository) || !this.stepcrafter$getMaintainingResources(repository, resource).isEmpty();
    }

    @Inject(method = "renderHoveredResourceTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lcom/refinedmods/refinedstorage/common/Platform;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;II)V"
        )
    )
    private void renderHoveredResourceTooltip(final GuiGraphics graphics,
                                              final int mouseX,
                                              final int mouseY,
                                              final GridResource resource,
                                              final CallbackInfo ci,
                                              @Local(name = "processedLines") final List<ClientTooltipComponent> processedLines) {
        for (final ResourceMinMaxAmount maintainingResource : this.stepcrafter$getMaintainingResources(this.getMenu().getRepository(), resource)) {
            processedLines.add(MaintainableClientTooltipComponent.maintaining(maintainingResource.minAmount(), maintainingResource.maxAmount()));
        }
    }

    @Unique
    private List<ResourceMinMaxAmount> stepcrafter$getMaintainingResources(final ResourceRepository<GridResource> repository, final GridResource resource) {
        if (repository instanceof MaintainingResource maintainingResource && resource instanceof AbstractGridResourceInvoker<?> resourceInvoker) {
            return maintainingResource.stepcrafter$getMaintainingResources(resourceInvoker.stepcrafter$getResource());
        }
        return List.of();
    }

    @Unique
    private static void stepcrafter$renderHalfSlotBackground(final GuiGraphics graphics,
                                                             final int slotX,
                                                             final int slotY,
                                                             final boolean left,
                                                             final int color) {
        graphics.fill(
            slotX + (left ? 0 : 8),
            slotY,
            slotX + (left ? 8 : 16),
            slotY + 16,
            color
        );
    }

    @Shadow(remap = false)
    protected abstract void renderAmount(GuiGraphics graphics, int slotX, int slotY, GridResource resource);
}
