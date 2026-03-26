package com.dds.shared;

import java.io.Serializable;

public enum DrinkBrand implements Serializable {
    COKE("Coca Cola", 120.0),
    FANTA("Fanta", 110.0),
    SPRITE("Sprite", 115.0),
    WATER("Mineral Water", 80.0),
    JUICE("Mango Juice", 150.0);

    private final String displayName;
    private final double unitPrice;

    DrinkBrand(String displayName, double unitPrice) {
        this.displayName = displayName;
        this.unitPrice = unitPrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }
}
