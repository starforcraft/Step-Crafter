package com.ultramega.stepcrafter.neoforge.datagen.model;

import com.ultramega.stepcrafter.common.registry.Blocks;
import com.ultramega.stepcrafter.common.registry.Items;
import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterBlock;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterBlock;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.direction.DefaultDirectionType;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirectionType;

import java.util.stream.Stream;

import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;
import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;

public class ModelProviders extends ModelProvider {
    private static final Identifier BOTTOM_TEXTURE = createIdentifier("block/bottom");

    private static final TextureSlot CUTOUT = TextureSlot.create("cutout");

    private static final ModelTemplate NORTH_CUTOUT_MODEL = ModelTemplates.create(
        "refinedstorage:north_cutout",
        TextureSlot.PARTICLE,
        TextureSlot.NORTH,
        TextureSlot.EAST,
        TextureSlot.SOUTH,
        TextureSlot.WEST,
        TextureSlot.UP,
        TextureSlot.DOWN,
        CUTOUT
    );
    private static final ModelTemplate EMISSIVE_NORTH_CUTOUT_MODEL = ModelTemplates.create(
        "refinedstorage:emissive_north_cutout",
        TextureSlot.PARTICLE,
        TextureSlot.NORTH,
        TextureSlot.EAST,
        TextureSlot.SOUTH,
        TextureSlot.WEST,
        TextureSlot.UP,
        TextureSlot.DOWN,
        CUTOUT
    );

