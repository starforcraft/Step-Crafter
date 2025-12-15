package com.ultramega.stepcrafter.common.mixin;

import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerViewType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AutocrafterManagerViewType.class)
public interface AutocrafterManagerViewTypeInvoker {
    @Invoker(value = "toggle", remap = false)
    AutocrafterManagerViewType stepcrafter$toggle();
}
