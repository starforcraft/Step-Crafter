package com.ultramega.stepcrafter.common.registry;

import com.ultramega.stepcrafter.common.ContentIds;
import com.ultramega.stepcrafter.common.ContentNames;
import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterBlock;
import com.ultramega.stepcrafter.common.stepcraftermanager.StepCrafterManagerBlock;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterBlock;
import com.ultramega.stepcrafter.common.steprequestermanager.StepRequesterManagerBlock;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import static com.refinedmods.refinedstorage.common.content.Blocks.COLOR;
import static java.util.Objects.requireNonNull;

public final class Blocks {
    public static final Blocks INSTANCE = new Blocks();

    @Nullable
    private Supplier<StepCrafterBlock> stepCrafter;
    @Nullable
    private Supplier<StepRequesterBlock> stepRequester;
    private final BlockColorMap<StepCrafterManagerBlock, BaseBlockItem> stepCrafterManager = new BlockColorMap<>(
        StepCrafterManagerBlock::new,
        ContentIds.STEP_CRAFTER_MANAGER,
        ContentNames.STEP_CRAFTER_MANAGER,
        COLOR
    );
    private final BlockColorMap<StepRequesterManagerBlock, BaseBlockItem> stepRequesterManager = new BlockColorMap<>(
        StepRequesterManagerBlock::new,
        ContentIds.STEP_REQUESTER_MANAGER,
        ContentNames.STEP_REQUESTER_MANAGER,
        COLOR
    );

    private Blocks() {
    }

    public void setStepCrafter(final Supplier<StepCrafterBlock> stepCrafter) {
        this.stepCrafter = stepCrafter;
    }

    public StepCrafterBlock getStepCrafter() {
        return requireNonNull(this.stepCrafter).get();
    }

    public void setStepRequester(final Supplier<StepRequesterBlock> stepRequester) {
        this.stepRequester = stepRequester;
    }

    public StepRequesterBlock getStepRequester() {
        return requireNonNull(this.stepRequester).get();
    }

    public BlockColorMap<StepCrafterManagerBlock, BaseBlockItem> getStepCrafterManager() {
        return this.stepCrafterManager;
    }

    public BlockColorMap<StepRequesterManagerBlock, BaseBlockItem> getStepRequesterManager() {
        return this.stepRequesterManager;
    }
}
