package com.dds.server;

import java.io.Serializable;

public class InventoryItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int quantity;
    private final int minimumThreshold;

    public InventoryItem(int quantity, int minimumThreshold) {
        this.quantity = quantity;
        this.minimumThreshold = minimumThreshold;
    }

    public int getQuantity() {
        return quantity;
    }

    public void reduce(int amount) {
        quantity -= amount;
    }

    public int getMinimumThreshold() {
        return minimumThreshold;
    }
}
