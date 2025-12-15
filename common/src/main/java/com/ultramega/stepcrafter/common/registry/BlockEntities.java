package com.ultramega.stepcrafter.common.registry;

import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterBlockEntity;
import com.ultramega.stepcrafter.common.stepcraftermanager.StepCrafterManagerBlockEntity;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterBlockEntity;
import com.ultramega.stepcrafter.common.steprequestermanager.StepRequesterManagerBlockEntity;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.entity.BlockEntityType;

import static java.util.Objects.requireNonNull;

public final class BlockEntities {
    public static final BlockEntities INSTANCE = new BlockEntities();

    @Nullable
    private Supplier<BlockEntityType<StepCrafterBlockEntity>> stepCrafter;
    @Nullable
    private Supplier<BlockEntityType<StepRequesterBlockEntity>> stepRequester;
    @Nullable
    private Supplier<BlockEntityType<StepCrafterManagerBlockEntity>> stepCrafterManager;
    @Nullable
    private Supplier<BlockEntityType<StepRequesterManagerBlockEntity>> stepRequesterManager;

    private BlockEntities() {
    }

    public BlockEntityType<StepCrafterBlockEntity> getStepCrafter() {
        return requireNonNull(this.stepCrafter).get();
    }

    public void setStepCrafter(final Supplier<BlockEntityType<StepCrafterBlockEntity>> supplier) {
        this.stepCrafter = supplier;
    }

    public BlockEntityType<StepRequesterBlockEntity> getStepRequester() {
        return requireNonNull(this.stepRequester).get();
    }

    public void setStepRequester(final Supplier<BlockEntityType<StepRequesterBlockEntity>> supplier) {
        this.stepRequester = supplier;
    }

    public BlockEntityType<StepCrafterManagerBlockEntity> getStepCrafterManager() {
        return requireNonNull(this.stepCrafterManager).get();
    }

    public void setStepCrafterManager(final Supplier<BlockEntityType<StepCrafterManagerBlockEntity>> supplier) {
        this.stepCrafterManager = supplier;
    }

    public BlockEntityType<StepRequesterManagerBlockEntity> getStepRequesterManager() {
        return requireNonNull(this.stepRequesterManager).get();
    }

    public void setStepRequesterManager(final Supplier<BlockEntityType<StepRequesterManagerBlockEntity>> supplier) {
        this.stepRequesterManager = supplier;
    }
}
