package com.ultramega.stepcrafter.common.registry;

import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterBlockEntity;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterBlockEntity;

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
}
