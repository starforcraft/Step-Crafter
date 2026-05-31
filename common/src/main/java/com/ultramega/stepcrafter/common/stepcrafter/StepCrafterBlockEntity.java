package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.ContentNames;
import com.ultramega.stepcrafter.common.Platform;
import com.ultramega.stepcrafter.common.UpgradeDestinations;
import com.ultramega.stepcrafter.common.registry.BlockEntities;
import com.ultramega.stepcrafter.common.registry.Items;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTask;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskImpl;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskSnapshot;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskSnapshotCodecs;
import com.ultramega.stepcrafter.common.support.AbstractEditableNameBlockEntity;
import com.ultramega.stepcrafter.common.support.PatternMinMax;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerContents;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerData;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkId;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.autocrafting.PlatformPatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterData;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.SimpleConnectionStrategy;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock.tryExtractDirection;

public class StepCrafterBlockEntity extends AbstractEditableNameBlockEntity<StepCrafterNetworkNode>
    implements ExtendedMenuProvider<StepCrafterData>, PatternProviderExternalPatternSink {
    static final int UPGRADES = 8;
    static final int PATTERNS = 9 * 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(StepCrafterBlockEntity.class);

    private static final String TAG_UPGRADES = "upgr";
    private static final String TAG_PATTERNS = "patterns";
    private static final String TAG_PATTERN_RESOURCES = "patternResources";
    private static final String TAG_PRIORITY = "pri";
    private static final String TAG_TASKS = "tasks";
    private static final String TAG_VISIBLE_TO_THE_STEP_CRAFTER_MANAGER = "vstm";
    private static final String TAG_INSERT_INTO_POINTED_CONTAINER = "iipc";
    private static final String TAG_ID = "auid";

    private final PatternResourceContainerImpl patternResourceContainer;
    private final UpgradeContainer upgradeContainer;
    private boolean visibleToTheStepCrafterManager = true;
    private boolean insertIntoPointedContainer = false;

    @Nullable
    private PlatformPatternProviderExternalPatternSink sink;
    @Nullable
    private ExternalPatternSinkId id;

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
        }, this::setChanged);
        this.patternResourceContainer.setListener(() -> {
            final long upgradeEnergyUsage = this.upgradeContainer.getEnergyUsage();
            final long baseEnergyUsage = Platform.INSTANCE.getConfig().getStepCrafter().getEnergyUsage();
            final long patternEnergyUsage = this.patternResourceContainer.getEnergyUsage();
            this.mainNetworkNode.setEnergyUsage(baseEnergyUsage + patternEnergyUsage + upgradeEnergyUsage);
            StepCrafterBlockEntity.this.setChanged();
        });
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

    static PatternResourceContainerImpl createPatternResourcesContainer(final Supplier<@Nullable Level> levelSupplier, final Supplier<Integer> amountSupplier) {
        return new PatternResourceContainerImpl(PATTERNS, levelSupplier, null, null, false, amountSupplier);
    }

    static PatternResourceContainerImpl createPatternResourcesContainer(final StepCrafterData data,
                                                                        final Supplier<@Nullable Level> levelSupplier,
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
    public void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.store(TAG_PATTERNS, ItemContainerContents.CODEC, ItemContainerContents.fromItems(this.patternResourceContainer.getItems()));
        output.store(TAG_PATTERN_RESOURCES, PatternResourceContainerContents.CODEC,
            PatternResourceContainerContents.of(this.patternResourceContainer));
        output.store(TAG_UPGRADES, ItemContainerContents.CODEC, ItemContainerContents.fromItems(this.upgradeContainer.getItems()));
        output.store(TAG_TASKS, StepTaskSnapshotCodecs.LIST_CODEC, this.collectTaskSnapshots());
        if (this.id != null) {
            output.store(TAG_ID, UUIDUtil.CODEC, this.id.id());
        }
    }

    @Override
    public void loadAdditional(final ValueInput input) {
        input.read(TAG_PATTERNS, ItemContainerContents.CODEC)
            .ifPresent(contents -> contents.copyInto(this.patternResourceContainer.getItems()));
        input.read(TAG_PATTERN_RESOURCES, PatternResourceContainerContents.CODEC)
            .ifPresent(this.patternResourceContainer::load);
        input.read(TAG_UPGRADES, ItemContainerContents.CODEC).ifPresent(this.upgradeContainer::load);
        input.read(TAG_TASKS, StepTaskSnapshotCodecs.LIST_CODEC)
            .ifPresent(snapshots ->
                snapshots.forEach(snapshot -> this.mainNetworkNode.addTask(new StepTaskImpl(snapshot))));
        if (this.level != null && !this.level.isClientSide()) {
            this.onPatternChanged();
        }
        this.id = new ExternalPatternSinkId(input.read(TAG_ID, UUIDUtil.CODEC).orElseGet(UUID::randomUUID));
        super.loadAdditional(input);
    }

    @Override
    public void writeConfiguration(final ValueOutput output) {
        super.writeConfiguration(output);
        output.putInt(TAG_PRIORITY, this.mainNetworkNode.getPriority());
        output.putBoolean(TAG_VISIBLE_TO_THE_STEP_CRAFTER_MANAGER, this.visibleToTheStepCrafterManager);
        output.putBoolean(TAG_INSERT_INTO_POINTED_CONTAINER, this.insertIntoPointedContainer);
    }

    @Override
    public void readConfiguration(final ValueInput input) {
        super.readConfiguration(input);
        input.getInt(TAG_PRIORITY).ifPresent(this.mainNetworkNode::setPriority);
        this.visibleToTheStepCrafterManager = input.getBooleanOr(TAG_VISIBLE_TO_THE_STEP_CRAFTER_MANAGER, true);
        this.insertIntoPointedContainer = input.getBooleanOr(TAG_INSERT_INTO_POINTED_CONTAINER, false);
    }

    private List<StepTaskSnapshot> collectTaskSnapshots() {
        final List<StepTaskSnapshot> snapshots = new ArrayList<>();
        for (final StepTask task : this.mainNetworkNode.getTasks()) {
            if (task instanceof StepTaskImpl taskImpl) {
                try {
                    snapshots.add(taskImpl.createSnapshot());
                } catch (final Exception e) {
                    LOGGER.error("Error while creating snapshot for step task {} {}", task.getResource(), task.getAmount(), e);
                }
            }
        }
        return snapshots;
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
    public void preRemoveSideEffects(final BlockPos pos, final BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            final NonNullList<ItemStack> drops = NonNullList.create();
            drops.addAll(this.upgradeContainer.getDrops());
            for (int i = 0; i < this.patternResourceContainer.getContainerSize(); ++i) {
                drops.add(this.patternResourceContainer.getItem(i));
            }
            Containers.dropContents(this.level, pos, drops);
        }
    }

    private void onPatternResourcesChange(final int index) {
        this.setChanged();
        this.onPatternChanged(index);
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

    boolean shouldInsertIntoPointedContainer() {
        return this.insertIntoPointedContainer;
    }

    void setInsertIntoPointedContainer(final boolean insertIntoPointedContainer) {
        this.insertIntoPointedContainer = insertIntoPointedContainer;
        this.setChanged();
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) {
            this.onPatternChanged();
        }
    }

    @Override
    protected void initialize(final ServerLevel level, final Direction direction) {
        super.initialize(level, direction);
        final Direction incomingDirection = direction.getOpposite();
        final BlockPos sourcePosition = this.worldPosition.relative(direction);
        if (this.id == null) {
            this.id = ExternalPatternSinkId.create();
        }
        this.sink = com.refinedmods.refinedstorage.common.Platform.INSTANCE.getPatternProviderExternalPatternSinkFactory()
            .create(level, sourcePosition, incomingDirection);
    }

    private void onPatternChanged() {
        if (this.level == null) {
            return;
        }
        for (int i = 0; i < this.patternResourceContainer.getContainerSize(); ++i) {
            this.onPatternChanged(i);
        }
    }

    private void onPatternChanged(final int index) {
        if (this.level == null) {
            return;
        }
        final Pattern pattern = RefinedStorageApi.INSTANCE.getPattern(this.patternResourceContainer.getItem(index), this.level)
            .orElse(null);
        final ResourceMinMaxAmount resource = this.patternResourceContainer.get(index);
        final PatternMinMax patternMinMax = pattern != null && resource != null
            ? new PatternMinMax(pattern, resource.minAmount(), resource.maxAmount(), resource.batchSize())
            : null;
        this.mainNetworkNode.tryUpdatePattern(index, patternMinMax);
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

    public @Nullable ExternalPatternSinkId getId() {
        return this.id;
    }

    @Override
    public ExternalPatternSink.Result insertAll(final Collection<ResourceAmount> resources, final Action action) {
        if (this.sink == null || !this.insertIntoPointedContainer) {
            return ExternalPatternSink.Result.SKIPPED;
        }
        return this.sink.insertAll(resources, action);
    }
}
