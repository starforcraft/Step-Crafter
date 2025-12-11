package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.support.PatternMinMax;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class StepCrafterNetworkNode extends SimpleNetworkNode implements StepCrafterProvider {
    private final Actor actor = new NetworkNodeActor(this);
    private final Set<StepCraftingParentContainer> parents = new HashSet<>();
    private final PatternMinMax[] patterns;

    private StepCrafterBlockEntity blockEntity;

    public StepCrafterNetworkNode(final long energyUsage, final int patterns) {
        super(energyUsage);
        this.patterns = new PatternMinMax[patterns];
    }

    @Override
    public void doWork() {
        super.doWork();

        if (this.network == null || !this.isActive()) {
            return;
        }

        final StorageNetworkComponent storageComponent = this.network.getComponent(StorageNetworkComponent.class);
        final PatternResourceContainerImpl patternResources = this.blockEntity.getPatternResourceContainer();
        for (int i = 0; i < patternResources.getContainerSize(); i++) {
            final ResourceMinMaxAmount resource = patternResources.get(i);
            final PatternMinMax pattern = this.patterns[i];
            if (resource == null || pattern == null) {
                continue;
            }

            for (int j = 0; j < this.blockEntity.getSpeed() + 1; j++) {
                // Only the first output is checked however as everything except processing patterns (which aren't allowed) only have one output
                // this *shouldn't* be an issue
                final ResourceKey resourceKey = pattern.pattern().layout().outputs().getFirst().resource();
                final long stored = storageComponent.get(resourceKey);
                final boolean crafting = resource.isCrafting();
                if (!crafting && stored >= resource.minAmount()) {
                    break;
                }
                if (crafting && stored >= resource.maxAmount()) {
                    patternResources.set(i, resource.toBuilder().isCrafting(false).build());
                    break;
                }

                if (!this.craftOneItemNew(storageComponent, pattern.pattern())) {
                    if (crafting) {
                        patternResources.set(i, resource.toBuilder().isCrafting(false).build());
                    }
                    break;
                } else {
                    if (!crafting) {
                        patternResources.set(i, resource.toBuilder().isCrafting(true).build());
                    }
                }
            }
        }
    }

    private boolean craftOneItemNew(final StorageNetworkComponent storageComponent, final Pattern pattern) {
        final List<Ingredient> uniqueIngredients = mergeIngredients(pattern.layout().ingredients());

        // Check if all ingredients can be extracted
        for (final Ingredient ingredient : uniqueIngredients) {
            for (final ResourceKey resource : ingredient.inputs()) {
                if (storageComponent.extract(resource, ingredient.amount(), Action.SIMULATE, this.actor) != ingredient.amount()) {
                    return false;
                }
            }
        }

        // Check if all outputs can be inserted
        for (final ResourceAmount resource : pattern.layout().outputs()) {
            if (storageComponent.insert(resource.resource(), resource.amount(), Action.SIMULATE, this.actor) != resource.amount()) {
                return false;
            }
        }

        // Check if all byproducts can be inserted
        for (final ResourceAmount resource : pattern.layout().byproducts()) {
            if (storageComponent.insert(resource.resource(), resource.amount(), Action.SIMULATE, this.actor) != resource.amount()) {
                return false;
            }
        }

        // Success, execute operation
        for (final Ingredient ingredient : uniqueIngredients) {
            for (final ResourceKey resource : ingredient.inputs()) {
                storageComponent.extract(resource, ingredient.amount(), Action.EXECUTE, this.actor);
            }
        }
        for (final ResourceAmount resource : pattern.layout().outputs()) {
            storageComponent.insert(resource.resource(), resource.amount(), Action.EXECUTE, this.actor);
        }
        for (final ResourceAmount resource : pattern.layout().byproducts()) {
            storageComponent.insert(resource.resource(), resource.amount(), Action.EXECUTE, this.actor);
        }

        return true;
    }

    private static List<Ingredient> mergeIngredients(final List<Ingredient> ingredients) {
        return ingredients.stream()
            .collect(Collectors.groupingBy(
                Ingredient::inputs,
                Collectors.summingLong(Ingredient::amount)
            ))
            .entrySet().stream()
            .map(e -> new Ingredient(e.getValue(), e.getKey()))
            .toList();
    }

    public void setBlockEntity(final StepCrafterBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void setPattern(final int index, @Nullable final PatternMinMax pattern) {
        final PatternMinMax oldPattern = this.patterns[index];
        if (oldPattern != null) {
            this.parents.forEach(parent -> parent.remove(this, oldPattern));
        }
        this.patterns[index] = pattern;
        if (pattern != null) {
            this.parents.forEach(parent -> parent.add(this, pattern));
        }
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        if (!newActive) {
            for (final PatternMinMax pattern : this.patterns) {
                if (pattern != null) {
                    this.parents.forEach(parent -> parent.remove(this, pattern));
                }
            }
            return;
        }
        for (final PatternMinMax pattern : this.patterns) {
            if (pattern != null) {
                this.parents.forEach(parent -> parent.add(this, pattern));
            }
        }
    }

    @Override
    public void onAddedIntoContainer(final StepCraftingParentContainer parentContainer) {
        this.parents.add(parentContainer);
        for (final PatternMinMax pattern : this.patterns) {
            if (pattern != null) {
                parentContainer.add(this, pattern);
            }
        }
    }

    @Override
    public void onRemovedFromContainer(final StepCraftingParentContainer parentContainer) {
        this.parents.remove(parentContainer);
        for (final PatternMinMax pattern : this.patterns) {
            if (pattern != null) {
                parentContainer.remove(this, pattern);
            }
        }
    }
}