    public ModelProviders(final PackOutput output) {
        super(output, MOD_ID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.of();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.of();
    }

    @Override
    protected void registerModels(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels) {
        this.registerStepCrafter(blockModels, itemModels);
        this.registerStepRequester(blockModels, itemModels);
        this.registerDirectionalBlock(itemModels, blockModels, "step_crafter_manager", Blocks.INSTANCE.getStepCrafterManager());
        this.registerDirectionalBlock(itemModels, blockModels, "step_requester_manager", Blocks.INSTANCE.getStepRequesterManager());
        this.registerDirectionalBlock(itemModels, blockModels, "step_crafting_monitor", Blocks.INSTANCE.getStepCraftingMonitor());
        this.registerSimpleItems(itemModels);
    }

    private void registerSimpleItems(final ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(Items.INSTANCE.getSlotUpgrade(), ModelTemplates.FLAT_ITEM);
    }

    private void registerStepCrafter(final BlockModelGenerators blockModels,
                                     final ItemModelGenerators itemModels) {
        final Identifier sideActive = createStepCrafterIdentifier("block/step_crafter/side_active");
        final Identifier sideInactive = createStepCrafterIdentifier("block/step_crafter/side_inactive");
        final Identifier top = createStepCrafterIdentifier("block/step_crafter/top");

        final Identifier activeModel = ModelTemplates.CUBE.create(
            createStepCrafterIdentifier("block/step_crafter/active"),
            new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(sideActive))
                .put(TextureSlot.NORTH, texture(sideActive))
                .put(TextureSlot.EAST, texture(sideActive))
                .put(TextureSlot.SOUTH, texture(sideActive))
                .put(TextureSlot.WEST, texture(sideActive))
                .put(TextureSlot.UP, texture(top))
                .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE)),
            blockModels.modelOutput
        );

        final Identifier inactiveModel = ModelTemplates.CUBE.create(
            createStepCrafterIdentifier("block/step_crafter/inactive"),
            new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(sideInactive))
                .put(TextureSlot.NORTH, texture(sideInactive))
                .put(TextureSlot.EAST, texture(sideInactive))
                .put(TextureSlot.SOUTH, texture(sideInactive))
                .put(TextureSlot.WEST, texture(sideInactive))
                .put(TextureSlot.UP, texture(top))
                .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE)),
            blockModels.modelOutput
        );

        itemModels.itemModelOutput.accept(Blocks.INSTANCE.getStepCrafter().asItem(), ItemModelUtils.plainModel(activeModel));

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.INSTANCE.getStepCrafter())
            .with(PropertyDispatch.initial(StepCrafterBlock.ACTIVE)
                .select(false, plainVariant(inactiveModel))
                .select(true, plainVariant(activeModel)))
            .with(PropertyDispatch.modify(DefaultDirectionType.FACE_CLICKED.getProperty())
                .generate(direction -> variant -> variant
                    .withXRot(this.getStepCrafterXRot(direction))
                    .withYRot(this.getStepCrafterYRot(direction)))));
    }

    private void registerStepRequester(final BlockModelGenerators blockModels,
                                       final ItemModelGenerators itemModels) {
        final Identifier activeModel = ModelTemplates.CUBE_ALL.create(
            createStepCrafterIdentifier("block/step_requester/active"),
            TextureMapping.cube(texture(createStepCrafterIdentifier("block/step_requester/active"))),
            blockModels.modelOutput
        );
        final Identifier inactiveModel = ModelTemplates.CUBE_ALL.create(
            createStepCrafterIdentifier("block/step_requester/inactive"),
            TextureMapping.cube(texture(createStepCrafterIdentifier("block/step_requester/inactive"))),
            blockModels.modelOutput
        );
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.INSTANCE.getStepRequester())
            .with(PropertyDispatch.initial(StepRequesterBlock.ACTIVE)
                .select(true, plainVariant(activeModel))
                .select(false, plainVariant(inactiveModel))));
        itemModels.itemModelOutput.accept(Blocks.INSTANCE.getStepRequester().asItem(),
            ItemModelUtils.plainModel(activeModel));
    }

    private void registerDirectionalBlock(final ItemModelGenerators itemModels,
                                          final BlockModelGenerators blockModels,
                                          final String name,
                                          final BlockColorMap<?, ?> blocks) {
        final Identifier right = createStepCrafterIdentifier("block/" + name + "/right");
        final Identifier front = createStepCrafterIdentifier("block/" + name + "/front");
        final Identifier back = createStepCrafterIdentifier("block/" + name + "/back");
        final Identifier left = createStepCrafterIdentifier("block/" + name + "/left");
        final Identifier top = createStepCrafterIdentifier("block/" + name + "/top");
        final Identifier inactiveBlockModel = NORTH_CUTOUT_MODEL.create(
            createStepCrafterIdentifier("block/" + name + "/inactive"),
            new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(right))
                .put(TextureSlot.NORTH, texture(front))
                .put(TextureSlot.EAST, texture(right))
                .put(TextureSlot.SOUTH, texture(back))
                .put(TextureSlot.WEST, texture(left))
                .put(TextureSlot.UP, texture(top))
                .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE))
                .put(CUTOUT, texture(createStepCrafterIdentifier("block/" + name + "/cutouts/inactive"))),
            blockModels.modelOutput
        );
        blocks.forEach((color, id, block) -> {
            final Identifier blockModel = EMISSIVE_NORTH_CUTOUT_MODEL.create(
                createStepCrafterIdentifier("block/" + name + "/" + color.getName()),
                new TextureMapping()
                    .put(TextureSlot.PARTICLE, texture(right))
                    .put(TextureSlot.NORTH, texture(front))
                    .put(TextureSlot.EAST, texture(right))
                    .put(TextureSlot.SOUTH, texture(back))
                    .put(TextureSlot.WEST, texture(left))
                    .put(TextureSlot.UP, texture(top))
                    .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE))
                    .put(CUTOUT, texture(createStepCrafterIdentifier("block/" + name + "/cutouts/" + color.getName()))),
                blockModels.modelOutput
            );
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block.get())
                .with(PropertyDispatch.initial(AbstractActiveColoredDirectionalBlock.ACTIVE)
                    .select(false, plainVariant(inactiveBlockModel))
                    .select(true, plainVariant(blockModel)))
                .with(PropertyDispatch.modify(OrientedDirectionType.INSTANCE.getProperty())
                    .generate(direction -> variant -> variant
                        .withXRot(this.getXRot(direction))
                        .withYRot(this.getYRot(direction)))));
            itemModels.itemModelOutput.accept(block.get().asItem(), ItemModelUtils.plainModel(blockModel));
        });
    }

    private Quadrant getXRot(final OrientedDirection direction) {
        return switch (direction) {
            case NORTH, EAST, SOUTH, WEST -> Quadrant.R0;
            case DOWN_NORTH, DOWN_EAST, DOWN_SOUTH, DOWN_WEST -> Quadrant.R90;
            case UP_NORTH, UP_EAST, UP_SOUTH, UP_WEST -> Quadrant.R270;
        };
    }

    private Quadrant getYRot(final OrientedDirection direction) {
        return switch (direction) {
            case NORTH, UP_SOUTH, DOWN_NORTH -> Quadrant.R0;
            case EAST, UP_WEST, DOWN_WEST -> Quadrant.R90;
            case SOUTH, UP_NORTH, DOWN_SOUTH -> Quadrant.R180;
            case WEST, UP_EAST, DOWN_EAST -> Quadrant.R270;
        };
    }

    private Quadrant getStepCrafterXRot(final Direction direction) {
        return switch (direction) {
            case DOWN -> Quadrant.R0;
            case UP -> Quadrant.R180;
            case NORTH, SOUTH, WEST, EAST -> Quadrant.R90;
        };
    }

    private Quadrant getStepCrafterYRot(final Direction direction) {
        return switch (direction) {
            case DOWN, UP, NORTH -> Quadrant.R0;
            case SOUTH -> Quadrant.R180;
            case EAST -> Quadrant.R90;
            case WEST -> Quadrant.R270;
        };
    }

    private static Material texture(final Identifier location) {
        return new Material(location);
    }
}
