package com.ultramega.stepcrafter.common.steprequestermanager;

import com.ultramega.stepcrafter.common.Platform;
import com.ultramega.stepcrafter.common.registry.Menus;
import com.ultramega.stepcrafter.common.stepmanager.AbstractStepManagerContainerMenu;
import com.ultramega.stepcrafter.common.stepmanager.StepManagerData;
import com.ultramega.stepcrafter.common.stepmanager.StepManagerSearchMode;

import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerViewType;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class StepRequesterManagerContainerMenu extends AbstractStepManagerContainerMenu {
    public StepRequesterManagerContainerMenu(final int syncId,
                                             final Inventory playerInventory,
                                             final StepManagerData data) {
        super(Menus.INSTANCE.getStepRequesterManager(), syncId, playerInventory, data);
    }

    StepRequesterManagerContainerMenu(final int syncId,
                                      final Inventory playerInventory,
                                      final StepRequesterManagerBlockEntity stepRequesterManager,
                                      final List<StepRequesterManagerBlockEntity.Group> groups) {
        super(Menus.INSTANCE.getStepRequesterManager(), syncId, playerInventory, stepRequesterManager, groups);
    }

    @Override
    public StepManagerSearchMode getSearchMode() {
        return Platform.INSTANCE.getConfig().getStepRequesterManager().getSearchMode();
    }

    @Override
    public void setSearchMode(final StepManagerSearchMode searchMode) {
        Platform.INSTANCE.getConfig().getStepRequesterManager().setSearchMode(searchMode);
        this.notifyListener();
    }

    @Override
    public AutocrafterManagerViewType getViewType() {
        return Platform.INSTANCE.getConfig().getStepRequesterManager().getViewType();
    }

    @Override
    public void setViewType(final AutocrafterManagerViewType toggle) {
        Platform.INSTANCE.getConfig().getStepRequesterManager().setViewType(toggle);
        this.notifyListener();
    }
}
