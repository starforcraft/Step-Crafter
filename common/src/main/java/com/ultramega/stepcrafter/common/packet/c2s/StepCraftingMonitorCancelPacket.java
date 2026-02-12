package com.ultramega.stepcrafter.common.packet.c2s;

import com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor.AbstractStepCraftingMonitorContainerMenu;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorStreamCodecs;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record StepCraftingMonitorCancelPacket(TaskId taskId) implements CustomPacketPayload {
    public static final Type<StepCraftingMonitorCancelPacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("step_crafting_monitor_cancel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StepCraftingMonitorCancelPacket> STREAM_CODEC = StreamCodec.composite(
        AutocraftingMonitorStreamCodecs.TASK_ID_STREAM_CODEC, StepCraftingMonitorCancelPacket::taskId,
        StepCraftingMonitorCancelPacket::new
    );

    public static void handle(final StepCraftingMonitorCancelPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractStepCraftingMonitorContainerMenu containerMenu) {
            containerMenu.cancelTask(packet.taskId());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
