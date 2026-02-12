package com.ultramega.stepcrafter.common.stepcrafter.task;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public final class StepTaskSnapshotPersistence {
    private static final String ID = "id";
    private static final String RESOURCE = "resource";
    private static final String AMOUNT = "amount";
    private static final String PATTERN = "pattern";
    private static final String ACTOR = "actor";
    private static final String NOTIFY_ACTOR = "notifyActor";
    private static final String START_TIME = "startTime";
    private static final String TASK_STATE = "state";
    private static final String CRAFTED_AMOUNT = "cancelled";
    private static final String CANCELLED = "cancelled";

    private static final String INGREDIENTS = "ingredients";
    private static final String OUTPUTS = "outputs";
    private static final String BYPRODUCTS = "byproducts";
    private static final String PATTERN_TYPE = "type";

    private static final String INPUTS = "inputs";

    private StepTaskSnapshotPersistence() {
    }

    public static CompoundTag encodeSnapshot(final StepTaskSnapshot stepTask) {
        final CompoundTag tag = new CompoundTag();
        tag.putUUID(ID, stepTask.id().id());
        final CompoundTag resourceTag = encodeResource(stepTask.resource());
        tag.put(RESOURCE, resourceTag);
        tag.putLong(AMOUNT, stepTask.amount());
        tag.put(PATTERN, encodePattern(stepTask.pattern()));
        if (stepTask.actor() instanceof PlayerActor(String name)) {
            tag.putString(ACTOR, name);
        }
        tag.putBoolean(NOTIFY_ACTOR, stepTask.notifyActor());
        tag.putLong(START_TIME, stepTask.startTime());
        tag.putString(TASK_STATE, stepTask.state().name());
        tag.putLong(CRAFTED_AMOUNT, stepTask.craftedAmount());
        tag.putBoolean(CANCELLED, stepTask.cancelled());
        return tag;
    }

    private static CompoundTag encodeResource(final ResourceKey resource) {
        return (CompoundTag) ResourceCodecs.CODEC.encode((PlatformResourceKey) resource, NbtOps.INSTANCE, new CompoundTag()).getOrThrow();
    }

    private static CompoundTag encodePattern(final Pattern pattern) {
        final CompoundTag tag = new CompoundTag();
        tag.putUUID(ID, pattern.id());
        final ListTag ingredients = new ListTag();
        for (final Ingredient ingredient : pattern.layout().ingredients()) {
            ingredients.add(encodeIngredient(ingredient));
        }
        tag.put(INGREDIENTS, ingredients);
        final ListTag outputs = new ListTag();
        for (final ResourceAmount output : pattern.layout().outputs()) {
            outputs.add(ResourceCodecs.AMOUNT_CODEC.encode(output, NbtOps.INSTANCE, new CompoundTag()).getOrThrow());
        }
        tag.put(OUTPUTS, outputs);
        final ListTag byproducts = new ListTag();
        for (final ResourceAmount byproduct : pattern.layout().byproducts()) {
            byproducts.add(ResourceCodecs.AMOUNT_CODEC.encode(byproduct, NbtOps.INSTANCE,
                new CompoundTag()).getOrThrow());
        }
        tag.put(BYPRODUCTS, byproducts);
        tag.putString(PATTERN_TYPE, pattern.layout().type().name());
        return tag;
    }

    private static CompoundTag encodeIngredient(final Ingredient ingredient) {
        final CompoundTag ingredientTag = new CompoundTag();
        ingredientTag.putLong(AMOUNT, ingredient.amount());
        final ListTag inputsTag = new ListTag();
        for (final ResourceKey input : ingredient.inputs()) {
            inputsTag.add(encodeResource(input));
        }
        ingredientTag.put(INPUTS, inputsTag);
        return ingredientTag;
    }

    public static StepTaskSnapshot decodeSnapshot(final CompoundTag tag) {
        final UUID id = tag.getUUID(ID);
        final ResourceKey resource = decodeResource(tag.getCompound(RESOURCE));
        final long amount = tag.getLong(AMOUNT);
        final Pattern pattern = decodePattern(tag.getCompound(PATTERN));
        final Actor actor = tag.contains(ACTOR, Tag.TAG_STRING)
            ? new PlayerActor(tag.getString(ACTOR))
            : Actor.EMPTY;
        final boolean notifyActor = tag.getBoolean(NOTIFY_ACTOR);
        final long startTime = tag.getLong(START_TIME);
        final StepTaskState state = StepTaskState.valueOf(tag.getString(TASK_STATE));
        final long craftedAmount = tag.getLong(CRAFTED_AMOUNT);
        final boolean cancelled = tag.getBoolean(CANCELLED);
        return new StepTaskSnapshot(
            new TaskId(id),
            resource,
            amount,
            pattern,
            actor,
            notifyActor,
            startTime,
            state,
            craftedAmount,
            cancelled
        );
    }

    private static ResourceKey decodeResource(final CompoundTag resourceTag) {
        return ResourceCodecs.CODEC.parse(NbtOps.INSTANCE, resourceTag).result().orElseThrow();
    }

    private static Pattern decodePattern(final CompoundTag tag) {
        final UUID id = tag.getUUID(ID);
        final List<Ingredient> ingredients = new ArrayList<>();
        for (final Tag ingredientTag : tag.getList(INGREDIENTS, Tag.TAG_COMPOUND)) {
            ingredients.add(decodeIngredient((CompoundTag) ingredientTag));
        }
        final List<ResourceAmount> outputs = new ArrayList<>();
        for (final Tag outputTag : tag.getList(OUTPUTS, Tag.TAG_COMPOUND)) {
            outputs.add(ResourceCodecs.AMOUNT_CODEC.parse(NbtOps.INSTANCE, outputTag).result().orElseThrow());
        }
        final List<ResourceAmount> byproducts = new ArrayList<>();
        for (final Tag byproductTag : tag.getList(BYPRODUCTS, Tag.TAG_COMPOUND)) {
            byproducts.add(ResourceCodecs.AMOUNT_CODEC.parse(NbtOps.INSTANCE, byproductTag).result().orElseThrow());
        }
        final PatternType type = PatternType.valueOf(tag.getString(PATTERN_TYPE));
        return new Pattern(id, type == PatternType.INTERNAL
            ? PatternLayout.internal(ingredients, outputs, byproducts)
            : PatternLayout.external(ingredients, outputs));
    }

    private static Ingredient decodeIngredient(final CompoundTag tag) {
        final long amount = tag.getLong(AMOUNT);
        final List<ResourceKey> inputs = new ArrayList<>();
        for (final Tag inputTag : tag.getList(INPUTS, Tag.TAG_COMPOUND)) {
            inputs.add(decodeResource((CompoundTag) inputTag));
        }
        return new Ingredient(amount, inputs);
    }
}
