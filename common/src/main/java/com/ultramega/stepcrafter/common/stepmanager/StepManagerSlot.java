package com.ultramega.stepcrafter.common.stepmanager;

import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class StepManagerSlot extends PatternResourceSlot {
    private final int originalY;
    private final int startY;
    private final int endY;
    private final boolean active;

    public StepManagerSlot(final PatternResourceContainerImpl container,
                           final int index,
                           final Component helpText,
                           final int x,
                           final int y,
                           final IntIntPair startEndY,
                           final Level level,
                           final boolean isFilter,
                           final boolean active) {
        super(container, index, helpText, x, y, level, isFilter);
        this.originalY = y;
        this.startY = startEndY.firstInt();
        this.endY = startEndY.secondInt();
        this.active = active;
    }

    int getOriginalY() {
        return this.originalY;
    }

    @Override
    public boolean isActive() {
        return this.y >= this.startY && this.y < this.endY && this.active;
    }

    @Override
    public boolean isHighlightable() {
        return false; // we render the highlight in the scissor render
    }
}
