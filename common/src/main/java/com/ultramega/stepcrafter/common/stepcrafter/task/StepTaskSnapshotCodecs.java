package com.ultramega.stepcrafter.common.stepcrafter.task;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.support.ErrorHandlingListCodec;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

public final class StepTaskSnapshotCodecs {
    private static final String DESERIALIZE_ERROR_MESSAGE = """
        Refined Storage/Step Crafter could not load an autocrafting task.
        This could be because a resource used in the task no longer exists after a mod update, or if the data format of
        the resource has changed. In any case, this is NOT caused by Refined Storage/Step Crafter.
        Refined Storage/Step Crafter will try to gracefully handle this problem and continue to load the other autocrafting tasks.
        The problematic autocrafting task will not be loaded.
        Error message:""";

    private static final Codec<Ingredient> INGREDIENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.fieldOf("amount").forGetter(Ingredient::amount),
        Codec.list(ResourceCodecs.NATIVE_CODEC).fieldOf("inputs").forGetter(Ingredient::inputs)
    ).apply(instance, Ingredient::new));

    private static final Codec<Pattern> PATTERN_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(Pattern::id),
        Codec.list(INGREDIENT_CODEC).fieldOf("ingredients").forGetter(p -> p.layout().ingredients()),
        Codec.list(ResourceCodecs.AMOUNT_CODEC).fieldOf("outputs").forGetter(p -> p.layout().outputs()),
        Codec.list(ResourceCodecs.AMOUNT_CODEC).fieldOf("byproducts").forGetter(p -> p.layout().byproducts()),
        Codec.BOOL.fieldOf("internal").forGetter(p -> p.layout().type() == PatternType.INTERNAL)
    ).apply(instance, (id, ingredients, outputs, byproducts, internal) -> new Pattern(
        id,
        Boolean.TRUE.equals(internal)
            ? PatternLayout.internal(ingredients, outputs, byproducts)
            : PatternLayout.external(ingredients, outputs)
    )));

    private static final Codec<StepTaskSnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id").xmap(TaskId::new, TaskId::id).forGetter(StepTaskSnapshot::id),
        ResourceCodecs.NATIVE_CODEC.fieldOf("resource").forGetter(StepTaskSnapshot::resource),
        Codec.LONG.fieldOf("amount").forGetter(StepTaskSnapshot::amount),
        PATTERN_CODEC.fieldOf("pattern").forGetter(StepTaskSnapshot::pattern),
        Codec.STRING.optionalFieldOf("actor").xmap(
            name -> name.map(n -> (Actor) new PlayerActor(n)).orElse(Actor.EMPTY),
            actor -> actor instanceof PlayerActor playerActor ? Optional.of(playerActor.getName()) : Optional.empty()
        ).forGetter(StepTaskSnapshot::actor),
        Codec.BOOL.fieldOf("notifyActor").forGetter(StepTaskSnapshot::notifyActor),
        Codec.LONG.fieldOf("startTime").forGetter(StepTaskSnapshot::startTime),
        StepTaskState.CODEC.fieldOf("state").forGetter(StepTaskSnapshot::state),
        Codec.LONG.fieldOf("craftedAmount").forGetter(StepTaskSnapshot::craftedAmount),
        Codec.BOOL.fieldOf("cancelled").forGetter(StepTaskSnapshot::cancelled)
    ).apply(instance, StepTaskSnapshot::new));

    public static final ErrorHandlingListCodec<StepTaskSnapshot> LIST_CODEC = new ErrorHandlingListCodec<>(
        CODEC,
        DESERIALIZE_ERROR_MESSAGE
    );

    private StepTaskSnapshotCodecs() {
    }
}

