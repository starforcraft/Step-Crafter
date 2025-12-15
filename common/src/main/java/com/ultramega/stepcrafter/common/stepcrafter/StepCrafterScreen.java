package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.stepcrafter.widget.VisibleToTheStepCrafterManagerSideButtonWidget;
import com.ultramega.stepcrafter.common.support.AbstractEditableNameScreen;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import com.refinedmods.refinedstorage.common.api.autocrafting.PatternOutputRenderingScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class StepCrafterScreen extends AbstractEditableNameScreen<StepCrafterContainerMenu> implements PatternOutputRenderingScreen {
    public StepCrafterScreen(final StepCrafterContainerMenu menu,
                             final Inventory playerInventory,
                             final Component title) {
        super(menu, playerInventory, new TextMarquee(title, getTitleMaxWidth()));
    }

    @Override
    protected void init() {
        super.init();

        this.addSideButton(new RedstoneModeSideButtonWidget(
            this.getMenu().getProperty(PropertyTypes.REDSTONE_MODE)
        ));
        this.addSideButton(new VisibleToTheStepCrafterManagerSideButtonWidget(
            this.getMenu().getProperty(StepCrafterPropertyTypes.VISIBLE_TO_THE_STEP_CRAFTER_MANAGER)
        ));

        // TODO: add a side button about redstone pulse (either request all items or only based on the strength)
        //  also show "not enough ingredients", "crafting" or "checkmark" icon in a corner
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderSlotMinMax(graphics, false);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (this.hoveredSlot instanceof PatternResourceSlot resourceSlot && resourceSlot.isActive()) {
            if (this.tryOpenResourceAmountScreen(resourceSlot, false, true)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    @Override
    public boolean canDisplayOutput(final ItemStack stack) {
        return this.getMenu().containsPattern(stack);
    }
}
