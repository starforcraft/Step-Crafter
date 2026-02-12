package com.ultramega.stepcrafter.common.mixin;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingRequest;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AutocraftingRequest.class)
public interface AutocraftingRequestInvoker {
    @Invoker(value = "getId", remap = false)
    UUID stepcrafter$getId();

    @Invoker(value = "getResource", remap = false)
    ResourceKey stepcrafter$getResource();

    @Invoker(value = "getAmount", remap = false)
    double stepcrafter$getAmount();
}
