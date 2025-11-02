package com.ultramega.stepcrafter.common.mixin;

import com.ultramega.stepcrafter.common.support.NetworkGetter;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "com.refinedmods.refinedstorage.common.grid.WirelessGrid")
public class MixinWirelessGrid implements NetworkGetter {
    @Shadow(remap = false)
    @Final
    private NetworkItemContext context;

    @Nullable
    @Override
    public Network stepcrafter$getNetwork() {
        return this.context.resolveNetwork().orElse(null);
    }
}
