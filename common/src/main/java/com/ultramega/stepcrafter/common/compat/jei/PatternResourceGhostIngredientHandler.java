package com.ultramega.stepcrafter.common.compat.jei;

import com.ultramega.stepcrafter.common.packet.c2s.PatternResourceFilterSlotChangePacket;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterScreen;
import com.ultramega.stepcrafter.common.support.AbstractPatternResourceContainerMenu;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;

public class PatternResourceGhostIngredientHandler implements IGhostIngredientHandler<StepRequesterScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(final StepRequesterScreen screen,
                                               final ITypedIngredient<I> ingredient,
                                               final boolean doStart) {
        return getTargetsTyped(screen, ingredient);
    }

    public static <I> List<Target<I>> getTargetsTyped(final AbstractBaseScreen<?> screen,
                                                      final ITypedIngredient<I> ingredient) {
        if (!(screen.getMenu() instanceof AbstractPatternResourceContainerMenu containerMenu)) {
            return List.of();
        }

        final List<Target<I>> targets = new ArrayList<>();
        RefinedStorageApi.INSTANCE.getIngredientConverter().convertToResource(ingredient.getIngredient())
            .ifPresent(resource -> {
                for (final PatternResourceSlot slot : containerMenu.getPatternResourceSlots()) {
                    if (slot.isActive() && slot.isFilter()) {
                        final Rect2i bounds = getBounds(screen, slot);
                        targets.add(new TargetImpl<>(bounds, slot.index));
                    }
                }
            });
        return targets;
    }

    private static Rect2i getBounds(final AbstractBaseScreen<?> screen, final Slot slot) {
        return new Rect2i(screen.getLeftPos() + slot.x, screen.getTopPos() + slot.y, 17, 17);
    }

    @Override
    public void onComplete() {
        // no op
    }

    private static class TargetImpl<I> implements Target<I> {
        private final Rect2i area;
        private final int slotIndex;

        TargetImpl(final Rect2i area, final int slotIndex) {
            this.area = area;
            this.slotIndex = slotIndex;
        }

        @Override
        public Rect2i getArea() {
            return this.area;
        }

        @Override
        public void accept(final I ingredient) {
            RefinedStorageApi.INSTANCE.getIngredientConverter().convertToResource(ingredient).ifPresent(
                convertedResource -> Platform.INSTANCE.sendPacketToServer(new PatternResourceFilterSlotChangePacket(this.slotIndex, convertedResource))
            );
        }
    }
}
