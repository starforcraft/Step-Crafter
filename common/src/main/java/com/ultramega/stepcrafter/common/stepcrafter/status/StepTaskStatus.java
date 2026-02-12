package com.ultramega.stepcrafter.common.stepcrafter.status;

import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskState;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus.TaskInfo;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import static com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorStreamCodecs.TASK_ID_STREAM_CODEC;

public record StepTaskStatus(TaskInfo info, StepTaskState state, double percentageCompleted, List<Item> ingredients) {
    private static final StreamCodec<RegistryFriendlyByteBuf, StepTaskStatus.Item> STATUS_ITEM_STREAM_CODEC = new StatusItemStreamCodec();

    private static final StreamCodec<RegistryFriendlyByteBuf, TaskStatus.TaskInfo> INFO_STREAM_CODEC = StreamCodec.composite(
        TASK_ID_STREAM_CODEC, TaskStatus.TaskInfo::id,
        ResourceCodecs.STREAM_CODEC, s -> (PlatformResourceKey) s.resource(),
        ByteBufCodecs.VAR_LONG, TaskStatus.TaskInfo::amount,
        ByteBufCodecs.VAR_LONG, TaskStatus.TaskInfo::startTime,
        TaskStatus.TaskInfo::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StepTaskStatus> STREAM_CODEC = StreamCodec.composite(
        INFO_STREAM_CODEC, StepTaskStatus::info,
        StepTaskState.STREAM_CODEC, StepTaskStatus::state,
        ByteBufCodecs.DOUBLE, StepTaskStatus::percentageCompleted,
        ByteBufCodecs.collection(ArrayList::new, STATUS_ITEM_STREAM_CODEC), StepTaskStatus::ingredients,
        StepTaskStatus::new
    );

    public record Item(
        ResourceKey resource,
        StepTaskStatus.ItemType type,
        long required,
        long used) {
    }

    public enum ItemType {
        NORMAL,
        MISSING
    }

    private static class StatusItemStreamCodec implements StreamCodec<RegistryFriendlyByteBuf, StepTaskStatus.Item> {
        private static final StreamCodec<ByteBuf, StepTaskStatus.ItemType> TYPE_STREAM_CODEC = PlatformUtil.enumStreamCodec(
            StepTaskStatus.ItemType.values()
        );

        @Override
        public StepTaskStatus.Item decode(final RegistryFriendlyByteBuf buf) {
            return new StepTaskStatus.Item(
                ResourceCodecs.STREAM_CODEC.decode(buf),
                TYPE_STREAM_CODEC.decode(buf),
                buf.readLong(),
                buf.readLong()
            );
        }

        @Override
        public void encode(final RegistryFriendlyByteBuf buf, final StepTaskStatus.Item item) {
            ResourceCodecs.STREAM_CODEC.encode(buf, (PlatformResourceKey) item.resource());
            TYPE_STREAM_CODEC.encode(buf, item.type());
            buf.writeLong(item.required());
            buf.writeLong(item.used());
        }
    }
}
