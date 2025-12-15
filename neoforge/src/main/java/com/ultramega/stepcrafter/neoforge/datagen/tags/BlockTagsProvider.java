package com.ultramega.stepcrafter.neoforge.datagen.tags;

import com.ultramega.stepcrafter.common.registry.Blocks;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;

public class BlockTagsProvider extends TagsProvider<Block> {
    public static final TagKey<Block> MINEABLE = TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("mineable/pickaxe"));

    public BlockTagsProvider(final PackOutput packOutput,
                             final CompletableFuture<Provider> providerCompletableFuture,
                             final @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, Registries.BLOCK, providerCompletableFuture, MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(final Provider provider) {
        this.markAsMineable(Blocks.INSTANCE.getStepCrafter());
        this.markAsMineable(Blocks.INSTANCE.getStepRequester());
        this.markAsMineable(Blocks.INSTANCE.getStepCrafterManager());
        this.markAsMineable(Blocks.INSTANCE.getStepRequesterManager());
    }

    private void markAsMineable(final BlockColorMap<?, ?> map) {
        super.tag(MINEABLE).addAll(map.values().stream().map(b -> ResourceKey.create(
            Registries.BLOCK,
            BuiltInRegistries.BLOCK.getKey(b)
        )).toList());
    }

    private void markAsMineable(final Block block) {
        super.tag(MINEABLE).add(ResourceKey.create(
            Registries.BLOCK,
            BuiltInRegistries.BLOCK.getKey(block)
        ));
    }
}
