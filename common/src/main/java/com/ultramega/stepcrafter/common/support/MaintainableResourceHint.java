package com.ultramega.stepcrafter.common.support;

public enum MaintainableResourceHint {
    MAINTAINABLE(0x80C6FF80);

    private final int color;

    MaintainableResourceHint(final int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }
}
