package com.ultramega.stepcrafter.common.support;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ResourceMinMaxAmount(ResourceKey resource, long minAmount, long maxAmount, long batchSize, boolean isCrafting) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceMinMaxAmount> STREAM_CODEC = StreamCodec.of(
        (buf, resourceAmount) -> {
            final ResourceKey resourceKey = resourceAmount.resource();
            if (!(resourceKey instanceof PlatformResourceKey platformResourceKey)) {
                throw new DecoderException("Cannot encode non-platform resource key");
            }
            ResourceCodecs.STREAM_CODEC.encode(buf, platformResourceKey);
            buf.writeLong(resourceAmount.minAmount());
            buf.writeLong(resourceAmount.maxAmount());
            buf.writeLong(resourceAmount.batchSize());
            buf.writeBoolean(resourceAmount.isCrafting());
        },
        buf -> {
            final PlatformResourceKey resourceKey = ResourceCodecs.STREAM_CODEC.decode(buf);
            final long minAmount = buf.readLong();
            final long maxAmount = buf.readLong();
            final long batchSize = buf.readLong();
            final boolean isCrafting = buf.readBoolean();
            return new ResourceMinMaxAmount(resourceKey, minAmount, maxAmount, batchSize, isCrafting);
        }
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ResourceMinMaxAmount>> STREAM_OPTIONAL_CODEC = ByteBufCodecs.optional(STREAM_CODEC);

    public static final Codec<ResourceMinMaxAmount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceCodecs.CODEC.fieldOf("resource").forGetter(resourceAmount -> (PlatformResourceKey) resourceAmount.resource()),
        Codec.LONG.fieldOf("minAmount").forGetter(ResourceMinMaxAmount::minAmount),
        Codec.LONG.fieldOf("maxAmount").forGetter(ResourceMinMaxAmount::maxAmount),
        Codec.LONG.fieldOf("batchSize").forGetter(ResourceMinMaxAmount::batchSize),
        Codec.BOOL.fieldOf("isCrafting").forGetter(ResourceMinMaxAmount::isCrafting)
    ).apply(instance, ResourceMinMaxAmount::new));

    /**
     * @param resource the resource, must be non-null
     * @param minAmount the minAmount, must be non-negative
     * @param maxAmount the maxAmount, must be non-negative
     * @param batchSize the batchSize, must be non-negative
     * @param isCrafting if the resource is currently being crafted, needed for the min max functionality
     */
    public ResourceMinMaxAmount {
        validate(resource, minAmount, maxAmount, batchSize);
    }

    public static void validate(final ResourceKey resource, final long minAmount, final long maxAmount, final long batchSize) {
        CoreValidations.validateNotNegative(minAmount, "Min amount must be non-negative");
        CoreValidations.validateNotNegative(maxAmount, "Max amount must be non-negative");
        CoreValidations.validateNotNegative(batchSize, "Batch Size must be non-negative");
        CoreValidations.validateNotNull(resource, "Resource must not be null");
    }

    public Builder toBuilder() {
        return new Builder()
            .resource(this.resource)
            .minAmount(this.minAmount)
            .maxAmount(this.maxAmount)
            .batchSize(this.batchSize)
            .isCrafting(this.isCrafting);
    }

    public static final class Builder {
        private ResourceKey resource;
        private long minAmount;
        private long maxAmount;
        private long batchSize;
        private boolean isCrafting;

        private Builder() {
        }

        public Builder resource(final ResourceKey newResource) {
            this.resource = newResource;
            return this;
        }

        public Builder minAmount(final long newMinAmount) {
            this.minAmount = newMinAmount;
            return this;
        }

        public Builder maxAmount(final long newMaxAmount) {
            this.maxAmount = newMaxAmount;
            return this;
        }

        public Builder batchSize(final long newBatchSize) {
            this.batchSize = newBatchSize;
            return this;
        }

        public Builder isCrafting(final boolean newIsCrafting) {
            this.isCrafting = newIsCrafting;
            return this;
        }

        public ResourceMinMaxAmount build() {
            return new ResourceMinMaxAmount(
                this.resource,
                this.minAmount,
                this.maxAmount,
                this.batchSize,
                this.isCrafting
            );
        }
    }
}
