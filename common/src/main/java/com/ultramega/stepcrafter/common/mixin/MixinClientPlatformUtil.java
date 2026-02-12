package com.ultramega.stepcrafter.common.mixin;

import com.ultramega.stepcrafter.common.stepcrafter.preview.StepCraftingPreviewScreen;

import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlatformUtil.class)
public abstract class MixinClientPlatformUtil {
    @Inject(method = "autocraftingPreviewCancelResponseReceived", at = @At("HEAD"), cancellable = true)
    private static void autocraftingPreviewCancelResponseReceived(final CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof StepCraftingPreviewScreen screen) {
            screen.cancelResponseReceived();
            ci.cancel();
        }
    }
}
