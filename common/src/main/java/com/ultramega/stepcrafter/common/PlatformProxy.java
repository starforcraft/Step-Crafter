package com.ultramega.stepcrafter.common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

public class PlatformProxy implements Platform {
    @Nullable
    private Platform platform;

    public static void loadPlatform(final Platform platform) {
        final PlatformProxy proxy = (PlatformProxy) INSTANCE;
        if (proxy.platform != null) {
            throw new IllegalStateException("Platform already set");
        }
        proxy.platform = platform;
    }

    @Override
    public Config getConfig() {
        return this.ensureLoaded().getConfig();
    }

    @Override
    @Nullable
    public ItemStackTemplate getCraftingRemainder(final ItemStack itemStack) {
        return this.ensureLoaded().getCraftingRemainder(itemStack);
    }

    private Platform ensureLoaded() {
        if (this.platform == null) {
            throw new IllegalStateException("Platform not loaded yet");
        }
        return this.platform;
    }
}
