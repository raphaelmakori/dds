package com.dds.shared;

import java.io.Serializable;

public enum Branch implements Serializable {
    NAIROBI("Nairobi Headquarter"),
    NAKURU("Nakuru Branch"),
    MOMBASA("Mombasa Branch"),
    KISUMU("Kisumu Branch");

    private final String displayName;

    Branch(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
