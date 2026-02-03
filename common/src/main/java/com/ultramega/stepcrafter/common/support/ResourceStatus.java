package com.ultramega.stepcrafter.common.support;

import java.util.List;
import java.util.Locale;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import static com.refinedmods.refinedstorage.common.support.Sprites.AUTOCRAFTING_INDICATOR;
import static com.refinedmods.refinedstorage.common.support.Sprites.WARNING;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.CRAFTING_INDICATOR_TOOLTIP;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.EXTERNAL_CONTAINER_FULL_INDICATOR_TOOLTIP;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.FINISHED_INDICATOR;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.FINISHED_INDICATOR_TOOLTIP;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.NETWORK_FULL_INDICATOR_TOOLTIP;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.NOT_ENOUGH_INGREDIENTS_INDICATOR_TOOLTIP;

public enum ResourceStatus implements StringRepresentable {
    FINISHED(FINISHED_INDICATOR, FINISHED_INDICATOR_TOOLTIP, ChatFormatting.GREEN),
    CRAFTING(AUTOCRAFTING_INDICATOR, CRAFTING_INDICATOR_TOOLTIP, ChatFormatting.DARK_GRAY),
    NOT_ENOUGH_INGREDIENTS(WARNING, NOT_ENOUGH_INGREDIENTS_INDICATOR_TOOLTIP, ChatFormatting.RED),
    NETWORK_FULL(WARNING, NETWORK_FULL_INDICATOR_TOOLTIP, ChatFormatting.RED),
    EXTERNAL_CONTAINER_FULL(WARNING, EXTERNAL_CONTAINER_FULL_INDICATOR_TOOLTIP, ChatFormatting.RED);

    public static final Codec<ResourceStatus> CODEC = StringRepresentable.fromEnum(ResourceStatus::values);

    private final ResourceLocation icon;
    private final List<ClientTooltipComponent> tooltip;

    ResourceStatus(final ResourceLocation icon, final MutableComponent tooltip, final ChatFormatting style) {
        this.icon = icon;
        this.tooltip = List.of(ClientTooltipComponent.create(tooltip.withStyle(style).getVisualOrderText()));
    }

    public ResourceLocation getIcon() {
        return this.icon;
    }

    public List<ClientTooltipComponent> getTooltip() {
        return this.tooltip;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return this.toString();
    }
}
