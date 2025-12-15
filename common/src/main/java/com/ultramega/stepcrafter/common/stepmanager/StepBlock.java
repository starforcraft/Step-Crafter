package com.ultramega.stepcrafter.common.stepmanager;

import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public interface StepBlock {
    Component getBlockName();

    PatternResourceContainerImpl getPatternContainer();

    int getSlotUpgradesCount();

    boolean isVisibleToTheStepManager();

    boolean isStepCrafter();

    BlockPos getLocalPosition();
}
