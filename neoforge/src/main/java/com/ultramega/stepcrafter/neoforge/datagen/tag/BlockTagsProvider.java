package com.ultramega.stepcrafter.neoforge.datagen.tag;

import com.ultramega.stepcrafter.common.registry.Blocks;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;

public class BlockTagsProvider extends IntrinsicHolderTagsProvider<Block> {
    public static final TagKey<Block> MINEABLE = TagKey.create(Registries.BLOCK,
        Identifier.withDefaultNamespace("mineable/pickaxe"));

    @SuppressWarnings("deprecation")
    public BlockTagsProvider(final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, Registries.BLOCK, registries, block -> block.builtInRegistryHolder().key(), MOD_ID);
    }

    @Override
    protected void addTags(final Provider provider) {
        this.markAsMineable(Blocks.INSTANCE.getStepCrafter());
        this.markAsMineable(Blocks.INSTANCE.getStepRequester());
        this.markAsMineable(Blocks.INSTANCE.getStepCrafterManager());
        this.markAsMineable(Blocks.INSTANCE.getStepCraftingMonitor());
        this.markAsMineable(Blocks.INSTANCE.getStepRequesterManager());
    }

    private <T extends Block & BlockItemProvider<I>, I extends BlockItem> void markAsMineable(final BlockColorMap<T, I> map) {
        this.tag(MINEABLE).addAll(map.values().stream().map(b -> (Block) b).toList());
    }

    private void markAsMineable(final Block block) {
        this.tag(MINEABLE).add(block);
    }
}
