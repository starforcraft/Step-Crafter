package com.ultramega.stepcrafter.common;

import com.ultramega.stepcrafter.common.network.stepcrafter.StepCraftingNetworkComponent;
import com.ultramega.stepcrafter.common.network.stepcrafter.StepCraftingNetworkComponentImpl;
import com.ultramega.stepcrafter.common.registry.BlockEntities;
import com.ultramega.stepcrafter.common.registry.Blocks;
import com.ultramega.stepcrafter.common.registry.Items;
import com.ultramega.stepcrafter.common.registry.Menus;
import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterBlock;
import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterBlockEntity;
import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterContainerMenu;
import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterData;
import com.ultramega.stepcrafter.common.stepcraftermanager.StepCrafterManagerBlockEntity;
import com.ultramega.stepcrafter.common.stepcraftermanager.StepCrafterManagerContainerMenu;
import com.ultramega.stepcrafter.common.stepmanager.StepManagerData;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterBlock;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterBlockEntity;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterContainerMenu;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterData;
import com.ultramega.stepcrafter.common.steprequestermanager.StepRequesterManagerBlockEntity;
import com.ultramega.stepcrafter.common.steprequestermanager.StepRequesterManagerContainerMenu;
import com.ultramega.stepcrafter.common.upgrade.SimpleUpgradeItem;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage.common.content.ExtendedMenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;

import java.util.function.Supplier;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class AbstractModInitializer {
    protected final void initializePlatformApi() {
        this.registerNetworkComponents();
    }

    protected final void registerBlocks(final RegistryCallback<Block> callback) {
        Blocks.INSTANCE.setStepCrafter(callback.register(ContentIds.STEP_CRAFTER, () -> new StepCrafterBlock(ContentNames.STEP_CRAFTER)));
        Blocks.INSTANCE.setStepRequester(callback.register(ContentIds.STEP_REQUESTER, () -> new StepRequesterBlock(ContentNames.STEP_REQUESTER)));
        Blocks.INSTANCE.getStepCrafterManager().registerBlocks(callback);
        Blocks.INSTANCE.getStepRequesterManager().registerBlocks(callback);
    }

    protected final void registerItems(final RegistryCallback<Item> callback) {
        callback.register(ContentIds.STEP_CRAFTER, () -> Blocks.INSTANCE.getStepCrafter().createBlockItem());
        callback.register(ContentIds.STEP_REQUESTER, () -> Blocks.INSTANCE.getStepRequester().createBlockItem());
        Blocks.INSTANCE.getStepCrafterManager().registerItems(callback, Items.INSTANCE::addStepCrafterManager);
        Blocks.INSTANCE.getStepRequesterManager().registerItems(callback, Items.INSTANCE::addStepRequesterManager);
        this.registerUpgrades(callback);
    }

    protected final void registerBlockEntities(final RegistryCallback<BlockEntityType<?>> callback,
                                               final BlockEntityTypeFactory typeFactory) {
        BlockEntities.INSTANCE.setStepCrafter(callback.register(
            ContentIds.STEP_CRAFTER,
            () -> typeFactory.create(StepCrafterBlockEntity::new, Blocks.INSTANCE.getStepCrafter())
        ));
        BlockEntities.INSTANCE.setStepRequester(callback.register(
            ContentIds.STEP_REQUESTER,
            () -> typeFactory.create(StepRequesterBlockEntity::new, Blocks.INSTANCE.getStepRequester())
        ));
        BlockEntities.INSTANCE.setStepCrafterManager(callback.register(
            ContentIds.STEP_CRAFTER_MANAGER,
            () -> typeFactory.create(StepCrafterManagerBlockEntity::new,
                Blocks.INSTANCE.getStepCrafterManager().toArray())
        ));
        BlockEntities.INSTANCE.setStepRequesterManager(callback.register(
            ContentIds.STEP_REQUESTER_MANAGER,
            () -> typeFactory.create(StepRequesterManagerBlockEntity::new,
                Blocks.INSTANCE.getStepRequesterManager().toArray())
        ));
    }

    protected final void registerMenus(final RegistryCallback<MenuType<?>> callback,
                                       final ExtendedMenuTypeFactory extendedMenuTypeFactory) {
        Menus.INSTANCE.setStepCrafter(callback.register(
            ContentIds.STEP_CRAFTER,
            () -> extendedMenuTypeFactory.create(StepCrafterContainerMenu::new, StepCrafterData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setStepRequester(callback.register(
            ContentIds.STEP_REQUESTER,
            () -> extendedMenuTypeFactory.create(StepRequesterContainerMenu::new, StepRequesterData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setStepCrafterManager(callback.register(
            ContentIds.STEP_CRAFTER_MANAGER,
            () -> extendedMenuTypeFactory.create(
                StepCrafterManagerContainerMenu::new,
                StepManagerData.STREAM_CODEC
            )
        ));
        Menus.INSTANCE.setStepRequesterManager(callback.register(
            ContentIds.STEP_REQUESTER_MANAGER,
            () -> extendedMenuTypeFactory.create(
                StepRequesterManagerContainerMenu::new,
                StepManagerData.STREAM_CODEC
            )
        ));
    }

    private void registerNetworkComponents() {
        RefinedStorageApi.INSTANCE.getNetworkComponentMapFactory().addFactory(
            StepCraftingNetworkComponent.class,
            network -> new StepCraftingNetworkComponentImpl()
        );
    }

    private void registerUpgrades(final RegistryCallback<Item> callback) {
        final Supplier<AbstractUpgradeItem> slotUpgrade = callback.register(
            ContentIds.SLOT_UPGRADE,
            SimpleUpgradeItem::slotUpgrade
        );
        Items.INSTANCE.setSlotUpgrade(slotUpgrade);
    }

    protected final void registerUpgradeMappings() {
        RefinedStorageApi.INSTANCE.getUpgradeRegistry().forDestination(UpgradeDestinations.STEP_CRAFTER)
            .add(Items.INSTANCE.getSlotUpgrade(), 4)
            .add(com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getSpeedUpgrade(), 5);
        RefinedStorageApi.INSTANCE.getUpgradeRegistry().forDestination(UpgradeDestinations.STEP_REQUESTER)
            .add(Items.INSTANCE.getSlotUpgrade(), 4)
            .add(com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getSpeedUpgrade(), 4);
    }
}
