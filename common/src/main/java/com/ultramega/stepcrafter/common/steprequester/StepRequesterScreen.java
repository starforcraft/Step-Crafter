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

import net.minecraft.client.gui.GuiGraphics;
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
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        this.renderSlotMinMax(graphics, true);
    }

    @Override
    protected void renderResourceSlots(final GuiGraphics graphics) {
        for (final PatternResourceSlot slot : this.getMenu().getPatternResourceSlots()) {
            if (slot.isActive()) {
                PatternResourceSlotRendering.render(graphics, slot, this.leftPos, this.topPos);
            }
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (this.hoveredSlot instanceof PatternResourceSlot resourceSlot && resourceSlot.isActive()) {
            if (!this.tryOpenResourceAmountScreen(resourceSlot, true, false)) {
                Platform.INSTANCE.sendPacketToServer(new PatternResourceSlotChangePacket(this.hoveredSlot.index, clickedButton == 1));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }
}
