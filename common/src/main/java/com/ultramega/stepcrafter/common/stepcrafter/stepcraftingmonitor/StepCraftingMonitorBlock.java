package com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor;

import com.ultramega.stepcrafter.common.registry.BlockEntities;
import com.ultramega.stepcrafter.common.registry.Blocks;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.BlockConstants;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.common.support.direction.BiDirectionType;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeBlockEntityTicker;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;

public class StepCraftingMonitorBlock extends AbstractActiveColoredDirectionalBlock<BiDirection, StepCraftingMonitorBlock, BaseBlockItem>
    implements BlockItemProvider<BaseBlockItem>, EntityBlock {
    private static final Component HELP = createStepCrafterTranslation("item", "step_crafting_monitor.help");
    private static final AbstractBlockEntityTicker<StepCraftingMonitorBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getStepCraftingMonitor, ACTIVE);

    public StepCraftingMonitorBlock(final DyeColor color, final MutableComponent name) {
        super(BlockConstants.PROPERTIES, color, name);
    }

    @Override
    public BlockColorMap<StepCraftingMonitorBlock, BaseBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getStepCraftingMonitor();
    }

    @Override
    public boolean canAlwaysConnect() {
        return true;
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(this, HELP);
    }

    @Override
    protected DirectionType<BiDirection> getDirectionType() {
        return BiDirectionType.INSTANCE;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        return new StepCraftingMonitorBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }
}
