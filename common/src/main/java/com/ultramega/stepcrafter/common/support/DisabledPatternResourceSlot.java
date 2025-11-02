package com.ultramega.stepcrafter.common.support;

import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DisabledPatternResourceSlot extends PatternResourceSlot {
    public DisabledPatternResourceSlot(final PatternResourceContainerImpl container,
                                       final int index,
                                       final Component helpText,
                                       final int x,
                                       final int y,
                                       final Level level,
                                       final boolean isFilter) {
        super(container, index, helpText, x, y, level, isFilter);
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(final Player player) {
        return false;
    }
}
