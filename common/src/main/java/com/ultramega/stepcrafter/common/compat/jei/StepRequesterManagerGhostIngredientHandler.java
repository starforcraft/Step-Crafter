package com.ultramega.stepcrafter.common.compat.jei;

import com.ultramega.stepcrafter.common.steprequestermanager.StepRequesterManagerScreen;

import java.util.List;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;

public class StepRequesterManagerGhostIngredientHandler implements IGhostIngredientHandler<StepRequesterManagerScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(final StepRequesterManagerScreen screen,
                                               final ITypedIngredient<I> ingredient,
                                               final boolean doStart) {
        return PatternResourceGhostIngredientHandler.getTargetsTyped(screen, ingredient);
    }

    @Override
    public void onComplete() {
        // no op
    }
}
