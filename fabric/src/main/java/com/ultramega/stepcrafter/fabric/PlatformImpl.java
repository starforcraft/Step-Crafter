package com.ultramega.stepcrafter.fabric;

import com.ultramega.stepcrafter.common.Config;
import com.ultramega.stepcrafter.common.Platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

public class PlatformImpl implements Platform {
    @Override
    public Config getConfig() {
        return ConfigImpl.get();
    }

    @Override
    @Nullable
    public ItemStackTemplate getCraftingRemainder(final ItemStack itemStack) {
        return itemStack.getCraftingRemainder();
    }
}
