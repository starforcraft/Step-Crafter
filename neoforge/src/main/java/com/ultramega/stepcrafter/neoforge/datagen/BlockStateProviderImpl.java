package com.ultramega.stepcrafter.neoforge.datagen;

import com.ultramega.stepcrafter.common.registry.Blocks;

import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.common.support.direction.BiDirectionType;

import java.util.function.Supplier;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public class BlockStateProviderImpl extends BlockStateProvider {
    private static final String BLOCK_PREFIX = "block";

    private final ExistingFileHelper existingFileHelper;

    public BlockStateProviderImpl(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, MOD_ID, existingFileHelper);
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    protected void registerStatesAndModels() {
        this.registerStepCrafterManagers();
        this.registerStepRequesterManagers();
    }

    private void registerStepCrafterManagers() {
        Blocks.INSTANCE.getStepCrafterManager().forEach((color, id, block) -> this.configureActiveColoredDirectionalBlock(
            color,
            block,
            "step_crafter_manager"
        ));
    }

    private void registerStepRequesterManagers() {
        Blocks.INSTANCE.getStepRequesterManager().forEach((color, id, block) -> this.configureActiveColoredDirectionalBlock(
            color,
            block,
            "step_requester_manager"
        ));
    }

    private void configureActiveColoredDirectionalBlock(final DyeColor color,
                                                        final Supplier<? extends Block> block,
                                                        final String name) {
        final ModelFile inactive = this.modelFile(createStepCrafterIdentifier(BLOCK_PREFIX + "/" + name + "/inactive"));
        final ModelFile active = this.modelFile(createStepCrafterIdentifier(BLOCK_PREFIX + "/" + name + "/" + color.getName()));
        final var builder = super.getVariantBuilder(block.get());
        builder.forAllStates(blockState -> {
            final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
            if (Boolean.TRUE.equals(blockState.getValue(AbstractActiveColoredDirectionalBlock.ACTIVE))) {
                model.modelFile(active);
            } else {
                model.modelFile(inactive);
            }
            final BiDirection direction = blockState.getValue(BiDirectionType.INSTANCE.getProperty());
            this.addRotationFrontFacingNorth(model, direction);
            return model.build();
        });
    }

    private void addRotationFrontFacingNorth(final ConfiguredModel.Builder<?> model, final BiDirection direction) {
        final int x = (int) direction.getVec().x();
        final int y = (int) direction.getVec().y();
        final int z = (int) direction.getVec().z();
        switch (direction) {
            case UP_EAST, UP_NORTH, UP_SOUTH, UP_WEST, DOWN_EAST, DOWN_WEST, DOWN_SOUTH, DOWN_NORTH -> model.rotationX(x * -1).rotationY(z);
            case EAST, WEST -> model.rotationY(y + 180);
            case NORTH, SOUTH -> model.rotationY(y);
        }
    }

    private ModelFile modelFile(final ResourceLocation location) {
        return new ModelFile.ExistingModelFile(location, this.existingFileHelper);
    }
}
