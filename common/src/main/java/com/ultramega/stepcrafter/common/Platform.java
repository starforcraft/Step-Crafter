package com.ultramega.stepcrafter.common;

import net.minecraft.world.item.ItemStack;

public interface Platform {
    Platform INSTANCE = new PlatformProxy();

    Config getConfig();

    ClientConfig getClientConfig();

    ItemStack getCraftingRemainingItem(ItemStack itemStack);
}
