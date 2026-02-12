package com.ultramega.stepcrafter.common.packet.s2c;

import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;
import com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor.AbstractStepCraftingMonitorContainerMenu;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record StepCraftingMonitorTaskAddedPacket(StepTaskStatus taskStatus) implements CustomPacketPayload {
    public static final Type<StepCraftingMonitorTaskAddedPacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("step_crafting_monitor_task_added"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StepCraftingMonitorTaskAddedPacket> STREAM_CODEC = StreamCodec.composite(
        StepTaskStatus.STREAM_CODEC, StepCraftingMonitorTaskAddedPacket::taskStatus,
        StepCraftingMonitorTaskAddedPacket::new
    );

    public static void handle(final StepCraftingMonitorTaskAddedPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractStepCraftingMonitorContainerMenu containerMenu) {
            containerMenu.taskAdded(packet.taskStatus());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
