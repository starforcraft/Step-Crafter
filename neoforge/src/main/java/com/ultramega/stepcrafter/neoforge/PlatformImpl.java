package com.ultramega.stepcrafter.neoforge;

import com.ultramega.stepcrafter.common.Platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import org.jspecify.annotations.Nullable;

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
    @Nullable
    public ItemStackTemplate getCraftingRemainder(final ItemStack itemStack) {
        return itemStack.getCraftingRemainder();
    }
}
