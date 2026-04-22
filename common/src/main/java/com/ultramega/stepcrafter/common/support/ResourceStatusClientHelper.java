package com.ultramega.stepcrafter.common.support;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.support.Sprites.AUTOCRAFTING_INDICATOR;
import static com.refinedmods.refinedstorage.common.support.Sprites.WARNING;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.CRAFTING_INDICATOR_TOOLTIP;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.EXTERNAL_CONTAINER_FULL_INDICATOR_TOOLTIP;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.FINISHED_INDICATOR;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.FINISHED_INDICATOR_TOOLTIP;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.NETWORK_FULL_INDICATOR_TOOLTIP;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.NOT_ENOUGH_INGREDIENTS_INDICATOR_TOOLTIP;

public final class ResourceStatusClientHelper {
    private ResourceStatusClientHelper() {
    }

    public static ResourceLocation getIcon(final ResourceStatus status) {
        return switch (status) {
            case FINISHED -> FINISHED_INDICATOR;
            case CRAFTING -> AUTOCRAFTING_INDICATOR;
            case NOT_ENOUGH_INGREDIENTS, NETWORK_FULL, EXTERNAL_CONTAINER_FULL -> WARNING;
        };
    }

    public static List<ClientTooltipComponent> getTooltip(final ResourceStatus status) {
        return switch (status) {
            case FINISHED -> tooltip(FINISHED_INDICATOR_TOOLTIP, ChatFormatting.GREEN);
            case CRAFTING -> tooltip(CRAFTING_INDICATOR_TOOLTIP, ChatFormatting.DARK_GRAY);
            case NOT_ENOUGH_INGREDIENTS -> tooltip(NOT_ENOUGH_INGREDIENTS_INDICATOR_TOOLTIP, ChatFormatting.RED);
            case NETWORK_FULL -> tooltip(NETWORK_FULL_INDICATOR_TOOLTIP, ChatFormatting.RED);
            case EXTERNAL_CONTAINER_FULL -> tooltip(EXTERNAL_CONTAINER_FULL_INDICATOR_TOOLTIP, ChatFormatting.RED);
        };
    }

    private static List<ClientTooltipComponent> tooltip(final MutableComponent text, final ChatFormatting style) {
        return List.of(ClientTooltipComponent.create(text.withStyle(style).getVisualOrderText()));
    }
}
