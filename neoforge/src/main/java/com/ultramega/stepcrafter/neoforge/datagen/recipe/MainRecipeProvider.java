package com.ultramega.stepcrafter.neoforge.datagen.recipe;

import com.ultramega.stepcrafter.common.registry.Blocks;

import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.misc.ProcessorItem;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.neoforged.neoforge.common.Tags;

public class MainRecipeProvider extends RecipeProvider {
    public MainRecipeProvider(final HolderLookup.Provider registries, final RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        this.stepCrafter();
        this.stepRequester();
        this.stepCrafterManager();
        this.stepCraftingMonitor();
        this.stepRequesterManager();
        this.slotUpgrade();
    }

    private void stepCrafter() {
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, Blocks.INSTANCE.getStepCrafter())
            .pattern("ECE")
            .pattern("AMA")
            .pattern("EDE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('C', Items.INSTANCE.getConstructionCore())
            .define('A', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('M', com.refinedmods.refinedstorage.common.content.Blocks.INSTANCE.getAutocrafter().getDefault())
            .define('D', Items.INSTANCE.getDestructionCore())
            .unlockedBy("has_autocrafter", this.has(com.refinedmods.refinedstorage.common.content.Blocks.INSTANCE.getAutocrafter().getDefault()))
            .save(this.output);
    }

    private void stepRequester() {
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, Blocks.INSTANCE.getStepRequester())
            .pattern("ECE")
            .pattern("AMA")
            .pattern("EDE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('C', com.refinedmods.refinedstorage.common.content.Blocks.INSTANCE.getDetector().getDefault())
            .define('A', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('M', com.refinedmods.refinedstorage.common.content.Blocks.INSTANCE.getMachineCasing())
            .define('D', Items.INSTANCE.getDestructionCore())
            .unlockedBy("has_detector", this.has(com.refinedmods.refinedstorage.common.content.Blocks.INSTANCE.getDetector().getDefault()))
            .save(this.output);
    }

    private void stepCrafterManager() {
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, Blocks.INSTANCE.getStepCrafterManager().getDefault())
            .pattern("PCG")
            .pattern("EMG")
            .pattern("PCG")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('C', Blocks.INSTANCE.getStepCrafter())
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('M', com.refinedmods.refinedstorage.common.content.Blocks.INSTANCE.getMachineCasing())
            .unlockedBy("has_step_crafter", this.has(Blocks.INSTANCE.getStepCrafter()))
            .save(this.output);
    }

    private void stepCraftingMonitor() {
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, Blocks.INSTANCE.getStepCraftingMonitor().getDefault())
            .pattern("PCG")
            .pattern("EMG")
            .pattern("PAG")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('C', Blocks.INSTANCE.getStepCrafter())
            .define('A', Items.INSTANCE.getPattern())
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('M', com.refinedmods.refinedstorage.common.content.Blocks.INSTANCE.getMachineCasing())
            .unlockedBy("has_step_crafter", this.has(Blocks.INSTANCE.getStepCrafter()))
            .save(this.output);
    }

    private void stepRequesterManager() {
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, Blocks.INSTANCE.getStepRequesterManager().getDefault())
            .pattern("PCG")
            .pattern("EMG")
            .pattern("PCG")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('C', Blocks.INSTANCE.getStepRequester())
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('M', com.refinedmods.refinedstorage.common.content.Blocks.INSTANCE.getMachineCasing())
            .unlockedBy("has_step_requester", this.has(Blocks.INSTANCE.getStepRequester()))
            .save(this.output);
    }

    private void slotUpgrade() {
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, com.ultramega.stepcrafter.common.registry.Items.INSTANCE.getSlotUpgrade())
            .pattern("ECE")
            .pattern("CUC")
            .pattern("EEE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('C', net.minecraft.world.level.block.Blocks.CHEST)
            .define('U', Items.INSTANCE.getUpgrade())
            .unlockedBy("has_upgrade", this.has(Items.INSTANCE.getUpgrade()))
            .save(this.output);
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(final HolderLookup.Provider registries,
                                                      final RecipeOutput output) {
            return new MainRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Step Crafter recipes";
        }
    }
}
