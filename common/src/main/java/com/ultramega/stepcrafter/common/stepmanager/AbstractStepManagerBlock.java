package com.ultramega.stepcrafter.common.stepmanager;

import com.refinedmods.refinedstorage.common.content.BlockProperties;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirectionType;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.EntityBlock;

public abstract class AbstractStepManagerBlock<T extends AbstractStepManagerBlock<T>> extends AbstractActiveColoredDirectionalBlock<OrientedDirection, T, BaseBlockItem>
    implements BlockItemProvider<BaseBlockItem>, EntityBlock {
    private final Identifier id;
    private final Component helpText;

    public AbstractStepManagerBlock(final Identifier id, final Component helpText, final DyeColor color, final MutableComponent name) {
        super(BlockProperties.stone(id), color, name);
        this.id = id;
        this.helpText = helpText;
    }

    @Override
    protected DirectionType<OrientedDirection> getDirectionType() {
        return OrientedDirectionType.INSTANCE;
    }

    @Override
    public boolean canAlwaysConnect() {
        return true;
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(this.id, this, this.helpText);
    }
}
