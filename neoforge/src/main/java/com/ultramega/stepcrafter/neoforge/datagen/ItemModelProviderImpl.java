package com.ultramega.stepcrafter.neoforge.datagen;

import com.ultramega.stepcrafter.common.registry.Blocks;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public class ItemModelProviderImpl extends ItemModelProvider {
    public ItemModelProviderImpl(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        this.registerStepCrafterManagers();
        this.registerStepRequesterManagers();
    }

    private void registerStepCrafterManagers() {
        final var blocks = Blocks.INSTANCE.getStepCrafterManager();
        blocks.forEach((color, id, block) -> super.withExistingParent(
            id.getPath(),
            createStepCrafterIdentifier("block/step_crafter_manager/" + color.getName())
        ));
    }

    private void registerStepRequesterManagers() {
        final var blocks = Blocks.INSTANCE.getStepRequesterManager();
        blocks.forEach((color, id, block) -> super.withExistingParent(
            id.getPath(),
            createStepCrafterIdentifier("block/step_requester_manager/" + color.getName())
        ));
    }
}
