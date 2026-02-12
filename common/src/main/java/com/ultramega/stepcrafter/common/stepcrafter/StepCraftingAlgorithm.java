package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.Platform;
import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.api.storage.root.FuzzyRootStorage;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

public final class StepCraftingAlgorithm {
    private StepCraftingAlgorithm() {
    }

    @Nullable
    public static CraftPreparation prepareCraft(final RootStorage storageComponent,
                                                final Pattern pattern,
                                                @Nullable final Map<ResourceKey, StepTaskStatus.ItemType> ingredientsStatus,
                                                final Actor actor) {
        final List<Ingredient> uniqueIngredients = mergeIngredients(pattern.layout().ingredients());
        final Map<Ingredient, ResolvedIngredient> resolvedInputs = new HashMap<>();

        for (final Ingredient ingredient : uniqueIngredients) {
            final ResolvedIngredient resolved = resolveIngredient(storageComponent, ingredient, ingredientsStatus, actor);
            if (resolved == null) {
                return null;
            }
            resolvedInputs.put(ingredient, resolved);
        }

        final List<ResourceAmount> totalResources = buildTotalResources(pattern, resolvedInputs);

        return new CraftPreparation(uniqueIngredients, resolvedInputs, totalResources);
    }

    @Nullable
    private static ResolvedIngredient resolveIngredient(final RootStorage storageComponent,
                                                        final Ingredient ingredient,
                                                        @Nullable final Map<ResourceKey, StepTaskStatus.ItemType> ingredientsStatus,
                                                        final Actor actor) {
        for (final ResourceKey input : ingredient.inputs()) {
            if (ingredientsStatus != null) {
                ingredientsStatus.put(input, StepTaskStatus.ItemType.MISSING);
            }

            for (final ResourceKey resourceCandidate : expandResourceCandidates(storageComponent, input)) {
                //TODO: check if the ingredients can be requested
                if (storageComponent.extract(resourceCandidate, ingredient.amount(), Action.SIMULATE, actor) == ingredient.amount()) {
                    if (ingredientsStatus != null) {
                        ingredientsStatus.put(input, StepTaskStatus.ItemType.NORMAL);
                    }

                    return new ResolvedIngredient(ingredient, resourceCandidate, input);
                }
            }
        }

        return null;
    }

    private static List<ResourceAmount> buildTotalResources(final Pattern pattern, final Map<Ingredient, ResolvedIngredient> resolvedInputs) {
        final List<ResourceAmount> totalResources = new ArrayList<>(pattern.layout().outputs());

        // Add byproducts
        for (final ResolvedIngredient resolved : resolvedInputs.values()) {
            if (resolved.extractedResource() instanceof ItemResource itemResource) {
                final ItemStack remainingItem = Platform.INSTANCE.getCraftingRemainingItem(itemResource.toItemStack());
                if (!remainingItem.isEmpty()) {
                    totalResources.add(new ResourceAmount(ItemResource.ofItemStack(remainingItem), remainingItem.getCount()));
                }
            }
        }

        return totalResources;
    }

    public static void extractResolvedInputs(final RootStorage storageComponent,
                                             final Map<Ingredient, ResolvedIngredient> resolvedInputs,
                                             @Nullable final Map<ResourceKey, Long> ingredientsUsed,
                                             final Actor actor) {
        for (final ResolvedIngredient resolved : resolvedInputs.values()) {
            if (ingredientsUsed != null) {
                ingredientsUsed.put(
                    resolved.originalInput(),
                    ingredientsUsed.getOrDefault(resolved.originalInput(), 0L) + resolved.ingredient().amount()
                );
            }

            storageComponent.extract(resolved.extractedResource(), resolved.ingredient().amount(), Action.EXECUTE, actor);
        }
    }

    public static long insertIntoNetwork(final RootStorage storageComponent,
                                         final List<ResourceAmount> totalResources,
                                         @Nullable final ResourceKey resource,
                                         final long craftedAmount,
                                         final Actor actor) {
        long newCraftedAmount = craftedAmount;
        for (final ResourceAmount resourceAmount : totalResources) {
            storageComponent.insert(resourceAmount.resource(), resourceAmount.amount(), Action.EXECUTE, actor);

            if (resourceAmount.resource().equals(resource)) {
                newCraftedAmount += resourceAmount.amount();
            }
        }
        return newCraftedAmount;
    }

    public static boolean canInsertIntoNetwork(final RootStorage storageComponent,
                                               final List<ResourceAmount> totalResources,
                                               final Actor actor) {
        for (final ResourceAmount resourceAmount : totalResources) {
            if (storageComponent.insert(resourceAmount.resource(), resourceAmount.amount(), Action.SIMULATE, actor) != resourceAmount.amount()) {
                return false;
            }
        }

        return true;
    }

    public static List<Ingredient> mergeIngredients(final List<Ingredient> ingredients) {
        return ingredients.stream()
            .collect(Collectors.groupingBy(
                Ingredient::inputs,
                Collectors.summingLong(Ingredient::amount)
            ))
            .entrySet().stream()
            .map(e -> new Ingredient(e.getValue(), e.getKey()))
            .toList();
    }

    private static Collection<ResourceKey> expandResourceCandidates(final RootStorage rootStorage,
                                                                   final ResourceKey resource) {
        if (!(rootStorage instanceof FuzzyRootStorage fuzzyRootStorage)) {
            return Collections.singletonList(resource);
        }
        return fuzzyRootStorage.getFuzzy(resource);
    }

    public record ResolvedIngredient(Ingredient ingredient, ResourceKey extractedResource, ResourceKey originalInput) {
    }

    public record CraftPreparation(List<Ingredient> uniqueIngredients,
                                    Map<Ingredient, ResolvedIngredient> resolvedInputs,
                                    List<ResourceAmount> totalResources) {
    }
}
