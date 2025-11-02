package com.ultramega.stepcrafter.common.steprequester;

import com.ultramega.stepcrafter.common.ContentNames;
import com.ultramega.stepcrafter.common.Platform;
import com.ultramega.stepcrafter.common.UpgradeDestinations;
import com.ultramega.stepcrafter.common.registry.BlockEntities;
import com.ultramega.stepcrafter.common.registry.Items;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerData;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;

import com.refinedmods.refinedstorage.api.core.NullableType;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StepRequesterBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<StepRequesterNetworkNode>
    implements ExtendedMenuProvider<StepRequesterData>, BlockEntityWithDrops {
    static final int UPGRADES = 8;
    static final int FILTERS = 9 * 5;

    private static final String TAG_UPGRADES = "upgr";
    private static final String TAG_RESOURCE_FILTER = "rf";

    private final PatternResourceContainerImpl filterContainer;
    private final UpgradeContainer upgradeContainer;

    private int speed = 0;
    private int amountSlotUpgrades = 0;

    public StepRequesterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getStepRequester(),
            pos,
            state,
            new StepRequesterNetworkNode(Platform.getConfig().getStepRequester().getEnergyUsage())
        );
        this.filterContainer = createFilterContainer(this::getLevel, this::getAmountSlotUpgrades);
        this.upgradeContainer = new UpgradeContainer(UPGRADES, UpgradeDestinations.STEP_REQUESTER, (c, upgradeEnergyUsage) -> {
            final long baseEnergyUsage = Platform.getConfig().getStepRequester().getEnergyUsage();
            this.mainNetworkNode.setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
            this.speed = c.getAmount(com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getSpeedUpgrade()) * 2;
            this.amountSlotUpgrades = c.getAmount(Items.INSTANCE.getSlotUpgrade());
            this.setChanged();
        });
        this.filterContainer.setChangedListener((i) -> this.setChanged());
        this.mainNetworkNode.setBlockEntity(this);
    }

    static PatternResourceContainerImpl createFilterContainer(final Supplier<@NullableType Level> levelSupplier, final Supplier<Integer> amountSupplier) {
        return new PatternResourceContainerImpl(
            FILTERS,
            levelSupplier,
            RefinedStorageApi.INSTANCE.getItemResourceFactory(),
            RefinedStorageApi.INSTANCE.getAlternativeResourceFactories(),
            true,
            amountSupplier
        );
    }

    static PatternResourceContainerImpl createFilterContainer(final StepRequesterData data,
                                                              final Supplier<@NullableType Level> levelSupplier,
                                                              final Supplier<Integer> amountSupplier) {
        final PatternResourceContainerImpl filterContainer = createFilterContainer(levelSupplier, amountSupplier);
        final PatternResourceContainerData resourceContainerData = data.filterContainerData();
        for (int i = 0; i < resourceContainerData.resources().size(); ++i) {
            final int ii = i;
            resourceContainerData.resources().get(i).ifPresent(resource -> filterContainer.set(ii, resource));
        }
        return filterContainer;
    }

    UpgradeContainer getUpgradeContainer() {
        return this.upgradeContainer;
    }

    PatternResourceContainerImpl getFilterContainer() {
        return this.filterContainer;
    }

    public int getSpeed() {
        return this.speed;
    }

    public int getAmountSlotUpgrades() {
        return this.amountSlotUpgrades;
    }

    @Override
    public Component getName() {
        final Component customName = this.getCustomName();
        if (customName != null) {
            return customName;
        }
        return ContentNames.STEP_REQUESTER;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new StepRequesterContainerMenu(syncId, inventory, this);
    }

    @Override
    public StepRequesterData getMenuData() {
        return new StepRequesterData(PatternResourceContainerData.of(this.filterContainer));
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, StepRequesterData> getMenuCodec() {
        return StepRequesterData.STREAM_CODEC;
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_RESOURCE_FILTER, this.filterContainer.createTag(provider));
        tag.put(TAG_UPGRADES, ContainerUtil.write(this.upgradeContainer, provider));
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_RESOURCE_FILTER)) {
            this.filterContainer.fromTag(tag.getList(TAG_RESOURCE_FILTER, Tag.TAG_COMPOUND), provider);
        }
        if (tag.contains(TAG_UPGRADES)) {
            ContainerUtil.read(tag.getCompound(TAG_UPGRADES), this.upgradeContainer, provider);
        }
        super.loadAdditional(tag, provider);
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        drops.addAll(this.upgradeContainer.getDrops());
        return drops;
    }
}
