package com.ultramega.stepcrafter.common.mixin;

import com.refinedmods.refinedstorage.common.api.grid.view.AbstractGridResource;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractGridResource.class)
public interface AbstractGridResourceInvoker<T extends PlatformResourceKey> {
    @Accessor(value = "resource", remap = false)
    T stepcrafter$getResource();
}
