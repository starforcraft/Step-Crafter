package com.ultramega.stepcrafter.neoforge.datagen.tag;

import com.ultramega.stepcrafter.common.registry.Blocks;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;
import static com.ultramega.stepcrafter.common.registry.Tags.STEP_CRAFTER_MANAGERS;
import static com.ultramega.stepcrafter.common.registry.Tags.STEP_CRAFTING_MONITORS;
import static com.ultramega.stepcrafter.common.registry.Tags.STEP_REQUESTER_MANAGERS;

public class ItemTagsProvider extends BlockTagCopyingItemTagProvider {
    public ItemTagsProvider(final PackOutput packOutput,
                            final CompletableFuture<HolderLookup.Provider> registries,
                            final CompletableFuture<TagLookup<Block>> blockTagsProvider) {
        super(packOutput, registries, blockTagsProvider, MOD_ID);
    }

    @Override
    protected void addTags(final Provider provider) {
        this.addAllToTag(STEP_CRAFTER_MANAGERS,
            Blocks.INSTANCE.getStepCrafterManager().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        this.addAllToTag(STEP_CRAFTING_MONITORS,
            Blocks.INSTANCE.getStepCraftingMonitor().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        this.addAllToTag(STEP_REQUESTER_MANAGERS,
            Blocks.INSTANCE.getStepRequesterManager().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
    }

    private <T extends Item> void addAllToTag(final TagKey<Item> t, final Collection<Supplier<T>> items) {
        super.tag(t).add(items.stream().map(Supplier::get).toArray(Item[]::new)).replace(false);
    }
}
