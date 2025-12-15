package com.ultramega.stepcrafter.common.registry;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;

import java.util.function.Consumer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class CreativeModeTabItems {
    private CreativeModeTabItems() {
    }

    public static void appendBlocks(final Consumer<ItemStack> consumer) {
        final Consumer<ItemLike> itemConsumer = item -> consumer.accept(new ItemStack(item));
        itemConsumer.accept(Blocks.INSTANCE.getStepCrafter());
        itemConsumer.accept(Blocks.INSTANCE.getStepRequester());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getStepCrafterManager());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getStepRequesterManager());

        itemConsumer.accept(Items.INSTANCE.getSlotUpgrade());
    }

    private static void appendDefaultBlockColor(final Consumer<ItemStack> consumer, final BlockColorMap<?, ?> map) {
        consumer.accept(new ItemStack(map.getDefault()));
    }

    public static void appendColoredVariants(final Consumer<ItemStack> consumer) {
        appendColoredBlocks(consumer, Blocks.INSTANCE.getStepCrafterManager());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getStepRequesterManager());
    }

    private static void appendColoredBlocks(final Consumer<ItemStack> consumer, final BlockColorMap<?, ?> map) {
        map.forEach((color, id, block) -> {
            if (!map.isDefaultColor(color)) {
                consumer.accept(new ItemStack(block.get()));
            }
        });
    }
}
