package com.ultramega.stepcrafter.common.registry;

import java.util.function.Consumer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class CreativeModeTabItems {
    private CreativeModeTabItems() {
    }

    public static void append(final Consumer<ItemStack> consumer) {
        final Consumer<ItemLike> itemConsumer = item -> consumer.accept(new ItemStack(item));
        itemConsumer.accept(Blocks.INSTANCE.getStepCrafter());
        itemConsumer.accept(Blocks.INSTANCE.getStepRequester());
        itemConsumer.accept(Items.INSTANCE.getSlotUpgrade());
    }
}
