package com.ultramega.stepcrafter.common.steprequestermanager;

import com.ultramega.stepcrafter.common.stepmanager.AbstractStepManagerScreen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StepRequesterManagerScreen extends AbstractStepManagerScreen<StepRequesterManagerContainerMenu> {
    public StepRequesterManagerScreen(final StepRequesterManagerContainerMenu menu,
                                      final Inventory playerInventory,
                                      final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected boolean showBatchSize() {
        return true;
    }

    @Override
    protected boolean pressShiftToOpenConfig() {
        return false;
    }
}
