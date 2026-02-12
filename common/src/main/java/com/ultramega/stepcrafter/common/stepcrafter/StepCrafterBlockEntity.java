package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.ContentNames;
import com.ultramega.stepcrafter.common.Platform;
import com.ultramega.stepcrafter.common.UpgradeDestinations;
import com.ultramega.stepcrafter.common.registry.BlockEntities;
import com.ultramega.stepcrafter.common.registry.Items;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTask;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskImpl;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskSnapshot;
import com.ultramega.stepcrafter.common.support.AbstractEditableNameBlockEntity;
import com.ultramega.stepcrafter.common.support.PatternMinMax;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerData;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink.Result;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.core.NullableType;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.ExternalPatternSinkKeyProvider;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.autocrafting.PlatformPatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.autocrafting.PatternInventory;
import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterData;
import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.InWorldExternalPatternSinkKey;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.SimpleConnectionStrategy;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock.tryExtractDirection;
import static com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskSnapshotPersistence.decodeSnapshot;
import static com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskSnapshotPersistence.encodeSnapshot;

public class StepCrafterBlockEntity extends AbstractEditableNameBlockEntity<StepCrafterNetworkNode>
    implements ExtendedMenuProvider<StepCrafterData>, BlockEntityWithDrops, PatternInventory.Listener,
    PatternProviderExternalPatternSink, ExternalPatternSinkKeyProvider {
    static final int UPGRADES = 8;
    static final int PATTERNS = 9 * 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(StepCrafterBlockEntity.class);

    private static final String TAG_UPGRADES = "upgr";
    private static final String TAG_PATTERNS = "patterns";
    private static final String TAG_PRIORITY = "pri";
    private static final String TAG_TASKS = "tasks";
    private static final String TAG_VISIBLE_TO_THE_STEP_CRAFTER_MANAGER = "vstm";

    private final PatternResourceContainerImpl patternResourceContainer;
    private final UpgradeContainer upgradeContainer;
    private boolean visibleToTheStepCrafterManager = true;

    @Nullable
    private PlatformPatternProviderExternalPatternSink sink;
    @Nullable
    private ExternalPatternSinkKey sinkKey;

    private int speed = 0;
    private int slotUpgradesCount = 0;

    public StepCrafterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getStepCrafter(),
            pos,
            state,
            new StepCrafterNetworkNode(Platform.INSTANCE.getConfig().getStepCrafter().getEnergyUsage(), PATTERNS)
        );
        this.patternResourceContainer = createPatternResourcesContainer(this::getLevel, this::getSlotUpgradesCount);
        this.upgradeContainer = new UpgradeContainer(UPGRADES, UpgradeDestinations.STEP_CRAFTER, (c, upgradeEnergyUsage) -> {
            final long baseEnergyUsage = Platform.INSTANCE.getConfig().getStepCrafter().getEnergyUsage();
            final long patternEnergyUsage = this.patternResourceContainer.getEnergyUsage();
            this.mainNetworkNode.setEnergyUsage(baseEnergyUsage + patternEnergyUsage + upgradeEnergyUsage);
            this.speed = Math.clamp((long) c.getAmount(com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getSpeedUpgrade())
                * Platform.INSTANCE.getConfig().getStepCrafter().getSpeedMultiplier(), 0, Integer.MAX_VALUE);
            this.slotUpgradesCount = c.getAmount(Items.INSTANCE.getSlotUpgrade());
            this.setChanged();
        });
        this.patternResourceContainer.addListener(container -> {
            final long upgradeEnergyUsage = this.upgradeContainer.getEnergyUsage();
            final long baseEnergyUsage = Platform.INSTANCE.getConfig().getStepCrafter().getEnergyUsage();
            final long patternEnergyUsage = this.patternResourceContainer.getEnergyUsage();
            this.mainNetworkNode.setEnergyUsage(baseEnergyUsage + patternEnergyUsage + upgradeEnergyUsage);
            this.setChanged();
        });
        this.patternResourceContainer.setListener(this);
        this.patternResourceContainer.setChangedListener(this::onPatternResourcesChange);
        this.mainNetworkNode.setBlockEntity(this);
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final StepCrafterNetworkNode networkNode) {
        return new StepCrafterNetworkNodeContainer(
            this,
            networkNode,
            "main",
            new SimpleConnectionStrategy(this.getBlockPos())
        );
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

    public int getSlotUpgradesCount() {
        return this.slotUpgradesCount;
    }

    @Override
    public Component getName() {
        final Component customName = this.getCustomName();
        if (customName != null) {
            return customName;
        }
        final BlockEntity connectedMachine = this.getConnectedMachine();
        if (connectedMachine instanceof Nameable nameable) {
            return nameable.getName();
        } else if (connectedMachine != null) {
            return connectedMachine.getBlockState().getBlock().getName();
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
        final ListTag tasks = new ListTag();
        for (final StepTask task : this.mainNetworkNode.getTasks()) {
            if (task instanceof StepTaskImpl taskImpl) {
                try {
                    tasks.add(encodeSnapshot(taskImpl.createSnapshot()));
                } catch (final Exception e) {
                    LOGGER.error("Error while saving step task {} {}", task.getResource(), task.getAmount(), e);
                }
            }
        }
        tag.put(TAG_TASKS, tasks);
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
        tag.putInt(TAG_PRIORITY, this.mainNetworkNode.getPriority());
        tag.putBoolean(TAG_VISIBLE_TO_THE_STEP_CRAFTER_MANAGER, this.visibleToTheStepCrafterManager);
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_PATTERNS)) {
            this.patternResourceContainer.fromTag(tag.getList(TAG_PATTERNS, Tag.TAG_COMPOUND), provider);
        }
        if (tag.contains(TAG_UPGRADES)) {
            ContainerUtil.read(tag.getCompound(TAG_UPGRADES), this.upgradeContainer, provider);
        }
        if (tag.contains(TAG_TASKS)) {
            final ListTag tasks = tag.getList(TAG_TASKS, Tag.TAG_COMPOUND);
            for (int i = 0; i < tasks.size(); ++i) {
                final CompoundTag taskTag = tasks.getCompound(i);
                try {
                    final StepTaskSnapshot snapshot = decodeSnapshot(taskTag);
                    this.mainNetworkNode.addTask(new StepTaskImpl(snapshot));
                } catch (final Exception e) {
                    LOGGER.error("Error while loading step task, skipping", e);
                }
            }
        }
        super.loadAdditional(tag, provider);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        if (tag.contains(TAG_PRIORITY)) {
            this.mainNetworkNode.setPriority(tag.getInt(TAG_PRIORITY));
        }
        if (tag.contains(TAG_VISIBLE_TO_THE_STEP_CRAFTER_MANAGER)) {
            this.visibleToTheStepCrafterManager = tag.getBoolean(TAG_VISIBLE_TO_THE_STEP_CRAFTER_MANAGER);
        }
    }

    @Override
    public List<ItemStack> getUpgrades() {
        return this.upgradeContainer.getUpgrades();
    }

    @Override
    public boolean addUpgrade(final ItemStack upgradeStack) {
        return this.upgradeContainer.addUpgrade(upgradeStack);
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

    private void onPatternResourcesChange(final int index) {
        this.setChanged();
        this.patternChanged(index);
    }

    int getPriority() {
        return this.mainNetworkNode.getPriority();
    }

    void setPriority(final int priority) {
        this.mainNetworkNode.setPriority(priority);
        this.setChanged();
    }

    boolean isVisibleToTheStepCrafterManager() {
        return this.visibleToTheStepCrafterManager;
    }

    void setVisibleToTheStepCrafterManager(final boolean visibleToTheStepCrafterManager) {
        this.visibleToTheStepCrafterManager = visibleToTheStepCrafterManager;
        this.setChanged();
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
    protected void initialize(final ServerLevel level, final Direction direction) {
        super.initialize(level, direction);
        final Direction incomingDirection = direction.getOpposite();
        final BlockPos sourcePosition = this.worldPosition.relative(direction);
        this.invalidateSinkKey();
        this.sink = RefinedStorageApi.INSTANCE.getPatternProviderExternalPatternSinkFactory()
            .create(level, sourcePosition, incomingDirection);
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

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }

    @Nullable
    private BlockEntity getConnectedMachine() {
        final Direction direction = tryExtractDirection(this.getBlockState());
        if (this.level == null || direction == null) {
            return null;
        }
        final BlockPos neighborPos = this.getBlockPos().relative(direction);
        if (!this.level.isLoaded(neighborPos)) {
            return null;
        }
        return this.level.getBlockEntity(neighborPos);
    }

    @Override
    @Nullable
    public ExternalPatternSinkKey getKey() {
        if (this.sinkKey == null) {
            this.tryUpdateSinkKey();
        }
        return this.sinkKey;
    }

    private void tryUpdateSinkKey() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        final Direction direction = tryExtractDirection(this.getBlockState());
        if (direction == null) {
            return;
        }
        final BlockEntity connectedMachine = this.getConnectedMachine();
        if (connectedMachine == null) {
            this.invalidateSinkKey();
            return;
        }
        final BlockState connectedMachineState = connectedMachine.getBlockState();
        final Player fakePlayer = this.getFakePlayer(serverLevel);
        final ItemStack connectedMachineStack = com.refinedmods.refinedstorage.common.Platform.INSTANCE.getBlockAsItemStack(
            connectedMachineState.getBlock(),
            connectedMachineState,
            direction.getOpposite(),
            serverLevel,
            connectedMachine.getBlockPos(),
            fakePlayer
        );
        this.sinkKey = new InWorldExternalPatternSinkKey(this.getName().getString(), connectedMachineStack);
    }

    private void invalidateSinkKey() {
        this.sinkKey = null;
    }

    @Override
    public Result accept(final Collection<ResourceAmount> resources, final Action action) {
        if (this.sink == null) {
            return ExternalPatternSink.Result.SKIPPED;
        }
        return this.sink.accept(resources, action);
    }
}
