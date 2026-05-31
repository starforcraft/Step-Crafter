package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.stepcrafter.widget.AutocrafterPrioritySideButtonWidget;
import com.ultramega.stepcrafter.common.stepcrafter.widget.InsertIntoPointedContainerSideButtonWidget;
import com.ultramega.stepcrafter.common.stepcrafter.widget.VisibleToTheStepCrafterManagerSideButtonWidget;
import com.ultramega.stepcrafter.common.support.AbstractEditableNameScreen;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import com.refinedmods.refinedstorage.common.api.autocrafting.PatternOutputRenderingScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
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
        this.addSideButton(new AutocrafterPrioritySideButtonWidget(
            this.getMenu().getProperty(StepCrafterPropertyTypes.PRIORITY),
            this.playerInventory,
            this
        ));
        this.addSideButton(new VisibleToTheStepCrafterManagerSideButtonWidget(
            this.getMenu().getProperty(StepCrafterPropertyTypes.VISIBLE_TO_THE_STEP_CRAFTER_MANAGER)
        ));
        this.addSideButton(new InsertIntoPointedContainerSideButtonWidget(
            this.getMenu().getProperty(StepCrafterPropertyTypes.INSERT_INTO_POINTED_CONTAINER)
        ));
    }

    @Override
    protected void extractSlot(final GuiGraphicsExtractor graphics, final Slot slot, final int mouseX, final int mouseY) {
        super.extractSlot(graphics, slot, mouseX, mouseY);
        this.renderSlotMinMax(graphics, false);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (this.hoveredSlot instanceof PatternResourceSlot resourceSlot && resourceSlot.isActive()) {
            if (this.tryOpenResourceAmountScreen(resourceSlot, false, true)) {
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean canDisplayOutput(final ItemStack stack) {
        return this.getMenu().containsPattern(stack);
    }
}
