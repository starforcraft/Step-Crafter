package com.ultramega.stepcrafter.common;

import com.refinedmods.refinedstorage.common.Config.SimpleEnergyUsageEntry;

public interface Config {
    SimpleEnergySpeedUsageEntry getStepCrafter();

    SimpleEnergyUsageEntry getStepRequester();

    SimpleEnergyUsageEntry getSlotUpgrade();

    interface SimpleEnergySpeedUsageEntry extends SimpleEnergyUsageEntry {
        int getSpeedMultiplier();
    }
}
