package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.ContentNames;
import com.ultramega.stepcrafter.common.Platform;
import com.ultramega.stepcrafter.common.UpgradeDestinations;
import com.ultramega.stepcrafter.common.registry.BlockEntities;
import com.ultramega.stepcrafter.common.registry.Items;
import com.ultramega.stepcrafter.common.support.PatternMinMax;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerData;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.NullableType;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.autocrafting.PatternInventory;
import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterData;
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

public class StepCrafterBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<StepCrafterNetworkNode>
    implements ExtendedMenuProvider<StepCrafterData>, BlockEntityWithDrops, PatternInventory.Listener {
    static final int UPGRADES = 8;
    static final int PATTERNS = 9 * 5;

    private static final String TAG_UPGRADES = "upgr";
    private static final String TAG_PATTERNS = "patterns";

    private final PatternResourceContainerImpl patternResourceContainer;
    private final UpgradeContainer upgradeContainer;

    private int speed = 0;
    private int amountSlotUpgrades = 0;

    //TODO: step crafter monitor to see all the step crafters in the network
    public StepCrafterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getStepCrafter(),
            pos,
            state,
            new StepCrafterNetworkNode(Platform.getConfig().getStepCrafter().getEnergyUsage(), PATTERNS)
        );
        this.patternResourceContainer = createPatternResourcesContainer(this::getLevel, this::getAmountSlotUpgrades);
        this.upgradeContainer = new UpgradeContainer(UPGRADES, UpgradeDestinations.STEP_CRAFTER, (c, upgradeEnergyUsage) -> {
            final long baseEnergyUsage = Platform.getConfig().getStepCrafter().getEnergyUsage();
            final long patternEnergyUsage = this.patternResourceContainer.getEnergyUsage();
            this.mainNetworkNode.setEnergyUsage(baseEnergyUsage + patternEnergyUsage + upgradeEnergyUsage);
            this.speed = Math.clamp((long) c.getAmount(com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getSpeedUpgrade())
                * Platform.getConfig().getStepCrafter().getSpeedMultiplier(), 0, Integer.MAX_VALUE);
            this.amountSlotUpgrades = c.getAmount(Items.INSTANCE.getSlotUpgrade());
            this.setChanged();
        });
        this.patternResourceContainer.addListener(container -> {
            final long upgradeEnergyUsage = this.upgradeContainer.getEnergyUsage();
            final long baseEnergyUsage = Platform.getConfig().getStepCrafter().getEnergyUsage();
            final long patternEnergyUsage = this.patternResourceContainer.getEnergyUsage();
            this.mainNetworkNode.setEnergyUsage(baseEnergyUsage + patternEnergyUsage + upgradeEnergyUsage);
            this.setChanged();
        });
        this.patternResourceContainer.setListener(this);
        this.patternResourceContainer.setChangedListener(this::onPatternResourcesChange);
        this.mainNetworkNode.setBlockEntity(this);
    }

    static PatternResourceContainerImpl createPatternResourcesContainer(final Supplier<@NullableType Level> levelSupplier, final Supplier<Integer> amountSupplier) {
        return new PatternResourceContainerImpl(PATTERNS, levelSupplier, null, null, false, amountSupplier);
    }

    static PatternResourceContainerImpl createPatternResourcesContainer(final StepCrafterData data,
                                                                        final Supplier<@NullableType Level> levelSupplier,
                                                                        final Supplier<Integer> amountSupplier) {
        final PatternResourceContainerImpl exportedResourcesContainer = createPatternResourcesContainer(levelSupplier, amountSupplier);
        final PatternResourceContainerData resourceContainerData = data.patternResources();
        for (int i = 0; i < resourceContainerData.resources().size(); ++i) {
            final int ii = i;
            resourceContainerData.resources().get(i).ifPresent(
                resource -> exportedResourcesContainer.set(ii, resource)
            );
        }
        return exportedResourcesContainer;
    }

    PatternResourceContainerImpl getPatternResourceContainer() {
        return this.patternResourceContainer;
    }

    UpgradeContainer getUpgradeContainer() {
        return this.upgradeContainer;
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
        return ContentNames.STEP_CRAFTER;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new StepCrafterContainerMenu(syncId, inventory, this);
    }

    @Override
    public StepCrafterData getMenuData() {
        return new StepCrafterData(new AutocrafterData(false, true, false),
            PatternResourceContainerData.of(this.patternResourceContainer));
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, StepCrafterData> getMenuCodec() {
        return StepCrafterData.STREAM_CODEC;
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_PATTERNS, this.patternResourceContainer.createTag(provider));
        tag.put(TAG_UPGRADES, ContainerUtil.write(this.upgradeContainer, provider));
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_PATTERNS)) {
            this.patternResourceContainer.fromTag(tag.getList(TAG_PATTERNS, Tag.TAG_COMPOUND), provider);
        }
        if (tag.contains(TAG_UPGRADES)) {
            ContainerUtil.read(tag.getCompound(TAG_UPGRADES), this.upgradeContainer, provider);
        }
        super.loadAdditional(tag, provider);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        drops.addAll(this.upgradeContainer.getDrops());
        for (int i = 0; i < this.patternResourceContainer.getContainerSize(); ++i) {
            drops.add(this.patternResourceContainer.getItem(i));
        }
        return drops;
    }

    void setCustomName(final String name) {
        this.setCustomName(name.trim().isBlank() ? null : Component.literal(name));
        this.setChanged();
    }

    private void onPatternResourcesChange(final int index) {
        this.setChanged();
        this.patternChanged(index);
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (level.isClientSide()) {
            return;
        }
        for (int i = 0; i < this.patternResourceContainer.getContainerSize(); ++i) {
            this.patternChanged(i);
        }
    }

    @Override
    public void patternChanged(final int slot) {
        if (this.level == null) {
            return;
        }
        final Pattern pattern = RefinedStorageApi.INSTANCE.getPattern(this.patternResourceContainer.getItem(slot), this.level)
            .orElse(null);
        final ResourceMinMaxAmount resource = this.patternResourceContainer.get(slot);
        this.mainNetworkNode.setPattern(
            slot,
            pattern != null && resource != null
                ? new PatternMinMax(pattern, resource.minAmount(), resource.maxAmount(), resource.batchSize())
                : null
        );
    }
}
