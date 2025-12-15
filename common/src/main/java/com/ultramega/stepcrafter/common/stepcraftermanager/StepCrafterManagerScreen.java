package com.ultramega.stepcrafter.common.stepcraftermanager;

import com.ultramega.stepcrafter.common.stepmanager.AbstractStepManagerScreen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StepCrafterManagerScreen extends AbstractStepManagerScreen<StepCrafterManagerContainerMenu> {
    public StepCrafterManagerScreen(final StepCrafterManagerContainerMenu menu,
                                    final Inventory playerInventory,
                                    final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected boolean showBatchSize() {
        return false;
    }

    @Override
    protected boolean pressShiftToOpenConfig() {
        return true;
    }
}
