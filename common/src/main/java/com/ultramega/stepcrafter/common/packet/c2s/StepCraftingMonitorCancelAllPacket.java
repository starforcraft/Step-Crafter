package com.ultramega.stepcrafter.common.packet.c2s;

import com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor.AbstractStepCraftingMonitorContainerMenu;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record StepCraftingMonitorCancelAllPacket() implements CustomPacketPayload {
    public static final Type<StepCraftingMonitorCancelAllPacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("step_crafting_monitor_cancel_all"));
    public static final StepCraftingMonitorCancelAllPacket INSTANCE = new StepCraftingMonitorCancelAllPacket();
    public static final StreamCodec<RegistryFriendlyByteBuf, StepCraftingMonitorCancelAllPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handle(final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractStepCraftingMonitorContainerMenu containerMenu) {
            containerMenu.cancelAllTasks();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
