package com.ultramega.stepcrafter.common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

public interface Platform {
    Platform INSTANCE = new PlatformProxy();

    Config getConfig();

    @Nullable
    ItemStackTemplate getCraftingRemainder(ItemStack itemStack);
}
