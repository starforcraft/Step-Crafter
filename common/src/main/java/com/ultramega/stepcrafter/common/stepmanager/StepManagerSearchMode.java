package com.ultramega.stepcrafter.common.stepmanager;

import com.ultramega.stepcrafter.common.stepmanager.AbstractStepManagerContainerMenu.ViewGroup;

import net.minecraft.world.level.Level;

public enum StepManagerSearchMode {
    ALL,
    PATTERN_INPUTS,
    PATTERN_OUTPUTS,
    STEP_NAMES;

    public StepManagerSearchMode toggle() {
        return switch (this) {
            case ALL -> PATTERN_INPUTS;
            case PATTERN_INPUTS -> PATTERN_OUTPUTS;
            case PATTERN_OUTPUTS -> STEP_NAMES;
            case STEP_NAMES -> ALL;
        };
    }

    boolean isSlotVisible(final ViewGroup group,
                          final Level level,
                          final String normalizedQuery,
                          final int index) {
        return switch (this) {
            case ALL -> group.nameContains(normalizedQuery)
                || group.hasPatternInput(level, normalizedQuery, index)
                || group.hasPatternOutput(level, normalizedQuery, index);
            case PATTERN_INPUTS -> group.hasPatternInput(level, normalizedQuery, index);
            case PATTERN_OUTPUTS -> group.hasPatternOutput(level, normalizedQuery, index);
            case STEP_NAMES -> group.nameContains(normalizedQuery);
        };
    }
}
