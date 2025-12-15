package com.ultramega.stepcrafter.neoforge.datagen.recipe;

import com.ultramega.stepcrafter.common.registry.Blocks;
import com.ultramega.stepcrafter.common.registry.Tags;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public class RecoloringRecipeProvider extends RecipeProvider {
    public RecoloringRecipeProvider(final PackOutput output,
                                    final CompletableFuture<Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(final RecipeOutput output) {
        Blocks.INSTANCE.getStepCrafterManager().forEach((color, id, block) ->
            this.recipe(Tags.STEP_CRAFTER_MANAGERS, block.get().asItem(), color)
                .save(output, this.recipeId(color, "step_crafter_manager")));
        Blocks.INSTANCE.getStepRequesterManager().forEach((color, id, block) ->
            this.recipe(Tags.STEP_REQUESTER_MANAGERS, block.get().asItem(), color)
                .save(output, this.recipeId(color, "step_requester_manager")));
    }

    private ResourceLocation recipeId(final DyeColor color, final String suffix) {
        return createStepCrafterIdentifier("coloring/" + color.getName() + "_" + suffix);
    }

    private ShapelessRecipeBuilder recipe(final TagKey<Item> dyeable, final Item result, final DyeColor color) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
            .requires(dyeable)
            .requires(getDyeTag(color))
            .unlockedBy("has_" + dyeable.location().getPath(), has(dyeable));
    }

    private static TagKey<Item> getDyeTag(final DyeColor color) {
        return switch (color) {
            case RED -> net.neoforged.neoforge.common.Tags.Items.DYES_RED;
            case WHITE -> net.neoforged.neoforge.common.Tags.Items.DYES_WHITE;
            case ORANGE -> net.neoforged.neoforge.common.Tags.Items.DYES_ORANGE;
            case MAGENTA -> net.neoforged.neoforge.common.Tags.Items.DYES_MAGENTA;
            case LIGHT_BLUE -> net.neoforged.neoforge.common.Tags.Items.DYES_LIGHT_BLUE;
            case YELLOW -> net.neoforged.neoforge.common.Tags.Items.DYES_YELLOW;
            case LIME -> net.neoforged.neoforge.common.Tags.Items.DYES_LIME;
            case PINK -> net.neoforged.neoforge.common.Tags.Items.DYES_PINK;
            case GRAY -> net.neoforged.neoforge.common.Tags.Items.DYES_GRAY;
            case LIGHT_GRAY -> net.neoforged.neoforge.common.Tags.Items.DYES_LIGHT_GRAY;
            case CYAN -> net.neoforged.neoforge.common.Tags.Items.DYES_CYAN;
            case PURPLE -> net.neoforged.neoforge.common.Tags.Items.DYES_PURPLE;
            case BLUE -> net.neoforged.neoforge.common.Tags.Items.DYES_BLUE;
            case BROWN -> net.neoforged.neoforge.common.Tags.Items.DYES_BROWN;
            case GREEN -> net.neoforged.neoforge.common.Tags.Items.DYES_GREEN;
            case BLACK -> net.neoforged.neoforge.common.Tags.Items.DYES_BLACK;
        };
    }
}
