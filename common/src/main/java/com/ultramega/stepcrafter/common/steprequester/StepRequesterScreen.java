package com.ultramega.stepcrafter.common.steprequester;

import com.ultramega.stepcrafter.common.packet.c2s.PatternResourceSlotChangePacket;
import com.ultramega.stepcrafter.common.steprequester.widget.VisibleToTheStepRequesterManagerSideButtonWidget;
import com.ultramega.stepcrafter.common.support.AbstractEditableNameScreen;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlotRendering;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StepRequesterScreen extends AbstractEditableNameScreen<StepRequesterContainerMenu> {
    public StepRequesterScreen(final StepRequesterContainerMenu menu,
                               final Inventory playerInventory,
                               final Component title) {
        super(menu, playerInventory, new TextMarquee(title, getTitleMaxWidth()));
    }

    @Override
    protected void init() {
        super.init();

        this.addSideButton(new RedstoneModeSideButtonWidget(this.getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
        this.addSideButton(new VisibleToTheStepRequesterManagerSideButtonWidget(
            this.getMenu().getProperty(StepRequesterPropertyTypes.VISIBLE_TO_THE_STEP_REQUESTER_MANAGER)
        ));
    }

    @Override
    protected void renderResourceSlots(final GuiGraphicsExtractor graphics) {
        for (final PatternResourceSlot slot : this.getMenu().getPatternResourceSlots()) {
            if (slot.isActive()) {
                PatternResourceSlotRendering.render(graphics, slot);
            }
        }
        this.renderSlotMinMax(graphics, true);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (this.hoveredSlot instanceof PatternResourceSlot resourceSlot && resourceSlot.isActive()) {
            if (!this.tryOpenResourceAmountScreen(resourceSlot, true, false)) {
                Platform.INSTANCE.sendPacketToServer(new PatternResourceSlotChangePacket(this.hoveredSlot.index, event.button() == 1));
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
