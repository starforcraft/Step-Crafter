package com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor;

import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;

import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;

public class StepCraftingMonitorItemTooltip implements ClientTooltipComponent {
    private static final int SPACING = 2;

    private static final MutableComponent MISSING_RESOURCE = createStepCrafterTranslation("gui", "step_crafting_monitor.missing_resource")
        .withStyle(ChatFormatting.RED);

    private final StepTaskStatus.Item item;
    private final ResourceRendering rendering;

    StepCraftingMonitorItemTooltip(final StepTaskStatus.Item item) {
        this.item = item;
        this.rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(item.resource().getClass());
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        int yy = y;
        graphics.drawString(
            font,
            this.rendering.getDisplayName(this.item.resource()),
            x,
            yy,
            0xFFFFFF
        );
        yy += 9 + SPACING;
        if (this.item.type() != StepTaskStatus.ItemType.NORMAL) {
            graphics.drawString(
                font,
                this.getErrorTooltip(this.item.type()),
                x,
                yy,
                0xAAAAAA
            );
        }
    }

    @Override
    public int getHeight() {
        return 9 + SPACING
            + (this.item.type() != StepTaskStatus.ItemType.NORMAL ? 9 + SPACING : 0);
    }

    @Override
    public int getWidth(final Font font) {
        final int resourceWidth = font.width(this.rendering.getDisplayName(this.item.resource()));
        final int errorWidth = this.item.type() != StepTaskStatus.ItemType.NORMAL ? font.width(this.getErrorTooltip(this.item.type())) : 0;
        return Math.max(resourceWidth, errorWidth);
    }

    private Component getErrorTooltip(final StepTaskStatus.ItemType type) {
        return switch (type) {
            case MISSING -> MISSING_RESOURCE;
            default -> Component.empty();
        };
    }
}
