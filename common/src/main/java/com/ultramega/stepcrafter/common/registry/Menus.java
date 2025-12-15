package com.ultramega.stepcrafter.common.registry;

import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterContainerMenu;
import com.ultramega.stepcrafter.common.stepcraftermanager.StepCrafterManagerContainerMenu;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterContainerMenu;
import com.ultramega.stepcrafter.common.steprequestermanager.StepRequesterManagerContainerMenu;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.inventory.MenuType;

import static java.util.Objects.requireNonNull;

public final class Menus {
    public static final Menus INSTANCE = new Menus();

    @Nullable
    private Supplier<MenuType<StepCrafterContainerMenu>> stepCrafter;
    @Nullable
    private Supplier<MenuType<StepRequesterContainerMenu>> stepRequester;
    @Nullable
    private Supplier<MenuType<StepCrafterManagerContainerMenu>> stepCrafterManager;
    @Nullable
    private Supplier<MenuType<StepRequesterManagerContainerMenu>> stepRequesterManager;

    private Menus() {
    }

    public MenuType<StepCrafterContainerMenu> getStepCrafter() {
        return requireNonNull(this.stepCrafter).get();
    }

    public void setStepCrafter(final Supplier<MenuType<StepCrafterContainerMenu>> supplier) {
        this.stepCrafter = supplier;
    }

    public MenuType<StepRequesterContainerMenu> getStepRequester() {
        return requireNonNull(this.stepRequester).get();
    }

    public void setStepRequester(final Supplier<MenuType<StepRequesterContainerMenu>> supplier) {
        this.stepRequester = supplier;
    }

    public MenuType<StepCrafterManagerContainerMenu> getStepCrafterManager() {
        return requireNonNull(this.stepCrafterManager).get();
    }

    public void setStepCrafterManager(final Supplier<MenuType<StepCrafterManagerContainerMenu>> stepCrafterManager) {
        this.stepCrafterManager = stepCrafterManager;
    }

    public MenuType<StepRequesterManagerContainerMenu> getStepRequesterManager() {
        return requireNonNull(this.stepRequesterManager).get();
    }

    public void setStepRequesterManager(final Supplier<MenuType<StepRequesterManagerContainerMenu>> stepRequesterManager) {
        this.stepRequesterManager = stepRequesterManager;
    }
}
