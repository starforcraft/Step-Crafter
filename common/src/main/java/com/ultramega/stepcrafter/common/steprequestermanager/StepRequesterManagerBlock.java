package com.ultramega.stepcrafter.common.steprequestermanager;

import com.ultramega.stepcrafter.common.registry.BlockEntities;
import com.ultramega.stepcrafter.common.registry.Blocks;
import com.ultramega.stepcrafter.common.stepmanager.AbstractStepManagerBlock;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeBlockEntityTicker;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;

public class StepRequesterManagerBlock extends AbstractStepManagerBlock<StepRequesterManagerBlock> {
    private static final Component HELP = createStepCrafterTranslation("item", "step_requester_manager.help");
    private static final AbstractBlockEntityTicker<StepRequesterManagerBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getStepRequesterManager, ACTIVE);

    public StepRequesterManagerBlock(final DyeColor color, final MutableComponent name) {
        super(HELP, color, name);
    }

    @Override
    public BlockColorMap<StepRequesterManagerBlock, BaseBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getStepRequesterManager();
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        return new StepRequesterManagerBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }
}
