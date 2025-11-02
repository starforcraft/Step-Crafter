package com.ultramega.stepcrafter.common.support;

import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public class MaintainableClientTooltipComponent implements ClientTooltipComponent {
    private static final ResourceLocation ICON = createStepCrafterIdentifier("grid/stock");
    private static final int ICON_SIZE = 8;
    private static final int ICON_MARGIN = 4;

    private static final BiFunction<Long, Long, Component> MAINTAINING = (minAmount, maxAmount) ->
        Component.translatable("tooltip." + MOD_ID + ".grid.maintaining", minAmount, maxAmount);
    private static final Function<Long, Component> MAINTAINING_SINGLE = (amount) ->
        Component.translatable("tooltip." + MOD_ID + ".grid.maintaining_single", amount);

    private final Component text;

    private MaintainableClientTooltipComponent(final Component text) {
        this.text = text;
    }

    public static MaintainableClientTooltipComponent maintaining(final long minAmount, final long maxAmount) {
        return new MaintainableClientTooltipComponent(minAmount != maxAmount ? MAINTAINING.apply(minAmount, maxAmount) : MAINTAINING_SINGLE.apply(minAmount));
    }

    @Override
    public int getHeight() {
        return ICON_SIZE + 2;
    }

    @Override
    public int getWidth(final Font font) {
        return ICON_SIZE + ICON_MARGIN + (int) (font.width(this.text) * SmallText.TOOLTIP_SCALE);
    }

    @Override
    public void renderText(final Font font,
                           final int x,
                           final int y,
                           final Matrix4f matrix,
                           final MultiBufferSource.BufferSource bufferSource) {
        final int yOffset = SmallText.isSmall() ? 2 : 0;
        SmallText.render(
            font,
            this.text.getVisualOrderText(),
            x + ICON_SIZE + ICON_MARGIN,
            y + yOffset,
            0x9F7F50,
            matrix,
            bufferSource,
            SmallText.TOOLTIP_SCALE
        );
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        graphics.blitSprite(ICON, x, y, ICON_SIZE, ICON_SIZE);
    }
}
