package com.ultramega.stepcrafter.neoforge.datagen.tags;

import com.ultramega.stepcrafter.common.registry.Blocks;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;
import static com.ultramega.stepcrafter.common.registry.Tags.STEP_CRAFTER_MANAGERS;
import static com.ultramega.stepcrafter.common.registry.Tags.STEP_REQUESTER_MANAGERS;

public class ItemTagsProviderImpl extends ItemTagsProvider {
    public ItemTagsProviderImpl(final PackOutput packOutput,
                                final CompletableFuture<Provider> registries,
                                final TagsProvider<Block> blockTagsProvider,
                                final ExistingFileHelper existingFileHelper) {
        super(packOutput, registries, blockTagsProvider.contentsGetter(), MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(final Provider provider) {
        this.addAllToTag(STEP_CRAFTER_MANAGERS,
            Blocks.INSTANCE.getStepCrafterManager().values().stream()
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
