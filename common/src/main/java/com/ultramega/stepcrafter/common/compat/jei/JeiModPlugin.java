package com.ultramega.stepcrafter.common.compat.jei;

import com.ultramega.stepcrafter.common.steprequester.StepRequesterScreen;
import com.ultramega.stepcrafter.common.steprequester.manager.StepRequesterManagerScreen;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.Identifier;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

@JeiPlugin
public class JeiModPlugin implements IModPlugin {
    private static final Identifier ID = createStepCrafterIdentifier("plugin");

    @Override
    public void registerGuiHandlers(final IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(StepRequesterScreen.class, new PatternResourceGhostIngredientHandler());
        registration.addGhostIngredientHandler(StepRequesterManagerScreen.class, new StepRequesterManagerGhostIngredientHandler());
    }

    @Override
    public Identifier getPluginUid() {
        return ID;
    }
}
