package com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor;

import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatusProvider;

import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorWatcher;

public interface StepCraftingMonitor extends StepTaskStatusProvider {
    void addWatcher(AutocraftingMonitorWatcher watcher);

    void removeWatcher(AutocraftingMonitorWatcher watcher);

    boolean isStepCraftingMonitorActive();
}
