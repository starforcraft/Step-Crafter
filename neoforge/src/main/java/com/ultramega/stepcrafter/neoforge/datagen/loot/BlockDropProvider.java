package com.ultramega.stepcrafter.neoforge.datagen.loot;

import com.ultramega.stepcrafter.common.registry.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;

public class BlockDropProvider extends BlockLootSubProvider {
    public BlockDropProvider(final HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        this.drop(Blocks.INSTANCE.getStepCrafter());
        this.drop(Blocks.INSTANCE.getStepRequester());
        Blocks.INSTANCE.getStepCrafterManager().forEach((color, id, block) -> this.drop(block.get()));
        Blocks.INSTANCE.getStepRequesterManager().forEach((color, id, block) -> this.drop(block.get()));
    }

    private void drop(final Block block) {
        this.add(block, this.createSingleItemTable(block)
            .apply(copyName()));
    }

    private static CopyComponentsFunction.Builder copyName() {
        return CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
            .include(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        final List<Block> blocks = new ArrayList<>();
        blocks.add(Blocks.INSTANCE.getStepCrafter());
        blocks.add(Blocks.INSTANCE.getStepRequester());
        blocks.addAll(Blocks.INSTANCE.getStepCrafterManager().values());
        blocks.addAll(Blocks.INSTANCE.getStepRequesterManager().values());
        return blocks;
    }
}
