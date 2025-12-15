package com.ultramega.stepcrafter.neoforge.datagen;

import com.ultramega.stepcrafter.common.registry.Blocks;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public class BlockModelProviderImpl extends BlockModelProvider {
    private static final String PARTICLE_TEXTURE = "particle";
    private static final String CUTOUT_TEXTURE = "cutout";
    private static final String BLOCK_PREFIX = "block";

    private static final ResourceLocation EMISSIVE_NORTH_CUTOUT = createIdentifier(BLOCK_PREFIX + "/emissive_north_cutout");

    private static final ResourceLocation NORTH_CUTOUT = createIdentifier(BLOCK_PREFIX + "/north_cutout");

    private static final ResourceLocation BOTTOM_TEXTURE = createIdentifier(BLOCK_PREFIX + "/bottom");

    private static final String NORTH = "north";
    private static final String EAST = "east";
    private static final String SOUTH = "south";
    private static final String WEST = "west";
    private static final String UP = "up";
    private static final String DOWN = "down";

    public BlockModelProviderImpl(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        this.registerRightLeftBackFrontTopModel(Blocks.INSTANCE.getStepCrafterManager(), "step_crafter_manager");
        this.registerRightLeftBackFrontTopModel(Blocks.INSTANCE.getStepRequesterManager(), "step_requester_manager");
    }

    private void registerRightLeftBackFrontTopModel(final BlockColorMap<?, ?> blockMap, final String name) {
        this.registerRightLeftBackFrontTopModel(blockMap, name, "");
    }

    private void registerRightLeftBackFrontTopModel(final BlockColorMap<?, ?> blockMap,
                                                    final String name,
                                                    final String modelPrefix) {
        blockMap.forEach((color, id, block) -> {
            final ResourceLocation cutout = createStepCrafterIdentifier(BLOCK_PREFIX + "/" + name + "/cutouts/" + color.getName());
            this.registerRightLeftBackFrontTopModel(name, modelPrefix + color.getName(), cutout, EMISSIVE_NORTH_CUTOUT);
        });
        final ResourceLocation inactiveCutout = createStepCrafterIdentifier(BLOCK_PREFIX + "/" + name + "/cutouts/inactive");
        this.registerRightLeftBackFrontTopModel(name, "inactive", inactiveCutout, NORTH_CUTOUT);
    }

    private void registerRightLeftBackFrontTopModel(final String name,
                                                    final String variantName,
                                                    final ResourceLocation cutout,
                                                    final ResourceLocation baseModel) {
        final ResourceLocation right = createStepCrafterIdentifier(BLOCK_PREFIX + "/" + name + "/right");
        final ResourceLocation left = createStepCrafterIdentifier(BLOCK_PREFIX + "/" + name + "/left");
        final ResourceLocation back = createStepCrafterIdentifier(BLOCK_PREFIX + "/" + name + "/back");
        final ResourceLocation front = createStepCrafterIdentifier(BLOCK_PREFIX + "/" + name + "/front");
        final ResourceLocation top = createStepCrafterIdentifier(BLOCK_PREFIX + "/" + name + "/top");
        super.withExistingParent(BLOCK_PREFIX + "/" + name + "/" + variantName, baseModel)
            .texture(PARTICLE_TEXTURE, right)
            .texture(NORTH, front)
            .texture(EAST, right)
            .texture(SOUTH, back)
            .texture(WEST, left)
            .texture(UP, top)
            .texture(DOWN, BOTTOM_TEXTURE)
            .texture(CUTOUT_TEXTURE, cutout);
    }
}
