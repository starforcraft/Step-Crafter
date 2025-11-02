package com.ultramega.stepcrafter.common;

import com.ultramega.stepcrafter.common.registry.Menus;
import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterScreen;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterScreen;

import com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenRegistration;

public abstract class AbstractClientModInitializer {
    protected static void registerScreens(final ScreenRegistration registration) {
        registration.register(Menus.INSTANCE.getStepCrafter(), StepCrafterScreen::new);
        registration.register(Menus.INSTANCE.getStepRequester(), StepRequesterScreen::new);
    }
}
