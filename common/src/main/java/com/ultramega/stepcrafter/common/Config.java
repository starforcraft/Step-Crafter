package com.ultramega.stepcrafter.common;

import com.ultramega.stepcrafter.common.stepmanager.StepManagerSearchMode;

import com.refinedmods.refinedstorage.common.Config.SimpleEnergyUsageEntry;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerViewType;

public interface Config {
    SimpleEnergySpeedUsageEntry getStepCrafter();

    SimpleEnergyUsageEntry getStepRequester();

    StepManagerEntry getStepCrafterManager();

    StepManagerEntry getStepRequesterManager();

    SimpleEnergyUsageEntry getSlotUpgrade();

    interface SimpleEnergySpeedUsageEntry extends SimpleEnergyUsageEntry {
        int getSpeedMultiplier();
    }

    interface StepManagerEntry extends SimpleEnergyUsageEntry {
        void setSearchMode(StepManagerSearchMode searchMode);

        StepManagerSearchMode getSearchMode();

        void setViewType(AutocrafterManagerViewType viewType);

        AutocrafterManagerViewType getViewType();
    }
}
