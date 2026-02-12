package com.ultramega.stepcrafter.common;

import com.ultramega.stepcrafter.common.registry.Menus;
import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterScreen;
import com.ultramega.stepcrafter.common.stepcrafter.manager.StepCrafterManagerScreen;
import com.ultramega.stepcrafter.common.stepcrafter.preview.StepCraftingPreviewContainerMenu;
import com.ultramega.stepcrafter.common.stepcrafter.preview.StepCraftingPreviewScreen;
import com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor.StepCraftingMonitorScreen;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterScreen;
import com.ultramega.stepcrafter.common.steprequester.manager.StepRequesterManagerScreen;

import com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenConstructor;
import com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenRegistration;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class AbstractClientModInitializer {
    protected static void registerScreens(final ScreenRegistration registration) {
        registration.register(Menus.INSTANCE.getStepCrafter(), StepCrafterScreen::new);
        registration.register(Menus.INSTANCE.getStepRequester(), StepRequesterScreen::new);
        registration.register(Menus.INSTANCE.getStepCrafterManager(), StepCrafterManagerScreen::new);
        registration.register(Menus.INSTANCE.getStepCraftingMonitor(), StepCraftingMonitorScreen::new);
        registration.register(Menus.INSTANCE.getStepRequesterManager(), StepRequesterManagerScreen::new);
        registration.register(Menus.INSTANCE.getStepCraftingPreview(),
            new ScreenConstructor<StepCraftingPreviewContainerMenu, StepCraftingPreviewScreen>() {
                @Override
                public StepCraftingPreviewScreen create(final StepCraftingPreviewContainerMenu menu,
                                                        final Inventory inventory,
                                                        final Component title) {
                    return new StepCraftingPreviewScreen(menu, inventory);
                }
            });
    }
}
