package com.ultramega.stepcrafter.common;

import com.ultramega.stepcrafter.common.registry.Blocks;

import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeDestination;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public enum UpgradeDestinations implements UpgradeDestination {
    STEP_CRAFTER(ContentNames.STEP_CRAFTER, () -> new ItemStack(Blocks.INSTANCE.getStepCrafter())),
    STEP_REQUESTER(ContentNames.STEP_REQUESTER, () -> new ItemStack(Blocks.INSTANCE.getStepRequester()));

    private final Component name;
    private final Supplier<ItemStack> stackFactory;
    @Nullable
    private ItemStack cachedStack;

    UpgradeDestinations(final Component name, final Supplier<ItemStack> stackFactory) {
        this.name = name;
        this.stackFactory = stackFactory;
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public ItemStack getStackRepresentation() {
        if (this.cachedStack == null) {
            this.cachedStack = this.stackFactory.get();
        }
        return this.cachedStack;
    }
}
