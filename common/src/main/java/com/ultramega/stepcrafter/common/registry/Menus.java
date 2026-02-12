package com.ultramega.stepcrafter.common.registry;

import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterContainerMenu;
import com.ultramega.stepcrafter.common.stepcrafter.manager.StepCrafterManagerContainerMenu;
import com.ultramega.stepcrafter.common.stepcrafter.preview.StepCraftingPreviewContainerMenu;
import com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor.StepCraftingMonitorContainerMenu;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterContainerMenu;
import com.ultramega.stepcrafter.common.steprequester.manager.StepRequesterManagerContainerMenu;

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
    private Supplier<MenuType<StepCraftingMonitorContainerMenu>> stepCraftingMonitor;
    @Nullable
    private Supplier<MenuType<StepRequesterManagerContainerMenu>> stepRequesterManager;
    @Nullable
    private Supplier<MenuType<StepCraftingPreviewContainerMenu>> stepCraftingPreview;

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

    public MenuType<StepCraftingMonitorContainerMenu> getStepCraftingMonitor() {
        return requireNonNull(this.stepCraftingMonitor).get();
    }

    public void setStepCraftingMonitor(final Supplier<MenuType<StepCraftingMonitorContainerMenu>> stepCraftingMonitor) {
        this.stepCraftingMonitor = stepCraftingMonitor;
    }

    public MenuType<StepRequesterManagerContainerMenu> getStepRequesterManager() {
        return requireNonNull(this.stepRequesterManager).get();
    }

    public void setStepRequesterManager(final Supplier<MenuType<StepRequesterManagerContainerMenu>> stepRequesterManager) {
        this.stepRequesterManager = stepRequesterManager;
    }

    public MenuType<StepCraftingPreviewContainerMenu> getStepCraftingPreview() {
        return requireNonNull(this.stepCraftingPreview).get();
    }

    public void setStepCraftingPreview(final Supplier<MenuType<StepCraftingPreviewContainerMenu>> supplier) {
        this.stepCraftingPreview = supplier;
    }
}
