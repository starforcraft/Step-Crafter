package com.ultramega.stepcrafter.common.mixin;

import com.ultramega.stepcrafter.common.support.PlayerInventorySlotMarker;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "com.refinedmods.refinedstorage.common.support.PlayerInventorySlot")
public class MixinPlayerInventorySlot implements PlayerInventorySlotMarker {
}
