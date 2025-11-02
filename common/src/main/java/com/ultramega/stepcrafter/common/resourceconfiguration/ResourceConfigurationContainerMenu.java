package com.ultramega.stepcrafter.common.resourceconfiguration;

import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ResourceConfigurationContainerMenu extends AbstractBaseContainerMenu {
    private final PatternResourceSlot resourceSlot;

    public ResourceConfigurationContainerMenu(final PatternResourceSlot slot, final int x, final int y) {
        super(null, 0);
        this.resourceSlot = slot.forAmountScreen(x, y);
        this.addSlot(this.resourceSlot);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }

    public PatternResourceSlot getResourceSlot() {
        return this.resourceSlot;
    }
}
