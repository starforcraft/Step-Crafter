package com.ultramega.stepcrafter.common.stepmanager;

import com.refinedmods.refinedstorage.common.content.BlockConstants;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.common.support.direction.BiDirectionType;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.EntityBlock;

public abstract class AbstractStepManagerBlock<T extends AbstractStepManagerBlock<T>> extends AbstractActiveColoredDirectionalBlock<BiDirection, T, BaseBlockItem>
    implements BlockItemProvider<BaseBlockItem>, EntityBlock {
    private final Component helpText;

    public AbstractStepManagerBlock(final Component helpText, final DyeColor color, final MutableComponent name) {
        super(BlockConstants.PROPERTIES, color, name);
        this.helpText = helpText;
    }

    @Override
    protected DirectionType<BiDirection> getDirectionType() {
        return BiDirectionType.INSTANCE;
    }

    @Override
    public boolean canAlwaysConnect() {
        return true;
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(this, this.helpText);
    }
}
