package com.ultramega.stepcrafter.neoforge;

import com.ultramega.stepcrafter.common.Platform;

import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

public final class PlatformImpl implements Platform {
    private final ConfigImpl config = new ConfigImpl();

    private final ClientConfigImpl clientConfig = new ClientConfigImpl();

    public PlatformImpl(final ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, this.config.getSpec());
        modContainer.registerConfig(ModConfig.Type.CLIENT, this.clientConfig.getSpec());
    }

    @Override
    public ConfigImpl getConfig() {
        return this.config;
    }

    @Override
    public ClientConfigImpl getClientConfig() {
        return this.clientConfig;
    }

    @Override
    public ItemStack getCraftingRemainingItem(final ItemStack itemStack) {
        return itemStack.getItem().getCraftingRemainingItem(itemStack);
    }
}
