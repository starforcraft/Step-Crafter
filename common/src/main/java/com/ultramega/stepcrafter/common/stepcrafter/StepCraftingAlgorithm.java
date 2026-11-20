package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.Platform;
import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.api.storage.root.FuzzyRootStorage;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

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
        final Map<ResourceKey, Long> reserved = new HashMap<>();
        final boolean allowBuckets = pattern.layout().type() == PatternType.INTERNAL;

        for (final Ingredient ingredient : uniqueIngredients) {
            final ResolvedIngredient resolved = resolveIngredient(
                storageComponent, ingredient, ingredientsStatus, actor, reserved, allowBuckets);
            if (resolved == null) {
                return null;
            }
            if (ingredientsStatus != null) {
                ingredientsStatus.put(resolved.originalInput(), StepTaskStatus.ItemType.NORMAL);
            }
            resolved.extractions().forEach(input -> reserved.merge(input.resource(), input.amount(), Long::sum));
            resolvedInputs.put(ingredient, resolved);
        }

        final List<ResourceAmount> totalResources = buildTotalResources(pattern, resolvedInputs);

        return new CraftPreparation(uniqueIngredients, resolvedInputs, totalResources);
    }

    @Nullable
    private static ResolvedIngredient resolveIngredient(final RootStorage storageComponent,
                                                        final Ingredient ingredient,
                                                        @Nullable final Map<ResourceKey, StepTaskStatus.ItemType> ingredientsStatus,
                                                        final Actor actor,
                                                        final Map<ResourceKey, Long> reserved,
                                                        final boolean allowBuckets) {
        for (final ResourceKey input : ingredient.inputs()) {
            if (ingredientsStatus != null) {
                ingredientsStatus.put(input, StepTaskStatus.ItemType.MISSING);
            }

            for (final ResourceKey resourceCandidate : expandResourceCandidates(storageComponent, input)) {
                //TODO: check if the ingredients can be requested
                if (available(storageComponent, resourceCandidate, ingredient.amount(), actor, reserved) == ingredient.amount()) {
                    return new ResolvedIngredient(ingredient, resourceCandidate, input);
                }
            }
        }

        if (allowBuckets) {
            for (final ResourceKey input : ingredient.inputs()) {
                if (input instanceof FluidResource fluid) {
                    final ResolvedIngredient resolved = resolveBuckets(storageComponent, ingredient, fluid, actor, reserved);
                    if (resolved != null) {
                        return resolved;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private static ResolvedIngredient resolveBuckets(final RootStorage storage,
                                                     final Ingredient ingredient,
                                                     final FluidResource fluid,
                                                     final Actor actor,
                                                     final Map<ResourceKey, Long> reserved) {
        if (!fluid.components().isEmpty()
            || !(fluid.fluid().getBucket() instanceof BucketItem filledBucket)
            || filledBucket.getContent() != fluid.fluid()) {
            return null;
        }
        final long volume = com.refinedmods.refinedstorage.common.Platform.INSTANCE.getBucketAmount();
        if (ingredient.amount() % volume != 0) {
            return null;
        }

        ResourceKey looseFluid = fluid;
        long looseAmount = 0;
        for (final ResourceKey candidate : expandResourceCandidates(storage, fluid)) {
            final long amount = available(storage, candidate, ingredient.amount(), actor, reserved);
            if (amount > looseAmount) {
                looseFluid = candidate;
                looseAmount = amount;
            }
        }
        final long fromStorage = looseAmount - looseAmount % volume;
        final long bucketCount = (ingredient.amount() - fromStorage) / volume;
        if (bucketCount == 0) {
            return new ResolvedIngredient(ingredient, looseFluid, fluid);
        }
        final ItemResource bucket = new ItemResource(filledBucket);
        if (available(storage, bucket, bucketCount, actor, reserved) != bucketCount) {
            return null;
        }
        final ResourceAmount bucketInput = new ResourceAmount(bucket, bucketCount);
        final List<ResourceAmount> extractions = fromStorage == 0
            ? List.of(bucketInput) : List.of(new ResourceAmount(looseFluid, fromStorage), bucketInput);
        return new ResolvedIngredient(ingredient, fluid, extractions);
    }

    private static long available(final RootStorage storage,
                                  final ResourceKey resource,
                                  final long amount,
                                  final Actor actor,
                                  final Map<ResourceKey, Long> reserved) {
        final long used = reserved.getOrDefault(resource, 0L);
        final long requested = used + Math.min(amount, Long.MAX_VALUE - used);
        return Math.max(0L, storage.extract(resource, requested, Action.SIMULATE, actor) - used);
    }

    private static List<ResourceAmount> buildTotalResources(final Pattern pattern, final Map<Ingredient, ResolvedIngredient> resolvedInputs) {
        final Map<ResourceKey, Long> amounts = new LinkedHashMap<>();
        pattern.layout().outputs().forEach(output -> amounts.merge(output.resource(), output.amount(), Long::sum));

        // Add byproducts
        for (final ResolvedIngredient resolved : resolvedInputs.values()) {
            for (final ResourceAmount extraction : resolved.extractions()) {
                if (extraction.resource() instanceof ItemResource itemResource) {
                    final ItemStackTemplate remainingItem = Platform.INSTANCE.getCraftingRemainder(itemResource.toItemStack());
                    if (remainingItem != null) {
                        final ItemResource resource = new ItemResource(remainingItem.item().value(), remainingItem.components());
                        final long remainderAmount = Math.multiplyExact(remainingItem.count(), extraction.amount());
                        amounts.merge(resource, remainderAmount, Long::sum);
                    }
                }
            }
        }

        final List<ResourceAmount> totalResources = new ArrayList<>();
        amounts.forEach((resource, amount) -> totalResources.add(new ResourceAmount(resource, amount)));
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

            resolved.extractions().forEach(input -> storageComponent.extract(input.resource(), input.amount(), Action.EXECUTE, actor));
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

    public record ResolvedIngredient(Ingredient ingredient, ResourceKey originalInput, List<ResourceAmount> extractions) {
        public ResolvedIngredient(final Ingredient ingredient, final ResourceKey extractedResource, final ResourceKey originalInput) {
            this(ingredient, originalInput, List.of(new ResourceAmount(extractedResource, ingredient.amount())));
        }

        public ResourceKey extractedResource() {
            return this.extractions.getFirst().resource();
        }
    }

    public record CraftPreparation(List<Ingredient> uniqueIngredients,
                                    Map<Ingredient, ResolvedIngredient> resolvedInputs,
                                    List<ResourceAmount> totalResources) {
    }
}
