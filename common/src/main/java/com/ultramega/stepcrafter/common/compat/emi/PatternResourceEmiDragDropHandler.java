package com.ultramega.stepcrafter.common.compat.emi;

import com.ultramega.stepcrafter.common.packet.c2s.PatternResourceFilterSlotChangePacket;
import com.ultramega.stepcrafter.common.support.AbstractPatternResourceContainerMenu;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class PatternResourceEmiDragDropHandler implements EmiDragDropHandler<Screen> {
    @Override
    public boolean dropStack(final Screen screen, final EmiIngredient stack, final int x, final int y) {
        if (!(screen instanceof AbstractBaseScreen<?> baseScreen)) {
            return false;
        }
        if (!(baseScreen.getMenu() instanceof AbstractPatternResourceContainerMenu menu)) {
            return false;
        }
        return RefinedStorageApi.INSTANCE.getIngredientConverter().convertToResource(stack)
            .map(resource -> this.dropStack(menu, baseScreen, resource, x, y))
            .orElse(false);
    }

    private boolean dropStack(final AbstractPatternResourceContainerMenu menu,
                              final AbstractBaseScreen<?> baseScreen,
                              final PlatformResourceKey resource,
                              final int x,
                              final int y) {
        for (final PatternResourceSlot slot : menu.getPatternResourceSlots()) {
            if (this.dropStack(resource, slot, x, y, baseScreen)) {
                return true;
            }
        }
        return false;
    }

    private boolean dropStack(final PlatformResourceKey resource,
                              final PatternResourceSlot slot,
                              final int x,
                              final int y,
                              final AbstractBaseScreen<?> baseScreen) {
        if (!isSlotValid(slot)) {
            return false;
        }
        final int slotX = baseScreen.getLeftPos() + slot.x;
        final int slotY = baseScreen.getTopPos() + slot.y;
        if (x < slotX || y < slotY || x > slotX + 16 || y > slotY + 16) {
            return false;
        }
        Platform.INSTANCE.sendPacketToServer(new PatternResourceFilterSlotChangePacket(slot.index, resource));
        return true;
    }

    @Override
    public void render(final Screen screen,
                       final EmiIngredient dragged,
                       final GuiGraphics draw,
                       final int mouseX,
                       final int mouseY,
                       final float delta) {
        if (!(screen instanceof AbstractBaseScreen<?> baseScreen)) {
            return;
        }
        if (!(baseScreen.getMenu() instanceof AbstractPatternResourceContainerMenu menu)) {
            return;
        }
        RefinedStorageApi.INSTANCE.getIngredientConverter().convertToResource(dragged).ifPresent(resource -> {
            final EmiDrawContext context = EmiDrawContext.wrap(draw);
            for (final PatternResourceSlot slot : menu.getPatternResourceSlots()) {
                if (!isSlotValid(slot)) {
                    continue;
                }
                context.fill(baseScreen.getLeftPos() + slot.x, baseScreen.getTopPos() + slot.y, 17, 17, 0x8822BB33);
            }
        });
    }

    private static boolean isSlotValid(final PatternResourceSlot slot) {
        return slot.isFilter() && slot.isActive();
    }
}
