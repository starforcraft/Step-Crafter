package com.ultramega.stepcrafter.common.mixin;

import com.ultramega.stepcrafter.common.support.AbstractPatternResourceContainerMenu;
import com.ultramega.stepcrafter.common.support.FilterTransfer;
import com.ultramega.stepcrafter.common.support.ResourceInventoryDestination;

import com.refinedmods.refinedstorage.common.support.containermenu.TransferDestination;
import com.refinedmods.refinedstorage.common.support.containermenu.TransferManager;

import java.util.function.Function;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TransferManager.class)
public abstract class MixinTransferManager implements FilterTransfer {
    @Shadow(remap = false)
    @Final
    private AbstractContainerMenu containerMenu;

    @Shadow(remap = false)
    @Final
    private Function<Container, TransferDestination> destinationFactory;

    @Override
    public void stepcrafter$addFilterTransfer(final Container from) {
        if (!(this.containerMenu instanceof AbstractPatternResourceContainerMenu resourceContainer)) {
            throw new UnsupportedOperationException(this.containerMenu.getClass().toString());
        }
        this.addTransfer(this.destinationFactory.apply(from), new ResourceInventoryDestination(resourceContainer));
    }

    @Shadow(remap = false)
    protected abstract void addTransfer(TransferDestination from, TransferDestination to);
}
