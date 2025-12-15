package com.ultramega.stepcrafter.common.stepmanager;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StepManagerData(List<StepManagerData.Group> groups, boolean active) {
    private static final StreamCodec<RegistryFriendlyByteBuf, StepManagerData.SubGroup> SUB_GROUP_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, StepManagerData.SubGroup::slotCount,
        ByteBufCodecs.INT, StepManagerData.SubGroup::slotUpgradesCount,
        ByteBufCodecs.BOOL, StepManagerData.SubGroup::visibleToTheStepManager,
        ByteBufCodecs.BOOL, StepManagerData.SubGroup::full,
        StepManagerData.SubGroup::new
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, StepManagerData.Group> GROUP_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, StepManagerData.Group::name,
        ByteBufCodecs.collection(ArrayList::new, SUB_GROUP_STREAM_CODEC), StepManagerData.Group::subGroups,
        StepManagerData.Group::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, StepManagerData> STREAM_CODEC = StreamCodec
        .composite(
            ByteBufCodecs.collection(ArrayList::new, GROUP_STREAM_CODEC), StepManagerData::groups,
            ByteBufCodecs.BOOL, StepManagerData::active,
            StepManagerData::new
        );

    public record Group(String name, List<StepManagerData.SubGroup> subGroups) {
        static StepManagerData.Group of(final AbstractStepManagerBlockEntity.Group group) {
            return new StepManagerData.Group(group.name(), group.subGroups().stream().map(
                StepManagerData.SubGroup::of).toList());
        }
    }

    public record SubGroup(int slotCount, int slotUpgradesCount, boolean visibleToTheStepManager, boolean full) {
        private static StepManagerData.SubGroup of(final AbstractStepManagerBlockEntity.SubGroup subGroup) {
            return new StepManagerData.SubGroup(
                subGroup.container().getContainerSize(),
                subGroup.slotUpgradesCount(),
                subGroup.visibleToTheStepManager(),
                subGroup.full()
            );
        }
    }
}
