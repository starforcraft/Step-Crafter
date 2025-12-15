package com.ultramega.stepcrafter.common.stepmanager;

@FunctionalInterface
interface StepManagerWatcher {
    void activeChanged(boolean active);
}
