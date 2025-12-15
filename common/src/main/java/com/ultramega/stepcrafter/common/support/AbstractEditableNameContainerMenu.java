package com.ultramega.stepcrafter.common.support;

import com.ultramega.stepcrafter.common.packet.c2s.StepNameChangePacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepNameUpdatePacket;

import com.refinedmods.refinedstorage.common.Platform;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class AbstractEditableNameContainerMenu extends AbstractPatternResourceContainerMenu {
    protected Component name;

    private final RateLimiter nameRateLimiter = RateLimiter.create(0.5);

    @Nullable
    private final AbstractEditableNameBlockEntity<?> blockEntity;
    @Nullable
    private Listener listener;

    public AbstractEditableNameContainerMenu(@Nullable final MenuType<?> type,
                                             final int syncId,
                                             final Player player,
                                             @Nullable final AbstractEditableNameBlockEntity<?> blockEntity) {
        super(type, syncId, player);
        this.blockEntity = blockEntity;
    }

    void setListener(@Nullable final Listener listener) {
        this.listener = listener;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.blockEntity == null) {
            return;
        }
        if (this.nameRateLimiter.tryAcquire()) {
            this.detectNameChange();
        }
    }

    private void detectNameChange() {
        if (this.blockEntity == null) {
            return;
        }
        final Component newName = this.blockEntity.getDisplayName();
        if (!newName.equals(this.name)) {
            this.name = newName;
            Platform.INSTANCE.sendPacketToClient((ServerPlayer) this.player, new StepNameUpdatePacket(newName));
        }
    }

    public void changeName(final String newName) {
        if (this.blockEntity != null) {
            this.blockEntity.setCustomName(newName);
            this.detectNameChange();
        } else {
            Platform.INSTANCE.sendPacketToServer(new StepNameChangePacket(newName));
        }
    }

    public void nameChanged(final Component newName) {
        if (this.listener != null) {
            this.listener.nameChanged(newName);
        }
    }

    public interface Listener {
        void nameChanged(Component name);
    }
}
