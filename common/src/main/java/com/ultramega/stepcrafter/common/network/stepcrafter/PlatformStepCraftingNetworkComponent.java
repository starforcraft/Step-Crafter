package com.ultramega.stepcrafter.common.network.stepcrafter;

import com.ultramega.stepcrafter.common.stepcrafter.task.StepTask;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocraftingTaskCompletedPacket;
import com.refinedmods.refinedstorage.common.util.ServerListener;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PlatformStepCraftingNetworkComponent extends StepCraftingNetworkComponentImpl {
    @Override
    public void taskCompleted(final StepTask task) {
        super.taskCompleted(task);
        if (task.shouldNotify()
            && task.getActor() instanceof PlayerActor(String name)
            && task.getResource() instanceof PlatformResourceKey resource) {
            sendToClient(task, name, resource);
        }
    }

    private static void sendToClient(final StepTask task, final String name, final PlatformResourceKey resource) {
        ServerListener.queue(server -> sendToClient(task, name, resource, server));
    }

    private static void sendToClient(final StepTask task,
                                     final String name,
                                     final PlatformResourceKey resource,
                                     final MinecraftServer server) {
        final ServerPlayer player = server.getPlayerList().getPlayerByName(name);
        if (player == null) {
            return;
        }
        Platform.INSTANCE.sendPacketToClient(player, new AutocraftingTaskCompletedPacket(
            resource,
            task.getAmount()
        ));
    }
}
