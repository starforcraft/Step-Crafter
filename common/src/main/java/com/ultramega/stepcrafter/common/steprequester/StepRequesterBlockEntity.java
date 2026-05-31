package com.ultramega.stepcrafter.common.steprequester;

import com.ultramega.stepcrafter.common.ContentNames;
import com.ultramega.stepcrafter.common.Platform;
import com.ultramega.stepcrafter.common.UpgradeDestinations;
import com.ultramega.stepcrafter.common.registry.BlockEntities;
import com.ultramega.stepcrafter.common.registry.Items;
import com.ultramega.stepcrafter.common.support.AbstractEditableNameBlockEntity;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerContents;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerData;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.SimpleConnectionStrategy;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class StepRequesterBlockEntity extends AbstractEditableNameBlockEntity<StepRequesterNetworkNode>
    implements ExtendedMenuProvider<StepRequesterData> {
    static final int UPGRADES = 8;
    static final int FILTERS = 9 * 5;

    private static final String TAG_UPGRADES = "upgr";
    private static final String TAG_RESOURCE_FILTER = "rf";
    private static final String TAG_VISIBLE_TO_THE_STEP_REQUESTER_MANAGER = "vsrm";

    private final PatternResourceContainerImpl filterContainer;
    private final UpgradeContainer upgradeContainer;
    private boolean visibleToTheStepRequesterManager = true;

    private int speed = 0;
    private int slotUpgradesCount = 0;

    public StepRequesterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getStepRequester(),
            pos,
            state,
            new StepRequesterNetworkNode(Platform.INSTANCE.getConfig().getStepRequester().getEnergyUsage())
        );
        this.filterContainer = createFilterContainer(this::getLevel, this::getSlotUpgradesCount);
        this.upgradeContainer = new UpgradeContainer(UPGRADES, UpgradeDestinations.STEP_REQUESTER, (c, upgradeEnergyUsage) -> {
            final long baseEnergyUsage = Platform.INSTANCE.getConfig().getStepRequester().getEnergyUsage();
            this.mainNetworkNode.setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
            this.speed = c.getAmount(com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getSpeedUpgrade()) * 2;
            this.slotUpgradesCount = c.getAmount(Items.INSTANCE.getSlotUpgrade());
        }, this::setChanged);
        this.filterContainer.setChangedListener((i) -> this.setChanged());
        this.mainNetworkNode.setBlockEntity(this);
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final StepRequesterNetworkNode networkNode) {
        return new StepRequesterNetworkNodeContainer(
            this,
            networkNode,
            "main",
            new SimpleConnectionStrategy(this.getBlockPos())
        );
    }

    static PatternResourceContainerImpl createFilterContainer(final Supplier<@Nullable Level> levelSupplier, final Supplier<Integer> amountSupplier) {
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
                                                              final Supplier<@Nullable Level> levelSupplier,
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

    public int getSlotUpgradesCount() {
        return this.slotUpgradesCount;
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
    public void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.store(TAG_UPGRADES, ItemContainerContents.CODEC, ItemContainerContents.fromItems(this.upgradeContainer.getItems()));
    }

    @Override
    public void loadAdditional(final ValueInput input) {
        input.read(TAG_UPGRADES, ItemContainerContents.CODEC).ifPresent(this.upgradeContainer::load);
        super.loadAdditional(input);
    }

    @Override
    public void writeConfiguration(final ValueOutput output) {
        super.writeConfiguration(output);
        output.store(TAG_RESOURCE_FILTER, PatternResourceContainerContents.CODEC, PatternResourceContainerContents.of(this.filterContainer));
        output.putBoolean(TAG_VISIBLE_TO_THE_STEP_REQUESTER_MANAGER, this.visibleToTheStepRequesterManager);
    }

    @Override
    public void readConfiguration(final ValueInput input) {
        super.readConfiguration(input);
        input.read(TAG_RESOURCE_FILTER, PatternResourceContainerContents.CODEC).ifPresent(this.filterContainer::load);
        this.visibleToTheStepRequesterManager = input.getBooleanOr(TAG_VISIBLE_TO_THE_STEP_REQUESTER_MANAGER, true);
    }

    @Override
    public void preRemoveSideEffects(final BlockPos pos, final BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            final NonNullList<ItemStack> drops = NonNullList.create();
            drops.addAll(this.upgradeContainer.getDrops());
            Containers.dropContents(this.level, pos, drops);
        }
    }

    boolean isVisibleToTheStepRequesterManager() {
        return this.visibleToTheStepRequesterManager;
    }

    void setVisibleToTheStepRequesterManager(final boolean visibleToTheStepRequesterManager) {
        this.visibleToTheStepRequesterManager = visibleToTheStepRequesterManager;
        this.setChanged();
    }
}
