package com.ultramega.stepcrafter.common.support.patternresource;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;

import net.minecraft.client.gui.GuiGraphics;

public class PatternResourceSlotRendering {
    private PatternResourceSlotRendering() {
    }

    public static void render(final GuiGraphics graphics,
                              final PatternResourceSlot slot,
                              final int leftPos,
                              final int topPos) {
        final ResourceKey resource = slot.getResource();
        if (resource == null) {
            return;
        }
        render(
            graphics,
            leftPos + slot.x,
            topPos + slot.y,
            resource
        );
    }

    private static void render(final GuiGraphics graphics,
                               final int x,
                               final int y,
                               final ResourceKey resource) {
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
        rendering.render(resource, graphics, x, y);
    }
}
