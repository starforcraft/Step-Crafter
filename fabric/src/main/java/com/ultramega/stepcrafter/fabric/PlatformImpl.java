package com.ultramega.stepcrafter.fabric;

import com.ultramega.stepcrafter.common.Config;
import com.ultramega.stepcrafter.common.Platform;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PlatformImpl implements Platform {
    @Override
    public Config getConfig() {
        return ConfigImpl.get();
    }

    @Override
    public ItemStack getCraftingRemainingItem(final ItemStack itemStack) {
        final Item item = itemStack.getItem();
        return !item.hasCraftingRemainingItem() || item.getCraftingRemainingItem() == null ? ItemStack.EMPTY : new ItemStack(item.getCraftingRemainingItem());
    }
}
