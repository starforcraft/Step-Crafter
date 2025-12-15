package com.ultramega.stepcrafter.common.stepcraftermanager;

import com.ultramega.stepcrafter.common.Platform;
import com.ultramega.stepcrafter.common.registry.Menus;
import com.ultramega.stepcrafter.common.stepmanager.AbstractStepManagerContainerMenu;
import com.ultramega.stepcrafter.common.stepmanager.StepManagerData;
import com.ultramega.stepcrafter.common.stepmanager.StepManagerSearchMode;

import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerViewType;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class StepCrafterManagerContainerMenu extends AbstractStepManagerContainerMenu {
    public StepCrafterManagerContainerMenu(final int syncId,
                                           final Inventory playerInventory,
                                           final StepManagerData data) {
        super(Menus.INSTANCE.getStepCrafterManager(), syncId, playerInventory, data);
    }

    StepCrafterManagerContainerMenu(final int syncId,
                                    final Inventory playerInventory,
                                    final StepCrafterManagerBlockEntity stepCrafterManager,
                                    final List<StepCrafterManagerBlockEntity.Group> groups) {
        super(Menus.INSTANCE.getStepCrafterManager(), syncId, playerInventory, stepCrafterManager, groups);
    }

    @Override
    public StepManagerSearchMode getSearchMode() {
        return Platform.INSTANCE.getConfig().getStepCrafterManager().getSearchMode();
    }

    @Override
    public void setSearchMode(final StepManagerSearchMode searchMode) {
        Platform.INSTANCE.getConfig().getStepCrafterManager().setSearchMode(searchMode);
        this.notifyListener();
    }

    @Override
    public AutocrafterManagerViewType getViewType() {
        return Platform.INSTANCE.getConfig().getStepCrafterManager().getViewType();
    }

    @Override
    public void setViewType(final AutocrafterManagerViewType toggle) {
        Platform.INSTANCE.getConfig().getStepCrafterManager().setViewType(toggle);
        this.notifyListener();
    }
}
