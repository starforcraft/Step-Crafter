package com.ultramega.stepcrafter.common.support;

import com.refinedmods.refinedstorage.common.support.containermenu.TransferDestination;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

public record ResourceInventoryDestination(AbstractPatternResourceContainerMenu destination) implements TransferDestination {
    @Nullable
    @Override
    public ItemStack transfer(final ItemStack stack) {
        this.destination.addToResourceSlotIfNotExisting(stack);
        return null;
    }
}
